package com.example.hearts.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Client extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Client.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 815, 475);
        stage.setScene(scene);
        stage.setResizable(false);
        HelloController controller = fxmlLoader.getController();
        controller.init();
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}