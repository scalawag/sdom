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

    def canonicalize(configuration:BuilderConfiguration = BuilderConfiguration.Simplest):T = {

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
            if ( configuration.collapseAdjacentTextLikes ) {
              canonicalChildren.foldLeft(Seq.empty[ChildSpec]) { (acc,child) =>
                if ( configuration.collapseAdjacentTextLikes && child.isInstanceOf[TextLikeSpec] )
                  TextLikeSpec.collapseAppend(acc,child)
                else
                  acc :+ child
              }
            } else {
              canonicalChildren
            }

          Iterable(e.copy(prefix = None,
                          namespaces = if ( e eq root ) namespaces else Iterable.empty,
                          attributes = canonicalAttributes,
                          children = collapsedCanonicalChildren))

        case c:CommentSpec =>
          if ( configuration.discardComments )
            Iterable.empty
          else
            Iterable(c)

        case t:TextLikeSpec =>
          val text = if ( configuration.trimWhitespace ) t.text.trim else t.text

          val createFn = t match {
            case c:CDataSpec if ! configuration.treatCDataAsText => CDataSpec.apply _
            case _ => TextSpec.apply _
          }

          if ( ! configuration.discardWhitespace || ! text.isEmpty )
            Iterable(createFn(text))
          else
            Iterable.empty

        case p:ProcessingInstructionSpec =>
          if ( configuration.discardProcessingInstructions )
            Iterable.empty
          else
            Iterable(p)

      }).asInstanceOf[Iterable[T]] // The code above ensures that this cast works.

      transform(xml).head
    }
  }

//  implicit def parentToCanonicalizer[T <: Parent](xml:T) = new ParentSpecCanonicalizer(xml.spec)
}

/* sdom -- Copyright 2014 Justin Patterson -- All Rights Reserved */
