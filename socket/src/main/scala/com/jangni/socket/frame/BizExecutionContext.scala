package com.jangni.socket.scala

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

object BizExecutionContext  {

  private val pool = {
    val pool = new ThreadPoolTaskExecutor()
    pool.setCorePoolSize(4)
    pool.setQueueCapacity(0)
    pool.setAllowCoreThreadTimeOut(true)
    pool.setThreadNamePrefix("Biz")
    pool.afterPropertiesSet()
    pool
  }

  implicit val global:ExecutionContextExecutor = ExecutionContext.fromExecutor(pool)
}
