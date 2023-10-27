package com.bill.tracker.server.routes

import com.bill.tracker.server.routes.AppError.BearerTokenError
import com.bill.tracker.server.{JwtService, UserDetails}
import pdi.jwt.JwtClaim
import zio.{RIO, ZIO}
import zio.http.{Header, Request}
import zio.json._

object Requesthelper {

  def getBearerToken(request: Request): Option[String] =
    request.header(Header.Authorization) match {
      case Some(Header.Authorization.Bearer(token)) => Some(token)
      case _ => None
  }

  def getUserDetails(request: Request): RIO[JwtService, Option[UserDetails]] = {
    getBearerToken(request) match {
      case Some(token) =>
        for {
          jwtClaimOption <- ZIO.serviceWith[JwtService](_.jwtDecode(token))
          userDetailsOption = getUserDetailsOption(jwtClaimOption)
        } yield userDetailsOption
      case _ => ZIO.succeed(None)
    }
  }

  def getUserDetailsOption(jwtClaimOption: Option[JwtClaim]): Option[UserDetails] = jwtClaimOption match {
    case Some(jwtClaim) =>
      jwtClaim.content.fromJson[UserDetails] match {
        case Right(value) => Some(value)
        case _ => None
      }
    case _ =>
      None
  }

  def getUserDetails(userDetailsOption: Option[UserDetails]) = userDetailsOption.getOrElse(throw new RuntimeException("Missing Bearer token"))

}
