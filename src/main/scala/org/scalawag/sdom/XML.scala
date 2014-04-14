package org.scalawag.sdom

import java.io.InputStream
import java.io.StringReader
import javax.xml.validation.Schema
import org.xml.sax.InputSource
import javax.xml.parsers.SAXParserFactory
import org.scalawag.sdom.parse.SdomContentHandler

object XML {
  def parse(in:InputStream):Document =
    parse(new InputSource(in))

  def parse(xml:String):Document =
    parse(new InputSource(new StringReader(xml)))

  def parse(src:InputSource):Document = {
    val spf = SAXParserFactory.newInstance()
    spf.setNamespaceAware(true)
    val saxParser = spf.newSAXParser
    val xmlReader = saxParser.getXMLReader
    val handler = new SdomContentHandler
    xmlReader.setContentHandler(handler)
    xmlReader.setProperty("http://xml.org/sax/properties/lexical-handler",handler)
    xmlReader.setEntityResolver(null)
    xmlReader.setErrorHandler(null)
    xmlReader.parse(src)
    Document(handler.document.get)
  }
/*
  def parse(in:InputStream,schema:Schema):ElementSpec =
    parse(new InputSource(in),schema)

  def parse(xml:String,schema:Schema):ElementSpec =
    parse(new InputSource(new StringReader(xml)),schema)

  def parse(src:InputSource,schema:Schema):ElementSpec = {
    // create an XMLReaderJDOMFactory by passing the schema
    val factory = new XMLReaderSchemaFactory(schema)
    // create a SAXBuilder using the XMLReaderJDOMFactory
    build(src,new SAXBuilder(factory))
  }

  private[this] def build(src:InputSource,builder:SAXBuilder):ElementSpec = {
    val doc = builder.build(src)
    new ElementSpec(doc.getRootElement)
  }
  */
}

/* sdom -- Copyright 2014 Justin Patterson -- All Rights Reserved */
