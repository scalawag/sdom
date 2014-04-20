package org.scalawag.sdom

import java.io.InputStream
import java.io.StringReader
import org.xml.sax.InputSource
import javax.xml.parsers.SAXParserFactory
import org.scalawag.sdom.parse.SdomContentHandler

class Parser(configuration:BuilderConfiguration = BuilderConfiguration.Truest) {
  def parse(in:InputStream):Document =
    parse(new InputSource(in))

  def parse(xml:String):Document =
    parse(new InputSource(new StringReader(xml)))

  def parse(src:InputSource):Document = {
    val spf = SAXParserFactory.newInstance()
    spf.setNamespaceAware(true)
    val saxParser = spf.newSAXParser
    val xmlReader = saxParser.getXMLReader
    val handler = new SdomContentHandler(configuration)
    xmlReader.setContentHandler(handler)
    xmlReader.setProperty("http://xml.org/sax/properties/lexical-handler",handler)
    xmlReader.setEntityResolver(null)
    xmlReader.setErrorHandler(null)
    xmlReader.parse(src)
    Document(handler.document.get)
  }
}

/* sdom -- Copyright 2014 Justin Patterson -- All Rights Reserved */
