package org.scalawag.sdom.transform

import scala.language.implicitConversions

import org.scalawag.sdom._
import org.scalawag.sdom.Attribute
import org.scalawag.sdom.Namespace
import org.scalawag.sdom.Document
import org.scalawag.sdom.AttributeSpec
import org.scalawag.sdom.NamespaceSpec

trait Transformers { self =>

  trait Transformer[A <: Node,B <: NodeSpec] {
    def transform(fn:PartialFunction[A,Iterable[B]]):Document

    def remove = transform(self.remove)
  }

  implicit class ChildTransformer(child:Child) extends Transformer[Child,ChildSpec] {
    override def transform(fn:PartialFunction[Child,Iterable[ChildSpec]]):Document =
      NodeReplacer.transform(child,fn)
  }

  implicit class AttributeTransformer(attribute:Attribute) extends Transformer[Attribute,AttributeSpec] {
    override def transform(fn:PartialFunction[Attribute,Iterable[AttributeSpec]]):Document =
      NodeReplacer.transform(attribute,fn)
  }

  implicit class NamespaceTransformer(namespace:Namespace) extends Transformer[Namespace,NamespaceSpec] {
    override def transform(fn:PartialFunction[Namespace,Iterable[NamespaceSpec]]):Document =
      NodeReplacer.transform(namespace,fn)
  }

  implicit class ChildrenTransformer(children:Iterable[Child]) extends Transformer[Child,ChildSpec] {
    override def transform(fn:PartialFunction[Child,Iterable[ChildSpec]]):Document =
      NodeReplacer.transformChildren(children,fn)
  }

  implicit class AttributesTransformer(attributes:Iterable[Attribute]) extends Transformer[Attribute,AttributeSpec] {
    override def transform(fn:PartialFunction[Attribute,Iterable[AttributeSpec]]):Document =
      NodeReplacer.transformAttributes(attributes,fn)
  }

  implicit class NamespacesTransformer(namespaces:Iterable[Namespace]) extends Transformer[Namespace,NamespaceSpec] {
    override def transform(fn:PartialFunction[Namespace,Iterable[NamespaceSpec]]):Document =
      NodeReplacer.transformNamespaces(namespaces,fn)
  }

  implicit class NodesTransformer(nodes:Iterable[Node]) extends Transformer[Node,NodeSpec] {
    override def transform(fn:PartialFunction[Node,Iterable[NodeSpec]]):Document =
      NodeReplacer.transformNodes(nodes,fn)
  }

  def remove[T]:PartialFunction[T,Iterable[Nothing]] = { case _ => Iterable.empty }

  def oneToOne[A,B](fn:A => B):PartialFunction[A,Iterable[B]] = { case a => Iterable(fn(a)) }

  def add(a:AttributeSpec):PartialFunction[Element,Iterable[ElementSpec]] =
    oneToOne( e => e.spec.copy(attributes = e.spec.attributes ++ Iterable(a) ))

  def mapChildren(fn:Iterable[Child] => Iterable[ChildSpec]):PartialFunction[Child,Iterable[ChildSpec]] = {
    case p:Element => Iterable(p.spec.copy(children = fn(p.children)))
  }

  def append(c:ChildSpec):PartialFunction[Child,Iterable[ChildSpec]] = mapChildren(_.map(_.spec) ++ Iterable(c))
  def prepend(c:ChildSpec):PartialFunction[Child,Iterable[ChildSpec]] = mapChildren(Iterable(c) ++ _.map(_.spec))
}

/* sdom -- Copyright 2014 Justin Patterson -- All Rights Reserved */
