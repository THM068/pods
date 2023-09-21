package com.bill.tracker.server
import com.bill.tracker.server.routes.{CategoryRoutes, HealthCheck, StockTicker}
import zio._
import zio.http.Middleware.basicAuth
import zio.http._

case class AppServer(healthCheck: HealthCheck, categoryRoutes: CategoryRoutes, stockTicker: StockTicker) {

  //val routes = Routes.fromIterable(healthCheck.a ++ categoryRoutes.routesChunk)
  // Create HTTP route
  val app = healthCheck.apps ++ categoryRoutes.apps ++ stockTicker.apps

  def runServer(): ZIO[Any, Throwable, Unit] = for {
    _ <- ZIO.debug(s"Starting server on http://localhost:8080")
    _ <- Server.serve(app)
      .provide(Server.defaultWithPort(8080))
  } yield ()

}

object AppServer {
  val layer = ZLayer.fromFunction(AppServer.apply _)
}

object HandleErrors {
  def handle(throwable: Throwable) = Response.text("The error is " + throwable).status(Status.InternalServerError)

}
