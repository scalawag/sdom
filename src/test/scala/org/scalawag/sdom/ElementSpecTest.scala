package org.scalawag.sdom

import org.scalatest.{Matchers,FunSuite}

class ElementSpecTest extends FunSuite with Matchers {

  test("build with constructors") {
    val x:ElementSpec = ElementSpec(ElementName("a","A"))
  }

  test("prevent conflicting prefix constraints (element/attribute)") {
    intercept[IllegalArgumentException] {
      ElementSpec(ElementName("a","A"),prefix=Some("pre"),attributes=Iterable(
        AttributeSpec(AttributeName("b","B"),"1",Some("pre"))))
    }
  }

  test("prevent conflicting prefix constraints (attribute/attribute)") {
    intercept[IllegalArgumentException] {
      val a1 = AttributeSpec(AttributeName("a","A"),"1",Some("pre"))
      val a2 = AttributeSpec(AttributeName("a","B"),"1",Some("pre"))
      ElementSpec(ElementName("a","A"),attributes=Iterable(a1,a2))
    }
  }
}

/* sdom -- Copyright 2014 Justin Patterson -- All Rights Reserved */
