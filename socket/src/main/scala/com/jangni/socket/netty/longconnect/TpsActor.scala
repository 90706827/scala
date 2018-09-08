package com.jangni.netty.longconnect

import akka.actor.Actor
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * @program: scala
  * @description: ${description}
  * @author: Mr.Jangni
  * @create: 2018-07-21 02:13
  **/

class TpsActor extends Actor{

  private val logger:Logger = LoggerFactory.getLogger("TPS")
  private var count =0
  override def receive: Receive = {
    case "count" =>
      count +=1
    case "sum" =>
      logger.error(s"tps：${count/10}")
      count = 0
    case _ =>
  }


  //定时器的上下文
  implicit val ex: ExecutionContextExecutor = context.system.dispatcher
  //定时统计，时间根据参数表的时间为准
  context
    .system
    .scheduler
    .schedule(10 seconds, 10 seconds, self, "sum")
}
