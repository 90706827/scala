package com.jangni.socket

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication(scanBasePackages = Array("com.jangni"))
class Application {


}

object Application extends App {

  SpringApplication.run(classOf[Application],args:_*)

}
