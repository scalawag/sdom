package org.scalawag.sdom

import scala.language.implicitConversions

import scala.{xml => sxml}

object ScalaXmlConversions {
  sealed class Strategy(val keepPrefixes:Boolean,val keepNamespaces:Boolean)

  object Strategies {
    implicit object Flexible extends Strategy(false,false)
    implicit object KeepPrefixes extends Strategy(true,false)
    implicit object KeepNamespaces extends Strategy(false,true)
    implicit object KeepPrefixesAndNamespaces extends Strategy(true,true)
  }
}

import ScalaXmlConversions._

trait ScalaXmlConversions {
  implicit def literalToElementSpec(e:sxml.Elem)(implicit strategy:Strategy = Strategies.Flexible) =
    literalToChildSpecs(e).head.asInstanceOf[ElementSpec]

  implicit def literalToChildSpecs(nodes:sxml.NodeSeq)(implicit strategy:Strategy = Strategies.Flexible):Iterable[ChildSpec] = {

    def convert(node:sxml.NodeSeq,fromParentScope:sxml.NamespaceBinding = sxml.TopScope,toParentScope:NamespacesLike = Namespaces.Empty):Iterable[NodeSpec] = node match {
      case n:sxml.Elem =>

        def getNamespaces(b:sxml.NamespaceBinding):Seq[NamespaceSpec] =
          if ( b == fromParentScope ) {
            Seq.empty
          } else if ( b == sxml.TopScope ) {
            /*
               Freaking scala.xml considers the following two documents different:

               val a = <outer xmlns="NS"><inner/></outer>
               val inner = <inner/>
               val b = <outer xmlns="NS">{inner}</outer>

               Yes, they look the same, but behold:

               ( a \ "inner" ).head.scope
               ( b \ "inner" ).head.scope

               So, try to compensate for that here by looking for a child element whose scope isn't a superset of its
               parent's.

               val a = <outer xmlns="NS1"><b:inner xmlns:b="NS2"/></outer>
               val inner = <b:inner xmlns:b="NS2"/>
               val b = <outer xmlns="NS1">{inner}</outer>

               val a = <outer xmlns="NS1"><inner xmlns="NS2"/></outer>
               val inner = <inner xmlns="NS2"/>
               val b = <outer xmlns="NS1">{inner}</outer>

               If we run into TopScope prior to hitting the parent's NamespaceBinding, go ahead and stop.
             */
            Seq.empty
          } else {
            val p = Option(b.prefix).getOrElse("")
            val u = Option(b.uri).getOrElse("")
            getNamespaces(b.parent) :+ NamespaceSpec(p,u)
          }

        val namespaces = getNamespaces(n.scope)
        val scope = Namespaces(toParentScope,namespaces)

        def maybeIncludePrefix(prefix:Option[String]) =
          if ( strategy.keepPrefixes )
            prefix
          else
            None

        def getAttributes(a:sxml.MetaData):Seq[AttributeSpec] =
          a match {
            case sxml.Null =>
              Seq.empty
            case sxml.PrefixedAttribute(prefix,key,value,_) =>
              // Lookup the URI for this attribute's prefix
              val uri = scope.prefixToUri(prefix)
              AttributeSpec(AttributeName(key,uri),value.toString,maybeIncludePrefix(Some(prefix))) +: getAttributes(a.next)
            case sxml.UnprefixedAttribute(key,value,_) =>
              // Unprefixed attributes always have an empty URI
              AttributeSpec(AttributeName(key,""),value.toString) +: getAttributes(a.next)
          }

        val uri = scope.prefixToUri(Option(n.prefix).getOrElse(""))

        Iterable(new ElementSpec(name = ElementName(n.label,uri),
                                 attributes = getAttributes(n.attributes),
                                 children = n.child.flatMap(convert(_,n.scope,scope).asInstanceOf[Iterable[ChildSpec]]),
                                 // Only the locally-introduced namespaces
                                 namespaces = if ( strategy.keepNamespaces ) namespaces else Seq.empty,
                                 prefix = maybeIncludePrefix(Option(n.prefix))))

      case n:sxml.PCData => Iterable(CDataSpec(n.text))
      case n:sxml.Atom[_] => Iterable(TextSpec(n.text))
      case n:sxml.ProcInstr => Iterable(ProcessingInstructionSpec(n.target,n.proctext))
//      case n:sxml.EntityRef => Iterable(EntityRef(n.entityName)) - dropped by sdom
      case n:sxml.Comment => Iterable(CommentSpec(n.commentText))

      case s:sxml.NodeSeq => s.flatMap(convert(_,fromParentScope,toParentScope))

//      case _ => Iterable.empty
    }

    convert(nodes).map(_.asInstanceOf[ChildSpec])
  }

  // Must be a Element so that the namespaces are all resolved and we can generate them in the scala.xml output
  implicit class ElementLiteralizer(e:Element) {
    def toElem:sxml.Elem = {

      def nullifyEmpty(s:String) = s match {
        case "" => null
        case s => s
      }

      def convert(node:Node,toParentScope:sxml.NamespaceBinding = sxml.TopScope):sxml.Node = node match {
        case n:Element =>

          // NB: scala.xml expects each Elem's scope to link to its parent Elem's scope at some point.  It has
          // to be the exact same instance or else bad XML can result.

          def getNamespaceBinding(namespaces:Iterable[Namespace]):sxml.NamespaceBinding =
            if ( namespaces.isEmpty ) {
              toParentScope
            } else {
              val ns = namespaces.head
              val p = nullifyEmpty(ns.prefix)
              val u = ns.uri
              sxml.NamespaceBinding(p,u,getNamespaceBinding(namespaces.tail))
            }

          val scope = getNamespaceBinding(n.namespaces)

          val attributeList = n.attributes map { a =>
            a.prefix match {
              case "" =>
                new sxml.UnprefixedAttribute(a.name.localName,a.value,sxml.Null)
              case p =>
                new sxml.PrefixedAttribute(p,a.name.localName,a.value,sxml.Null)
            }
          }
          val attributes = attributeList.foldRight[sxml.MetaData](sxml.Null) { case (l,r) =>
            l.copy(next = r)
          }

          val children = n.children.map(convert(_,scope)).toSeq

          new sxml.Elem(nullifyEmpty(n.prefix),n.name.localName,attributes,scope,true,children:_*)

        case n:CData => sxml.PCData(n.text)
        case n:Text => sxml.Text(n.text)
        case n:ProcessingInstruction => sxml.ProcInstr(n.target,n.data)
        case n:Comment => sxml.Comment(n.text)
//        case n:EntityRef => sxml.EntityRef(n.entity) - not supported

        case d:Document =>
          // converts as root Elem (scala.xml doesn't have a DocumentSpec type)
          convert(d.root)

        case _:Attribute =>
          throw new IllegalArgumentException("unsupported Attribute, this node can only be converted as part of a Element")
        case _:Namespace =>
          throw new IllegalArgumentException("unsupported Namespace, this node can only be converted as part of a Element")
      }

      convert(e).asInstanceOf[sxml.Elem]
    }
  }
}

/* sdom -- Copyright 2014 Justin Patterson -- All Rights Reserved */
