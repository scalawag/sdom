package org.scalawag.sdom

import scala.language.implicitConversions
import scala.reflect.ClassTag
import scala.collection._
import scala.collection.generic.{GenericCompanion, GenericTraversableTemplate, CanBuildFrom}

trait Selectors {

  val `*` = Selector[Element]( _ => true )

  val text = Selector[TextLike]( _ => true )

  val node = Selector[Child]( _ => true )

}

class Selector[O:ClassTag](fn:O => Boolean) extends Function1[Any,Iterable[O]] {
  override def apply(i:Any):Iterable[O] = i match {
    case o:O if fn(o) =>
      Iterable(i.asInstanceOf[O])
    case _ =>
      Iterable.empty
  }
}

object Selector {
  def apply[T:ClassTag](fn:T => Boolean) = new Selector[T](fn)
}

class Selection[+T](val document:Document,items:Iterable[T])
  extends Iterable[T]
     with IterableLike[T,Selection[T]]
{
  override def iterator:Iterator[T] = items.iterator
  override protected[this] def newBuilder = new Selection.SelectionBuilder(document)
}

object Selection {
  def apply[T](document:Document,nodes:Iterable[T]) = new Selection(document,nodes)

  implicit def canBuildFromSelection[T] = new CanBuildFrom[Selection[_],T,Selection[T]] {
    override def apply() = throw new Exception("can't build a Selection from scratch")
    override def apply(from:Selection[_]) = new SelectionBuilder(from.document)
  }

  class SelectionBuilder[T](document:Document) extends mutable.Builder[T,Selection[T]] {
    private[this] var items:Seq[T] = Seq.empty

    override def result():Selection[T] = Selection(document,items)

    override def clear() {
      items = Seq.empty
    }

    override def +=(elem:T):this.type = {
      if ( elem.isInstanceOf[Node] ) {
        val d1 = document
        val d2 = elem.asInstanceOf[Node].document
        if ( ! ( d1 eq d2 ) )
          throw new IllegalArgumentException("can't mix nodes from two different documents into one Selection")
      }
      items = items :+ elem
      this
    }
  }
}

/* sdom -- Copyright 2014 Justin Patterson -- All Rights Reserved */
