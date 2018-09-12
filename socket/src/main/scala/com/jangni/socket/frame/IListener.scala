package com.jangni.socket.frame

import io.netty.channel.ChannelHandlerContext

import scala.concurrent.Future

trait IListener {

  def proc(req:String):Future[String]
}
