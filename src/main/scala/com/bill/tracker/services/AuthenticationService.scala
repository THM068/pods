package com.bill.tracker.services

import com.bill.tracker.repository.AccountRepository
import com.bill.tracker.server.routes.AppError.WrongCredentialsError
import com.bill.tracker.util.BCryptEncoder
import zio.crypto.hash.Hash
import zio.{Task, ZIO}

case class LoginCredentials(email: String, password: String)
case class LoginResponse(email: String, profileId: Long)
trait AuthenticationService {
  def login(loginCredentials: LoginCredentials): Task[LoginResponse]
}

case class AuthenticationServiceLive(accountRepository: AccountRepository) extends AuthenticationService {
  override def login(loginCredentials: LoginCredentials): Task[LoginResponse] = for {
    accountOption <- accountRepository.findByEmail(loginCredentials.email)
    account = accountOption.getOrElse(throw WrongCredentialsError("account does not exist"))
    isCredentialsValid <- BCryptEncoder.matches(loginCredentials.password, account.password).provide(Hash.live)

  } yield isCredentialsValid match {
    case true => LoginResponse(account.email, account.account_id)
    case _ => throw WrongCredentialsError("Username or password is not correct")
  }
}

object AutoWireAuthenticationService {

}
