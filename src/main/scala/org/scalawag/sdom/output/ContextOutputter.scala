package org.scalawag.sdom.output

import org.scalawag.sdom._
import org.scalawag.sdom.validation.ValidationContext

import scala.xml.Utility.escape

object ContextOutputter {
  import Outputter._

  def outputValidationContext(node:Node,badNode:ValidationContext) = {
    // These are all the not bad chunks, leading up to the bad one...
    val (preBadChunks,badChunkEtc) = stringify(node).span(_._1 != badNode)

    val preBadString = preBadChunks.map(_._2).mkString

    // Determine where to put the caret by looking for the last newline in the preBadString
    val column = preBadString.length - preBadString.lastIndexOf(endl) - 1

    // Add an ellipsis if there are more nodes...
    val ellipsis = if ( badChunkEtc.tail.isEmpty ) "" else "..."

    preBadString + badChunkEtc.head._2 + ellipsis + "\n" + ( " " * column ) + "^"
  }

  // Flattens the DOM into a sequence of ValidationContexts and the string representation of those contexts.
  private[this] def stringify(root:Node):Iterable[(ValidationContext,String)] = {

    def helper(node:Node):Iterable[(ValidationContext,String)] = node match {
      case d:Document =>
        // TODO: xml version, etc.
        d.children.flatMap(helper)

      case e:Element =>
        val qn = qname(e)

        val attributes = e.attributes map { a =>
          qname(a) + "=\"" + escape(a.value) + '"'
        }

        val namespaces = e.namespaces map { n =>
          "xmlns" + (
            if ( ! n.prefix.isEmpty )
              ":" + n.prefix
            else
              ""
          ) + "=\"" + escape(n.uri) + '"'
        }

        val openTag = ( Seq(qn) ++ attributes ++ namespaces ).mkString("<"," ",if ( e.children.isEmpty ) "/>" else ">")
        val open = Iterable((ValidationContext(e,false),openTag))

        val close =
          if ( e.children.isEmpty ) {
            Iterable.empty
          } else {
            val closeTag = "</" + qn + '>'
            Iterable((ValidationContext(e, true), closeTag))
          }

        open ++ e.children.flatMap(helper) ++ close

      case t:Text =>
        Iterable((ValidationContext(t),escape(t.text)))

      case c:CData =>
        // TODO: escape bad characters?
        Iterable((ValidationContext(c),s"<![CDATA[${c.text}]]>"))

      case c:Comment =>
        // TODO: escape bad characters?
        Iterable((ValidationContext(c),s"<!--${c.text}-->"))

      case p:ProcessingInstruction =>
        // TODO: escape anything here?
        Iterable((ValidationContext(p),s"<?${p.target} ${p.data}?>"))

      // These never get called.  They're handled within the Element case
      case a:Attribute => Iterable.empty
      case n:Namespace => Iterable.empty
    }

    helper(root)
  }

  private[this] val endl = System.getProperty("line.separator")
}

/* sdom -- Copyright 2014 Justin Patterson -- All Rights Reserved */
