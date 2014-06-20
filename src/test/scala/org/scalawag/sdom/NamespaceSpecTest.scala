package org.scalawag.sdom

import org.scalatest.FunSuite
import org.scalatest.Matchers

class NamespaceSpecTest extends FunSuite with Matchers {

  test("illegal use of empty URI with prefix") {
    intercept[IllegalArgumentException] {
      NamespaceSpec("pre","")
    }
  }

}

/* sdom -- Copyright 2014 Justin Patterson -- All Rights Reserved */
