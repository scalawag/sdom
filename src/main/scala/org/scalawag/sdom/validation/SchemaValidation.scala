package org.scalawag.sdom.validation

import org.scalawag.sdom._
import javax.xml.validation.{ValidatorHandler, Schema}
import org.xml.sax.helpers.AttributesImpl
import org.xml.sax.SAXParseException

trait SchemaValidation {
  implicit class SchemaValidator(xml:Element) {

    /* Validates the DOM by walking it and firing the same events that a SAX parser would. */

    def validate(schema:Schema) {
      val validator = schema.newValidatorHandler

      def makeQName(prefix:String,localName:String) =
        if ( prefix == "" )
          localName
        else
          s"$prefix:$localName"

      /* This var is kind of ugly but I think it's the most efficient way to know what we were processing when
       * we encountered a validation error.  Since we don't know the position (line & column) since we're not
       * actually parsing from a stream, this is about the best we can do.
       */

      var lastNodeVisited:Node = null

      def helper(child:Child):Unit = {
        lastNodeVisited = child
        child match {
          case e:Element =>

            val namespaces = e.namespaces
            namespaces foreach { ns =>
              validator.startPrefixMapping(ns.prefix,ns.uri)
            }

            val attributes = new AttributesImpl
            e.attributes foreach { a =>
              val name = a.name
              val localName = name.localName
              val uri = name.uri
              val prefix = a.prefix
              val qname = makeQName(prefix,localName)
              attributes.addAttribute(uri,localName,qname,"CDATA",a.value)
            }

            val name = e.name
            val localName = name.localName
            val uri = name.uri
            val prefix = e.prefix
            val qname = makeQName(prefix,localName)

            validator.startElement(uri,localName,qname,attributes)
            e.children.foreach(helper)
            validator.endElement(uri,localName,qname)

            namespaces foreach { ns =>
              validator.endPrefixMapping(ns.prefix)
            }

          case t:TextLike =>
            validator.characters(t.text.toCharArray,0,t.text.length)

          case p:ProcessingInstruction =>
            validator.processingInstruction(p.target,p.data)

          case c:Comment => // ignore
        }
      }

      try {
        validator.startDocument()
        helper(xml)
        validator.endDocument()
      } catch {
        case ex:SAXParseException =>
          throw new ValidationException(ex,lastNodeVisited)
      }
    }
  }
}

case class ValidationException(cause:SAXParseException,context:Node) extends Exception {
  override def getMessage = {
    cause.getMessage + " at " + context.ancestry.mkString(" <- ")
  }
}

/* sdom -- Copyright 2014 Justin Patterson -- All Rights Reserved */
