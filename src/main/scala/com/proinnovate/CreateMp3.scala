package com.proinnovate

import java.io.File
import com.typesafe.scalalogging.slf4j.Logging
import sys.process.{Process, ProcessLogger}
import javafx.beans.property.SimpleDoubleProperty

object CreateMp3 extends Logging {

  val progress = new SimpleDoubleProperty(0)

  def createMp3(sourceFile: File, outputFile: File, title: String, author: String, album: String,
                 year: String, comment: String): Option[File] = {
    val commandLine = Seq("lame", "-af", "--tt", title, "--ta", author, "--tl", album, "--ty", year, "--tc", comment,
      "--tg", "Speech", sourceFile.getAbsolutePath, outputFile.getAbsolutePath)
    Process(commandLine).!(ThisLogger) match {
      case 0 => Some(outputFile)
      case _ => None
    }

  }

  private object ThisLogger extends ProcessLogger {

    /**
     * Regular express to extract progress information from output written to StdErr of the form:
     * "26500/86700  (31%)|    0:07/    0:25|    0:08/    0:26|   89.244x|    0:18".
     */
    val ProgressRE = """.+\(\s?(\d+)%\).+""".r

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
