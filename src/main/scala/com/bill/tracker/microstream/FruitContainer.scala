package com.bill.tracker.microstream

import java.util.Map
import java.util.concurrent.ConcurrentHashMap

class FruitContainer {

  private val fruits: Map[String, Fruit] =
    new ConcurrentHashMap[String, Fruit]()

  def getFruits(): Map[String, Fruit] = fruits

}
