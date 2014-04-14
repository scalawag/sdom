package org.scalawag.sdom

import org.scalatest.FunSuite
import org.scalatest.Matchers

class NamespacesTest extends FunSuite with Matchers {

  test("illegal prefix") {
    intercept[IllegalArgumentException] {
      Namespaces(":" -> "blah")
    }
  }

  test("illegal URI") {
    intercept[IllegalArgumentException] {
      Namespaces("b" -> "-bad")
    }
  }

  test("default default namespace") {
    Namespaces("a" -> "A").prefixToUri("") should be ("")
  }

  test("override default namespace") {
    Namespaces("" -> "A").prefixToUri("") should be ("A")
  }

  test("specified namespace") {
    Namespaces("a" -> "A").prefixToUri("a") should be ("A")
  }

}

/* sdom -- Copyright 2014 Justin Patterson -- All Rights Reserved */
