package org.scalawag.sdom.transform

import org.scalatest.{FunSpec,Matchers}
import org.scalawag.sdom._

class TransformersTest extends FunSpec with Matchers {

// replace child with any other child
// transform empty result set
  describe("transform") {
    it("should do nothing when the Iterable is empty") {
      val x = Document(<a><b><c/></b><b><d/></b></a>)

      val t = ( x \ "z" ) transform append(<e/>)

      t shouldEqual x
    }
  }

  describe("append on Iterable[Element]") {
    it("should append a child to the end of each Elements children") {
      val x = Document(<a><b><c/></b><b><d/></b></a>)

      val t = ( x \\ "b" ) transform append(<e/>)

      t.asString shouldEqual Document(<a><b><c/><e/></b><b><d/><e/></b></a>).asString
    }
  }

}