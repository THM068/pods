package com.bill.tracker.repository

import com.bill.tracker.model.{AccountTable, ExpensePeriod, ExpensePeriodTable}
import slick.interop.zio.DatabaseProvider
import slick.lifted.TableQuery
import zio.{IO, ZEnvironment, ZIO, ZLayer}
import slick.jdbc.PostgresProfile.api._
import slick.interop.zio.syntax._

case class ExpensePeriodRepository (db: DatabaseProvider) {

  private val expensePeriodTable = TableQuery[ExpensePeriodTable.ExpensePeriodTable]

  def create(): IO[Throwable, Unit] =
    ZIO.fromDBIO(expensePeriodTable.schema.createIfNotExists).provideEnvironment(ZEnvironment(db))

  def createCategory(expensePeriod: ExpensePeriod): IO[Throwable, Long] =
    ZIO.fromDBIO(expensePeriodTable returning expensePeriodTable.map(_.id) += expensePeriod).provideEnvironment(ZEnvironment(db))


}

object AutowireExpensePeriodRepository {
  val layer: ZLayer[DatabaseProvider, Nothing, ExpensePeriodRepository] =
    ZLayer.fromFunction(ExpensePeriodRepository.apply _)

}
