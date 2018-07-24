package com.jangni.netty.longconnect

import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.TooLongFrameException
import io.netty.handler.timeout.ReadTimeoutException
import org.slf4j.{Logger, LoggerFactory}

/**
  * @program: scala
  * @description: ${description}
  * @author: Mr.Jangni
  * @create: 2018-07-21 00:38
  **/
class ClientHandler extends SimpleChannelInboundHandler[Array[Byte]] {
  private val logger: Logger = LoggerFactory.getLogger("ClientHandler")

  override def channelRead0(ctx: ChannelHandlerContext, msg: Array[Byte]): Unit = {
    logger.warn("接收返回报文："+ new String(msg,"utf-8"))
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    cause match {
      case _: TooLongFrameException =>
        ctx.close()
        logger.warn("接收报文超过长度", cause)
      case _: ReadTimeoutException =>
        ctx.close()
        logger.warn("接收报文超时", cause)
      case _ =>
        ctx.close()
        logger.warn("接收报文超时长度", cause)
    }
  }
}
