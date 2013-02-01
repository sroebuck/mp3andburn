package com.proinnovate

import java.io.File
import sys.process.{ProcessLogger, Process}
import javafx.beans.property.SimpleDoubleProperty
import com.typesafe.scalalogging.slf4j.Logging

/*
 * Copyright (c) Stuart Roebuck, 2013
 */
object SplitRecordingIntoTracks extends Logging {

  val progress = new SimpleDoubleProperty(0)

  def splitRecording(inputFile: File, outputDir: File, trackSizeSeconds: Int = 120): Option[File] = {
    val mkdir = Process(Seq("mkdir", outputDir.getAbsolutePath))
    val split = Process(Seq("sox", "-S", "-V1", inputFile.getAbsolutePath, outputDir.getAbsolutePath + "/TRACK.wav",
    "trim", "0", trackSizeSeconds.toString, ":", "newfile", ":", "restart"))
    (mkdir #&& split).!(ProgressLogger) match {
      case 0 => Some(outputDir)
      case _ => None
    }
  }

  private object ProgressLogger extends ProcessLogger {

    /**
     * Regular express to extract progress information from output written to StdErr of the form:
     * "In:28.4% 00:10:41.06 [00:26:56.32] Out:28.3M [======|======] Hd:1.0 Clip:0 ".
     */
    val ProgressRE = """In:([0-9\.]+)%.+""".r

    def out(s: => String) {
      logger.debug(s)
    }

    def err(s: => String) {
      logger.debug(s)
      try {
        val ProgressRE(p) = s
        progress.set(p.toDouble / 100.0)
      } catch {
        case e:MatchError => // Ignore
      }
    }

    def buffer[T](f: => T): T = f
  }

}
