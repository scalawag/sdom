package org.scalawag.sdom

import org.scalatest.FunSuite
import org.scalatest.Matchers

class ElementsTest extends FunSuite with Matchers {

  test("grab children from non-operator descent") {
    val x = XML.parse("""<a><b><x/></b><b><y/></b><c><z/></c></a>""")
    val results = ( x.childElements \ "b" \ "y" ).toSeq
    results.size shouldBe 1
    val b = results.head
    b.name shouldBe ElementName("y")
  }

  test("grab children") {
    val x = XML.parse("""<a><b><x/></b><b><y/></b><c><z/></c></a>""")
    val results = ( x \ "a" \ "b" \ "y" ).toSeq
    results.size shouldBe 1
    val b = results.head
    b.name shouldBe ElementName("y")
  }

  test("grab children with wildcard") {
    val x = XML.parse("""<a><b><x/></b><b><y/></b><c><z/></c></a>""")
    val results = ( x \ "a" \ "b" \ * ).toSeq
    results.size shouldBe 2
    results(0).name shouldBe ElementName("x")
    results(1).name shouldBe ElementName("y")
  }

  test("grab children with namespaces") {
    val x = XML.parse("""<a><b><x xmlns="B"/></b><b><x xmlns="C"/></b><c><x xmlns="B"/></c></a>""")
    implicit val namespaces = Namespaces("b" -> "B")
    val results = ( x \ "a" \ "b" \ "b:x" ).toSeq
    results.size shouldBe 1
    results.head.name shouldBe ElementName("x","B")
  }

  test("grab children with default namespace") {
    val x = XML.parse("""<a><b><x xmlns="B"/></b><b><x xmlns="C"/></b><c><x xmlns="B"/></c></a>""")
    implicit val namespaces = Namespaces("" -> "B")
    val results = ( x \ * \ "{}b" \ "x" ).toSeq
    results.size shouldBe 1
    results.head.name shouldBe ElementName("x","B")
  }

  test("grab descendants") {
    val x = XML.parse((
      <a>
        <m>
          <c>
            <b id="1"/>
            <b id="2"/>
          </c>
        </m>
        <m>
          <b id="3"/>
        </m>
      </a>
    ).toString)

    val results = ( x \ "a" \ "m" \\ "b" ).toSeq
    results.size shouldBe 3
    results(0).name shouldBe ElementName("b","")
    results(0).attributeMap("id").value shouldBe "1"
    results(1).name shouldBe ElementName("b","")
    results(1).attributeMap("id").value shouldBe "2"
    results(2).name shouldBe ElementName("b","")
    results(2).attributeMap("id").value shouldBe "3"
  }

  test("grab descendants with wildcards ") {
    val x = XML.parse((
      <a>
        <m>
          <c>
            <b id="1"/>
          </c>
        </m>
        <m>
          <b id="3"/>
        </m>
      </a>
    ).toString)

    val results = ( x \ "a" \\ * ).toSeq
    results.size shouldBe 5
    results(0).name shouldBe ElementName("m")
    results(1).name shouldBe ElementName("c")
    results(2).name shouldBe ElementName("b")
    results(2).attributeMap("id").value shouldBe "1"
    results(3).name shouldBe ElementName("m")
    results(4).name shouldBe ElementName("b")
    results(4).attributeMap("id").value shouldBe "3"
  }

  test("grab attribute") {
    val x = XML.parse((
      <root>
        <a b="1"/>
        <a b="2"/>
      </root>
    ).toString)

    val results = ( x \ "root" \ "a" \@ "b" ).toSeq
    results.size shouldBe 2
    results(0).name shouldBe AttributeName("b","")
    results(0).value shouldBe "1"
    results(1).name shouldBe AttributeName("b","")
    results(1).value shouldBe "2"
  }

  test("grab attribute with namespace") {
    val x = XML.parse((
      <root xmlns:t="NS2">
        <a b="1" t:b="3"/>
        <a b="2" t:b="4"/>
      </root>
    ).toString)

    implicit val namespaces = Namespaces("ns2" -> "NS2")
    val results = ( x \ * \ "a" \@ "ns2:b" ).toSeq
    results.size shouldBe 2
    results(0).name shouldBe AttributeName("b","NS2")
    results(0).value shouldBe "3"
    results(1).name shouldBe AttributeName("b","NS2")
    results(1).value shouldBe "4"
  }

  test("xpath selecting elements") {
    val x = XML.parse((
      <root>
        <m>
          <a id="1"/>
          <a id="2"/>
        </m>
        <m>
          <a id="1"/>
          <a id="3"/>
        </m>
        <n>
          <a id="1"/>
          <a id="4"/>
        </n>
      </root>
    ).toString)

    val results = ( x \ * \ "m" %< "*[@id>=3]" ).toSeq
    results.size shouldBe 1
    results(0).name shouldBe ElementName("a")
    results(0).attributeMap("id").value shouldBe "3"
  }

  test("xpath selecting attributes") {
    val x = XML.parse((
      <root>
        <m>
          <a id="1"/>
          <a id="2"/>
        </m>
        <m>
          <a id="1"/>
          <a id="3"/>
        </m>
        <n>
          <a id="1"/>
          <a id="4"/>
        </n>
      </root>
    ).toString)

    val results = ( x.root \ "m" %@ "*[@id>=3]/@id" ).toSeq
    results.size shouldBe 1
    results(0).value shouldBe "3"
  }

  test("xpath selecting content") {
    val x = XML.parse((
      <root>
        <m>
          <a id="1">A</a>
          <a id="2">B</a>
        </m>
        <m>
          <a id="1">C</a>
          <a id="3">D</a>
        </m>
        <n>
          <a id="1">E</a>
          <a id="4">F</a>
        </n>
      </root>
    ).toString)

    val results = ( x \ * \ "m" %% "*[@id>=2]/text()" ).toSeq
    results.size shouldBe 2
    results(0).asInstanceOf[Text].text shouldBe "B"
    results(1).asInstanceOf[Text].text shouldBe "D"
  }

  test("xpath selecting anything") {
    val x = XML.parse((
      <root>
        <m>
          <a id="1">A</a>
          <a id="2">B</a>
        </m>
        <m>
          <a id="1">C</a>
          <a id="3">D</a>
        </m>
        <n>
          <a id="1">E</a>
          <a id="4">F</a>
        </n>
      </root>
    ).toString)

    val results = ( x.root % "count(//a)" ).toSeq
    results.size shouldBe 1
    results(0) shouldBe 6
  }
}

/* sdom -- Copyright 2014 Justin Patterson -- All Rights Reserved */
