package com.jangni.netty.longconnect.client

import io.netty.channel.{ChannelFutureListener, ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.TooLongFrameException
import io.netty.handler.timeout.{IdleState, IdleStateEvent, ReadTimeoutException}
import org.slf4j.{Logger, LoggerFactory}

/**
  * @program: scala
  * @description: ${description}
  * @author: Mr.Jangni
  * @create: 2018-07-21 13:31
  **/
class ClientHandler extends SimpleChannelInboundHandler[Array[Byte]] {

  private val logger: Logger = LoggerFactory.getLogger("ClientHandler")


  override def channelInactive(ctx: ChannelHandlerContext): Unit = {
    logger.warn("[CLIENT] - " + ctx.channel().remoteAddress + "->" + "远程主机关闭了连接")
  }

  override def channelRead0(ctx: ChannelHandlerContext, msg: Array[Byte]): Unit = {
    logger.warn("[CLIENT] - " + ctx.channel().remoteAddress + "->" + "接收返回报文：" + new String(msg, "utf-8"))
  }

  override def channelReadComplete(ctx: ChannelHandlerContext): Unit = {
    logger.warn("[CLIENT] - " + ctx.channel().remoteAddress + "->" + "客户端读取返回报文结束")
    ctx.flush()
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    cause match {
      case _: TooLongFrameException =>
        ctx.close()
        logger.warn("[CLIENT] - " + ctx.channel().remoteAddress + "->" +
          "接收报文超过长度", cause)
      case _: ReadTimeoutException =>
        ctx.close()
        logger.warn("[CLIENT] - " + ctx.channel().remoteAddress + "->" +
          "接收报文超时", cause)
      case _ =>
        ctx.close()
        logger.warn("[CLIENT] - " + ctx.channel().remoteAddress + "->" +
          "接收报文超时长度", cause)
    }
  }

  /**
    * 处理超时事件发送心跳
    * ALL_IDLE : 一段时间内没有数据接收或者发送
    * READER_IDLE ： 一段时间内没有数据接收
    * WRITER_IDLE ： 一段时间内没有数据发送
    */
  override def userEventTriggered(ctx: ChannelHandlerContext, evt: scala.Any): Unit = {

    evt match {
      case event: IdleStateEvent =>
        event.state match {
          case IdleState.READER_IDLE =>
            logger.warn("[CLIENT] - " + ctx.channel.remoteAddress + "读取服务端返回报文超时，后续做读取超时操作")
            ctx.close().addListener(ChannelFutureListener.CLOSE_ON_FAILURE)
          case IdleState.WRITER_IDLE =>
            logger.warn("[CLIENT] - " + ctx.channel.remoteAddress + "发送服务端报文超时，后续做发送超时操作")
            ctx.close().addListener(ChannelFutureListener.CLOSE_ON_FAILURE)
          case IdleState.ALL_IDLE =>
            ctx.writeAndFlush("发送心跳报文".getBytes)
              .addListener(ChannelFutureListener.CLOSE_ON_FAILURE)
            logger.warn("[CLIENT] - " + ctx.channel.remoteAddress + "一段时间内没有数据接收或者发送报文，客户端主动发送心跳报文")
        }
      case _ => super.userEventTriggered(ctx, evt)
    }
  }
}
