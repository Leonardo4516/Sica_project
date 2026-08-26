package com.sicaproject;

import com.sicaproject.sica.ui.SceneManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class SicaApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        SceneManager.setPrimaryStage(primaryStage);

        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/com/sicaproject/sica/ui/login.fxml")
        );
        Parent root = loader.load();

        Scene scene = new Scene(root, 1280, 720);
        scene.getStylesheets().add(
            getClass().getResource("/com/sicaproject/sica/ui/styles.css").toExternalForm()
        );

        primaryStage.setTitle("SICA — Sistema Integrado de Control de Acceso");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1024);
        primaryStage.setMinHeight(600);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
