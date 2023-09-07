import com.bill.tracker.model.ConfigLayer
import com.bill.tracker.repository.CategoryRepository
import com.bill.tracker.server.AppServer
import com.bill.tracker.server.routes.{CategoryRoutes, HealthCheck}
import zio.Console.{printLine, readLine}
import zio._



object Main extends ZIOAppDefault {


  // Run it like any simple app
  override val run: Task[Unit] = for {
    f <- ZIO.serviceWithZIO[AppServer](_.runServer())
      .provide(
        ZLayer.Debug.mermaid,
        AppServer.layer,
        HealthCheck.layer,
        CategoryRepository.layer,
        CategoryRoutes.layer,
        ConfigLayer.layer
      ).forkDaemon
    _ <- printLine("Press Any Key to stop the server") *> readLine.catchAll(e =>
      printLine(s"There was an error server can't be started !!! ${e.getMessage}")
    ) *> f.interrupt

  } yield ()
}