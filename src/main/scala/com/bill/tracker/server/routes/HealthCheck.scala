package com.bill.tracker.server.routes
import com.bill.tracker.model.AppStatus
import zio._
import zio.http._
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, EncoderOps, JsonDecoder, JsonEncoder}
class HealthCheck {

  val appStatus =
    Method.GET / "health" / "status" -> handler {
      Response.json(AppStatus("up").toJson)
    }

  val jsonRoute =
    Method.GET / "json" -> handler(Response.json("""{"greetings": "Hello World!"}"""))

  val routes = Chunk(appStatus, jsonRoute)

}

object HealthCheck {
  val layer = ZLayer.succeed(new HealthCheck())
}


