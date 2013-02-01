package com.proinnovate

import com.typesafe.config.ConfigFactory
import util.Try
import java.io.File

/*
 * Copyright (c) Stuart Roebuck, 2013
 */
object Config {

  private val config = ConfigFactory.load()

  val burnForReal = Try(config.getBoolean("burn.forreal")).getOrElse(true)

  val userHome = new File(System.getProperty("user.home"))

}
