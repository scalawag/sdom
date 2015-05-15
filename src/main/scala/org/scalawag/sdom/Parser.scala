package org.scalawag.sdom

import java.io.InputStream
import java.io.StringReader
import java.util.concurrent.atomic.AtomicInteger
import org.xml.sax.{XMLReader, InputSource}
import javax.xml.parsers.SAXParserFactory
import org.scalawag.sdom.parse.SdomContentHandler

class Parser(configuration:BuilderConfiguration = BuilderConfiguration.Truest,maxPoolSize:Int = 8) {

  private[this] val parserFactory = {
    val f = SAXParserFactory.newInstance()
    f.setNamespaceAware(true)
    f
  }

  private[this] val size = new AtomicInteger(0)
  private[this] val parsers = new ObjectPool[XMLReader](maxPoolSize,parserFactory.newSAXParser.getXMLReader)

  def parse(in:InputStream):Document =
    parse(new InputSource(in))

  def parse(xml:String):Document =
    parse(new InputSource(new StringReader(xml)))

  def parse(src:InputSource):Document = parsers.use { xmlReader =>
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
