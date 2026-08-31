package com.sicaproject;

import com.sicaproject.sica.shared.infrastructure.config.CompositionRoot;
import com.sicaproject.sica.ui.RefreshScheduler;
import com.sicaproject.sica.ui.SceneManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.util.Duration;

public class SicaApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        SceneManager.setPrimaryStage(primaryStage);

        CompositionRoot root = CompositionRoot.getInstance();
        RefreshScheduler.init(
            root.visitaService(),
            root.personaService(),
            root.empresaService(),
            root.rbacService(),
            root.authService(),
            root.auditoriaService()
        );
        RefreshScheduler.getInstance().startRefresh(Duration.seconds(5));

        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/com/sicaproject/sica/ui/login.fxml")
        );
        Parent view = loader.load();

        Scene scene = new Scene(view, 1280, 720);
        scene.getStylesheets().add(
            getClass().getResource("/com/sicaproject/sica/ui/styles.css").toExternalForm()
        );

        primaryStage.setTitle("SICA — Sistema Integrado de Control de Acceso");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1024);
        primaryStage.setMinHeight(600);
        primaryStage.centerOnScreen();
        primaryStage.show();

        primaryStage.setOnCloseRequest(e -> RefreshScheduler.getInstance().stop());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
