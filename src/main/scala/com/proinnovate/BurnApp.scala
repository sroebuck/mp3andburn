package com.proinnovate

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import javafx.scene.control.{ProgressIndicator, TextField, Button}
import com.typesafe.scalalogging.slf4j.Logging
import javafx.event.{ActionEvent, EventHandler}
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Pane
import concurrent.Future
import concurrent.ExecutionContext.Implicits.global
import java.io.File
import org.joda.time.{LocalDateTime, LocalDate}
import util.Try

object BurnApp {

  def main(args: Array[String]) {
    Application.launch(classOf[BurnApp], args: _*)
  }

}


class BurnApp extends Application with Logging {

  def start(stage: Stage) {
    // Text.fontSmoothingType(FontSmoothingType.LCD) - I think this requires JavaFX2.2
    val resource = getClass.getResource("mp3andburn.fxml")
    val root = FXMLLoader.load[Parent](resource)

    setupControllers(root)

    stage.setTitle("mp3 and burn")
    val scene = new Scene(root)
    stage.setScene(scene)
    stage.show()
  }

  private def setupControllers(root: Parent) {
    val burnButton:Button = root.lookup("#burnButton").asInstanceOf[Button]
    burnButton.setDisable(true)

    val titleField = root.lookup("#titleField").asInstanceOf[TextField]
    val authorField = root.lookup("#authorField").asInstanceOf[TextField]
    val seriesField = root.lookup("#seriesField").asInstanceOf[TextField]
    val commentField = root.lookup("#commentField").asInstanceOf[TextField]

    val seriesPrefix = "St Mungo's"

    setMaxFieldLength(titleField, 30)
    setMaxFieldLength(authorField, 30)
    setMaxFieldLength(seriesField, 30 - (seriesPrefix.length + 1))
    setMaxFieldLength(commentField, 30)

    // Require a title and author field before enabling the Burn button.
    object burnButtonEnablingEventHandler extends EventHandler[KeyEvent] {
      def handle(keyEvent: KeyEvent) {
        if (titleField.getText.length > 0 && authorField.getText.length > 2) burnButton.setDisable(false)
        else burnButton.setDisable(true)
      }
    }
    Seq(titleField, authorField).map(_.addEventHandler(KeyEvent.KEY_TYPED, burnButtonEnablingEventHandler))

    setupBurnButtonHandler(root, burnButton)

    // Connect progress displays with workers
    val normaliseProgress = root.lookup("#normaliseProgress").asInstanceOf[ProgressIndicator]
    val splitProgress = root.lookup("#splitProgress").asInstanceOf[ProgressIndicator]
    val burnProgress = root.lookup("#burnProgress").asInstanceOf[ProgressIndicator]
    val mp3Progress = root.lookup("#mp3Progress").asInstanceOf[ProgressIndicator]
    normaliseProgress.progressProperty.bind(Normalize.progress)
    splitProgress.progressProperty.bind(SplitRecordingIntoTracks.progress)
    burnProgress.progressProperty.bind(BurnCD.progress)
    mp3Progress.progressProperty.bind(CreateMp3.progress)
  }

  private def setupBurnButtonHandler(root: Parent, burnButton: Button) {
    burnButton.setOnAction(new EventHandler[ActionEvent]() {
      def handle(actionEvent: ActionEvent) {

        val pane1 = root.lookup("#pane1").asInstanceOf[Pane]
        val pane2 = root.lookup("#pane2").asInstanceOf[Pane]
        pane1.setVisible(false)
        pane2.setVisible(true)

        // If it exists, remove the "Untitled CD.fpbf" file on the desktop.
        val untitledCd = new File(Config.userHome, "Desktop/Untitled CD.fpbf")
        if (untitledCd.isDirectory) {
          Try(untitledCd.delete())
        }

        val title = root.lookup("#titleField").asInstanceOf[TextField].getText
        val author = root.lookup("#authorField").asInstanceOf[TextField].getText
        val series = "St Mungo's:" + root.lookup("#seriesField").asInstanceOf[TextField].getText
        val year = new LocalDate().getYear.toString
        val comment = root.lookup("#commentField").asInstanceOf[TextField].getText
        val successFuture = prepareMp3(title, author, series, year, comment)
      }
    })
  }

  private def setMaxFieldLength(field: TextField, length: Int) {
    field.addEventFilter(KeyEvent.KEY_TYPED, new EventHandler[KeyEvent] {
      def handle(keyEvent: KeyEvent) {
        val text = field.getText
        if (text.length >= length) keyEvent.consume()
      }
    })
  }

  private def prepareMp3(title: String, author: String, album: String, year: String, comment: String): Future[Boolean] = {
    // FIXME: Find the SONGS directory by checking all potential volumes from an appropriately named file.
    val inputDir = new File("/Volumes/NO NAME/YPE/SONGS")
    val outputFile = new File("/Users/sroebuck/Desktop/file.mp3")
    val splitTrackDir = new File("/Users/sroebuck/Desktop/audiocd")

    val files = inputDir.listFiles

    val latestFileOpt = files.sortWith{ case (x: File,y: File) => x.lastModified > y.lastModified }.headOption
    val finalMp3File = latestFileOpt.map {
      latestFile: File =>
        val modifiedDate = new LocalDateTime(latestFileOpt.get.lastModified)
        modifiedDate.getYear
        val year = f"${modifiedDate.getYear}%04d"
        val month = f"${modifiedDate.getMonthOfYear}%02d"
        val date = f"${modifiedDate.getDayOfMonth}%02d"
        val ampm = if (modifiedDate.getHourOfDay < 15) "am" else "pm"
        val fullname = s"$year-$month-$date-$ampm.mp3"
        new File(Config.userHome, "Desktop/" + fullname)
    }.getOrElse(new File(Config.userHome, "Desktop/today.mp3"))
    val normFileFuture = Future { latestFileOpt.flatMap(Normalize.normalise(_, outputFile)) }
    val splitDirFuture = normFileFuture.map(_.flatMap(SplitRecordingIntoTracks.splitRecording(_, splitTrackDir)))
    val burnCdOkayFuture = splitDirFuture.map(_.map {
      cdDir =>
        BurnCD.burn(cdDir)
        Try(cdDir.listFiles().foreach(_.delete()))
        Try(cdDir.delete())
    })
    val createMp3OkayFuture = normFileFuture.map(_.flatMap(CreateMp3.createMp3(_,finalMp3File, title, author, album, year, comment)))
    val success = for {
      burnCdOkay <- burnCdOkayFuture
      createMp3Okay <- createMp3OkayFuture
    } yield {
      Try(outputFile.delete())
      burnCdOkay.isDefined && createMp3Okay.isDefined
    }
    success
  }


}

