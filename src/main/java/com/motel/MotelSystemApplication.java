package com.motel;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import lombok.Getter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class MotelSystemApplication extends Application {

    @Getter
    private static ConfigurableApplicationContext context;
    private Parent rootNode;

    @Override
    public void init() throws Exception {
        context = SpringApplication.run(MotelSystemApplication.class);
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/ReceptionistView.fxml"));
        fxmlLoader.setControllerFactory(context::getBean);
        rootNode = fxmlLoader.load();

        Thread.setDefaultUncaughtExceptionHandler(MotelSystemApplication::showError);
    }

    private static void showError(Thread t, Throwable e) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("An unexpected error occurred: " + e.getMessage());
            alert.showAndWait();
        });
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(rootNode));
        primaryStage.setTitle("Motel System");
        primaryStage.setResizable(false);
        primaryStage.show();
        context.publishEvent(new StageReadyEvent(primaryStage));
    }

    @Override
    public void stop() throws Exception {
        context.close();
        Platform.exit();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

    static class StageReadyEvent extends ApplicationEvent {
        public StageReadyEvent(Stage stage) {
            super(stage);
        }

        public Stage getStage() {
            return (Stage) getSource();
        }
    }
}
