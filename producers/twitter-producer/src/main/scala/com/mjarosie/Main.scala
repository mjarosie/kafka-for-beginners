package com.mjarosie

import org.apache.kafka.clients.producer.{Callback, KafkaProducer, ProducerConfig, ProducerRecord, RecordMetadata}
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.LoggerFactory
import twitter4j.{FilterQuery, StallWarning, Status, StatusDeletionNotice, StatusListener, TwitterStreamFactory}

import java.util.Properties

object Main extends App {
  val logger = LoggerFactory.getLogger(this.getClass)

  val kafkaProducer = KafkaUtil.initialiseProducer()
  val topicName = "tweets"
  val twitterStream = TwitterUtil.initialiseStream(kafkaProducer, topicName)

  twitterStream.filter(new FilterQuery().track("kafka", "twitter"))
  Thread.sleep(2000)
  twitterStream.cleanUp()
  twitterStream.shutdown()
  kafkaProducer.flush()
  kafkaProducer.close()
}

object KafkaUtil {
  def initialiseProducer() = {
    val props = new Properties()
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092")
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
    new KafkaProducer[String, String](props)
  }
}

object TwitterUtil {
  val logger = LoggerFactory.getLogger(this.getClass)

  def initialiseStream(kafkaProducer: KafkaProducer[String, String], topicName: String) = {
    val twitterStream = new TwitterStreamFactory().getInstance
    twitterStream.addListener(simpleStatusListener(kafkaProducer, topicName))
  }

  def simpleStatusListener(kafkaProducer: KafkaProducer[String, String], topicName: String) = new StatusListener() {
    def onStatus(status: Status) {
      println(s"got tweet ID: ${status.getId} (is possibly sensitive: ${status.isPossiblySensitive})")
      val recordKey = status.getId
      val recordValue = status.getText
      val record = new ProducerRecord[String, String](topicName, recordKey.toString, recordValue)
      kafkaProducer.send(record, new Callback {
        override def onCompletion(metadata: RecordMetadata, exception: Exception): Unit =
          if (exception != null) logger.error("Error while producing", exception)
          else logger.info(
            s"""Successfully sent a message."
        Topic: ${metadata.topic}
        Partition: ${metadata.partition}
        Offset: ${metadata.offset}
        Timestamp: ${metadata.timestamp}""")
      })
    }
    def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice) {}
    def onTrackLimitationNotice(numberOfLimitedStatuses: Int) {}
    def onException(ex: Exception) { ex.printStackTrace }
    def onScrubGeo(arg0: Long, arg1: Long) {}
    def onStallWarning(warning: StallWarning) {}
  }
}