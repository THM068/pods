package com.bill.tracker.server
import com.bill.tracker.server.routes.{AuthenticationRoute, CategoryRoutes, HealthCheck, RegisterRoutes, StockTicker}
import zio._
import zio.http._

case class AppServer(healthCheck: HealthCheck, categoryRoutes: CategoryRoutes,
                     stockTicker: StockTicker, registerRoutes: RegisterRoutes,
                     authenticationRoute: AuthenticationRoute) {

  val app = healthCheck.apps ++ categoryRoutes.apps ++ stockTicker.apps ++ registerRoutes.apps ++ authenticationRoute.apps

  def runServer(): ZIO[Any, Throwable, Unit] = for {
    _ <- ZIO.debug(s"Starting server on http://localhost:8080")
    _ <- Server.serve(app)
      .provide(Server.defaultWithPort(8080))
  } yield ()

}

object AutowireAppServer {
  val layer = ZLayer.fromFunction(AppServer.apply _)
}


