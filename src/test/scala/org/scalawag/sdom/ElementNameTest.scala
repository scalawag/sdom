package org.scalawag.sdom

import org.scalatest.FunSuite
import org.scalatest.Matchers

class ElementNameTest extends FunSuite with Matchers {

  test("illegal local name") {
    intercept[IllegalArgumentException] {
      ElementName(":","")
    }
  }

  test("illegal namespace name") {
    intercept[IllegalArgumentException] {
      ElementName("a","-badnamespace")
    }
  }

  test("missing namespace name") {
    intercept[IllegalArgumentException] {
      ElementName("","")
    }
  }

  test("parse with no namespace") {
    ElementName("a") should be (ElementName("a"))
  }

  test("parse name with prefix") {
    implicit val namespaces = Namespaces("b" -> "B")
    ElementName("b:a") should be (ElementName("a","B"))
  }

  test("parse name with invalid prefix") {
    intercept[IllegalArgumentException] {
      ElementName("-:a")
    }
  }

  test("parse name with no prefix when other prefixes are specified ") {
    implicit val namespaces = Namespaces("b" -> "B")
    ElementName("a") should be (ElementName("a"))
  }

  test("parse name with default namespace") {
    implicit val namespaces = Namespaces("" -> "B")
    ElementName("a") should be (ElementName("a","B"))
  }

  test("parse name with unknown prefix") {
    implicit val namespaces = Namespaces("" -> "B")
    intercept[NoSuchElementException] {
      ElementName("b:a")
    }
  }

  test("parse name with illegal zero-length prefix") {
    implicit val namespaces = Namespaces("" -> "B")
    intercept[IllegalArgumentException] {
      ElementName(":a")
    }
  }

  test("parse name with namespace embedded") {
    implicit val namespaces = Namespaces("" -> "B","a" -> "A")
    ElementName("{C}a") should be (ElementName("a","C"))
  }

  test("parse name with empty namespace embedded") {
    implicit val namespaces = Namespaces("" -> "B")
    ElementName("{}a") should be (ElementName("a",""))
  }

  test("parse name with illegal namespace embedded") {
    implicit val namespaces = Namespaces("" -> "B")
    intercept[IllegalArgumentException] {
      ElementName("{-bad}a")
    }
  }
}

/* sdom -- Copyright 2014 Justin Patterson -- All Rights Reserved */
