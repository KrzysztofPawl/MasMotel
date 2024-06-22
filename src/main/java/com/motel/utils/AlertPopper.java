package com.motel.utils;


import javafx.application.Platform;
import javafx.scene.control.Alert;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AlertPopper {
    public void showErrorAlert(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
