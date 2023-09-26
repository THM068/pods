package com.bill.tracker.server.routes
import zio.http.{Response, Status}

object HandleErrors {
  def handle(throwable: Throwable) = Response.text("The error is " + throwable).status(Status.InternalServerError)

}
