package com.jangni.kafka

import org.springframework.boot.{SpringApplication, SpringBootConfiguration}

/**
  * Author ZhangGuoQiang
  * Date: 2018/7/25/025
  * Time: 16:36
  * Description:
  */
@SpringBootConfiguration
class KafkaApplication {

}
object KafkaApplication extends App{
  SpringApplication.run(classOf[KafkaApplication],args:_*)
}
