package org.scalawag.sdom

import scala.language.implicitConversions

abstract class ExpandedName[SELF <: ExpandedName[_]](val localName:String,val uri:String) extends Ordered[SELF] {
  Option(Verifier.checkElementName(localName)) map { reason =>
    throw new IllegalArgumentException(s"invalid element local name '$localName': $reason")
  }
  Option(Verifier.checkNamespaceURI(uri)) map { reason =>
    throw new IllegalArgumentException(s"invalid element namespace URI '$uri': $reason")
  }

  def compare(that:SELF):Int = {
    Iterable[() => Int](
      () => this.uri.compare(that.uri),
      () => this.localName.compare(that.localName)
    ).map(_.apply).find(_ != 0).getOrElse(0)
  }
}

private[sdom] object ExpandedName {
  val FQNameRegExp = """\{([^}]*)\}(.*)""".r
  val QNameRegExp = """([^:]+):([^:]+)""".r
}

// This name is different from an AttributeName only in that there's a different implicit conversion to it
// from a String.  This is because of the difference in the way that no prefix is handled (default namespace
// for elements v. empty namespace for attributes).

case class ElementName(override val localName:String,override val uri:String)
  extends ExpandedName[ElementName](localName,uri)

object ElementName {
  import ExpandedName._

  implicit def apply(s:String)(implicit namespaces:NamespacesLike = Namespaces.Empty):ElementName = s match {
    case FQNameRegExp(namespace,lname) =>
      ElementName(lname,namespace)
    case QNameRegExp(prefix,lname) =>
      Option(Verifier.checkNamespacePrefix(prefix)) map { reason =>
        throw new IllegalArgumentException(s"invalid element namespace prefix '$prefix': $reason")
      }
      ElementName(lname,namespaces.prefixToUri(prefix))
    case s =>
      ElementName(s,namespaces.prefixToUri(""))
  }
}

case class AttributeName(override val localName:String,override val uri:String)
  extends ExpandedName[AttributeName](localName,uri)

object AttributeName {
  import ExpandedName._

  implicit def apply(s:String)(implicit namespaces:NamespacesLike = Namespaces.Empty):AttributeName = s match {
    case FQNameRegExp(namespace,lname) =>
      AttributeName(lname,namespace)
    case QNameRegExp(prefix,lname) =>
      Option(Verifier.checkNamespacePrefix(prefix)) map { reason =>
        throw new IllegalArgumentException(s"invalid element namespace prefix '$prefix': $reason")
      }
      AttributeName(lname,namespaces.prefixToUri(prefix))
    case s =>
      AttributeName(s,"")
  }
}

/* sdom -- Copyright 2014 Justin Patterson -- All Rights Reserved */
