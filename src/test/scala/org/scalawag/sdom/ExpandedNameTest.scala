package org.scalawag.sdom

import org.scalatest.FunSuite
import org.scalatest.Matchers

class ExpandedNameTest extends FunSuite with Matchers {

  // Needed because ExpandedName is abstract.
  private[this] case class FakeName(override val localName:String,override val uri:String)
    extends ExpandedName[FakeName](localName,uri)

  test("illegal local name") {
    intercept[IllegalArgumentException] {
      FakeName(":","")
    }
  }

  test("illegal namespace name") {
    intercept[IllegalArgumentException] {
      FakeName("a","-badnamespace")
    }
  }

  test("missing namespace name") {
    intercept[IllegalArgumentException] {
      FakeName("","")
    }
  }
}

/* sdom -- Copyright 2014 Justin Patterson -- All Rights Reserved */
