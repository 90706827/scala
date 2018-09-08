package com.jangni.socket.scala


import com.jangni.socket.scala.BizExecutionContext.global
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.TooLongFrameException
import io.netty.handler.timeout.ReadTimeoutException
import org.slf4j.{Logger, LoggerFactory}

class SocketServerHandler(timeoutSeconds: Int, ilistener: IListener)
  extends SimpleChannelInboundHandler[Array[Byte]] {

  private val logger: Logger = LoggerFactory.getLogger("Server")

  private val isKeepAlive: Boolean = true

  override def channelRead0(ctx: ChannelHandlerContext, msg: Array[Byte]): Unit = {
    val reqXml = new String(msg, "utf-8")
    logger.info(s"req xml:${reqXml}")
    for {
      resp <- ilistener.proc(reqXml)
    } yield {
      logger.info(s"resp xml:${resp}")
      ctx.writeAndFlush(resp.getBytes)
    }

    if (isKeepAlive) {
      logger.info("keep alive")
    } else {
      ctx.close()
    }
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    cause match {
      case _: ReadTimeoutException =>
        logger.warn(s"客户端连接服务端在${timeoutSeconds}秒内未发送数据，连接将进行关闭！")
      case _: TooLongFrameException =>
        logger.warn(s"客户端发送给服务端的报文超长，请检查报文！错误信息：$cause")
      case _: Throwable =>
        logger.warn(s"客户端请求服务端发送错误，错误信息：$cause")
    }
    ctx.channel().close()
  }
}
