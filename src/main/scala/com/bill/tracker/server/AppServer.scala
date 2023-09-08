package com.bill.tracker.server
import com.bill.tracker.server.routes.{CategoryRoutes, HealthCheck}
import zio._
import zio.http.Middleware.basicAuth
import zio.http._

case class AppServer(healthCheck: HealthCheck, categoryRoutes: CategoryRoutes) {

  val routes = Routes.fromIterable(healthCheck.routesChunk ++ categoryRoutes.routesChunk)
  // Create HTTP route
  val app = routes.handleError(throwable =>
    Response.text("The error is " + throwable).status(Status.InternalServerError)
  ).toHttpApp

  def runServer(): ZIO[Any, Throwable, Unit] = for {
    _ <- ZIO.debug(s"Starting server on http://localhost:9091")
    _ <- Server.serve(app)
      .provide(Server.defaultWithPort(9091))
  } yield ()

}

object AppServer {
  val layer = ZLayer.fromFunction(AppServer.apply _)
}

object HandleErrors {
  def handle(throwable: Throwable) = Response.text("The error is " + throwable).status(Status.InternalServerError)

}
