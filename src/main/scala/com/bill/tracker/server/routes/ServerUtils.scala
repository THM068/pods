package com.bill.tracker.server.routes

import zio.http.Request
import zio.json.{DecoderOps, JsonDecoder}
import zio.{IO, ZIO}

object ServerUtils {
  def parseBody[A: JsonDecoder](request: Request): IO[AppError, A] =
    for {
      body   <- request.body.asString.orElseFail(AppError.MissingBodyError)
      parsed <- ZIO.from(body.fromJson[A]).mapError(msg => AppError.JsonDecodingError(msg))
    } yield parsed
}
sealed trait AppError extends Throwable

object AppError {

  case object MissingBodyError extends AppError

  final case class JsonDecodingError(message: String) extends AppError

  final case class AccountError(message: String) extends AppError

  //  final case class InvalidIdError(message: String) extends AppError

}
