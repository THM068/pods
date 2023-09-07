package com.bill.tracker.repository
import com.bill.tracker.model.CategoryTable
import com.bill.tracker.model.Category
import slick.jdbc.PostgresProfile.api._
import slick.interop.zio.syntax._
import slick.interop.zio.DatabaseProvider
import zio._

case class CategoryRepository(db: DatabaseProvider) {
  private val categoryTable =TableQuery[CategoryTable.Categorys]

  def findAll(): IO[Throwable, Seq[Category]] =
    ZIO.fromDBIO(categoryTable.result).provideEnvironment(ZEnvironment(db))

  def findByName(name: String): IO[Throwable, Seq[Category]] =
    ZIO.fromDBIO(categoryTable.filter( _.name === name).result).provideEnvironment(ZEnvironment(db))

  def createCategory(category: Category): IO[Throwable, Long] =
    ZIO.fromDBIO(categoryTable returning categoryTable.map(_.category_id) += category).provideEnvironment(ZEnvironment(db))

  def deleteCategory(id: Long): IO[Throwable, Int] =
    ZIO.fromDBIO(categoryTable.filter(_.category_id === id).delete).provideEnvironment(ZEnvironment(db))

}

object CategoryRepository {
  val layer: ZLayer[DatabaseProvider, Nothing, CategoryRepository] =
    ZLayer {
      for {
        db   <- ZIO.service[DatabaseProvider]
      } yield CategoryRepository(db)
    }
}

