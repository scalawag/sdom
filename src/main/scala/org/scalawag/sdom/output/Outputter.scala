package org.scalawag.sdom.output

import org.scalawag.sdom.{Named, Node}
import java.io.{StringWriter, Writer}

trait Outputter {
  def output(node:Node,writer:Writer)

  def output(node:Node):String = {
    val out = new StringWriter
    output(node,out)
    out.toString
  }

  protected[this] def qname(n:Named) =
    if ( ! n.prefix.isEmpty )
      s"${n.prefix}:${n.name.localName}"
    else
      n.name.localName
}

/* sdom -- Copyright 2014 Justin Patterson -- All Rights Reserved */
