package org.scalawag.sdom.xpath

import scala.reflect.ClassTag
import scala.collection.JavaConversions.collectionAsScalaIterable
import org.scalawag.sdom._
import org.scalawag.sdom.Attribute
import org.scalawag.sdom.Element

// Similar to jaxen's XPath except that this one is more type-safe because it's specifically limited to
// handling contexts that are Nodes.

class XPath[+T](xpath:SdomXPath) {
  def evaluate(context:Node):Iterable[T] = {
    xpath.evaluate(context) match {
      case l:java.util.List[_] =>
        val iterable = collectionAsScalaIterable(l)
        iterable.map(_.asInstanceOf[T])
      case t =>
        Iterable(t.asInstanceOf[T])
    }
  }
}

object XPath {
  def elements(s:String)(implicit namespaces:NamespacesLike = Namespaces.Empty):XPath[Element] =
    new XPath[Element](new SdomXPath(s))

  def attributes(s:String)(implicit namespaces:NamespacesLike = Namespaces.Empty):XPath[Attribute] =
    new XPath[Attribute](new SdomXPath(s))

  def nodes(s:String)(implicit namespaces:NamespacesLike = Namespaces.Empty):XPath[Node] =
    new XPath[Node](new SdomXPath(s))

  def apply[T <: Node](s:String)(implicit namespaces:NamespacesLike = Namespaces.Empty,classTag:ClassTag[T]):XPath[T] =
    apply(s,classTag.runtimeClass.asInstanceOf[Class[T]])(namespaces)

  def apply[T](s:String,runtimeClass:Class[T])(implicit namespaces:NamespacesLike):XPath[T] =
    new XPath[T](new SdomXPath(s))
}

/* sdom -- Copyright 2014 Justin Patterson -- All Rights Reserved */
