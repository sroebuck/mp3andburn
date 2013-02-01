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
    // FIXME: Dummy run
    val commandLine = Seq("cdrecord", "-dummy", "-dao", "-audio", "-pad", "-eject") ++ files
    logger.warn(s"command = $commandLine")
    val burn = Process(commandLine)
    logger.warn("set burn")
    progress.set(0.05)
    val success = burn.!(ProgressLogger) == 0
    progress.set(1.0)
    success
  }

  private object ProgressLogger extends ProcessLogger {

    /**
     * Regular express to extract progress information from output written to StdErr of the form:
     * " file.mp3           36% done, ETA 00:00:05 (batch  36% done, ETA 00:00:05)".  However the second percentage
     * erroneously displays 0% just before completion so the regular expression excludes it.
     */
    val ProgressRE = """Track\s(\d+):.+""".r

    def out(s: => String) {
      try {
        val ProgressRE(p) = s
        progress.set(p.toInt / (tracks + 1.0))
      } catch {
        case e:MatchError => // Ignore
      }
    }

    def err(s: => String) {
      logger.warn(s)
    }

    def buffer[T](f: => T): T = f
  }


}
