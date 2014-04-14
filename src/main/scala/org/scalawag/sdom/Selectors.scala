package org.scalawag.sdom

import scala.language.implicitConversions
import scala.reflect.ClassTag

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
  def apply[O:ClassTag](fn:O => Boolean) = new Selector[O](fn)
}

/* sdom -- Copyright 2014 Justin Patterson -- All Rights Reserved */
