package org.scalawag.sdom.validation

import javax.xml.XMLConstants
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.Schema

import org.scalatest.{FunSuite, Matchers}
import org.scalawag.sdom._

import org.scalawag.sdom.BuilderConfiguration.Truest

object ValidationErrorMessageTest {
  def createSchema(resourcePath:String):Schema = {
    val in = getClass.getClassLoader.getResourceAsStream(resourcePath)
    try {
      val source = new StreamSource(in)
      val schemaFactory = javax.xml.validation.SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
      schemaFactory.newSchema(source)
    } finally {
      in.close
    }
  }
}

import ValidationErrorMessageTest._

class ValidationErrorMessageTest extends FunSuite with Matchers {
  val schema = createSchema("test.xsd")

  test("unknown element") {
    val x = Document(<a xmlns="NS1" xmlns:t="NS2" b="1" t:c="2"/>).root
    val ex = intercept[ValidationException] {
      x.validate(schema)
    }

    ex.xmlContext shouldBe
      """
        |<a b="1" ns1:c="2" xmlns="NS1" xmlns:ns1="NS2"/>
        |^
      """.stripMargin.trim
  }

  test("unknown attribute") {
    val x = Document(<root xmlns="urn:org.scalawag.sdom:test" b="1" c="2"/>).root
    val ex = intercept[ValidationException] {
      x.validate(schema)
    }

    ex.xmlContext shouldBe
      """
        |<root b="1" c="2" xmlns="urn:org.scalawag.sdom:test"/>
        |^
      """.stripMargin.trim
  }

  test("unexpected text") {
    val x = Document(<root xmlns="urn:org.scalawag.sdom:test">something</root>).root
    val ex = intercept[ValidationException] {
      x.validate(schema)
    }

    ex.xmlContext shouldBe
      """
        |<root xmlns="urn:org.scalawag.sdom:test">something</root>
        |                                                  ^
      """.stripMargin.trim
  }

  test("unexpected text among elements") {
    val x = Document(<root xmlns="urn:org.scalawag.sdom:test"><a>2</a><b>4</b>wait<c>8</c></root>).root
    val ex = intercept[ValidationException] {
      x.validate(schema)
    }

    ex.xmlContext shouldBe
      """
        |<root xmlns="urn:org.scalawag.sdom:test"><a>2</a><b>4</b>wait<c>8</c></root>
        |                                                                     ^
      """.stripMargin.trim
  }

  test("missing child element") {
    val x = Document(
<root xmlns="urn:org.scalawag.sdom:test">
  <a>2</a>
  <b>4</b>
  <b>5</b>
  <b>6</b>
  <b>7</b>
</root>
    ).root
    val ex = intercept[ValidationException] {
      x.validate(schema)
    }

    ex.xmlContext shouldBe
      """
        |<root xmlns="urn:org.scalawag.sdom:test">
        |  <a>2</a>
        |  <b>4</b>
        |  <b>5</b>
        |  <b>6</b>
        |  <b>7</b>
        |</root>
        |^
      """.stripMargin.trim
  }

  test("invalid child") {
    val x = Document(<root xmlns="urn:org.scalawag.sdom:test"><f>2</f></root>).root
    val ex = intercept[ValidationException] {
      x.validate(schema)
    }

    ex.xmlContext shouldBe
      """
        |<root xmlns="urn:org.scalawag.sdom:test"><f>...
        |                                         ^
      """.stripMargin.trim
  }

  test("invalid grandchild") {
    val x = Document(<root xmlns="urn:org.scalawag.sdom:test"><a><f>2</f></a></root>).root
    val ex = intercept[ValidationException] {
      x.validate(schema)
    }

    ex.xmlContext shouldBe
      """
        |<root xmlns="urn:org.scalawag.sdom:test"><a><f>2</f></a>...
        |                                                    ^
      """.stripMargin.trim
  }

  test("validate single element only") {
    val x = Document(
<root xmlns="urn:org.scalawag.sdom:test">
  <a>2</a>
  <b>4</b>
  <b>5</b>
  <b>6</b>
  <b>7</b>
</root>
    ).root

    val ex = intercept[ValidationException] {
      x.childElements.head.validate(schema)
    }

    ex.xmlContext shouldBe
      """
        |<a>...
        |^
      """.stripMargin.trim

  }
}

/* sdom -- Copyright 2014 Justin Patterson -- All Rights Reserved */
