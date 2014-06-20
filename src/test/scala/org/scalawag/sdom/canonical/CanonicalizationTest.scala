package org.scalawag.sdom.canonical

import org.scalatest.FunSuite
import org.scalatest.Matchers

class CanonicalizationTest extends FunSuite with Matchers {

  test("whitespace differences are insignificant") {
    assertSameCanonically(
      """<b>5</b>""",
      """<b> 5 </b>"""
    )
  }

  test("namespace prefix differences are insignificant") {
    assertSameCanonically(
      """<c:b xmlns:c="blah">5</c:b>""",
      """<d:b xmlns:d="blah">5</d:b>"""
    )
  }

  test("default namespace and prefix are insignificant") {
    assertSameCanonically(
      """<c:b xmlns:c="blah">5</c:b>""",
      """<b xmlns="blah">5</b>"""
    )
  }

  test("comments are insignificant") {
    assertSameCanonically(
      """<b>5<!-- insignificant comment --></b>""",
      """<b>5</b>"""
    )
  }

  test("attribute ordering is insignificant") {
    assertSameCanonically(
      """<e a="1" b="2"/>""",
      """<e b="2" a="1"/>"""
    )
  }

  test("close tag style is insignificant") {
    assertSameCanonically(
      """<e/>""",
      """<e></e>"""
    )
  }

  test("namespace ordering is insignificant") {
    assertSameCanonically(
      """<e xmlns:a="u1" xmlns:b="u2"><a:c/><b:c/></e>""",
      """<e xmlns:b="u2" xmlns:a="u1"><a:c/><b:c/></e>"""
    )
  }

  test("superfluous namespaces are insignificant") {
    assertSameCanonically(
      """<e xmlns:b="ns1"/>""",
      """<e/>"""
    )
  }

  test("superfluous default namespace is insignificant") {
    assertSameCanonically(
      """<b:e xmlns:b="ns1" xmlns="ns2"/>""",
      """<b:e xmlns:b="ns1"/>"""
    )
  }

  test("namespace location is insignificant") {
    assertSameCanonically(
      """<e xmlns:b="ns1"><c b:a="blah"/></e>""",
      """<e><c xmlns:b="ns1" b:a="blah"/></e>"""
    )
  }

  test("CDATA sections are insignificant") {
    assertSameCanonically(
      """<e>j<![CDATA[o]]>g</e>""",
      """<e>jog</e>"""
    )
  }

  test("procession instructions are insignificant") {
    assertSameCanonically(
      """<?pi?><e/>""",
      """<e/>"""
    )
  }

  test("multiple default namespaces work") {
    assertSameCanonically(
      """<e><c xmlns="a"/><c xmlns="b"/></e>""",
      """<e xmlns:a="a" xmlns:b="b"><a:c/><b:c/></e>"""
    )
  }

  private def assertSameCanonically(s1:String,s2:String) {
    import org.scalawag.sdom._
    s1 should not equal (s2)
    val xml1 = XML.parse(s1)
    val xml2 = XML.parse(s2)
    val c1 = xml1.spec.canonical
    val c2 = xml2.spec.canonical
    c1 should be (c2)
  }
}

/* sdom -- Copyright 2014 Justin Patterson -- All Rights Reserved */
