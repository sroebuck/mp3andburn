package com.proinnovate

import com.typesafe.scalalogging.slf4j.Logging
import javafx.beans.property.SimpleDoubleProperty
import java.io.File
import sys.process.{ProcessLogger, Process}

/*
 * Copyright (c) Stuart Roebuck, 2013
 */
object BurnCD extends Logging {

  val progress = new SimpleDoubleProperty(0)

  private var tracks = 0

  def burn(sourceDir: File): Boolean = {
    val files = sourceDir.listFiles.sortBy(_.getName).map(_.getAbsolutePath)
    tracks = files.length
    val command = "cdrecord"
    val mainParams = Seq("-dao", "-audio", "-pad", "-eject")
    // Add in the "-dummy" parameter if we have the "burn.forreal" config set to false.
    val commandLine = Config.burnForReal match {
      case true => Seq(command) ++ mainParams ++ files
      case false => Seq(command, "-dummy") ++ mainParams ++ files
    }
    val burn = Process(commandLine)
    progress.set(0.05)
    val success = burn.!(ThisLogger) == 0
    progress.set(1.0)
    success
  }

  private object ThisLogger extends ProcessLogger {

    /**
     * Regular express to extract progress information from output written to StdErr of the form:
     * " file.mp3           36% done, ETA 00:00:05 (batch  36% done, ETA 00:00:05)".  However the second percentage
     * erroneously displays 0% just before completion so the regular expression excludes it.
     */
    val ProgressRE = """Track\s(\d+):.+""".r

    def out(s: => String) {
      logger.debug(s)
      try {
        val ProgressRE(p) = s
        progress.set(p.toInt / (tracks + 1.0))
      } catch {
        case e:MatchError => // Ignore
      }
    }

    def err(s: => String) {
      logger.debug(s)
    }

    def buffer[T](f: => T): T = f
  }


}
