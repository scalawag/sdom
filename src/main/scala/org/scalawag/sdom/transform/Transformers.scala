package org.scalawag.sdom.transform

import scala.language.implicitConversions
import org.scalawag.sdom._
import org.scalawag.sdom.Attribute
import org.scalawag.sdom.Namespace
import org.scalawag.sdom.Document
import org.scalawag.sdom.AttributeSpec
import org.scalawag.sdom.DocumentSpec
import org.scalawag.sdom.NamespaceSpec

trait Transformers { self =>

  trait Transformer[T] {
    def transform(fn:PartialFunction[T,Iterable[T]]):Document =
      Document(transformNodes(fn).head.asInstanceOf[DocumentSpec])

    def transformNodes(fn:PartialFunction[T,Iterable[T]]):Iterable[NodeSpec]

    def remove = transform(self.remove)
  }

  implicit class ChildTransformer(child:Child) extends Transformer[ChildSpec] {
    def transformNodes(fn:PartialFunction[ChildSpec,Iterable[ChildSpec]]):Iterable[NodeSpec] =
      NodeReplacer.transform(child,fn)
  }

  implicit class AttributeTransformer(attribute:Attribute) extends Transformer[AttributeSpec] {
    def transformNodes(fn:PartialFunction[AttributeSpec,Iterable[AttributeSpec]]):Iterable[NodeSpec] =
      NodeReplacer.transform(attribute,fn)
  }

  implicit class NamespaceTransformer(namespace:Namespace) extends Transformer[NamespaceSpec] {
    def transformNodes(fn:PartialFunction[NamespaceSpec,Iterable[NamespaceSpec]]):Iterable[NodeSpec] =
      NodeReplacer.transform(namespace,fn)
  }

  implicit class ChildrenTransformer(children:Iterable[Child]) extends Transformer[ChildSpec] {
    def transformNodes(fn:PartialFunction[ChildSpec,Iterable[ChildSpec]]):Iterable[NodeSpec] =
      NodeReplacer.transformChildren(children,fn)
  }

  implicit class AttributesTransformer(attributes:Iterable[Attribute]) extends Transformer[AttributeSpec] {
    def transformNodes(fn:PartialFunction[AttributeSpec,Iterable[AttributeSpec]]):Iterable[NodeSpec] =
      NodeReplacer.transformAttributes(attributes,fn)
  }

  implicit class NamespacesTransformer(namespaces:Iterable[Namespace]) extends Transformer[NamespaceSpec] {
    def transformNodes(fn:PartialFunction[NamespaceSpec,Iterable[NamespaceSpec]]):Iterable[NodeSpec] =
      NodeReplacer.transformNamespaces(namespaces,fn)
  }

  implicit class NodesTransformer(nodes:Iterable[Node]) extends Transformer[NodeSpec] {
    def transformNodes(fn:PartialFunction[NodeSpec,Iterable[NodeSpec]]):Iterable[NodeSpec] =
      NodeReplacer.transformNodes(nodes,fn)
  }

  def remove[T]:PartialFunction[T,Iterable[Nothing]] = { case _ => Iterable.empty }

  def oneToOne[T](fn:T => T):PartialFunction[T,Iterable[T]] = { case t => Iterable(fn(t)) }

  def add(a:AttributeSpec):PartialFunction[ElementSpec,Iterable[ElementSpec]] =
    oneToOne( e => e.copy(attributes = e.attributes ++ Iterable(a) ))

  def mapChildren(fn:Iterable[ChildSpec] => Iterable[ChildSpec]):PartialFunction[ChildSpec,Iterable[ChildSpec]] = {
    case p:ElementSpec => Iterable(p.copy(children = fn(p.children)))
  }

  def append(c:ChildSpec):PartialFunction[ChildSpec,Iterable[ChildSpec]] = mapChildren(_ ++ Iterable(c))
  def prepend(c:ChildSpec):PartialFunction[ChildSpec,Iterable[ChildSpec]] = mapChildren(Iterable(c) ++ _)
}

/* sdom -- Copyright 2014 Justin Patterson -- All Rights Reserved */
