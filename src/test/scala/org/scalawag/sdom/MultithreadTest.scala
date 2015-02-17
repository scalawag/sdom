package org.scalawag.sdom

import org.scalatest.{Matchers,FunSuite}
import scala.concurrent.{Await, ExecutionContext, future, Future}
import scala.concurrent.duration.Duration
import org.scalawag.sdom._

import ExecutionContext.Implicits.global

class MultithreadTest extends FunSuite with Matchers {

  test("1000 parses at a time") {
    val ints = Stream.from(0).take(1000)
    val xmls = ints.map( n => s"<a><b><c>$n</c></b></a>")
    val futures = xmls map { xml =>
      Future {
        val doc = XML.parse(xml)
        ( doc \\ "b" \ * ).head.text.toInt
      }
    }
    Await.result(Future.sequence(futures),Duration.Inf) shouldEqual ints
  }
}

/* sdom -- Copyright 2014 Justin Patterson -- All Rights Reserved */
