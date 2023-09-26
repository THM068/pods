package com.bill.tracker.util

import java.nio.charset.StandardCharsets.US_ASCII
import zio.crypto.hash.{Hash, HashAlgorithm}

object BCryptEncoder {

  def encode(password: String) = for {
    digest <- Hash.hash[HashAlgorithm.SHA256](
      password,
      US_ASCII
    )
  } yield digest

//  def matches(rawValue: String, hashedValue: String) = for {
//    verified <- Hash.verify[HashAlgorithm.SHA256](
//      m = rawValue,
//      digest = MessageDigest(hashedValue),
//      charset = US_ASCII
//    )
//  } yield verified

}
