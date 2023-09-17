package com.bill.tracker.server.routes
import com.bill.tracker.model._
import com.bill.tracker.repository.{AccountRepository, CategoryRepository}
import com.bill.tracker.server.HandleErrors
import com.bill.tracker.server.routes.ServerUtils.parseBody
import zio._
import zio.http.Middleware.basicAuth
import zio.http.{Method, Request, Response, Routes, Status, handler, long, string}
import zio.json.EncoderOps

case class CategoryRoutes(categoryRepository: CategoryRepository) {

  val categories = Method.GET / "category" -> handler {
    (for {
      categoryList <- categoryRepository.findAll()
      categoryMapperList = CategoryMapperList(categoryMapperList = categoryList.map(_.toCategoryMapper()))
    } yield(Response.json(categoryMapperList.toJson))).catchSome(catchException)
  }

  val findByName = Method.GET / "category" / "name" / string("name") -> handler { (name: String, _: Request) =>
    (for {
      categoryList <- categoryRepository.findByName(name)
      categoryMapperList = CategoryMapperList(categoryMapperList = categoryList.map(_.toCategoryMapper()))
    } yield Response.json(categoryMapperList.toJson)).catchSome(catchException)
  }

  val deleteCategory = Method.DELETE / "category" / long("id") -> handler { (id: Long, _: Request) =>
    (for {
      _ <- categoryRepository.deleteCategory(id)
    } yield Response.status(Status.Ok)).catchSome(catchException)
  }

  val addCategory = Method.POST / "category" -> handler { (request: Request) =>
    (for {
        categoryDTO <- parseBody[CategoryDTO](request)
        id <- categoryRepository.createCategory(categoryDTO.toCategory())
      } yield Response.json(CategoryDTO(Some(id), categoryDTO.name).toJson).status(Status.Created)).catchSome(catchException)
  }

  val getCategory = Method.GET / "category" / long("id") -> handler { (id: Long, _ : Request) =>
    (for {
      categoryList <- categoryRepository.getCategory(id)
      categoryOption = categoryList.headOption
    } yield {
      categoryOption match {
        case Some(category) =>
          Response.json(category.toJson)
        case _ =>
          Response.text("Missing category").status(Status.NotFound)
      }
    }).catchSome(catchException)
  }

  def catchException: PartialFunction[Throwable, ZIO[Any, Throwable, Response]] = {
    case e: Exception =>
      ZIO.succeed(
        Response.json(
          Message(e.getMessage, "category-repository").toJson)
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
  }
  val apps = Routes(categories,  findByName, getCategory, addCategory, deleteCategory)
    .handleError(HandleErrors.handle)
    .toHttpApp

}

object CategoryRoutes {
  val layer: ZLayer[CategoryRepository, Nothing, CategoryRoutes] =
    ZLayer.fromFunction(CategoryRoutes.apply _)
}
