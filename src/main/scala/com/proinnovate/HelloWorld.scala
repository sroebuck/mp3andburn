package com.proinnovate

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import javafx.scene.control.{TextField, Button}
import com.typesafe.scalalogging.slf4j.Logging
import javafx.event.EventHandler
import javafx.scene.input.KeyEvent

object HelloWorld {

  def main(args: Array[String]) {
    Application.launch(classOf[HelloWorld], args: _*)
  }

}


class HelloWorld extends Application with Logging {

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

    val titleField:TextField = root.lookup("#titleField").asInstanceOf[TextField]
    val authorField:TextField = root.lookup("#authorField").asInstanceOf[TextField]
    val seriesField:TextField = root.lookup("#seriesField").asInstanceOf[TextField]
    val commentField:TextField = root.lookup("#commentField").asInstanceOf[TextField]

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

  }

  private def setMaxFieldLength(field: TextField, length: Int) {
    field.addEventFilter(KeyEvent.KEY_TYPED, new EventHandler[KeyEvent] {
      def handle(keyEvent: KeyEvent) {
        val text = field.getText
        if (text.length >= length) keyEvent.consume()
      }
    })
  }


}

