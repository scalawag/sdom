package org.scalawag.sdom.parse

import org.xml.sax.{Attributes,Locator,ContentHandler}
import org.scalawag.sdom._
import scala.collection.mutable.Stack
import org.xml.sax.ext.LexicalHandler

class SdomContentHandler(configuration:BuilderConfiguration) extends ContentHandler with LexicalHandler {
  private[this] var locator:Option[Locator] = None
  private[this] val attributeStack = new Stack[Seq[AttributeSpec]]
  private[this] val childrenStack = new Stack[Seq[ChildSpec]]
  private[this] val namespaceStack = new Stack[Seq[NamespaceSpec]]
  private[this] var namespaces = Seq.empty[NamespaceSpec]
  private[this] var isCData = false

  var document:Option[DocumentSpec] = None

  override def startDocument() {
    require(document.isEmpty)
    childrenStack.push(Seq.empty)
  }

  override def endDocument() {
    document = Some(new DocumentSpec(childrenStack.pop))
  }

  override def startElement(uri:String,localName:String,qName:String,srcAttributes:Attributes) {
    val attributes =
      ( 0 until srcAttributes.getLength ) map { i =>
        val aname = AttributeName(srcAttributes.getLocalName(i),srcAttributes.getURI(i))
        AttributeSpec(aname,srcAttributes.getValue(i),getPrefix(srcAttributes.getQName(i)))
      }

    attributeStack.push(attributes)
    childrenStack.push(Seq.empty)
    namespaceStack.push(namespaces)
    namespaces = Seq.empty
  }

  override def endElement(uri:String,localName:String,qName:String) {
    val element =
      ElementSpec(ElementName(localName,uri),attributeStack.pop,childrenStack.pop,namespaceStack.pop,getPrefix(qName))
    appendToChildren(element)
  }

  override def characters(ch:Array[Char],start:Int,length:Int) {
    val original = new String(ch,start,length)
    val trimmed = if ( configuration.trimWhitespace ) original.trim else original
    if ( ! trimmed.isEmpty || ! configuration.discardWhitespace ) {
      if ( isCData && ! configuration.treatCDataAsText )
        appendToChildren(CDataSpec(trimmed))
      else
        appendToChildren(TextSpec(trimmed))
    }
  }

  override def ignorableWhitespace(ch:Array[Char],start:Int,length:Int) =
    characters(ch,start,length)

  override def startPrefixMapping(prefix:String,uri:String) {
    namespaces :+ NamespaceSpec(prefix,uri)
  }

  override def endPrefixMapping(prefix:String) {
    // NOOP
  }

  override def comment(ch:Array[Char],start:Int,length:Int) {
    if ( ! configuration.discardComments )
      appendToChildren(CommentSpec(new String(ch,start,length)))
  }

  override def startCDATA() {
    isCData = true
  }

  override def endCDATA() {
    isCData = false
  }

  override def endEntity(name: String) {
    // NOOP
  }

  override def startEntity(name: String) {
    // NOOP
  }

  override def endDTD() {
    // NOOP
  }

  override def startDTD(name: String, publicId: String, systemId: String) {
    // NOOP
  }

  override def skippedEntity(name:String) {
    // noop
  }

  override def processingInstruction(target:String,data:String) {
    if ( ! configuration.discardProcessingInstructions )
      appendToChildren(ProcessingInstructionSpec(target,data))
  }

  override def setDocumentLocator(locator:Locator) {
    this.locator = Some(locator)
  }

  private[this] def appendToChildren(child:ChildSpec) {
    val oldChildren = childrenStack.pop

    val newChildren =
      if ( configuration.collapseAdjacentTextLikes && child.isInstanceOf[TextLikeSpec] )
        TextLikeSpec.collapseAppend(oldChildren,child)
      else
        oldChildren :+ child

    childrenStack.push(newChildren)
  }

  private[this] def getPrefix(qName:String):Option[String] = qName.indexOf(':') match {
    case n if n < 0 => None
    case n => Some(qName.substring(0,n))
  }
}

/* sdom -- Copyright 2014 Justin Patterson -- All Rights Reserved */
