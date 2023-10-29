package com.bill.tracker.microstream

import zio.ZLayer
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class Fruit(var name: String, var description: String)
case class FruitCommand(name: String, description: String)

case class Fruits(fruits: List[Fruit])

object Fruits {
  implicit val decoder: JsonDecoder[Fruits] = DeriveJsonDecoder.gen[Fruits]
  implicit val encoder: JsonEncoder[Fruits] = DeriveJsonEncoder.gen[Fruits]
}

object Fruit {
  implicit val decoder: JsonDecoder[Fruit] = DeriveJsonDecoder.gen[Fruit]
  implicit val encoder: JsonEncoder[Fruit] = DeriveJsonEncoder.gen[Fruit]
}

object FruitCommand {
  implicit val decoder: JsonDecoder[FruitCommand] = DeriveJsonDecoder.gen[FruitCommand]
  implicit val encoder: JsonEncoder[FruitCommand] = DeriveJsonEncoder.gen[FruitCommand]
}

object FruitStorage {
  val layer = ZLayer.fromZIO {
    for {
      container <- MicroStream.make
    } yield container
  }
}