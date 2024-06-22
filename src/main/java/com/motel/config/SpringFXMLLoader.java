package com.motel.config;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class SpringFXMLLoader {
    private final ConfigurableApplicationContext context;

    @Autowired
    public SpringFXMLLoader(ConfigurableApplicationContext context) {
        this.context = context;
    }

    public Parent load(String path) throws IOException {
        try (InputStream fxmlStream = SpringFXMLLoader.class.getResourceAsStream(path)) {
            FXMLLoader loader = new FXMLLoader();
            loader.setControllerFactory(context::getBean);
            loader.load(fxmlStream);
            return loader.getRoot();
        }
    }
}
