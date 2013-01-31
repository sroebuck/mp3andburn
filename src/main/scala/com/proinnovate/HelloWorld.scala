package com.proinnovate

import javafx.application.Application
import javafx.scene.control.Button
import javafx.scene.layout.{FlowPane, StackPane}
import javafx.scene.Scene
import javafx.event.{EventHandler, ActionEvent}
import javafx.stage.Stage

object HelloWorld {

  def main(args: Array[String]) {
    Application.launch(classOf[HelloWorld], args: _*)
  }

}

class HelloWorld extends Application {

  def start(primaryStage: Stage) {
    primaryStage.setTitle("Hello World!")

    val btn = new Button
    btn.setText("Say 'Hello World'")
    btn.setOnAction(new EventHandler[ActionEvent] {
      def handle(event: ActionEvent) {
        System.out.println("Hello World!")
      }
    })
    val root = new FlowPane
    root.getChildren.add(btn)

    val input = new javafx.scene.control.TextField("Title")
    root.getChildren.add(input)

    primaryStage.setScene(new Scene(root, 300, 250))
    primaryStage.show()
  }

}

