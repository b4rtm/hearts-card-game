package com.example.hearts.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * The main class for the Hearts game client application.
 */
public class Client extends Application {

    /**
     * The entry point for the application.
     *
     * @param stage The primary stage for this application.
     */
    @Override
    public void start(Stage stage) {
        try {
            initControllerAndStage(stage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initializes the controller and stage for the application.
     *
     * @param stage The primary stage for this application.
     * @throws IOException If an error occurs while loading the FXML file.
     */
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