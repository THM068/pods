package com.bill.tracker.server.routes
import com.bill.tracker.model.AppStatus
import com.bill.tracker.server.HandleErrors
import zio._
import zio.http.Middleware.basicAuth
import zio.http._
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, EncoderOps, JsonDecoder, JsonEncoder}
class HealthCheck {

  val appStatus =
    Method.GET / "health" / "status" -> handler {
      Response.json(AppStatus("up").toJson)
    }

  val jsonRoute =
    Method.GET / "json" -> handler(Response.json("""{"greetings": "Hello World!"}"""))

  val apps = Routes(appStatus, jsonRoute)
    .handleError(HandleErrors.handle)
    .toHttpApp

}

object HealthCheck {
  val layer = ZLayer.succeed(new HealthCheck())
}


