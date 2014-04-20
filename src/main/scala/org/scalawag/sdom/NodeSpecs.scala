package org.scalawag.sdom

sealed trait NodeSpec {
  protected[sdom] val usesNamespaces:Boolean = false
}

sealed trait NamedSpec[T <: NamedSpec[_]] extends NodeSpec {
  val name:ExpandedName[_ <: ExpandedName[_]]
  val prefix:Option[String]

  def withName(localName:String,uri:String):T

  def withLocalName(localName:String):T =
    withName(localName,name.uri)

  def withUri(uri:String):T =
    withName(name.localName,uri)

  def withPrefix(prefix:Option[String]):T

  def withPrefix(prefix:String):T =
    withPrefix(Some(prefix))

  def withoutPrefix:T =
    withPrefix(None)
}

sealed trait ParentSpec[SELF <: ParentSpec[_]] extends NodeSpec {
  val children:Iterable[ChildSpec]

  def childElements:Iterable[ElementSpec] =
    children.filter(_.isInstanceOf[ElementSpec]).map(_.asInstanceOf[ElementSpec])

  def withChildren(children:Iterable[ChildSpec]):SELF

  def withChildren(children:ChildSpec*):SELF =
    withChildren(children)

  def withAdditionalChildren(children:ChildSpec*):SELF =
    withChildren(this.children ++ children)

  def withoutChildren:SELF =
    withChildren(Iterable.empty)
}

sealed trait ChildSpec extends NodeSpec

case class DocumentSpec(children:Iterable[ChildSpec]) extends ParentSpec[DocumentSpec] {
  require( childElements.size == 1 , "XML document must have a single root element.")
  val rootElement = childElements.head

  override protected[sdom] val usesNamespaces = children.exists(_.usesNamespaces)

  override def withChildren(children:Iterable[ChildSpec]) =
    this.copy(children = children)
}

object DocumentSpec {
  def apply(children:ChildSpec*):DocumentSpec = apply(children:_*)
}

case class ElementSpec(name:ElementName,
                       attributes:Iterable[AttributeSpec] = Iterable.empty,
                       children:Iterable[ChildSpec] = Iterable.empty,
                       namespaces:Iterable[NamespaceSpec] = Iterable.empty,
                       prefix:Option[String] = None) extends ParentSpec[ElementSpec] with ChildSpec with NamedSpec[ElementSpec]
{

  override protected[sdom] val usesNamespaces =
    ! name.uri.isEmpty || children.exists(_.usesNamespaces) || attributes.exists(_.usesNamespaces)

  // Validate that this element and its attributes don't imply conflicting namespace declarations.
  // This happens when the same prefix is specified on multiple Named nodes that have different URIs.
  {
    val nameds = attributes ++ Iterable(this)
    val prefixesAndURIs = nameds.filter(_.prefix.isDefined).map( n => NamespaceSpec(n.prefix.get,n.name.uri) )

    prefixesAndURIs.groupBy(_.prefix).foreach { case (prefix,mappings) =>
      val uris = mappings.map(_.uri).toSeq.distinct
      if ( uris.size != 1 )
        throw new IllegalArgumentException(s"the prefix '$prefix' can not be mapped to multiple URIs: ${uris.mkString(" ")}")
    }
  }

  lazy val text:String = children.map {
    case t:TextLikeSpec => t.text
    case e:ElementSpec => e.text
    case _ => ""
  }.mkString

  def withName(name:ElementName) =
    this.copy(name = name)

  override def withName(localName:String,uri:String) =
    this.copy(name = ElementName(localName,uri))

  override def withPrefix(prefix:Option[String]) =
    this.copy(prefix = prefix)

  override def withChildren(children:Iterable[ChildSpec]) =
    this.copy(children = children)

  def withAttributes(attributes:Iterable[AttributeSpec]):ElementSpec =
    this.copy(attributes = attributes)

  def withAttributes(attributes:AttributeSpec*):ElementSpec =
    withAttributes(attributes)

  def withAdditionalAttributes(attributes:AttributeSpec*):ElementSpec =
    withAttributes(this.attributes ++ attributes)

  def withoutAttributes:ElementSpec =
    withAttributes(Iterable.empty)

  def withNamespaces(namespaces:Iterable[NamespaceSpec]):ElementSpec =
    this.copy(namespaces = namespaces)

  def withNamespaces(namespaces:NamespaceSpec*):ElementSpec =
    withNamespaces(namespaces)

  def withAdditionalNamespaces(namespaces:NamespaceSpec*):ElementSpec =
    withNamespaces(this.namespaces ++ namespaces)

  def withoutNamespaces:ElementSpec =
    withNamespaces(Iterable.empty)
}

case class AttributeSpec(name:AttributeName,value:String,prefix:Option[String] = None) extends NodeSpec with NamedSpec[AttributeSpec] {

  override protected[sdom] val usesNamespaces = ! name.uri.isEmpty

  def withName(name:AttributeName) =
    this.copy(name = name)

  override def withName(localName:String,uri:String) =
    this.copy(name = AttributeName(localName,uri))

  override def withPrefix(prefix:Option[String]) =
    this.copy(prefix = prefix)

}

case class NamespaceSpec(prefix:String,uri:String) extends NodeSpec {
  Option(Verifier.checkNamespacePrefix(prefix)) foreach { reason =>
    throw new IllegalArgumentException(s"invalid element namespace prefix '$prefix': $reason")
  }
  Option(Verifier.checkNamespaceURI(uri)) foreach { reason =>
    throw new IllegalArgumentException(s"invalid element namespace URI '$uri': $reason")
  }
  if ( uri.isEmpty && ! prefix.isEmpty )
    throw new IllegalArgumentException(s"empty namespace declarations can not have a prefix, one has the prefix '$prefix'")
}

sealed trait TextLikeSpec extends ChildSpec {
  val text:String
}

private object TextLikeSpec {
  def collapseAppend(existingChildren:Seq[ChildSpec],newChild:ChildSpec):Seq[ChildSpec] =
    if ( newChild.isInstanceOf[TextSpec] && ! existingChildren.isEmpty && existingChildren.last.isInstanceOf[TextSpec] ) {
      val newLast = TextSpec(existingChildren.last.asInstanceOf[TextSpec].text + newChild.asInstanceOf[TextSpec].text)
      existingChildren.dropRight(1) :+ newLast
    } else if ( newChild.isInstanceOf[CDataSpec] && ! existingChildren.isEmpty && existingChildren.last.isInstanceOf[CDataSpec] ) {
      val newLast = CDataSpec(existingChildren.last.asInstanceOf[CDataSpec].text + newChild.asInstanceOf[CDataSpec].text)
      existingChildren.dropRight(1) :+ newLast
    } else {
      existingChildren :+ newChild
    }
}

case class CDataSpec(text:String) extends TextLikeSpec

case class TextSpec(text:String) extends TextLikeSpec

case class ProcessingInstructionSpec(target:String,data:String) extends ChildSpec

case class CommentSpec(text:String) extends ChildSpec

/* sdom -- Copyright 2014 Justin Patterson -- All Rights Reserved */
