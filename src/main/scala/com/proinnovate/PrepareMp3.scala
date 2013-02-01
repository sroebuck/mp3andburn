package com.proinnovate

import java.io.File
import com.typesafe.scalalogging.slf4j.Logging

/*
 * Copyright (c) Stuart Roebuck, 2013
 */
object PrepareMp3 extends Logging {

  def prepareMp3() {
    val inputDir = new File("/Volumes/NO NAME/YPE/SONGS")
    val outputFile = new File("/Users/sroebuck/Desktop/file.mp3")
    val splitTrackDir = new File("/Users/sroebuck/Desktop/audiocd")

    val files = inputDir.listFiles

    val latestFileOpt = files.sortWith{ case (x: File,y: File) => x.lastModified > y.lastModified }.headOption
    for {
      latestFile <- latestFileOpt
      normFile <- Normalize.normalise(latestFile, outputFile)
      splitDir <- SplitRecordingIntoTracks.splitRecording(normFile, splitTrackDir)
    } BurnCD.burn(splitDir)


  }

}
