package com.bill.tracker

import com.bill.tracker.model.Account
import com.bill.tracker.publisher.{AccountData, PublishEvent, PublishUserRegisteredEvent}
import com.bill.tracker.repository.AccountRepository
import com.bill.tracker.util.BCryptEncoder
import zio.crypto.hash.Hash
import zio.{IO, ZIO, ZLayer}

case class AccountService(accountRepository: AccountRepository, publishUserRegisteredEvent: PublishEvent[AccountData]) {
  def createAccount(account: Account): IO[Throwable, Long] = for {
    hashedPassword <- BCryptEncoder.encode(account.password).provide(Hash.live)
    accountWithHashedPassword = account.copy(password = hashedPassword.value)
    id <- accountRepository.createAccount(accountWithHashedPassword)
    publish <- publishUserRegisteredEvent.publishEvent(AccountData(id, account.email)).fork
    _ <- publish.join
  } yield id

}

object AutowireAccountService {
  val layer: ZLayer[AccountRepository with PublishEvent[AccountData] , Nothing, AccountService] =
    ZLayer.fromFunction(AccountService.apply _)
}
