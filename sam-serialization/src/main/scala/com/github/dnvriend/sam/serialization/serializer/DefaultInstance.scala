package com.github.dnvriend.sam.serialization
package serializer

import scalaz._
import scalaz.Scalaz._

import com.typesafe.scalalogging.LazyLogging

object DefaultInstance extends LazyLogging {
  def apply[A](implicit t: reflect.ClassTag[A]): DTry[A] = {
    logger.info("Creating a default instance for class: '{}'", t.runtimeClass.getCanonicalName)

    val defaultInstance: DTry[A] = {
      import reflect.runtime.{ universe => ru, currentMirror => cm }

      val clazz = cm.classSymbol(t.runtimeClass)
      val mod = clazz.companion.asModule
      val im = cm.reflect(cm.reflectModule(mod).instance)
      val ts = im.symbol.typeSignature
      val mApply = ts.member(ru.TermName("apply")).asMethod
      val syms = mApply.paramLists.flatten
      val args = syms.zipWithIndex.map {
        case (p, i) =>
          val mDef = ts.member(ru.TermName(s"apply$$default$$${i + 1}")).asMethod
          im.reflectMethod(mDef)()
      }
      im.reflectMethod(mApply)(args: _*).asInstanceOf[A]
    }.safe

    if (defaultInstance.isLeft) {
      logger.error(
        "error while instantiating class: '{}', error: '{}'",
        t.runtimeClass.getCanonicalName,
        defaultInstance.swap.foldMap(_.getMessage)
      )
    }

    defaultInstance
  }
}