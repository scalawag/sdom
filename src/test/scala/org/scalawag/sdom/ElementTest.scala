package org.scalawag.sdom

import org.scalatest.{Matchers,FunSuite}
import org.scalawag.sdom.xpath.XPath

class ElementTest extends FunSuite with Matchers {

  test("name") {
    val x = XML.parse("""<a xmlns="NS"/>""").root
    x.name should be (ElementName("a","NS"))
  }

  test("attributes") {
    val x = XML.parse("""<a xmlns="NS1" xmlns:t="NS2" b="1" t:c="2"/>""").root
    val attrs = x.attributeMap
    attrs.size should be (2)
    attrs(AttributeName("b","")).value should be ("1")
    attrs(AttributeName("c","NS2")).value should be ("2")
  }

  test("grab children") {
    val x = XML.parse("""<a><b><c/></b><c/></a>""").root
    val results = ( x \ "b" )
    results.size should be (1)
    val b = results.head
    b.name should be (ElementName("b"))
  }

  test("grab children with wildcard") {
    val x = XML.parse("""<a><b></b><c/></a>""").root
    val results = ( x \ * ).toSeq
    results.size should be (2)
    results(0).name should be (ElementName("b"))
    results(1).name should be (ElementName("c"))
  }

  test("grab children with namespaces") {
    val x = XML.parse("""<a xmlns="A"><b xmlns="B"/><c/></a>""").root
    implicit val namespaces = Namespaces("pb" -> "B")
    val b = ( x \ "pb:b" ).head
    b.name should be (ElementName("b","B"))
  }

  test("grab children with default namespace") {
    val x = XML.parse("""<a xmlns="A"><b xmlns="B"/><c/></a>""").root
    implicit val namespaces = Namespaces("" -> "B")
    val b = ( x \ "b" ).head
    b.name should be (ElementName("b","B"))
  }

  test("grab descendants") {
    val x = XML.parse("""<a><m><b id="1"/></m><m><b id="2"/></m></a>""").root
    val results = ( x \\ "b" ).toSeq
    results.size should be (2)
    results(0).name should be (ElementName("b",""))
    results(0).attributeMap("id").value should be ("1")
    results(1).name should be (ElementName("b",""))
    results(1).attributeMap("id").value should be ("2")
  }

  test("grab descendants with namespaces") {
    val x = XML.parse("""<a xmlns="A"><m><b xmlns="B"/></m><m><b xmlns="C"/></m></a>""").root
    implicit val namespaces = Namespaces("pb" -> "B")
    val results = ( x \\ "pb:b" )
    results.size should be (1)
    val b = results.head
    b.name should be (ElementName("b","B"))
  }

  test("grab attribute") {
    val x = XML.parse("""<a xmlns="NS1" xmlns:t="NS2" b="1" t:c="2"/>""").root
    val results = x \@ "b"
    results.size should be (1)
    val a = results.head
    a.name should be (AttributeName("b",""))
    a.value should be ("1")
  }

  test("grab attribute with namespace") {
    val x = XML.parse("""<a xmlns="NS1" xmlns:t="NS2" b="1" t:c="2"/>""").root
    implicit val namespaces = Namespaces("n" -> "NS2")
    val results = x \@ "n:c"
    results.size should be (1)
    val a = results.head
    a.name should be (AttributeName("c","NS2"))
    a.value should be ("2")
  }

  test("don't grab attribute with different namespace") {
    val x = XML.parse("""<a xmlns="NS1" xmlns:t="NS2" b="1" t:c="2"/>""").root
    val results = x \@ "t"
    results.size should be (0)
  }

  test("xpath matching elements - no namespaces") {
    val x = XML.parse("""<a><b><c id="1"/><c id="2"/></b><b><d><c id="3"/></d></b></a>""")
    val results = ( x %< "//c" ).toSeq
    results.size should be (3)
    results(0).name should be (ElementName("c"))
    results(0).attributeMap("id").value should be ("1")
    results(1).name should be (ElementName("c"))
    results(1).attributeMap("id").value should be ("2")
    results(2).name should be (ElementName("c"))
    results(2).attributeMap("id").value should be ("3")
  }

  test("xpath matching elements") {
    val x = XML.parse("""<a xmlns="A"><b xmlns="B" id="4"/><c xmlns="C" id="3"/><d xmlns="D" id="2"/></a>""")
    val results = ( x %< "//*[@id>=3]" ).toSeq
    results.size should be (2)
    results(0).name should be (ElementName("b","B"))
    results(0).attributeMap("id").value should be ("4")
    results(1).name should be (ElementName("c","C"))
    results(1).attributeMap("id").value should be ("3")
  }

  test("xpath matching attributes") {
    val x = XML.parse("""<a xmlns="A"><b xmlns="B" id="4"/><c xmlns="C" id="3"/><d xmlns="D" id="2"/></a>""")
    val results = ( x %@ ".//*[@id>=3]/@id" ).toSeq
    results.size should be (2)
    results(0).value should be ("4")
    results(1).value should be ("3")
  }

  test("xpath matching content") {
    val x = XML.parse("""<a xmlns="A"><b id="4">44</b><b id="3">33</b><b id="2">22</b></a>""")
    val results = ( x % "//*[@id>=3]/text()" ).toSeq
    results.size should be (2)
    results(0).asInstanceOf[Text].text should be ("44")
    results(1).asInstanceOf[Text].text should be ("33")
  }

  test("xpath matching elements (precompiled)") {
    val x = XML.parse("""<a xmlns="A"><b xmlns="B" id="4"/><c xmlns="C" id="3"/><d xmlns="D" id="2"/></a>""")
    val xpath = XPath.elements("//*[@id>=3]")
    val results = ( x % xpath ).toSeq
    results.size should be (2)
    results(0).name should be (ElementName("b","B"))
    results(0).attributeMap("id").value should be ("4")
    results(1).name should be (ElementName("c","C"))
    results(1).attributeMap("id").value should be ("3")
  }
}

/* sdom -- Copyright 2014 Justin Patterson -- All Rights Reserved */
