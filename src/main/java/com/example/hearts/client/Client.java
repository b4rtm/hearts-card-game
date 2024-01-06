package com.example.hearts.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;

public class Client extends Application {
    @Override
    public void start(Stage stage) {
        try {
            initControllerAndStage(stage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void initControllerAndStage(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Client.class.getResource("hello-view.fxml"));
        Pane pane = fxmlLoader.load();
        Scene scene = new Scene(pane, 815, 475);
        stage.setScene(scene);
        stage.setResizable(false);
        Controller controller = fxmlLoader.getController();
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}