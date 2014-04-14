package org.scalawag.sdom

import org.scalatest.FunSuite
import org.scalatest.Matchers

class AttributeNameTest extends FunSuite with Matchers {

  test("illegal local name") {
    intercept[IllegalArgumentException] {
      AttributeName(":","")
    }
  }

  test("illegal namespace name") {
    intercept[IllegalArgumentException] {
      AttributeName("a","-badnamespace")
    }
  }

  test("missing namespace name") {
    intercept[IllegalArgumentException] {
      AttributeName("","")
    }
  }

  test("parse with no namespace") {
    AttributeName("a") should be (AttributeName("a"))
  }

  test("parse name with prefix") {
    implicit val namespaces = Namespaces("b" -> "B")
    AttributeName("b:a") should be (AttributeName("a","B"))
  }

  test("parse name with invalid prefix") {
    intercept[IllegalArgumentException] {
      AttributeName("-:a")
    }
  }

  test("parse name with no prefix when other prefixes are specified ") {
    implicit val namespaces = Namespaces("b" -> "B")
    AttributeName("a") should be (AttributeName("a"))
  }

  test("parse name with no prefix") {
    implicit val namespaces = Namespaces("" -> "B")
    AttributeName("a") should be (AttributeName("a",""))
  }

  test("parse name with unknown prefix") {
    implicit val namespaces = Namespaces("" -> "B")
    intercept[NoSuchElementException] {
      AttributeName("b:a")
    }
  }

  test("parse name with illegal zero-length prefix") {
    implicit val namespaces = Namespaces("" -> "B")
    intercept[IllegalArgumentException] {
      AttributeName(":a")
    }
  }

  test("parse name with namespace embedded") {
    implicit val namespaces = Namespaces("" -> "B","a" -> "A")
    AttributeName("{C}a") should be (AttributeName("a","C"))
  }

  test("parse name with empty namespace embedded") {
    implicit val namespaces = Namespaces("" -> "B")
    AttributeName("{}a") should be (AttributeName("a",""))
  }

  test("parse name with illegal namespace embedded") {
    implicit val namespaces = Namespaces("" -> "B")
    intercept[IllegalArgumentException] {
      AttributeName("{-bad}a")
    }
  }
}

/* sdom -- Copyright 2014 Justin Patterson -- All Rights Reserved */
