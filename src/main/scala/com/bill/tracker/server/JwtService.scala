package com.bill.tracker.server

import com.bill.tracker.model.JWTConfig
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import zio._
import zio.json._

import java.time.Clock

case class UserDetails(username: String, profileId: Long)
object UserDetails {
  implicit val decode: JsonDecoder[UserDetails] =
    DeriveJsonDecoder.gen[UserDetails]
  implicit val encode: JsonEncoder[UserDetails] =
    DeriveJsonEncoder.gen[UserDetails]
}
trait JwtService {
  def jwtEncode(userDetails: UserDetails): String
  def jwtDecode(token: String): Option[JwtClaim]

  def getContent(jwtClaimOption: Option[JwtClaim]) = ???

}

object AutowireJwtService {
  val layer: ZLayer[Any, Config.Error, JwtService] = ZLayer.fromZIO(
    ZIO.config[JWTConfig](JWTConfig.config).map { config =>
      JwtServiceLive(config.secret)
    }
  )
}

case class JwtToken(token: String)
object JwtToken {
  implicit val decoder: JsonDecoder[JwtToken] = DeriveJsonDecoder.gen[JwtToken]
  implicit val encoder: JsonEncoder[JwtToken] = DeriveJsonEncoder.gen[JwtToken]
}

case class JwtServiceLive(jwtSecret: String) extends JwtService {
  implicit val clock: Clock = Clock.systemUTC
  override def jwtEncode(userDetails: UserDetails): String = {
    import UserDetails._
    val claim = JwtClaim {
      userDetails.toJson
    }.issuedNow.expiresIn(604800)
    Jwt.encode(claim, jwtSecret, JwtAlgorithm.HS512)
  }
  override def jwtDecode(token: String): Option[JwtClaim] =
    Jwt.decode(token, jwtSecret, Seq(JwtAlgorithm.HS512)).toOption
}
