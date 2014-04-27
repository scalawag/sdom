package org.scalawag.sdom

import org.scalatest.{FunSuite, Matchers}

class SelectionTest extends FunSuite with Matchers {
  test("don't mix nodes from different documents") {
    val x1 = Document(<a><b/></a>)
    val x2 = Document(<a><c/></a>)
    val s1 = x1 \ * \ *
    val s2 = x2 \ * \ *
    intercept[IllegalArgumentException] {
      ( s1 ++ s2 )
    }
  }

  test("mixing outside of a Selection is fine") {
    val x1 = Document(<a><b/></a>)
    val x2 = Document(<a><c/></a>)
    val s1 = x1 \ * \ *
    val s2 = x2 \ * \ *
    ( s1.seq ++ s2 ).size shouldBe 2
  }
}
