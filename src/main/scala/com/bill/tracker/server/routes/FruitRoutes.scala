package com.bill.tracker.server.routes

import com.bill.tracker.microstream.{FruitCommand, FruitRepository, Fruits}
import zio.ZLayer
import zio.http.{Method, Response, Routes, handler}
import zio.json.EncoderOps
import scala.jdk.CollectionConverters._
case class FruitRoutes(fruitRepository: FruitRepository) {

  val create = Method.POST / "fruit"  -> handler {
    for {
      fruit <- fruitRepository.create(FruitCommand("xxttttxxx", "xxfffffxx apples")).fork
      f <- fruit.join
    } yield Response.json(f.toJson)
  }

  val list = Method.GET / "fruit" -> handler {
    for {
      fruits <- fruitRepository.list
    } yield Response.json(Fruits(fruits.asScala.toList).toJson)
  }

  val apps = Routes(create, list)
    .handleError(HandleErrors.handle)
    .toHttpApp


}

object FruitRoutes {
  val layer: ZLayer[FruitRepository, Nothing, FruitRoutes] =
    ZLayer.fromFunction(FruitRoutes.apply _)

}
