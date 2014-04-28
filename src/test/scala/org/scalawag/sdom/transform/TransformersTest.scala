package org.scalawag.sdom.transform

import org.scalatest.{FunSpec,Matchers}
import org.scalawag.sdom._

class TransformersTest extends FunSpec with Matchers {

  describe("transform") {
    it("should do nothing when the Iterable is empty") {
      val x = Document(<a><b><c/></b><b><d/></b></a>)

      val t = ( x \ "z" ) transform append(<e/>)

      t shouldEqual x
    }

    it("should allow replacing a Child with any other Child") {
      val x = Document(<a><b>1</b><b><!-- blah --></b><b><d/></b><b><?pi details?></b></a>)

      // Replace <b/> elements with their own children
      val t = ( x \\ "b" ) transform { case e:Element => e.spec.children }

      t shouldEqual Document(<a>1<!-- blah --><d/><?pi details?></a>)
    }
  }

  describe("append on Iterable[Element]") {
    it("should append a child to the end of each Elements children") {
      val x = Document(<a><b><c/></b><b><d/></b></a>)

      val t = ( x \\ "b" ) transform append(<e/>)

      t.asString shouldEqual Document(<a><b><c/><e/></b><b><d/><e/></b></a>).asString
    }
  }

  describe("transform XPath results") {
    it("remove elements returned") {
      val x = Document(<a><b id="1"/><b id="2"><c>8</c></b><d><b id="3"/></d></a>)

      val t = ( x %< "//b" ) transform remove

      t.asString shouldEqual Document(<a><d/></a>).asString
    }
  }

}