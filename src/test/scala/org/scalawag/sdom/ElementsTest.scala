package org.scalawag.sdom
/*
import org.scalatest.FunSuite
import org.scalatest.Matchers
import Implicits._

class ElementsTest extends FunSuite with Matchers {

  test("grab children") {
    val x = XML.parse("""<a><b><x/></b><b><y/></b><c><z/></c></a>""")
    val results = ( x \ "b" \ "y" ).toSeq
    results.size should be (1)
    val b = results.head
    b.name should be (ExpandedName("y"))
  }

  test("grab children with wildcard") {
    val x = XML.parse("""<a><b><x/></b><b><y/></b><c><z/></c></a>""")
    val results = ( x \ "b" \ "_" ).toSeq
    results.size should be (2)
    results(0).name should be (ExpandedName("x"))
    results(1).name should be (ExpandedName("y"))
  }

  test("grab children with namespaces") {
    val x = XML.parse("""<a><b><x xmlns="B"/></b><b><x xmlns="C"/></b><c><x xmlns="B"/></c></a>""")
    implicit val namespaces = Namespaces("b" -> "B")
    val results = ( x \ "b" \ "b:x" ).toSeq
    results.size should be (1)
    results.head.name should be (ExpandedName("x","B"))
  }

  test("grab children with default namespace") {
    val x = XML.parse("""<a><b><x xmlns="B"/></b><b><x xmlns="C"/></b><c><x xmlns="B"/></c></a>""")
    implicit val namespaces = Namespaces("" -> "B")
    val results = ( x \ "{}b" \ "x" ).toSeq
    results.size should be (1)
    results.head.name should be (ExpandedName("x","B"))
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

    val results = ( x \ "m" \\ "b" ).toSeq
    results.size should be (3)
    results(0).name should be (ExpandedName("b",""))
    results(0).attributes("id") should be ("1")
    results(1).name should be (ExpandedName("b",""))
    results(1).attributes("id") should be ("2")
    results(2).name should be (ExpandedName("b",""))
    results(2).attributes("id") should be ("3")
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

    val results = ( x \ "m" \\ "_" ).toSeq
    results.size should be (5)
    results(0).name should be (ExpandedName("m"))
    results(1).name should be (ExpandedName("c"))
    results(2).name should be (ExpandedName("b"))
    results(2).attributes("id") should be ("1")
    results(3).name should be (ExpandedName("m"))
    results(4).name should be (ExpandedName("b"))
    results(4).attributes("id") should be ("3")
  }

  test("grab attribute") {
    val x = XML.parse((
      <root>
        <a b="1"/>
        <a b="2"/>
      </root>
    ).toString)

    val results = ( x \ "a" \@ "b" ).toSeq
    results.size should be (2)
    results(0).name should be (ExpandedName("b",""))
    results(0).value should be ("1")
    results(1).name should be (ExpandedName("b",""))
    results(1).value should be ("2")
  }

  test("grab attribute with namespace") {
    val x = XML.parse((
      <root xmlns:t="NS2">
        <a b="1" t:b="3"/>
        <a b="2" t:b="4"/>
      </root>
    ).toString)

    implicit val namespaces = Namespaces("ns2" -> "NS2")
    val results = ( x \ "a" \@ "ns2:b" ).toSeq
    results.size should be (2)
    results(0).name should be (ExpandedName("b","NS2"))
    results(0).value should be ("3")
    results(1).name should be (ExpandedName("b","NS2"))
    results(1).value should be ("4")
  }

  test("xpath matching elements") {
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

    val results = ( x \ "m" %< "*[@id>=3]" ).toSeq
    results.size should be (1)
    results(0).name should be (ExpandedName("a"))
    results(0).attributes("id") should be ("3")
  }

  test("xpath matching attributes") {
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

    val results = ( x \ "m" %@ "*[@id>=3]/@id" ).toSeq
    results.size should be (1)
    results(0).value should be ("3")
  }

  test("xpath matching content") {
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

    val results = ( x \ "m" % "*[@id>=2]/text()" ).toSeq
    results.size should be (2)
    results(0).asInstanceOf[TextSpec].text should be ("B")
    results(1).asInstanceOf[TextSpec].text should be ("D")
  }
}
*/
/* sdom -- Copyright 2014 Justin Patterson -- All Rights Reserved */
