package com.bill.tracker.server
import com.bill.tracker.server.routes.{AuthenticationRoute, CategoryRoutes, HealthCheck, RegisterRoutes, StockTicker}
import zio._
import zio.http._

case class AppServer(healthCheck: HealthCheck, categoryRoutes: CategoryRoutes,
                     stockTicker: StockTicker, registerRoutes: RegisterRoutes,
                     authenticationRoute: AuthenticationRoute) {

  val app = healthCheck.apps ++ categoryRoutes.apps ++ stockTicker.apps ++ registerRoutes.apps ++ authenticationRoute.apps
  val port = 9998
  def runServer(): ZIO[Any, Throwable, Unit] = for {
    _ <- ZIO.debug(s"Starting server on http://localhost:${port}")
    _ <- Server.serve(app)
      .provide(Server.defaultWithPort(port))
  } yield ()

}

object AutowireAppServer {
  val layer = ZLayer.fromFunction(AppServer.apply _)
}


