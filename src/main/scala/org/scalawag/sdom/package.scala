package org.scalawag

import scala.language.implicitConversions

import org.scalawag.sdom.transform.Transformers
import org.scalawag.sdom.xpath.XPathImplicits
import org.scalawag.sdom.validation.SchemaValidation
import org.scalawag.sdom.canonical.Canonicalization

package object sdom extends Selectors with Transformers with XPathImplicits with ScalaXmlConversions with SchemaValidation with Canonicalization {
  implicit def stringToTextSpec(text:String) = TextSpec(text)

  implicit class Parents(nodes:Selection[Parent]) extends ChildContainer {
    override def \[T <: Child](selector:Selector[T]):Selection[T] =
      nodes.flatMap( _ \ selector )

    override def \\[T <: Child](selector:Selector[T]):Selection[T] =
      nodes.flatMap( _ \\ selector )
  }

  implicit class Elements(nodes:Selection[Element]) extends AttributeContainer {
    def \@(selector:Selector[Attribute]) =
      nodes.flatMap( _ \@ selector )
  }

  // The default parser
  object XML extends Parser
}

/* sdom -- Copyright 2014 Justin Patterson -- All Rights Reserved */
