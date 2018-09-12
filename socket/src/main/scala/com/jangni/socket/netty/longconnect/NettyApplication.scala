package com.jangni.socket.netty.longconnect

import akka.actor.{ActorRef, ActorSystem, Props}
import com.jangni.netty.longconnect.client.SocketClient
import com.jangni.netty.longconnect.server.SocketServer
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean

/**
  * Author ZhangGuoQiang
  * Date: 2018/7/24/024
  * Time: 11:06
  * Description:
  */
@SpringBootApplication
class NettyApplication {
  @Bean
  def systemActor:ActorSystem = {
    ActorSystem("netty")
  }

  @Bean
  def tpsActor(systemActor:ActorSystem):ActorRef = {
    systemActor.actorOf(Props(new TpsActor))
  }

  @Bean
  def socketClient: SocketClient = {
    new SocketClient
  }
  @Bean
  def socketServer(tpsActor:ActorRef):SocketServer ={
    new SocketServer(tpsActor)
  }

}
object NettyApplication extends App{

  SpringApplication.run(classOf[NettyApplication],args:_*)
}
