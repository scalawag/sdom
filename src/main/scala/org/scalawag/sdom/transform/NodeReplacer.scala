package org.scalawag.sdom.transform

import org.scalawag.sdom._
import org.scalawag.sdom.ElementSpec
import org.scalawag.sdom.Namespace
import org.scalawag.sdom.Document
import org.scalawag.sdom.AttributeSpec
import org.scalawag.sdom.Attribute
import org.scalawag.sdom.Element
import org.scalawag.sdom.DocumentSpec
import org.scalawag.sdom.NamespaceSpec
import scala.annotation.tailrec

case class Replacements(children:Map[Child,Iterable[ChildSpec]] = Map.empty,
                        attributes:Map[Attribute,Iterable[AttributeSpec]] = Map.empty,
                        namespaces:Map[Namespace,Iterable[NamespaceSpec]] = Map.empty,
                        documents:Map[Document,DocumentSpec] = Map.empty) {
  val isEmpty = children.isEmpty && attributes.isEmpty && /* namespaces.isEmpty && */ documents.isEmpty

  def +(that:Replacements) =
    Replacements(children = this.children ++ that.children,
                 attributes = this.attributes ++ that.attributes,
                 namespaces = this.namespaces ++ that.namespaces,
                 documents = this.documents ++ that.documents)
}

object Replacements {
  val empty = Replacements()
}

