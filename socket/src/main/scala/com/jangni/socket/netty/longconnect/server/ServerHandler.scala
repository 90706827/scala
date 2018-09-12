package com.jangni.socket.netty.longconnect.server

import akka.actor.ActorRef
import io.netty.channel.{ChannelHandlerContext, ChannelInboundHandlerAdapter}
import io.netty.handler.codec.TooLongFrameException
import io.netty.handler.timeout.ReadTimeoutException
import org.slf4j.{Logger, LoggerFactory}

/**
  * Author ZhangGuoQiang
  * Date: 2018/7/20/020
  * Time: 18:49
  * Description:
  */
class ServerHandler(tpsActor: ActorRef) extends ChannelInboundHandlerAdapter {
  private val logger: Logger = LoggerFactory.getLogger("ServerHandler")

  private var count: Int = 0

  override def handlerAdded(ctx: ChannelHandlerContext): Unit = {
    val incoming = ctx.channel
    logger.warn("[SERVER] - " + incoming.remoteAddress + " ->" + incoming.id() + "进来了")
  }

  override def handlerRemoved(ctx: ChannelHandlerContext): Unit = {
    val incoming = ctx.channel
    logger.warn(("[SERVER] - " + incoming.remoteAddress + " ->" + incoming.id() + "出去了"))
  }

  override def channelRead(ctx: ChannelHandlerContext, msg: scala.Any): Unit = {
    tpsActor ! "count"
    val reqMsg = msg.asInstanceOf[Array[Byte]]
    logger.info("[SERVER] - " + ctx.channel().remoteAddress() + "->" + new String(reqMsg, "utf-8"))
    val respMsg = "<hhap>ok</hhap>".getBytes
    logger.warn("写入返回报文：" + new String(respMsg, "utf-8"))
    ctx.write(respMsg)
  }

  override def channelInactive(ctx: ChannelHandlerContext): Unit = {
    val incoming = ctx.channel
    logger.warn("[SERVER] - " + incoming.remoteAddress + "远程主机关闭了连接")
  }

  override def channelReadComplete(ctx: ChannelHandlerContext): Unit = {
    logger.warn("[SERVER] - " + ctx.channel().remoteAddress + "发送返回报文...")
    ctx.flush()
    ctx.close()
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    cause match {
      case _: TooLongFrameException =>
        ctx.close()
        logger.warn("[SERVER] - " + ctx.channel().remoteAddress + "接收报文超过长度：", cause)
      case _: ReadTimeoutException =>
        ctx.close()
        logger.warn("[SERVER] - " + ctx.channel().remoteAddress + "接收报文超时长度：", cause)
      case _ =>
        ctx.close()
        logger.warn("[SERVER] - " + ctx.channel().remoteAddress + "其他错误：", cause)
    }
  }
}
