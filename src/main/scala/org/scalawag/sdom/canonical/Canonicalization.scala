package org.scalawag.sdom.canonical

import org.scalawag.sdom._

trait Canonicalization {
  /** This canonicalizer does the following to ensure the equality of two equivalent XML documents in our system:
    *  - Rewrites the namespace prefixes serially (in lexicographical order)
    *  - Orders the attributes on each element (in lexicographical order, including the prefix)
    *  - Replaces <a></a> with <a/>
    *  - Removes comments (optionally)
    *  - Removes whitespace nodes (optionally)
    *  - Trims whitespace from text nodes
    */

  implicit class ParentSpecCanonicalizer[T <: ParentSpec[_]](xml:T) {

    def canonical = canonicalize()

    def canonicalize(treatCDataAsText:Boolean = true,
                     collapseAdjacentTexts:Boolean = true,
                     includeWhitespace:Boolean = false,
                     includeComments:Boolean = false,
                     includeProcessingInstructions:Boolean = false):T = {

      // This is where we'll attach all the new canonical namespace declarations
      val root = xml match {
        case d:DocumentSpec => d.childElements.head
        case e:ElementSpec => e
      }

      // Returns all *used* namespaces for this element, including its own its attributes' and its children's.
      def getNamespaceURIs(e:ElementSpec):Iterable[String] =
        Iterable(e.name.uri) ++ e.attributes.map(_.name.uri) ++ e.childElements.flatMap(getNamespaceURIs)

      val namespaceURIs = getNamespaceURIs(root).filterNot(_.isEmpty).toSeq.distinct.sorted
      val namespaces = namespaceURIs.zipWithIndex.map { case(uri,index) =>
        NamespaceSpec(s"n$index",uri)
      }

      def transform[T <: NodeSpec](n:T):Iterable[T] = (n match {

        case d:DocumentSpec =>
          Iterable(d.copy(children = d.children.flatMap(transform)))

        case e:ElementSpec =>

          // Rewrite the attributes to remove their prefixes.  This will make them use the new, canonical prefixes.

          val canonicalAttributes = e.attributes.map(_.copy(prefix = None)).toSeq.sortBy(_.name)

          // Include no namespace bindings here. They'll be added to the root element.

          val canonicalChildren = e.children.flatMap(transform)

          // Collapse multiple adjacent TextSpec nodes into a single TextSpec node.

          val collapsedCanonicalChildren =
            if ( collapseAdjacentTexts ) {
              canonicalChildren.foldRight(Seq.empty[ChildSpec]) { (child,acc) =>
                // If both the current child and the first child in the accumulator are text, combine them.
                if ( child.isInstanceOf[TextSpec] && ! acc.isEmpty && acc.head.isInstanceOf[TextSpec] ) {
                  val newHead = TextSpec(child.asInstanceOf[TextSpec].text + acc.head.asInstanceOf[TextSpec].text)
                  newHead +: acc.tail
                } else {
                  child +: acc
                }
              }
            } else {
              canonicalChildren
            }

          Iterable(e.copy(prefix = None,
                          namespaces = if ( e eq root ) namespaces else Iterable.empty,
                          attributes = canonicalAttributes,
                          children = collapsedCanonicalChildren))

        case c:CommentSpec =>
          if ( includeComments )
            Iterable(c)
          else
            Iterable.empty

        case t:TextLikeSpec =>
          val text = if ( includeWhitespace ) t.text else t.text.trim

          val createFn = t match {
            case c:CDataSpec if ! treatCDataAsText => CDataSpec.apply _
            case _ => TextSpec.apply _
          }

          if ( includeWhitespace || ! text.isEmpty )
            Iterable(createFn(text))
          else
            Iterable.empty

        case p:ProcessingInstructionSpec =>
          if ( includeProcessingInstructions )
            Iterable(p)
          else
            Iterable.empty

      }).asInstanceOf[Iterable[T]] // The code above ensures that this cast works.

      transform(xml).head
    }
  }

//  implicit def parentToCanonicalizer[T <: Parent](xml:T) = new ParentSpecCanonicalizer(xml.spec)
}

/* sdom -- Copyright 2014 Justin Patterson -- All Rights Reserved */
