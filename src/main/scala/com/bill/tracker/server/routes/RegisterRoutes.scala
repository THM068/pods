package com.bill.tracker.server.routes
import com.bill.tracker._
import com.bill.tracker.model.{Account, AccountDTO, Message}
import com.bill.tracker.server.routes.ServerUtils.parseBody
import zio.{ZLayer, _}
import zio.http.{Method, Request, Response, Routes, Status, handler}
import zio.json.EncoderOps

case class RegisterRoutes(accountService: AccountService) {

  val create = Method.POST / "register" -> handler { (request: Request) =>
    (for {
      accountDto <- parseBody[AccountDTO](request)
      account = Account.fromAccountDto(accountDto)
      id <- accountService.createAccount(account)
    } yield Response.json(AccountDTO(Some(id), accountDto.email, password = "").toJson).status(Status.Created))
      .catchSome {
        case e: Exception =>
          ZIO.succeed(
            Response.json(
              Message(e.getMessage, "account-repository").toJson)
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
        case AppError.AccountError(message) =>
          ZIO.succeed(
            Response.json(
              Message(message, "account-repository").toJson)
              .status(Status.BadRequest)
          )
      }
  }

  val apps = Routes(create)
    .handleError(HandleErrors.handle)
    .toHttpApp

}

object RegisterRoutes {
  val layer: ZLayer[AccountService, Nothing, RegisterRoutes] =
    ZLayer.fromFunction(RegisterRoutes.apply _)
}
