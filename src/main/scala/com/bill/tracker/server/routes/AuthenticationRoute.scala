package com.bill.tracker.server.routes

import com.bill.tracker.model._
import com.bill.tracker.server.routes.HandleErrors.catchException
import com.bill.tracker.server.{JwtService, JwtToken, UserDetails}
import com.bill.tracker.server.routes.ServerUtils.parseBody
import com.bill.tracker.services.{AuthenticationService, LoginCredentials, LoginResponse}
import zio.ZLayer
import zio.http.{Method, Request, Response, Routes, Status, handler}
import zio.json.EncoderOps


case class AuthenticationRoute(authenticationService: AuthenticationService, jwtService: JwtService) {

  val login = Method.POST / "login" -> handler { (request: Request) =>
    (for {
      loginCredentials <- parseBody[LoginCredentials](request)
      loginResponse <- authenticationService.login(loginCredentials)
    } yield {
      loginResponse match {
        case Some(LoginResponse(email, profileId)) =>
          val jwtResult  =  jwtService.jwtEncode(UserDetails(profileId = profileId, username = email))
          Response.json(JwtToken(token = jwtResult).toJson)
        case _ =>
          Response.json(
            Message("Username or password is wrong", "authentication service").toJson)
            .status(Status.Forbidden)
      }

    }).catchSome(catchException)
  }


  val apps = Routes(login)
    .handleError(HandleErrors.handle)
    .toHttpApp
}

object AutoWireAuthenticationRoute {
  val layer: ZLayer[AuthenticationService with JwtService, Nothing, AuthenticationRoute] =
    ZLayer.fromFunction(AuthenticationRoute.apply _)
}
