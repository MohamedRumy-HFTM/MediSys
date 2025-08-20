package ch.medisys;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Hauptklasse der MediSys Praxisverwaltungsanwendung
 * 
 * @author Mohamed Rumy
 * @version 1.0.0
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Lade Login-Fenster
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
        
        primaryStage.setTitle("MediSys - Praxisverwaltungssystem");
        primaryStage.setScene(new Scene(root, 400, 300));
        primaryStage.setResizable(false);
        primaryStage.centerOnScreen();
        
        // TODO: Icon setzen wenn vorhanden
        // primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon.png")));
        
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