private[sdom] object NodeReplacer {
  def rebuildTree(from:Node,replacements:Replacements):Document = {
    def helper(current:Node):Iterable[NodeSpec] = current match {
      case d:Document =>
        Iterable(replacements.documents.get(d).getOrElse {
          // No replacement specified, rebuild.  Documents must rebuild their children.
          val newChildren = d.children.flatMap(helper).toSeq.asInstanceOf[Seq[ChildSpec]]
          d.spec.copy(children = newChildren)
        })

      case e:Element =>
        replacements.children.get(e).getOrElse {
          // No replacement specified, rebuild.  Elements must rebuild their children, attributes and namespaces
          val newChildren = e.children.flatMap(helper).toSeq.asInstanceOf[Seq[ChildSpec]]
          val newAttributes = e.attributes.flatMap(helper).toSeq.asInstanceOf[Seq[AttributeSpec]]
          val newNamespaces = e.namespaces.flatMap(helper).toSeq.asInstanceOf[Seq[NamespaceSpec]]
          Iterable(e.spec.copy(children = newChildren,attributes = newAttributes,namespaces = newNamespaces))
        }

      // If there are no replacements for the following, just return the originals.  They are leaf nodes, so no
      // rebuilding is necessary (they can't contain other Nodes that _are_ slated for replacement).
      case c:Child =>
        replacements.children.getOrElse(c,Iterable(c.spec))

      case a:Attribute =>
        replacements.attributes.getOrElse(a,Iterable(a.spec))

      case n:Namespace =>
        replacements.namespaces.getOrElse(n,Iterable(n.spec))
    }

    @tailrec
    def rootNode(node:Node):Node = node.parent match {
      case Some(p) => rootNode(p)
      case None => node
    }

    val root = rootNode(from).asInstanceOf[Document]

    if ( replacements.isEmpty ) {
      root
    } else {
      // Passing in a Document to the helper function always yields an Iterable with exactly one DocumentSpec in it.
      Document(helper(root).head.asInstanceOf[DocumentSpec])
    }
  }

  // Handles the common behavior of doing nothing when the resulting transformation is either not supported for this
  // instance or returns the same instance (or an Iterable containing only this instance).

  def calculateReplacementsInternal[A <: Node,B <: NodeSpec](node:A,fn:PartialFunction[A,Iterable[B]])(onDifferent:Iterable[B] => Replacements):Replacements = {
    if ( fn.isDefinedAt(node) ) {
      val transformed = fn(node)
      // Don't be fooled by the same spec returned in an Iterable...
      if ( transformed.size != 1 || transformed.head != node.spec ) {
        onDifferent(transformed)
      } else {
        Replacements.empty
      }
    } else {
      Replacements.empty
    }
  }

  private[sdom] def calculateReplacements(document:Document,fn:PartialFunction[Document,DocumentSpec]):Replacements = {
    if ( fn.isDefinedAt(document) ) {
      val transformed = fn(document)
      if ( transformed != document.spec ) {
        Replacements(documents = Map(document -> transformed))
      } else {
        Replacements.empty
      }
    } else {
      Replacements.empty
    }
  }

  private[this] def calculateReplacements(child:Child,fn:PartialFunction[Child,Iterable[ChildSpec]]):Replacements =
    calculateReplacementsInternal(child,fn) { transformed =>
      Replacements(children = Map(child -> transformed))
    }

  private[this] def calculateReplacements(attribute:Attribute,fn:PartialFunction[Attribute,Iterable[AttributeSpec]]):Replacements =
    calculateReplacementsInternal(attribute,fn) { transformed =>
      Replacements(attributes = Map(attribute -> transformed))
    }

  private[this] def calculateReplacements(namespace:Namespace,fn:PartialFunction[Namespace,Iterable[NamespaceSpec]]):Replacements =
    calculateReplacementsInternal(namespace,fn) { transformed =>
      Replacements(namespaces = Map(namespace -> transformed))
    }

  private[this] def calculateReplacements(node:Node,fn:PartialFunction[Node,Iterable[NodeSpec]]):Replacements = {
    def buildReplacement[A <: Node,B <: NodeSpec](targetClass:Class[B]):Iterable[B] = {
      if ( fn.isDefinedAt(node) ) {
        val transformed = fn(node)
        // Don't be fooled by the same spec returned in an Iterable...
        if ( transformed.size != 1 || transformed.head != node.spec ) {
          // Check to make sure the replacements are valid for the type being replaced...
          transformed foreach { r =>
            if ( ! targetClass.isAssignableFrom(r.getClass) )
              throw new IllegalArgumentException(s"Nodes of type ${node.getClass} can only be transformed into specs of type ${targetClass} yet one was transformed into type '${r.getClass}': $node -> $r")
          }
          transformed.asInstanceOf[Iterable[B]]
        } else {
          Iterable.empty
        }
      } else {
        Iterable.empty
      }
    }

    node match {
      case d:Document =>
        val replacements = buildReplacement(classOf[DocumentSpec])
        if ( replacements.size != 1 )
          throw new IllegalArgumentException(s"A Document node must be transformed into exactly one new Document node, yet one was transformed into ${replacements.size} Document nodes.")
        Replacements(documents = Map(d -> replacements.head))
      case e:Child =>
        Replacements(children = Map(e -> buildReplacement(classOf[ChildSpec])))
      case a:Attribute =>
        Replacements(attributes = Map(a -> buildReplacement(classOf[AttributeSpec])))
      case n:Namespace =>
        Replacements(namespaces = Map(n -> buildReplacement(classOf[NamespaceSpec])))
    }
  }

  def transform(document:Document,fn:PartialFunction[Document,DocumentSpec]) = {
    val replacements = calculateReplacements(document,fn)
    NodeReplacer.rebuildTree(document,replacements)
  }

  def transform(child:Child,fn:PartialFunction[Child,Iterable[ChildSpec]]) = {
    val replacements = calculateReplacements(child,fn)
    NodeReplacer.rebuildTree(child,replacements)
  }

  def transform(attribute:Attribute,fn:PartialFunction[Attribute,Iterable[AttributeSpec]]) = {
    val replacements = calculateReplacements(attribute,fn)
    NodeReplacer.rebuildTree(attribute,replacements)
  }

  def transform(namespace:Namespace,fn:PartialFunction[Namespace,Iterable[NamespaceSpec]]) = {
    val replacements = calculateReplacements(namespace,fn)
    NodeReplacer.rebuildTree(namespace,replacements)
  }

  def transform(node:Node,fn:PartialFunction[Node,Iterable[NodeSpec]]) = {
    val replacements = calculateReplacements(node,fn)
    NodeReplacer.rebuildTree(node,replacements)
  }

  def transformChildren(children:Iterable[Child],fn:PartialFunction[Child,Iterable[ChildSpec]]) = {
    val replacements = children.map(calculateReplacements(_,fn)).reduceLeft(_ + _)
    NodeReplacer.rebuildTree(children.head,replacements)
  }

  def transformAttributes(attributes:Iterable[Attribute],fn:PartialFunction[Attribute,Iterable[AttributeSpec]]) = {
    val replacements = attributes.map(calculateReplacements(_,fn)).reduceLeft(_ + _)
    NodeReplacer.rebuildTree(attributes.head,replacements)
  }

  def transformNamespaces(namespaces:Iterable[Namespace],fn:PartialFunction[Namespace,Iterable[NamespaceSpec]]) = {
    val replacements = namespaces.map(calculateReplacements(_,fn)).reduceLeft(_ + _)
    NodeReplacer.rebuildTree(namespaces.head,replacements)
  }

  def transformNodes(nodes:Iterable[Node],fn:PartialFunction[Node,Iterable[NodeSpec]]) = {
    val replacements = nodes.map(calculateReplacements(_,fn)).reduceLeft(_ + _)
    NodeReplacer.rebuildTree(nodes.head,replacements)
  }
}

/* sdom -- Copyright 2014 Justin Patterson -- All Rights Reserved */
