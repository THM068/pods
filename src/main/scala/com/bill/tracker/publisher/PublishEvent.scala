package com.bill.tracker.publisher

import zio.{Random, Task, ZIO, ZLayer}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, EncoderOps, JsonDecoder, JsonEncoder}
import zio.kafka.producer.{Producer, ProducerSettings}
import zio.kafka.serde.Serde

case class AccountData(accountId: Long, email: String)

object AccountData {
  implicit val encode: JsonEncoder[AccountData] = DeriveJsonEncoder.gen[AccountData]
  implicit val decode: JsonDecoder[AccountData] = DeriveJsonDecoder.gen[AccountData]
}
trait PublishEvent[E] {
  def publishEvent[E](msg: E)(implicit msgEncoder: JsonEncoder[E]) : Task[Unit]
}

case class PublishUserRegisteredEvent(producer: Producer) extends PublishEvent[AccountData] {
  override def publishEvent[AccountData](event: AccountData)(implicit msgEncoder: JsonEncoder[AccountData]): Task[Unit] =
    ZIO.log(s"Publishing Event Payload [$event]") *> (for {
      key <- Random.nextUUID
      _ <- producer.produce("stripe-events", key.toString, event.toJson, Serde.string, Serde.string)

    } yield ())
}

object PublishUserRegisteredEvent {
  val layer: ZLayer[Producer, Throwable, PublishUserRegisteredEvent] = {
    ZLayer.fromFunction(PublishUserRegisteredEvent.apply _)
  }
}


object KafkaProducer {
  private val BOOSTRAP_SERVERS = List("bright-mammoth-6011-eu1-kafka.upstash.io:9092")
  val layer: ZLayer[Any, Throwable, Producer] =
    ZLayer.scoped(
      Producer.make(
        ProducerSettings(BOOSTRAP_SERVERS)
          .withProperty("sasl.mechanism", "SCRAM-SHA-256")
          .withProperty("security.protocol", "SASL_SSL")
          .withProperty("sasl.jaas.config", "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"YnJpZ2h0LW1hbW1vdGgtNjAxMSQ-uUOt2Eoqa2H0S4zUc4QvfJ9OtvWToaI1PoU\" password=\"95ec241d9760453cb93eeb7296a1b5de\";")
          .withProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
          .withProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")

      )
    )
}
