package org.scalawag.sdom.xpath

import scala.language.implicitConversions

import org.scalawag.sdom._

trait XPathImplicits {

  implicit class NodeXPathSelector(node:Node)(implicit namespaces:NamespacesLike = Namespaces.Empty) extends XPathSelectable {
    override def %[T](expr:XPath[T]):Iterable[T] =
      expr.evaluate(node)
  }

  implicit class NodesXPathSelector(nodes:Iterable[Node])(implicit namespaces:NamespacesLike = Namespaces.Empty) extends XPathSelectable {
    override def %[T](expr:XPath[T]):Iterable[T] =
      nodes.flatMap( _ % expr )
  }

}

/* sdom -- Copyright 2014 Justin Patterson -- All Rights Reserved */
