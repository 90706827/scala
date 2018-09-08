package com.jangni.kafka

import java.util.Properties

import org.apache.kafka.clients.producer.{Callback, KafkaProducer, ProducerRecord, RecordMetadata}

/**
  * @program: scala
  * @description: ${description}
  * @author: Mr.Jangni
  * @create: 2018-09-08 21:42
  **/
class KafkaClient{
  val topic:String = "topic"
  val props:Properties = new Properties()
  //kafka集群地址
  props.setProperty("bootstrap.server","127.0.0.1:8888,127.0.0.1:9999")
  //缓冲区满、topic源数据无效时send方法和partitionsFor方法阻塞时间
  props.setProperty("max.block.ms","5000")
  //客户端等待一个请求的最大时间，配置的值不能小于broker的
  //replica.lag.time.max.ms值 （默认是10000）
  props.setProperty("request.timeout.ms","15000")
  //value的序列化
  props.setProperty("value.serializer","org.apache.kafka.common.serialization.StringSerializer")
  //key的序列化
  props.setProperty("key.serializer","org.apache.kafka.common.serialization.StringSerializer")
  //当一次发送消息请求被认为完成时的确认值，就是指procuder需要多少个broker返回的确认信号
  //all表示需要等待所有的备份都成功写入日志
  props.setProperty("acks","all")
  //如果将retries的值大于0，客户端将重新发送之前发送失败的数据
  props.setProperty("retries","0")
  //只要有多个记录被发送到同一个分区，生产者就会尝试将记录组合成一个batch发送
  props.setProperty("batch.size","16384")
  //为每次发送增加一些延迟，以此来聚合更多的Message，单位ms
  props.setProperty("linger.ms","5")
  //设置生产者可用于缓冲等待发送到服务器的记录的总字节数
  props.setProperty("buffer.memory","33554432")

  val producer:KafkaProducer[String,String] = new KafkaProducer[String,String](props)

  def send(msg:String):Unit = {
    producer.send(new ProducerRecord[String,String](topic,msg),new Callback {
      override def onCompletion(recordMetadata: RecordMetadata, e: Exception): Unit = {
        Option(e).fold{
          println("send kafka success")
        }{
          e=>
            e.printStackTrace()
            println("send kafka failed")
        }
      }
    })
    producer.flush()
  }
}
