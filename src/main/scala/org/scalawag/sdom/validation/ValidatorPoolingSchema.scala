package org.scalawag.sdom.validation

import scala.language.implicitConversions

import javax.xml.validation.{ValidatorHandler, Schema}

import org.scalawag.sdom.ObjectPool

object ValidatorPoolingSchema {
  implicit def toSchema(vpschema:ValidatorPoolingSchema):Schema = vpschema.schema
}

class ValidatorPoolingSchema(val schema:Schema,maxSize:Int = 8) {
  private[this] val validators = new ObjectPool[ValidatorHandler](maxSize,schema.newValidatorHandler)
  def withValidatorHandler[A](fn:ValidatorHandler => A):A = validators.use(fn)
}

/* sdom -- Copyright 2014 Justin Patterson -- All Rights Reserved */
