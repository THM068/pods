package com.bill.tracker.services

import com.bill.tracker.model.Account
import com.bill.tracker.repository.AccountRepository
import com.bill.tracker.util.BCryptEncoder
import zio.crypto.hash.Hash
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}
import zio.{Task, ZIO, ZLayer}

case class LoginCredentials(email: String, password: String)
object LoginCredentials {
  implicit val encode: JsonEncoder[LoginCredentials] = DeriveJsonEncoder.gen[LoginCredentials]
  implicit val decode: JsonDecoder[LoginCredentials] = DeriveJsonDecoder.gen[LoginCredentials]
}
case class LoginResponse(email: String, profileId: Long)
trait AuthenticationService {
  def login(loginCredentials: LoginCredentials): Task[Option[LoginResponse]]
}

case class AuthenticationServiceLive(accountRepository: AccountRepository) extends AuthenticationService {
  override def login(loginCredentials: LoginCredentials): Task[Option[LoginResponse]] = for {
    accountOption <- accountRepository.findByEmail(loginCredentials.email)
    loginResponseOption <- login(loginCredentials, accountOption)

  } yield loginResponseOption

  def login(loginCredentials: LoginCredentials, accountOption: Option[Account]): ZIO[Any, Throwable, Option[LoginResponse]] = {
    accountOption match {
      case Some(Account(account_id,email, password)) =>
        for {
          isCredentialsValid <- BCryptEncoder.matches(loginCredentials.password, password).provide(Hash.live)
        } yield isCredentialsValid match {
          case true => Some(LoginResponse(email,account_id))
          case _ => None
        }
      case None => ZIO.succeed(None)
    }
  }

}

object AutoWireAuthenticationService {

  val layer: ZLayer[AccountRepository, Nothing, AuthenticationService] =
    ZLayer.fromFunction(AuthenticationServiceLive.apply _)
}
