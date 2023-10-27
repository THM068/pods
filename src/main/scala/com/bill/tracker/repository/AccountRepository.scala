package com.bill.tracker.repository

import com.bill.tracker.model.{Account, AccountTable}
import com.bill.tracker.server.routes.AppError.AccountError
import slick.interop.zio.DatabaseProvider
import slick.lifted.TableQuery
import slick.jdbc.PostgresProfile.api._
import slick.interop.zio.syntax._
import zio.{IO, ZEnvironment, ZIO, ZLayer}


case class AccountRepository (db: DatabaseProvider) {
  private val accountTable = TableQuery[AccountTable.Accounts]

  def create(): IO[Throwable, Unit] =
    ZIO.fromDBIO(accountTable.schema.createIfNotExists).provideEnvironment(ZEnvironment(db))

  def createAccount(account: Account): IO[Throwable, Long] =
    ZIO.fromDBIO(accountTable returning accountTable.map(_.account_id) += account).provideEnvironment(ZEnvironment(db))
      .mapError(t => AccountError(s"An errors has occurred: ${t.getMessage}"))

  def findByEmail(email: String): IO[Throwable, Option[Account]] =
    ZIO.fromDBIO(accountTable.filter(_.email === email).result.headOption).provideEnvironment(ZEnvironment(db))

}

object AutowireAccountRepository {
  val layer: ZLayer[DatabaseProvider, Nothing, AccountRepository] =
    ZLayer {
      for {
        db   <- ZIO.service[DatabaseProvider]
      } yield AccountRepository(db)
    }
}


