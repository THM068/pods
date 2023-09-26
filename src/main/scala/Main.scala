import com.bill.tracker.AutowireAccountService
import com.bill.tracker.model.DBConfigLayer
import com.bill.tracker.repository.{AutowireAccountRepository, AutowireCategoryRepository}
import com.bill.tracker.server.routes._
import com.bill.tracker.server.{AppServer, AutowireAppServer, AutowireJwtService}
import zio._
import zio.logging.LogFormat
import zio.logging.backend.SLF4J



object Main extends ZIOAppDefault {

  override val bootstrap = SLF4J.slf4j(LogFormat.colored)

  // Run it like any simple app
  override val run: Task[Unit] = for {
    _ <- StockTickerBroadCaster.scheduleNotification.fork
    _ <- ZIO.serviceWithZIO[AppServer](_.runServer())
      .provide(
        ZLayer.Debug.mermaid,
        AutowireAppServer.layer,
        AutoWireHealthCheck.layer,
        AutowireCategoryRepository.layer,
        CategoryRoutes.layer,
        DBConfigLayer.layer,
        AutowireStockTicker.layer,
        AutowireAccountRepository.layer,
        AutowireAccountService.layer,
        RegisterRoutes.layer,
        AutowireJwtService.layer
      )

  } yield ()
}