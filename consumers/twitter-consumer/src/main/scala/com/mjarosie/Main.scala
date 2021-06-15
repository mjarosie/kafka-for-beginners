package com.mjarosie

import scala.concurrent._
import scala.concurrent.duration.{FiniteDuration, SECONDS}
import scala.jdk.CollectionConverters.IterableHasAsScala
import scala.jdk.DurationConverters.ScalaDurationOps
import ExecutionContext.Implicits.global
import com.sksamuel.elastic4s.http.JavaClient
import com.sksamuel.elastic4s.requests.bulk.BulkResponse
import com.sksamuel.elastic4s.{ElasticClient, ElasticProperties, RequestFailure, RequestSuccess}
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord, KafkaConsumer}
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory

import java.util.{Collections, Properties}
import scala.util.{Failure, Success}

object Main extends App {
  @volatile var keepRunning = true
  val mainThread = Thread.currentThread()
  val logger = LoggerFactory.getLogger(this.getClass)

  val topicName = "tweets"
  val groupId = "elasticsearch-test"

  val kafkaConsumer = KafkaUtil.initialiseConsumer(topicName, groupId)
  val elasticsearchClient = ElasticsearchUtil.initialiseClient()

  Runtime.getRuntime.addShutdownHook(new Thread() {
    override def run = {
      keepRunning = false
      mainThread.join()
    }
  })

  def indexRecords(records: Iterable[ConsumerRecord[String, String]]) = {
    import com.sksamuel.elastic4s.ElasticDsl._

    val documentsIndexOperations = records.map(r => {
      indexInto("tweets").withId(r.key()).fields(
        "content" -> r.value()
      )
    })

    logger.info(s"about to bulk index ${documentsIndexOperations.size} documents")

    elasticsearchClient.execute {
      bulk(documentsIndexOperations)
    }
  }

  while (keepRunning) {
    val records = kafkaConsumer.poll(FiniteDuration(5, SECONDS).toJava).asScala

    logger.info(s"received ${records.size} records")

    if (records.nonEmpty) {
      val respFuture = indexRecords(records)
      respFuture.onComplete {
        case Failure(exception) =>
          logger.info(s"Exception while indexing records: ${exception}")
        case Success(resp) =>
          logger.info("---- results of indexing documents in bulk ----")
          resp match {
            case rs: RequestSuccess[BulkResponse] => {
              val result = rs.result
              logger.info(s"successfully processed ${result.successes.length} records")
              result.successes.foreach(s => {
                s.id
              })
              logger.info(s"errors processing ${result.failures.length} records")
              result.failures.foreach(f => {
                logger.info(f.result)
                f.error.foreach(e => {
                  logger.info(s"error: ${e.reason}")
                  e.caused_by.foreach(cb => {
                    logger.info(s"reason: ${cb.reason}")
                  })
                })
              })
            }
            case rf: RequestFailure => logger.info(rf.error.toString)
          }
      }
    }
  }

  logger.info(s"closing Kafka consumer")
  kafkaConsumer.close()
  logger.info(s"closing Elasticsearch client")
  elasticsearchClient.close()
}

object KafkaUtil {
  def initialiseConsumer(topicName: String, groupId: String) = {
    val bootstrapServers = "kafka:9092"

    val properties = new Properties
    properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
    properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer].getName)
    properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer].getName)
    properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId)
    properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    val consumer = new KafkaConsumer[String, String](properties)

    consumer.subscribe(Collections.singleton(topicName))
    consumer
  }
}

object ElasticsearchUtil {
  def initialiseClient() = {
    val props = ElasticProperties("http://es01:9200")
    val client = ElasticClient(JavaClient(props))
    client
  }
}