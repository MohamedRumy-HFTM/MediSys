package ch.medisys.controller;

import at.favre.lib.crypto.bcrypt.BCrypt;
import ch.medisys.util.DatabaseConnection;
import ch.medisys.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Controller für die Login-Funktionalität
 */
public class LoginController {
    
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    
    @FXML
    public void initialize() {
        // Enter-Taste im Passwortfeld löst Login aus
        passwordField.setOnAction(e -> handleLogin());
        
        // Debug: Test-Datenbank-Verbindung
        testDatabaseConnection();
    }
    
    private void testDatabaseConnection() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            System.out.println("=== Datenbank-Test ===");
            System.out.println("Verbindung erfolgreich: " + !conn.isClosed());
            
            // Liste alle Mitarbeiter auf
            String query = "SELECT MitarbeiterID, Email, Rolle, LEFT(Passwort, 30) as PWD FROM Mitarbeiter";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            
            System.out.println("Vorhandene Mitarbeiter:");
            while (rs.next()) {
                System.out.println("  ID: " + rs.getInt("MitarbeiterID") + 
                                 ", Email: " + rs.getString("Email") + 
                                 ", Rolle: " + rs.getString("Rolle") +
                                 ", PWD-Start: " + rs.getString("PWD"));
            }
            System.out.println("===================");
        } catch (Exception e) {
            System.err.println("Datenbank-Test fehlgeschlagen: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    public void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        
        System.out.println("\n=== Login-Versuch ===");
        System.out.println("Email: '" + email + "'");
        System.out.println("Passwort-Länge: " + password.length());
        
        // Validierung
        if (email.isEmpty() || password.isEmpty()) {
            showError("Bitte E-Mail und Passwort eingeben.");
            return;
        }
        
        // Authentifizierung
        if (authenticateUser(email, password)) {
            System.out.println("Login erfolgreich!");
            loadMainWindow();
        } else {
            System.out.println("Login fehlgeschlagen!");
            showError("Ungültige Anmeldedaten.");
        }
    }
    
    private boolean authenticateUser(String email, String password) {
        String query = "SELECT MitarbeiterID, Vorname, Nachname, Rolle, Passwort FROM Mitarbeiter WHERE Email = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, email);
            System.out.println("SQL-Query: " + query);
            System.out.println("Parameter: " + email);
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                System.out.println("Benutzer gefunden!");
                System.out.println("  Name: " + rs.getString("Vorname") + " " + rs.getString("Nachname"));
                System.out.println("  Rolle: " + rs.getString("Rolle"));
                
                String hashedPassword = rs.getString("Passwort");
                System.out.println("  Hash aus DB (erste 30 Zeichen): " + hashedPassword.substring(0, Math.min(30, hashedPassword.length())));
                System.out.println("  Hash-Länge: " + hashedPassword.length());
                
                // Test mit einem neuen Hash für "admin123"
                String testHash = BCrypt.withDefaults().hashToString(12, "admin123".toCharArray());
                System.out.println("  Test-Hash für 'admin123': " + testHash.substring(0, 30));
                
                // Verifiziere Passwort mit BCrypt
                System.out.println("Versuche Passwort-Verifikation...");
                BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), hashedPassword);
                System.out.println("  Verifikation erfolgreich: " + result.verified);
                
                if (result.verified) {
                    // Speichere Benutzerinformationen in Session
                    SessionManager.getInstance().setUserId(rs.getInt("MitarbeiterID"));
                    SessionManager.getInstance().setUserName(rs.getString("Vorname") + " " + rs.getString("Nachname"));
                    SessionManager.getInstance().setUserRole(rs.getString("Rolle"));
                    SessionManager.getInstance().setUserEmail(email);
                    
                    return true;
                }
            } else {
                System.out.println("Kein Benutzer mit Email '" + email + "' gefunden!");
            }
        } catch (Exception e) {
            System.err.println("Fehler bei der Authentifizierung:");
            e.printStackTrace();
            showError("Datenbankfehler: " + e.getMessage());
        }
        
        return false;
    }
    
    private void loadMainWindow() {
        try {
            // Lade Hauptfenster
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main-window.fxml"));
            Parent root = loader.load();
            
            // Neue Scene erstellen
            Scene scene = new Scene(root, 1200, 800);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            
            // Stage holen und konfigurieren
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setTitle("MediSys - " + SessionManager.getInstance().getUserName());
            stage.setScene(scene);
            stage.setResizable(true);
            stage.centerOnScreen();
            
        } catch (Exception e) {
            e.printStackTrace();
            showError("Fehler beim Laden des Hauptfensters.");
        }
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
