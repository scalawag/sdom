package org.scalawag.sdom

case class BuilderConfiguration(val discardComments:Boolean = true,
                                val discardWhitespace:Boolean = true,
                                val discardProcessingInstructions:Boolean = true,
                                val trimWhitespace:Boolean = true,
                                val treatCDataAsText:Boolean = true,
                                val collapseAdjacentTextLikes:Boolean = true)

object BuilderConfiguration {
  implicit val Simplest = BuilderConfiguration()
  implicit val Truest = BuilderConfiguration(false,false,false,false,false,false)
}

/* sdom -- Copyright 2014 Justin Patterson -- All Rights Reserved */
