package com.mjarosie

import org.apache.kafka.clients.producer._
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.LoggerFactory

import java.util.Properties

object ProducerDemoWithCallback extends App {
  val logger = LoggerFactory.getLogger(this.getClass)

  val props = new Properties()
  props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092")
  props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
  props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
  val producer = new KafkaProducer[String, String](props)

  (1 to 10).map(i => {
    val record = new ProducerRecord[String, String]("first_topic", s"Hello world from Scala producer, message #${i}")
    producer.send(record, new Callback {
      override def onCompletion(metadata: RecordMetadata, exception: Exception): Unit =
        if (exception != null) logger.error("Error while producing", exception)
        else logger.info(
          s"""Received callback after sending a message."
        Topic: ${metadata.topic}
        Partition: ${metadata.partition}
        Offset: ${metadata.offset}
        Timestamp: ${metadata.timestamp}""")
    })
  })

  producer.flush()
  producer.close()
}
