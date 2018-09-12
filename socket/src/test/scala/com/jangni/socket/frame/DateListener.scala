package com.jangni.socket.scala

import com.jangni.socket.scala.BizExecutionContext.global

import scala.concurrent.Future
import scala.xml.XML

class DateListener extends IListener with IContext {
  override def proc(req: String): Future[String] = Future {
    val jobContext = new JobContext()
    val xml = XML.loadString(req)
    xml.child.foreach {
      node =>
        jobContext.toValues(node.label, node.text)
    }

    jobContext.respCode = "00"
    jobContext.respDesc = "交易成功"

    val respMsg = jobContext.contextValues
      .filter {
        case (_, value) => value != null && value.nonEmpty
      }
      .map {
        case (key, value) => s"<$key>$value</$key>"
      }
      .mkString("<body>", "", "</body>")
      .replaceAll(">[\\s]+<", "><")
    respMsg
  }
}
