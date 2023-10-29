import com.bill.tracker.AutowireAccountService
import com.bill.tracker.microstream.{FruitContainer, FruitRepository, FruitStorage, MicroStream}
import com.bill.tracker.model.DBConfigLayer
import com.bill.tracker.publisher.{KafkaProducer, PublishUserRegisteredEvent}
import com.bill.tracker.repository.{AutowireAccountRepository, AutowireCategoryRepository, AutowireExpensePeriodRepository}
import com.bill.tracker.server.routes._
import com.bill.tracker.server.{AppServer, AutowireAppServer, AutowireJwtService}
import com.bill.tracker.services.AutoWireAuthenticationService
import one.microstream.concurrency.XThreads
import one.microstream.storage.embedded.types.EmbeddedStorage
import zio._
import zio.logging.LogFormat
import zio.logging.backend.SLF4J

import scala.collection.mutable



object Main extends ZIOAppDefault {

  override val bootstrap = SLF4J.slf4j(LogFormat.colored)

  //val storageManager = EmbeddedStorage.start



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
        AutowireJwtService.layer,
        AutoWireAuthenticationRoute.layer,
        AutoWireAuthenticationService.layer,
        AutowireExpensePeriodRepository.layer,
        PublishUserRegisteredEvent.layer,
        KafkaProducer.layer,
        FruitRoutes.layer,
        FruitRepository.layer,
        FruitStorage.layer
      )

  } yield ()
}