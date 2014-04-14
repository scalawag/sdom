package org.scalawag.sdom

import org.scalatest.{FunSpec, Matchers}
import scala.xml.Elem
import org.scalawag.sdom.ScalaXmlConversions.{Strategies, Strategy}

class ScalaXmlConversionsTest extends FunSpec with Matchers {

  describe("element") {
    val sdom = ElementSpec("a")
    val literal = <a/>

    bothWays(sdom,literal)
  }

  describe("child elements") {
    val sdom =
      ElementSpec("a",children = Iterable(
        ElementSpec("b",children = Iterable(
          ElementSpec("c")
        ))
      ))

    val literal = <a><b><c/></b></a>

    bothWays(sdom,literal)
  }

  describe("attribute") {
    val sdom = 
      ElementSpec("a",attributes = Iterable(
        AttributeSpec("f","5"),
        AttributeSpec("g","h")
      ))

    val literal = <a f="5" g="h"/>

    bothWays(sdom,literal)
  }

  describe("unprefixed namespace") {
    val sdom =
      ElementSpec(ElementName("a","NS1"),
                  namespaces = Iterable(NamespaceSpec("","NS1")),
                  children = Iterable(
        ElementSpec(ElementName("b","NS2"),
                    namespaces = Iterable(NamespaceSpec("","NS2"))
        )
      ))
    
    val literal = <a xmlns="NS1"><b xmlns="NS2"/></a>

    bothWays(sdom,literal)
  }

  describe("prefixed namespace") {
    val sdom =
      ElementSpec(ElementName("a","NS1"),prefix = Some("ns1"),
                  namespaces = Iterable(NamespaceSpec("ns1","NS1"),
                                        NamespaceSpec("ns2","NS2")),
                  children = Iterable(
        ElementSpec(ElementName("b","NS2"),prefix = Some("ns2"))
      ))

    val literal = <ns1:a xmlns:ns1="NS1" xmlns:ns2="NS2"><ns2:b/></ns1:a>

    bothWays(sdom,literal)
  }

  describe("text") {
    val sdom =
      ElementSpec("a",
                  children = Iterable(TextSpec("meh")))

    val literal = <a>meh</a>

    bothWays(sdom,literal)
  }

  describe("convert CDATA from literals as text") {
    val sdom =
      ElementSpec("a",
                  children = Iterable(TextSpec("meh")))

    val literal = <a><![CDATA[meh]]></a>

    bothWays(sdom,literal)
  }

  describe("convert Comments from literals") {
    val sdom =
      ElementSpec("a",
                  children = Iterable(CommentSpec("meh")))

    val literal = <a><!--meh--></a>

    bothWays(sdom,literal)
  }

  describe("convert ProcessingInstruction from literals") {
    val sdom =
      ElementSpec("a",
                  children = Iterable(ProcessingInstructionSpec("meh","blah")))
    
    val literal = <a><?meh blah?></a>

    bothWays(sdom,literal)
  }

  describe("conversion strategy - Flexible") {
    val sdom =
      ElementSpec(ElementName("a","NS1"),children = Iterable(
        ElementSpec(ElementName("b","NS2"))
      ))

    val literal = <ns1:a xmlns:ns1="NS1" xmlns:ns2="NS2"><ns2:b/></ns1:a>

    import Strategies.Flexible
    (literal:ElementSpec) shouldBe sdom
  }

  describe("conversion strategy - KeepPrefixes") {
    val sdom =
      ElementSpec(ElementName("a","NS1"),prefix = Some("ns1"),children = Iterable(
        ElementSpec(ElementName("b","NS2"),prefix = Some("ns2"))
      ))

    val literal = <ns1:a xmlns:ns1="NS1" xmlns:ns2="NS2"><ns2:b/></ns1:a>

    import Strategies.KeepPrefixes
    (literal:ElementSpec) shouldBe sdom
  }

  describe("conversion strategy - KeepNamespaces") {
    val sdom =
      ElementSpec(ElementName("a","NS1"),
                  namespaces = Iterable(NamespaceSpec("ns1","NS1"),
                                        NamespaceSpec("ns2","NS2")),
                  children = Iterable(
        ElementSpec(ElementName("b","NS2"))
      ))

    val literal = <ns1:a xmlns:ns1="NS1" xmlns:ns2="NS2"><ns2:b/></ns1:a>

    import Strategies.KeepNamespaces
    (literal:ElementSpec) shouldBe sdom
  }

  describe("conversion strategy - KeepPrefixesAndNamespaces") {
    val sdom =
      ElementSpec(ElementName("a","NS1"),prefix = Some("ns1"),
                  namespaces = Iterable(NamespaceSpec("ns1","NS1"),
                                        NamespaceSpec("ns2","NS2")),
                  children = Iterable(
        ElementSpec(ElementName("b","NS2"),prefix = Some("ns2"))
      ))

    val literal = <ns1:a xmlns:ns1="NS1" xmlns:ns2="NS2"><ns2:b/></ns1:a>

    import Strategies.KeepPrefixesAndNamespaces
    (literal:ElementSpec) shouldBe sdom
  }

  private[this] def bothWays(sdom:ElementSpec,literal:Elem) {
    it("convert from literal") {
      import Strategies.KeepPrefixesAndNamespaces
      val x:ElementSpec = literal
      x shouldBe sdom
    }

    it("convert to literal") {
      val x:Elem = Element(sdom,None).toElem
      x shouldBe literal
    }
  }
}

/* sdom -- Copyright 2014 Justin Patterson -- All Rights Reserved */
