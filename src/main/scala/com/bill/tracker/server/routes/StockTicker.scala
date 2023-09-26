package com.bill.tracker.server.routes

import com.bill.tracker.model.Stock
import zio._
import zio.http.ChannelEvent.{Read, Unregistered, UserEvent, UserEventTriggered}
import zio.http.codec.PathCodec.string
import zio.http._
import zio.json.EncoderOps
class StockTicker {
  private val socketApp: (String) => WebSocketApp[Any] = (clientId: String) =>
    Handler.webSocket { channel =>
      channel.receiveAll {
        case Read(WebSocketFrame.Text("end")) =>
          StockTickerBroadCaster.unRegister(clientId)
          channel.shutdown *>
          ZIO.log(s"${clientId} has disconnected")
        case Read(WebSocketFrame.Text(text)) =>
          channel.send(Read(WebSocketFrame.Text(text.toUpperCase())))
        case UserEventTriggered(UserEvent.HandshakeComplete) =>
          StockTickerBroadCaster.register(clientId, channel)
          channel.send(Read(WebSocketFrame.text("Greetings!")))
        case Unregistered =>
          ZIO.succeed(StockTickerBroadCaster.unRegister(clientId)) *>
          ZIO.log(s"${clientId} has disconnected")
        case _ =>
          ZIO.unit
      }
    }

   val apps: HttpApp[Any] =
    Routes(
      Method.GET / "greet" / string("name") -> handler { (name: String, _: Request) =>
        Response.text(s"Greetings {$name}!")
      },
      Method.GET / "subscriptions" / string("id") -> handler { (id: String, _: Request) =>
        socketApp(id).toResponse
      }
    ).toHttpApp
}

object AutowireStockTicker {
  val layer: ZLayer[Any, Nothing, StockTicker] =
    ZLayer.succeed(new StockTicker())
}

object StockTickerBroadCaster {
  import scala.collection.mutable.Map

  val clientsMap: Map[String, WebSocketChannel] = Map[String, WebSocketChannel]()
  def register(clientId: String, channel: WebSocketChannel) =
    clientsMap += ( clientId -> channel)

  def unRegister(clientId: String) = clientsMap.remove(clientId)

  private def notifiyClients(): ZIO[Any, Throwable, Any] =
    Random.nextIntBetween(200, 500 ).flatMap { stockValue =>
      ZIO.foreachDiscard(clientsMap.map { case (key, value) => (key, value) }) { entry =>
        val stock = Stock(price = stockValue)
        val channel = entry._2
        for {
          _ <- channel.send(Read(WebSocketFrame.Text(stock.toJson)))
        } yield ()
    }

  }

  def scheduleNotification = notifiyClients().repeat(
    zio.Schedule.spaced(2.seconds) &&
      zio.Schedule.recurs(10000)
  )
}

