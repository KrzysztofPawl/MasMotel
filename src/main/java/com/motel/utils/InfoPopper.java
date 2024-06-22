package com.motel.utils;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import lombok.experimental.UtilityClass;

@UtilityClass
public class InfoPopper {
    public void showInfo(String headerText, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setHeaderText(headerText);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
