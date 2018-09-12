package com.jangni.socket.netty.longconnect

import com.jangni.socket.netty.longconnect.client.SocketClient
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
  * @program: scala
  * @description: ${description}
  * @author: Mr.Jangni
  * @create: 2018-07-21 16:24
  **/
@Component
class TimeHandle(socketClient: SocketClient) {

  @Scheduled(fixedRate = 50000,initialDelay = 1000)
  def sendClientMsg(): Unit ={
    socketClient.connect
    socketClient.send("abcd".getBytes)
  }
}
