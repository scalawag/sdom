package org.scalawag.sdom

object Namespaces {
  def apply(namespaces:Iterable[NamespaceSpec]):ChildNamespaces =
    new ChildNamespaces(Empty,namespaces)

  def apply(parent:NamespacesLike,namespaces:Iterable[NamespaceSpec]):ChildNamespaces =
    new ChildNamespaces(parent,namespaces)

  def apply(namespaces:(String,String)*):ChildNamespaces =
    new ChildNamespaces(Empty,namespaces.map( p => NamespaceSpec(p._1,p._2) ))

  def apply(parent:NamespacesLike,namespaces:(String,String)*):ChildNamespaces =
    new ChildNamespaces(parent,namespaces.map( p => NamespaceSpec(p._1,p._2) ))

  object Empty extends Namespaces(Iterable(NamespaceSpec("","")))
}

trait NamespacesLike {
  val namespaces:Iterable[NamespaceSpec]
  val prefixToUri:Map[String,String]
  val uriToPrefix:Map[String,String]
}

class Namespaces private[sdom] (override val namespaces:Iterable[NamespaceSpec]) extends NamespacesLike {
  override val prefixToUri = namespaces.map( n => n.prefix -> n.uri ).toMap
  override val uriToPrefix = namespaces.map( n => n.uri -> n.prefix ).toMap

  if ( prefixToUri.size != namespaces.size ) {
    val dup = namespaces.groupBy(_.prefix).find(_._2.size > 1).get
    throw new IllegalArgumentException(s"Namespaces must have unique prefixes but '$dup' is declared twice")
  }

  override val toString = prefixToUri.toString
}

class ChildNamespaces private[sdom] (val parent:NamespacesLike,
                                     localNamespaces:Iterable[NamespaceSpec]) extends NamespacesLike {
  val local:Namespaces = new Namespaces(localNamespaces)
  override val namespaces = localNamespaces ++ parent.namespaces
  override val prefixToUri = parent.prefixToUri ++ local.prefixToUri
  override val uriToPrefix = namespaces.groupBy(_.prefix).map(_._2.head).map( n => n.uri -> n.prefix ).toMap

  override val toString = prefixToUri.toString
}

/* sdom -- Copyright 2014 Justin Patterson -- All Rights Reserved */
