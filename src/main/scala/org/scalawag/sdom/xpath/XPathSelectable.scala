package org.scalawag.sdom.xpath

import org.scalawag.sdom._

trait XPathSelectable {
  def %<(xpathExpression:String)(implicit namespaces:NamespacesLike = Namespaces.Empty):Selection[Element] =
    %<(XPath.elements(xpathExpression))

  def %<(expr:XPath[Element]):Selection[Element] =
    %(expr).map(_.asInstanceOf[Element])

  def %@(xpathExpression:String)(implicit namespaces:NamespacesLike = Namespaces.Empty):Selection[Attribute] =
    %@(XPath.attributes(xpathExpression))

  def %@(expr:XPath[Attribute]):Selection[Attribute] =
    %(expr).map(_.asInstanceOf[Attribute])

  def %%(xpathExpression:String)(implicit namespaces:NamespacesLike = Namespaces.Empty):Selection[Node] =
    %%(XPath.nodes(xpathExpression))

  def %%(expr:XPath[Node]):Selection[Node] =
    %(expr).map(_.asInstanceOf[Node])

  def %(xpathExpression:String)(implicit namespaces:NamespacesLike = Namespaces.Empty):Selection[Any] =
    %(XPath(xpathExpression))

  def %[T](expr:XPath[T]):Selection[T]
}

/* sdom -- Copyright 2014 Justin Patterson -- All Rights Reserved */
