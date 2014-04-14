package org.scalawag.sdom.parse

import org.xml.sax.{Attributes,Locator,ContentHandler}
import org.scalawag.sdom._
import scala.collection.mutable.Stack
import org.xml.sax.ext.LexicalHandler

class SdomContentHandler extends ContentHandler with LexicalHandler {
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
    if ( isCData )
      appendToChildren(CDataSpec(new String(ch,start,length)))
    else
      appendToChildren(TextSpec(new String(ch,start,length)))
  }

  override def ignorableWhitespace(ch:Array[Char],start:Int,length:Int) =
    characters(ch,start,length)

  override def startPrefixMapping(prefix:String,uri:String) {
    namespaces :+ NamespaceSpec(prefix,uri)
  }

  override def endPrefixMapping(prefix:String) {
  }

  override def comment(ch:Array[Char],start:Int,length:Int) {
    appendToChildren(CommentSpec(new String(ch,start,length)))
  }

  override def startCDATA() {
    isCData = true
  }

  override def endCDATA() {
    isCData = false
  }

  override def endEntity(name: String) {
    // noop
  }

  override def startEntity(name: String) {
    // noop
  }

  override def endDTD() {
    // noop
  }

  override def startDTD(name: String, publicId: String, systemId: String) {
    // noop
  }

  override def skippedEntity(name:String) {
    // noop
  }

  override def processingInstruction(target:String,data:String) {
    appendToChildren(ProcessingInstructionSpec(target,data))
  }

  override def setDocumentLocator(locator:Locator) {
    this.locator = Some(locator)
  }

  private[this] def appendToChildren(child:ChildSpec) {
    childrenStack.push(childrenStack.pop :+ child)
  }

  private[this] def getPrefix(qName:String):Option[String] = qName.indexOf(':') match {
    case n if n < 0 => None
    case n => Some(qName.substring(0,n))
  }
}

/* sdom -- Copyright 2014 Justin Patterson -- All Rights Reserved */
