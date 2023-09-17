package com.bill.tracker.repository

import com.bill.tracker.model.{AccountTable}
import slick.interop.zio.DatabaseProvider
import slick.lifted.TableQuery
import slick.jdbc.PostgresProfile.api._
import slick.interop.zio.syntax._
import zio.{IO, ZEnvironment, ZIO, ZLayer}

case class AccountRepository (db: DatabaseProvider) {
  private val accountTable = TableQuery[AccountTable.Accounts]

  def create(): IO[Throwable, Unit] =
    ZIO.fromDBIO(accountTable.schema.createIfNotExists).provideEnvironment(ZEnvironment(db))
}

object AccountRepository {
  val layer: ZLayer[DatabaseProvider, Nothing, AccountRepository] =
    ZLayer {
      for {
        db   <- ZIO.service[DatabaseProvider]
      } yield AccountRepository(db)
    }
}


