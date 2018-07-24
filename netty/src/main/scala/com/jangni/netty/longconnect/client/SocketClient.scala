package com.jangni.netty.longconnect.client

import java.net.InetSocketAddress
import java.nio.ByteOrder
import java.util.concurrent.TimeUnit

import io.netty.bootstrap.Bootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.channel.{Channel, ChannelInitializer, ChannelOption}
import io.netty.handler.codec.bytes.{ByteArrayDecoder, ByteArrayEncoder}
import io.netty.handler.codec.{LengthFieldBasedFrameDecoder, LengthFieldPrepender}
import io.netty.handler.logging.{LogLevel, LoggingHandler}
import io.netty.handler.timeout.IdleStateHandler
import org.slf4j.{Logger, LoggerFactory}

import scala.util.Try

/**
  * @program: scala
  * @description: ${description}
  * @author: Mr.Jangni
  * @create: 2018-07-21 13:31
  **/
class SocketClient {
  private val logger: Logger = LoggerFactory.getLogger("SocketClient")
  val socketAddress = new InetSocketAddress("127.0.0.1", 37020)
  private val READ_IDEL_TIME_OUT = 3 // 读超时
  private val WRITE_IDEL_TIME_OUT = 3 // 写超时
  private val ALL_IDEL_TIME_OUT = 60 // 所有超时
  protected val workerGroup = new NioEventLoopGroup(1)
  var channel: Option[Channel] = None

  def connect(): Unit = {
    val result = Try {
      val bootstrap = new Bootstrap
      bootstrap.group(workerGroup)
        .channel(classOf[NioSocketChannel])
        .option(ChannelOption.SO_KEEPALIVE, Boolean.box(true))
        .handler(new ChannelInitializer[SocketChannel] {
          override def initChannel(c: SocketChannel): Unit = {
            val pipeline = c.pipeline()
            pipeline.addLast(new LoggingHandler("client", LogLevel.DEBUG))
              .addLast(new IdleStateHandler(READ_IDEL_TIME_OUT, WRITE_IDEL_TIME_OUT, ALL_IDEL_TIME_OUT, TimeUnit.SECONDS))
              .addLast(new LengthFieldBasedFrameDecoder(ByteOrder.BIG_ENDIAN, 48, 0, 4, 0, 4, true))
              .addLast(new ByteArrayDecoder)
              .addLast(new LengthFieldPrepender(ByteOrder.BIG_ENDIAN, 4, 0, false))
              .addLast(new ByteArrayEncoder)
              .addLast(new ClientHandler)
          }
        })
      bootstrap.connect(socketAddress).sync().channel()
    }
    result.failed.foreach(t => logger.error(s"connect to $socketAddress failed...", t))
    result.foreach(c => channel = Option(c))
  }

  def send(data: Array[Byte]): Unit = {
    channel.foreach(c => {
      c.writeAndFlush(data)
    })
  }
}

