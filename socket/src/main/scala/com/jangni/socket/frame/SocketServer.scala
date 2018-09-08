package com.jangni.socket.scala


import java.nio.ByteOrder

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.bytes.{ByteArrayDecoder, ByteArrayEncoder}
import io.netty.handler.codec.{LengthFieldBasedFrameDecoder, LengthFieldPrepender}
import io.netty.handler.logging.{LogLevel, LoggingHandler}
import io.netty.handler.timeout.ReadTimeoutHandler
import javax.annotation.{PostConstruct, PreDestroy}
import org.slf4j.{Logger, LoggerFactory}

class SocketServer(iListener: IListener) {
  protected val logger: Logger = LoggerFactory.getLogger("server")
  private val port: Int = 8989
  private val timeoutSeconds: Int = 60
  private val listenGroup = new NioEventLoopGroup(1)
  private val ioGroup = new NioEventLoopGroup()

  /**
    * 启动服务
    */
  @PostConstruct
  def start(): Unit = {
    val bootstrap = new ServerBootstrap()
    bootstrap.group(listenGroup, ioGroup)
      .channel(classOf[NioServerSocketChannel])
      .childHandler(new ChannelInitializer[SocketChannel] {
        override def initChannel(ch: SocketChannel): Unit = {
          ch.pipeline()
            .addLast(new LoggingHandler("server", LogLevel.DEBUG))
            .addLast(new LengthFieldBasedFrameDecoder(ByteOrder.BIG_ENDIAN, 10240, 0, 2, 0, 2, true))
            .addLast(new LengthFieldPrepender(ByteOrder.BIG_ENDIAN, 2, 0, false))
            .addLast(new ByteArrayDecoder)
            .addLast(new ByteArrayEncoder)
            .addLast(new ReadTimeoutHandler(timeoutSeconds))
            .addLast(new SocketServerHandler(timeoutSeconds, iListener))
        }
      })

    //绑定端口 调用sync方法等待绑定操作完成
    bootstrap.bind(port).sync()
    logger.info(s"服务监听断口[${port}]成功！")
  }

  /**
    * 停止服务
    */
  @PreDestroy
  def stop(): Unit = {
    listenGroup.shutdownGracefully()
    ioGroup.shutdownGracefully()
    logger.info("NETTY服务已停止完成")
  }

}
