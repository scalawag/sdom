package org.scalawag.sdom.validation

import org.scalatest.{Matchers,FunSuite}
import org.scalawag.sdom.{Document, XML}
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration

class MultithreadTest extends FunSuite with Matchers {
  import ExecutionContext.Implicits.global

  test("10000 validations") {
    val schema = ValidationErrorMessageTest.createSchema("test.xsd")
    val vpschema = new ValidatorPoolingSchema(schema)

    val ints = Stream.from(0).take(10000)
    val xmls = ints.map( n =>
      <root xmlns="urn:org.scalawag.sdom:test">
        <a>{n}</a>
        <b>{n}</b>
        <c>{n}</c>
      </root>
    ).map(Document.apply).force

    val nonPooledTime = {
      val start = System.currentTimeMillis
      val futures = xmls map { xml =>
        Future {
          xml.root.validate(schema)
        }
      }
      Await.result(Future.sequence(futures),Duration.Inf)
      System.currentTimeMillis - start
    }

    val pooledTime = {
      val start = System.currentTimeMillis
      val futures = xmls map { xml =>
        Future {
          xml.root.validate(vpschema)
        }
      }
      Await.result(Future.sequence(futures),Duration.Inf)
      System.currentTimeMillis - start
    }

    pooledTime should be < (nonPooledTime)
  }
}

/* sdom -- Copyright 2014 Justin Patterson -- All Rights Reserved */
