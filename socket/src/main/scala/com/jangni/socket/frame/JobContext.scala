package com.jangni.socket.scala

import scala.collection.mutable

class JobContext {

  private val startMillions: Long = System.currentTimeMillis()

  def curCountMillions: Long = System.currentTimeMillis() - startMillions

  val contextObjects = mutable.Map.empty[String, Object]

  val contextValues = mutable.Map.empty[String, String]

}
