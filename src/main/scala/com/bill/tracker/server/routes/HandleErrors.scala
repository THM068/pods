package com.bill.tracker.server.routes
import com.bill.tracker.model.Message
import zio.ZIO
import zio.http.{Response, Status}
import zio.json.EncoderOps

object HandleErrors {
  def handle(throwable: Throwable) = Response.text("The error is " + throwable).status(Status.InternalServerError)

  def catchException: PartialFunction[Throwable, ZIO[Any, Throwable, Response]] = {
    case e: Exception =>
      ZIO.succeed(
        Response.json(
          Message(e.getMessage, "category-repository").toJson)
          .status(Status.BadRequest)
      )
    case AppError.MissingBodyError =>
      ZIO.succeed(
        Response.json(
          Message("Missing body in request", "category-repository").toJson)
          .status(Status.BadRequest)
      )
    case AppError.JsonDecodingError(message) =>
      ZIO.succeed(
        Response.json(
          Message(message, "category-repository").toJson)
          .status(Status.BadRequest)
      )
  }

}
