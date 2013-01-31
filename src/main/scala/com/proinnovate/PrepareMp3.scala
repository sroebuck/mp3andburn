package com.proinnovate

import java.io.File
import com.typesafe.scalalogging.slf4j.Logging

/*
 * Copyright (c) Stuart Roebuck, 2013
 */
object PrepareMp3 extends Logging {

  def prepareMp3() {

    logger.warn("--> prepareMp3")

    val inputDir = new File("/Volumes/NO NAME/YPE/SONGS")
    val outputFile = new File("/Users/sroebuck/Desktop/file.mp3")

    val files = inputDir.listFiles

    files.sortWith{ case (x: File,y: File) => x.lastModified > y.lastModified }.headOption match {
      case Some(latestFile) =>
        logger.warn(s"latestFile = $latestFile")

        Normalize.normalise(latestFile, outputFile)
      case None =>
        logger.warn("No file found to normalize!")
    }



  }

}
