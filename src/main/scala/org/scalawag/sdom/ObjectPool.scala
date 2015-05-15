package org.scalawag.sdom

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.TimeUnit

private class ObjectPool[T](maxSize:Int,createFn: => T) {

  private[this] val size = new AtomicInteger(0)
  private[this] val queue = new ArrayBlockingQueue[T](maxSize,true)

  def use[A](fn:T => A):A = {
    val obj = Option(queue.poll) getOrElse {
      if ( size.getAndIncrement < maxSize ) {
        createFn
      } else {
        size.decrementAndGet
        queue.poll(Int.MaxValue,TimeUnit.DAYS)
      }
    }

    try {
      fn(obj)
    } finally {
      queue.offer(obj)
    }
  }
}

/* sdom -- Copyright 2014 Justin Patterson -- All Rights Reserved */
