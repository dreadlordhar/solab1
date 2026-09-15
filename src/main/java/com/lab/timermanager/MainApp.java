package com.lab.timermanager;

import com.lab.timermanager.controller.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * Punctul de intrare al aplicatiei. Incarca fereastra principala din FXML,
 * ataseaza CSS-ul si se asigura ca toate timerele sunt oprite la iesire.
 */
public class MainApp extends Application {

    private MainController controller;

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/lab/timermanager/view/MainView.fxml"));
        Region root = loader.load();
        controller = loader.getController();

        Scene scene = new Scene(root, 1024, 720);
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());

        primaryStage.setTitle("Timer Manager");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(760);
        primaryStage.setMinHeight(560);
        primaryStage.show();
    }

    @Override
    public void stop() {
        if (controller != null) {
            controller.shutdown();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
