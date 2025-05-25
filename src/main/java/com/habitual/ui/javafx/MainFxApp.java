package com.habitual.ui.javafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;

import com.habitual.db.DatabaseConnector;

public class MainFxApp extends Application {

    private static Stage primaryStage; // Keep a reference to the primary stage

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage; // Initialize the static stage
        // Initialize database (and connection) when the FX app starts
        DatabaseConnector.initializeDatabase();

        // Load the initial view (e.g., LoginView)
        loadScene("LoginView.fxml", "Habitual - Login");
    }

    public static void loadScene(String fxmlFile, String title) throws IOException {
        String fxmlPath = "/com/habitual/ui/javafx/" + fxmlFile;
        URL fxmlUrl = MainFxApp.class.getResource(fxmlPath);
        if (fxmlUrl == null) {
            throw new IOException("Cannot load FXML file: " + fxmlPath + ". Check path and ensure it's in resources.");
        }
        Parent root = FXMLLoader.load(fxmlUrl);
        Scene scene = new Scene(root);
        URL cssUrl = MainFxApp.class.getResource("/com/habitual/ui/javafx/styles.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.out.println("Warning: styles.css not found.");
        }
        primaryStage.setTitle(title);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    // Call this when the application is closing to ensure DB connection is released
    @Override
    public void stop() throws Exception {
        DatabaseConnector.closeConnection();
        System.out.println("Application closing, database connection closed.");
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
