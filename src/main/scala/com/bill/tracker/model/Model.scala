package com.bill.tracker.model
import com.bill.tracker.repository.CategoryRepository
import com.typesafe.config.{Config, ConfigFactory}
import slick.interop.zio.DatabaseProvider
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._
import slick.sql.SqlProfile.ColumnOption.SqlType
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}
import zio.ZLayer

import java.sql.Timestamp
import java.time.LocalDateTime
import scala.jdk.CollectionConverters.{dictionaryAsScalaMap, mapAsJavaMapConverter}
case class AppStatus(status: String)
case class Category(
                     category_id: Long,
                     name: String,
                     last_update: Timestamp) {

  def toCategoryMapper(): CategoryMapper = {
    CategoryMapper(category_id, name, last_update
    )
  }

}
case class CategoryMapper(category_id: Long, name: String, last_update: Timestamp)

object AppStatus {
  implicit val decoder: JsonDecoder[AppStatus] = DeriveJsonDecoder.gen[AppStatus]
  implicit val encoder: JsonEncoder[AppStatus] = DeriveJsonEncoder.gen[AppStatus]
}
object CategoryTable {
  class Categorys(tag: Tag) extends Table[Category](_tableTag = tag, "category") {
    def category_id = column[Long]("category_id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def last_update = column[Timestamp]("last_update", SqlType("timestamp not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP"))

    def * = (category_id, name, last_update) <> ((Category.apply _).tupled, Category.unapply _)
  }
}


object Category extends TimestampReadWrite {
  implicit val decoder: JsonDecoder[Category] = DeriveJsonDecoder.gen[Category]
  implicit val encoder: JsonEncoder[Category] = DeriveJsonEncoder.gen[Category]
}

trait TimestampReadWrite {
  implicit val timestampEncoder: JsonEncoder[Timestamp] =
    JsonEncoder[Long].contramap(_.getTime)

  implicit val timestampDecoder: JsonDecoder[Timestamp] =
    JsonDecoder[Long].map(new Timestamp(_))
}

case class CategoryMapperList(categoryMapperList: Seq[CategoryMapper])

object CategoryMapperList {
  implicit val encodeCategoryList: JsonEncoder[CategoryMapperList] = DeriveJsonEncoder.gen[CategoryMapperList]
  implicit val decodeCategoryList: JsonDecoder[CategoryMapperList] = DeriveJsonDecoder.gen[CategoryMapperList]
}

object CategoryMapper extends TimestampReadWrite{
  implicit val encodeCategoryMapper: JsonEncoder[CategoryMapper] = DeriveJsonEncoder.gen[CategoryMapper]
  implicit val decodeCategoryMapper: JsonDecoder[CategoryMapper] = DeriveJsonDecoder.gen[CategoryMapper]
}

object DatabaseProviderLayer {

  val layer: ZLayer[Config with JdbcProfile, Throwable, DatabaseProvider] = DatabaseProvider.fromConfig("kafka")
}

object ConfigLayer {
  private val config = ConfigFactory.parseMap(
     Map(
      "url" -> "jdbc:postgresql:pagila",
      "driver" -> "org.postgresql.Driver",
      "user" -> "postgres",
      "password" -> "test",
      "numThreads" -> "10",
      "maxConnections" -> "10"
    ).asJava


  )

  val layer: ZLayer[Any, Throwable, DatabaseProvider] = (ZLayer.succeed(config) ++ ZLayer.succeed[JdbcProfile](
    slick.jdbc.PostgresProfile
  )) >>> DatabaseProvider.fromConfig()
}

case class CategoryDTO(id: Option[Long] = None, name: String) {
  def toCategory() = Category(name = name, last_update = new Timestamp(System.currentTimeMillis()), category_id = 0L)
}

object CategoryDTO {
  implicit val decoder: JsonDecoder[CategoryDTO] = DeriveJsonDecoder.gen[CategoryDTO]
  implicit val encoder: JsonEncoder[CategoryDTO] = DeriveJsonEncoder.gen[CategoryDTO]
}

case class Message(message: String, service: String)
object Message {
  implicit val encoder: JsonEncoder[Message] = DeriveJsonEncoder.gen[Message]
  implicit val decoder: JsonDecoder[Message] = DeriveJsonDecoder.gen[Message]
}

case class Stock(name: String = "AMZ", price: Long)

object Stock {
  implicit val decoder: JsonDecoder[Stock] = DeriveJsonDecoder.gen[Stock]
  implicit val encoder: JsonEncoder[Stock] = DeriveJsonEncoder.gen[Stock]
}

case class Account(account_id: Long, email: String, password: String)

object AccountTable {
  class Accounts(tag: Tag) extends Table[Account](_tableTag = tag, "account") {
    def account_id = column[Long]("account_id", O.PrimaryKey, O.AutoInc)

    def email = column[String]("email", O.Unique)
    def password = column[String]("password")

    def * = (account_id, email, password) <> ((Account.apply _).tupled, Account.unapply _)

  }
}


