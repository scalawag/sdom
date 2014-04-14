package org.scalawag.sdom.xpath

import org.jaxen.DefaultNavigator
import org.scalawag.sdom._
import scala.collection.JavaConversions.asJavaIterator
import scala.annotation.tailrec
import org.jaxen.pattern.Pattern

class SdomXPath(expr:String)(implicit namespaces:NamespacesLike = Namespaces.Empty) extends org.jaxen.BaseXPath(expr,SdomNavigator) {
  namespaces.prefixToUri foreach { case (prefix,uri) =>
    addNamespace(prefix,uri)
  }
}

object SdomNavigator extends DefaultNavigator {
  override def parseXPath(expr:String) = new SdomXPath(expr)

  override def getNodeType(any:Any) = any match {
    case _:Document => Pattern.DOCUMENT_NODE
    case _:Element => Pattern.ELEMENT_NODE
    case _:Attribute => Pattern.ATTRIBUTE_NODE
    case _:Text => Pattern.TEXT_NODE
    case _:Comment => Pattern.COMMENT_NODE
    case _:ProcessingInstruction => Pattern.PROCESSING_INSTRUCTION_NODE
    case _:Namespace => Pattern.NAMESPACE_NODE
    case _ => Pattern.UNKNOWN_NODE
  }

  override def isDocument(any:Any) = any.isInstanceOf[Document]

  override def isProcessingInstruction(any:Any) = any.isInstanceOf[ProcessingInstruction]
  override def getProcessingInstructionTarget(any:Any) = any.asInstanceOf[ProcessingInstruction].target
  override def getProcessingInstructionData(any:Any) = any.asInstanceOf[ProcessingInstruction].data

  override def isText(any:Any) = any.isInstanceOf[TextLike]
  override def getTextStringValue(any:Any) = any.asInstanceOf[TextLike].text

  override def isComment(any:Any) = any.isInstanceOf[Comment]
  override def getCommentStringValue(any:Any) = any.asInstanceOf[Comment].text

  override def isNamespace(any:Any) = any.isInstanceOf[Namespace]
  override def getNamespacePrefix(any:Any) = any.asInstanceOf[Namespace].prefix
  override def getNamespaceStringValue(any:Any) = any.asInstanceOf[Namespace].uri

  override def isAttribute(any:Any) = any.isInstanceOf[Attribute]
  override def getAttributeQName(any:Any) = ??? // getAttributeName(any)
  override def getAttributeName(any:Any) = any.asInstanceOf[Attribute].name.localName
  override def getAttributeNamespaceUri(any:Any) = any.asInstanceOf[Attribute].name.uri
  override def getAttributeStringValue(any:Any) = any.asInstanceOf[Attribute].value

  override def isElement(any:Any) = any.isInstanceOf[Element]
  override def getElementQName(any:Any) = ??? // getElementName(any)
  override def getElementName(any:Any) = any.asInstanceOf[Element].name.localName
  override def getElementNamespaceUri(any:Any) = any.asInstanceOf[Element].name.uri
  override def getElementStringValue(any:Any) = any.asInstanceOf[Element].text

  private[this] val EMPTY:java.util.Iterator[_] = Iterator.empty

  override def getChildAxisIterator(any:Any) = any match {
    case d:Document => Iterator(d.root)
    case e:Element => e.children.iterator
    case _ => EMPTY
  }

  override def getNamespaceAxisIterator(any:Any) = any match {
    case e:Element => e.namespaces.iterator
    case _ => EMPTY
  }

  override def getAttributeAxisIterator(any:Any) = any match {
    case e:Element => e.attributes.iterator
    case _ => EMPTY
  }

  override def getDescendantOrSelfAxisIterator(any:Any) = any match {
    case p:Parent => Iterator(p) ++ p.descendants.iterator
    case n:Node => Iterator(n)
    case _ => EMPTY
  }

  override def getDescendantAxisIterator(any:Any) = any match {
    case p:Parent => p.descendants.iterator
    case _ => EMPTY
  }

  override def getSelfAxisIterator(any:Any) = Iterator(any)

  override def getParentNode(any:Any) = any match {
    case n:Node => n.parent.orNull
  }

  override def getParentAxisIterator(any:Any) = any match {
    case n:Node => n.parent.iterator
  }

  override def getAncestorAxisIterator(any:Any) = any match {
    case r:Node => r.ancestry.iterator
  }

  // I don't know why this is the only one that needs an explicit return type.
  override def getAncestorOrSelfAxisIterator(any:Any):java.util.Iterator[Any] = any match {
    case r:Node => Iterator(r) ++ r.ancestry.iterator
  }

  override def getPrecedingSiblingAxisIterator(any:Any) = any match {
    case a:Attribute => EMPTY
    case n:Namespace => EMPTY
    case d:Document => EMPTY
    case c:Node => c.parent match {
      case None => EMPTY
      case Some(p) => p.children.toSeq.reverseIterator.dropWhile( child => ! ( child eq c ) ).drop(1)
    }
  }

  override def getFollowingSiblingAxisIterator(any:Any) = any match {
    case a:Attribute => EMPTY
    case n:Namespace => EMPTY
    case d:Document => EMPTY
    case c:Node => c.parent match {
      case None => EMPTY
      case Some(p) => p.children.iterator.dropWhile( child => ! ( child eq c ) ).drop(1)
    }
  }

  @tailrec
  override def getDocumentNode(any:Any) = any match {
    case d:Document => d
    case r:Node => r.parent match {
      case Some(p) => getDocumentNode(p)
      case None => null
    }
  }

  override def translateNamespacePrefixToUri(prefix:String,any:Any) = any match {
    case e:Element => e.scope.prefixToUri.get(prefix).orNull
    case d:Document => null
    case c:Child => c.parent.map(translateNamespacePrefixToUri(prefix,_)).orNull
    case a:Attribute => a.parent.map(translateNamespacePrefixToUri(prefix,_)).orNull
    case n:Namespace => n.parent.map(translateNamespacePrefixToUri(prefix,_)).orNull
  }

//  override def getDocument(url: String): AnyRef = super.getDocument(url) //TODO?
//  override def getElementById(contextNode: scala.Any, elementId: String): AnyRef = super.getElementById(contextNode, elementId) // TODO
}

/* sdom -- Copyright 2014 Justin Patterson -- All Rights Reserved */
