package com.example.algo_project;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        URL url = GrahamScan.class.getResource("scene3.fxml");
        FXMLLoader loader = new FXMLLoader(url);
        BorderPane root = loader.load();

        GrahamScan controller = loader.getController();
        controller.initialize(); 

        Scene scene = new Scene(root, 900, 700);

        stage.setTitle("Convex Hull Jarvis March");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
