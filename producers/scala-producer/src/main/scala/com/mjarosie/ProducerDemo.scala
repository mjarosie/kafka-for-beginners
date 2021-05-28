package com.mjarosie

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer

import java.util.Properties

object ProducerDemo extends App {
  val props = new Properties()
  props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092")
  props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
  props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
  val producer = new KafkaProducer[String, String](props)
  val record = new ProducerRecord[String, String]("first_topic", "Hello world from Scala producer!")
  producer.send(record)
  producer.flush()
  producer.close()
}
