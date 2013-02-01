package com.proinnovate

import java.io.File
import sys.process.{ProcessLogger, Process}
import com.typesafe.scalalogging.slf4j.Logging
import javafx.beans.property.SimpleDoubleProperty

/**
 * Methods for issuing commands to the Unix command line utility "Normalize".
 *
 * Note that the object is spelt "Normalize" to match the unix command line command and the method is spelt
 * "normalise" because that's what it does!
 */
object Normalize extends Logging {

  val progress = new SimpleDoubleProperty(0)

  def normalise(inputFile: File, outputFile: File): Option[File] = {
    val copy = Process(Seq("cp", inputFile.getAbsolutePath, outputFile.getAbsolutePath))
    val normalize = Process(Seq("normalize", outputFile.getAbsolutePath))
    (copy #&& normalize).!(ProgressLogger) match {
      case 0 => Some(outputFile)
      case _ => None
    }

  }

  private object ProgressLogger extends ProcessLogger {

    /**
     * Regular express to extract progress information from output written to StdErr of the form:
     * " file.mp3           36% done, ETA 00:00:05 (batch  36% done, ETA 00:00:05)".  However the second percentage
     * erroneously displays 0% just before completion so the regular expression excludes it.
     */
    val ProgressRE = """.+\s(\d+)%.+\(batch.+""".r

    def out(s: => String) {
      logger.debug(s)
    }

    def err(s: => String) {
      logger.debug(s)
      try {
        val ProgressRE(p) = s
        progress.set(p.toInt / 100.0)
      } catch {
        case e:MatchError => // Ignore
      }
    }

    def buffer[T](f: => T): T = f
  }

}
