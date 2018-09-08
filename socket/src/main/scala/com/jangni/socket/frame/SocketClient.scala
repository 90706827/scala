package com.jangni.socket.scala


import java.net.{ConnectException, InetSocketAddress}
import java.nio.ByteOrder
import java.util.concurrent.TimeUnit

import akka.actor.Status.Failure
import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.jangni.socket.scala.BizExecutionContext.global
import io.netty.bootstrap.Bootstrap
import io.netty.channel._
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.bytes.{ByteArrayDecoder, ByteArrayEncoder}
import io.netty.handler.codec.{LengthFieldBasedFrameDecoder, LengthFieldPrepender}
import io.netty.handler.logging.{LogLevel, LoggingHandler}
import io.netty.handler.timeout.{ReadTimeoutException, ReadTimeoutHandler}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Try
import scala.xml.XML

class SocketClient(host: String, port: Int, timeout: Int) extends IContext {

  protected val logger: Logger = LoggerFactory.getLogger("client")

  private val matchActor = ActorSystemUtil.actorSystem.actorOf(Props(new MatchActor))
  private var channel: Option[Channel] = None
  private val workerGroup = new NioEventLoopGroup()
  private val connectTimeout: Int = 5000

  def post(jobContext: JobContext): Future[Unit] = {
    val reqMsg = jobContext.contextValues
      .filter {
        case (_, value) => value != null && value.nonEmpty
      }
      .map {
        case (key, value) => s"<$key>$value</$key>"
      }
      .mkString("<context>", "", "</context>")
      .replaceAll(">[\\s]+<", "><")
    val result = (matchActor ? SendAndReq(getkey(jobContext), reqMsg)) (Timeout(timeout, MILLISECONDS)).asInstanceOf[Future[JobContext]]
    result.map {
      resp =>
        resp.contextValues.foreach(jobContext.contextValues += _)
    }
  }

  /**
    * 创建连接
    *
    */
  def connect(): Unit = Try {
    closeChannel()
    val bootstarp = new Bootstrap()
      .group(workerGroup)
      .channel(classOf[NioSocketChannel])
      .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Int.box(connectTimeout))
      .option(ChannelOption.SO_KEEPALIVE, Boolean.box(true))
      .handler(new ChannelInitializer[NioSocketChannel] {
        override def initChannel(ch: NioSocketChannel): Unit = {
          ch.pipeline()
            .addLast(new LoggingHandler("client", LogLevel.DEBUG))
            .addLast(new LengthFieldBasedFrameDecoder(ByteOrder.BIG_ENDIAN, 102400, 0, 2, 0, 2, true))
            .addLast(new LengthFieldPrepender(ByteOrder.BIG_ENDIAN, 2, 0, false))
            .addLast(new ByteArrayDecoder)
            .addLast(new ByteArrayEncoder)
            .addLast(new ReadTimeoutHandler(timeout, TimeUnit.MILLISECONDS))
            .addLast(new SimpleChannelInboundHandler[Array[Byte]] {
              override def channelRead0(ctx: ChannelHandlerContext, msg: Array[Byte]): Unit = {
                val jobContext = new JobContext()
                val xml = XML.loadString(new String(msg, "utf-8"))
                xml.child.foreach {
                  node => jobContext.toValues(node.label, node.text)
                }
                matchActor ! ReceiveAndResp(jobContext)
              }

              override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
                cause match {
                  case _: ReadTimeoutException =>
                    logger.error(s"请求[$host:$port]在[$timeout 毫秒]内没有收到响应，关闭连接！")
                  case _ =>
                    logger.error(s"连接[$host:$port]发生异常，关闭连接！", cause)
                }
                ctx.close()
              }
            })
        }
      })
    channel = Some(bootstarp.connect(new InetSocketAddress(host, port)).sync().channel())
  }.failed.foreach {
    t =>
      logger.error("s\"连接[$host:$port]发生异常", t)
      throw new ConnectException(s"连接[$host:$port]发生异常")
  }

  private def write(reqMsg: Array[Byte]): Unit = {
    if (!isConnect) {
      connect()
    }
    channel.foreach {
      c =>
        c.writeAndFlush(reqMsg)
    }
  }

  /**
    * 连接是否存在
    *
    * @return true连接可用 false连接不可用
    */
  private def isConnect: Boolean = {
    channel.fold(false) {
      c =>
        //channel是活动的，被连接的并且被注册到EventLoopGroup
        c.isActive && c.isRegistered && c.isOpen
    }
  }

  /**
    * 关闭所有的Channel
    */
  private def closeChannel(): Unit = {
    channel.foreach {
      ch =>
        logger.warn("在重连之前关闭[$host:$port]连接！")
        ch.close().sync()
    }
  }

  /**
    * 获取唯一交易key
    *
    * @param jobContext 上下文
    * @return
    */
  private def getkey(jobContext: JobContext): String = {
    val sb = new mutable.StringBuilder("pay")
    sb.append(jobContext.thridLsId).toString()
  }

  class MatchActor extends Actor {
    private val logger = LoggerFactory.getLogger("MatchActor")
    private val map = mutable.Map.empty[String, ActorRef]

    override def receive: Receive = {
      case send: SendAndReq =>
        if (map.contains(send.key)) {
          sender() ! Failure(new RepeatTranException(s"Repeat Tran, TranNo:[${send.key}]"))
        } else {
          map += send.key -> sender()
          //发送报文
          Try(write(send.reqMsg.getBytes)).failed.foreach {
            t => sender() ! t
          }
        }
      case ReceiveAndResp(jobContext) =>
        val key = getkey(jobContext)
        map.get(key).fold {
          logger.error(s"Map 不存在key:[$key]")
        } {
          ref =>
            self ! key
            ref ! jobContext
        }
      case key: String =>
        map.remove(key)
    }
  }

}
