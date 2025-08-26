package ch.medisys.controller;

import ch.medisys.util.DatabaseConnection;
import ch.medisys.util.SessionManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.util.Duration;
import ch.medisys.controller.PatientFormController;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Controller für das Hauptfenster
 */
public class MainWindowController {
    
    @FXML private TabPane mainTabPane;
    @FXML private Label welcomeLabel;
    @FXML private Label todayAppointmentsLabel;
    @FXML private Label openInvoicesLabel;
    @FXML private Label totalPatientsLabel;
    @FXML private Label statusLabel;
    @FXML private Label userLabel;
    @FXML private Label timeLabel;
    @FXML private Menu adminMenu;
    
    @FXML
    public void initialize() {
        // Begrüssung setzen
        SessionManager session = SessionManager.getInstance();
        welcomeLabel.setText("Hallo " + session.getUserName() + " (" + session.getUserRole() + ")");
        userLabel.setText("Angemeldet als: " + session.getUserName());
        
        // Admin-Menü nur für Ärzte anzeigen
        adminMenu.setVisible(session.isArzt());
        
        // Uhrzeit-Anzeige
        startClock();
        
        // Dashboard-Statistiken laden
        loadDashboardStats();
    }
    
    private void startClock() {
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            LocalDateTime now = LocalDateTime.now();
            timeLabel.setText(now.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")));
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }
    
    private void loadDashboardStats() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Heutige Termine
            String query1 = "SELECT COUNT(*) FROM Termin WHERE Datum = CURRENT_DATE AND Status = 'geplant'";
            PreparedStatement stmt1 = conn.prepareStatement(query1);
            ResultSet rs1 = stmt1.executeQuery();
            if (rs1.next()) {
                todayAppointmentsLabel.setText(String.valueOf(rs1.getInt(1)));
            }
            
            // Offene Rechnungen
            String query2 = "SELECT COUNT(*) FROM Rechnung WHERE Status = 'offen'";
            PreparedStatement stmt2 = conn.prepareStatement(query2);
            ResultSet rs2 = stmt2.executeQuery();
            if (rs2.next()) {
                openInvoicesLabel.setText(String.valueOf(rs2.getInt(1)));
            }
            
            // Anzahl Patienten
            String query3 = "SELECT COUNT(*) FROM Patient";
            PreparedStatement stmt3 = conn.prepareStatement(query3);
            ResultSet rs3 = stmt3.executeQuery();
            if (rs3.next()) {
                totalPatientsLabel.setText(String.valueOf(rs3.getInt(1)));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleLogout() {
        SessionManager.getInstance().clearSession();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) mainTabPane.getScene().getWindow();
            stage.setScene(new Scene(root, 400, 300));
            stage.setTitle("MediSys - Anmeldung");
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleExit() {
        Platform.exit();
    }
    
    @FXML
    private void showPatients() {
        try {
            statusLabel.setText("Patientenverwaltung wird geladen...");

            // Prüfe ob Tab bereits offen ist
            for (Tab tab : mainTabPane.getTabs()) {
                if ("Patientenverwaltung".equals(tab.getText())) {
                    mainTabPane.getSelectionModel().select(tab);
                    return;
                }
            }

            // Lade Patientenliste
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/patient-list.fxml"));
            Parent root = loader.load();

            // Erstelle neuen Tab
            Tab patientTab = new Tab("Patientenverwaltung");
            patientTab.setContent(root);
            patientTab.setClosable(true);

            // Füge Tab hinzu und wähle es aus
            mainTabPane.getTabs().add(patientTab);
            mainTabPane.getSelectionModel().select(patientTab);

            statusLabel.setText("Patientenverwaltung geladen");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Fehler beim Laden der Patientenverwaltung");
            showError("Fehler", "Patientenverwaltung konnte nicht geladen werden: " + e.getMessage());
        }
    }
    
    @FXML
    private void newPatient() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/patient-form.fxml"));
            Parent root = loader.load();

            PatientFormController controller = loader.getController();
            controller.setNewPatientMode();

            Stage stage = new Stage();
            stage.setTitle("Neuer Patient");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Wenn gespeichert wurde und Patientenliste offen ist, aktualisiere sie
            if (controller.isSaved()) {
                for (Tab tab : mainTabPane.getTabs()) {
                    if ("Patientenverwaltung".equals(tab.getText())) {
                        // Patientenliste neu laden (falls offen)
                        showPatients();
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Fehler", "Formular konnte nicht geladen werden.");
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    @FXML
    private void showAppointments() {
        statusLabel.setText("Terminkalender wird geladen...");
        // TODO: Implementierung
    }
    
    @FXML
    private void newAppointment() {
        statusLabel.setText("Neuer Termin wird erstellt...");
        // TODO: Implementierung
    }
    
    @FXML
    private void showTreatments() {
        statusLabel.setText("Behandlungen werden geladen...");
        // TODO: Implementierung
    }
    
    @FXML
    private void showInvoices() {
        statusLabel.setText("Rechnungen werden geladen...");
        // TODO: Implementierung
    }
    
    @FXML
    private void showServices() {
        statusLabel.setText("Leistungskatalog wird geladen...");
        // TODO: Implementierung
    }
    
    @FXML
    private void showEmployees() {
        statusLabel.setText("Mitarbeiterverwaltung wird geladen...");
        // TODO: Implementierung
    }
    
    @FXML
    private void showMedications() {
        statusLabel.setText("Medikamentenverwaltung wird geladen...");
        // TODO: Implementierung
    }
    
    @FXML
    private void showAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Über MediSys");
        alert.setHeaderText("MediSys - Praxisverwaltungssystem");
        alert.setContentText("Version 1.0.0\n\n" +
                "Entwickelt von: Mohamed Rumy\n" +
                "Klasse: BBIN24.2a\n" +
                "© 2025 - Transfer-Projekt");
        alert.showAndWait();
    }
}
