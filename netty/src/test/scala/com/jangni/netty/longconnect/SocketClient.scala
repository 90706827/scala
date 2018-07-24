package com.jangni.netty.longconnect

import java.net.InetSocketAddress
import java.nio.ByteOrder

import io.netty.bootstrap.Bootstrap
import io.netty.channel.{Channel, ChannelInitializer, ChannelOption}
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.{LengthFieldBasedFrameDecoder, LengthFieldPrepender}
import io.netty.handler.codec.bytes.{ByteArrayDecoder, ByteArrayEncoder}
import io.netty.handler.logging.{LogLevel, LoggingHandler}
import org.slf4j.{Logger, LoggerFactory}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try


/**
  * Author ZhangGuoQiang
  * Date: 2018/7/20/020
  * Time: 19:01
  * Description:
  */
object SocketClient {
  private val logger: Logger = LoggerFactory.getLogger("SocketClient")
  val socketAddress = new InetSocketAddress("127.0.0.1", 37020)
  protected val workerGroup = new NioEventLoopGroup(1)
  var channel: Option[Channel] = None

  def main(args: Array[String]): Unit = {

    connect
    val data = ("123456789abcdef123456789abcdef").getBytes()

    var start = 100000
    while (start <1000001){
      for(i <- 1 to 1){
        start += 1
        val f = Future {
//          connect
          channel.foreach(c => {
            c.writeAndFlush(data)
          })
        }

        f.onComplete(_ => println("send ok"))
      }
      Thread.sleep(1000)
    }

  }

  private def connect: Unit = {
    val result = Try {
      val bootstrap = new Bootstrap
      bootstrap.group(workerGroup)
        .channel(classOf[NioSocketChannel])
        .option(ChannelOption.SO_KEEPALIVE, Boolean.box(true))
        .handler(new ChannelInitializer[SocketChannel] {
          override def initChannel(c: SocketChannel): Unit = {
            val pipeline = c.pipeline()
            pipeline.addLast(new LoggingHandler("client", LogLevel.DEBUG))
              .addLast(new LengthFieldBasedFrameDecoder(ByteOrder.BIG_ENDIAN, 48, 0, 4, 0, 4, true))
              .addLast(new ByteArrayDecoder)
              .addLast(new LengthFieldPrepender(ByteOrder.BIG_ENDIAN, 4, 0, false))
              .addLast(new ByteArrayEncoder)
              .addLast(new ClientHandler)
          }
        })
      bootstrap.connect(socketAddress).sync().channel()
    }
    result.foreach(c => channel = Option(c))
//    result.failed.foreach(t =>logger.error(s"connect to $socketAddress failed...", t))
  }

}
