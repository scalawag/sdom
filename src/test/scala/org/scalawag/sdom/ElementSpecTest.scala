package org.scalawag.sdom

import org.scalatest.{Matchers,FunSuite}

class ElementSpecTest extends FunSuite with Matchers {

  test("build with constructors") {
    val x:ElementSpec = ElementSpec(ElementName("a","A"))
  }
}

/* sdom -- Copyright 2014 Justin Patterson -- All Rights Reserved */
