package org.scalawag.sdom

import org.scalatest.{Matchers,FunSuite}
import scala.concurrent.{Await, ExecutionContext, future, Future}
import scala.concurrent.duration.Duration

class MultithreadTest extends FunSuite with Matchers {
  import ExecutionContext.Implicits.global

  test("1000 parse-and-selects at a time") {
    val ints = Stream.from(0).take(1000)
    val xmls = ints.map( n => s"<a><b><c>$n</c></b></a>").force
    val futures = xmls map { xml =>
      future {
        val doc = XML.parse(xml)
        ( doc \\ "b" \ * ).head.text.toInt
      }
    }
    Await.result(Future.sequence(futures),Duration.Inf) shouldEqual ints
  }

  test("10000 parses at a time") {
    val ints = Stream.from(0).take(10000)
    val xmls = ints.map( n => s"<a><b><c>$n</c></b></a>").force
    val start = System.currentTimeMillis
    val futures = xmls map { xml =>
      future {
        XML.parse(xml)
      }
    }
    Await.result(Future.sequence(futures),Duration.Inf)
    val finish = System.currentTimeMillis
  }
}

/* sdom -- Copyright 2014 Justin Patterson -- All Rights Reserved */
