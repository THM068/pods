package com.bill.tracker

import com.bill.tracker.model.Account
import com.bill.tracker.repository.AccountRepository
import com.bill.tracker.util.BCryptEncoder
import zio.crypto.hash.Hash
import zio.{IO, ZIO, ZLayer}

case class AccountService(accountRepository: AccountRepository) {
  def createAccount(account: Account): IO[Throwable, Long] = for {
    hashedPassword <- BCryptEncoder.encode(account.password).provide(Hash.live)
    accountWithHashedPassword = account.copy(password = hashedPassword.value)
    id <- accountRepository.createAccount(accountWithHashedPassword)
  } yield id

}

object AutowireAccountService {
  val layer: ZLayer[AccountRepository, Nothing, AccountService] =
    ZLayer.fromFunction(AccountService.apply _)
}
