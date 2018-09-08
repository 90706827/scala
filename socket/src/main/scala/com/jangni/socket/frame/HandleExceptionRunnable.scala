package com.jangni.socket.scala

import scala.util.Try

class HandleExceptionRunnable(exceptionBody: => Unit, val exceptionHandle: Throwable => Unit) extends Runnable {
  override def run(): Unit = {
    Try(exceptionBody).failed.foreach(exceptionHandle)
  }
}
