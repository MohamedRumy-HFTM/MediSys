package ch.medisys.controller;

import ch.medisys.model.Patient;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

/**
 * Controller für die Patienten-Detail-Ansicht
 */
public class PatientDetailController {
    
    @FXML private Label nameLabel;
    @FXML private Label patientIdLabel;
    @FXML private Label geburtsdatumLabel;
    @FXML private Label alterLabel;
    @FXML private Label geschlechtLabel;
    @FXML private Label adresseLabel;
    @FXML private Label telefonLabel;
    @FXML private Hyperlink emailLink;
    @FXML private Label versicherungsnummerLabel;
    @FXML private Label krankenkasseLabel;
    @FXML private Label notfallkontaktNameLabel;
    @FXML private Label notfallkontaktTelefonLabel;
    
    private Patient currentPatient;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    
    public void setPatient(Patient patient) {
        this.currentPatient = patient;
        loadPatientData();
    }
    
    private void loadPatientData() {
        nameLabel.setText(currentPatient.getVollerName());
        patientIdLabel.setText("Patient-Nr: " + currentPatient.getPatientID());
        
        if (currentPatient.getGeburtsdatum() != null) {
            geburtsdatumLabel.setText(dateFormatter.format(currentPatient.getGeburtsdatum()));
            alterLabel.setText(currentPatient.getAlter() + " Jahre");
        }
        
        // Geschlecht ausschreiben
        String geschlecht = currentPatient.getGeschlecht();
        if ("M".equals(geschlecht)) {
            geschlechtLabel.setText("Männlich");
        } else if ("W".equals(geschlecht)) {
            geschlechtLabel.setText("Weiblich");
        } else if ("D".equals(geschlecht)) {
            geschlechtLabel.setText("Divers");
        } else {
            geschlechtLabel.setText("-");
        }
        
        // Adresse
        String adresse = "";
        if (currentPatient.getStrasse() != null && !currentPatient.getStrasse().isEmpty()) {
            adresse = currentPatient.getStrasse();
        }
        if (currentPatient.getPlz() != null && !currentPatient.getPlz().isEmpty()) {
            if (!adresse.isEmpty()) adresse += ", ";
            adresse += currentPatient.getPlz();
        }
        if (currentPatient.getOrt() != null && !currentPatient.getOrt().isEmpty()) {
            if (!adresse.isEmpty() && currentPatient.getPlz() != null) adresse += " ";
            adresse += currentPatient.getOrt();
        }
        adresseLabel.setText(adresse.isEmpty() ? "-" : adresse);
        
        telefonLabel.setText(currentPatient.getTelefon() != null ? currentPatient.getTelefon() : "-");
        
        // E-Mail als Hyperlink
        if (currentPatient.getEmail() != null && !currentPatient.getEmail().isEmpty()) {
            emailLink.setText(currentPatient.getEmail());
            emailLink.setOnAction(e -> {
                // Öffne E-Mail-Programm
                try {
                    java.awt.Desktop.getDesktop().mail(new java.net.URI("mailto:" + currentPatient.getEmail()));
                } catch (Exception ex) {
                    showError("E-Mail konnte nicht geöffnet werden", ex.getMessage());
                }
            });
        } else {
            emailLink.setText("-");
            emailLink.setDisable(true);
        }
        
        versicherungsnummerLabel.setText(currentPatient.getVersicherungsnummer() != null ? 
            currentPatient.getVersicherungsnummer() : "-");
        krankenkasseLabel.setText(currentPatient.getKrankenkasse() != null ? 
            currentPatient.getKrankenkasse() : "-");
        
        notfallkontaktNameLabel.setText(currentPatient.getNotfallkontaktName() != null ? 
           currentPatient.getNotfallkontaktName() : "-");
       notfallkontaktTelefonLabel.setText(currentPatient.getNotfallkontaktTelefon() != null ? 
           currentPatient.getNotfallkontaktTelefon() : "-");
   }
   
   @FXML
   private void handleEdit() {
       try {
           FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/patient-form.fxml"));
           Parent root = loader.load();
           
           PatientFormController controller = loader.getController();
           controller.setEditMode(currentPatient);
           
           Stage stage = new Stage();
           stage.setTitle("Patient bearbeiten: " + currentPatient.getVollerName());
           stage.setScene(new Scene(root));
           stage.initModality(Modality.APPLICATION_MODAL);
           stage.showAndWait();
           
           if (controller.isSaved()) {
               // Aktualisiere die Anzeige
               loadPatientData();
           }
       } catch (IOException e) {
           showError("Fehler", "Formular konnte nicht geladen werden.");
       }
   }
   
   @FXML
   private void handleShowAppointments() {
       showInfo("Termine", "Terminanzeige für Patient " + currentPatient.getVollerName() + 
               "\n\nDiese Funktion wird in einem späteren Sprint implementiert.");
   }
   
   @FXML
   private void handleShowTreatments() {
       showInfo("Behandlungen", "Behandlungshistorie für Patient " + currentPatient.getVollerName() + 
               "\n\nDiese Funktion wird in einem späteren Sprint implementiert.");
   }
   
   @FXML
   private void handleShowDocuments() {
       showInfo("Dokumente", "Dokumentenverwaltung für Patient " + currentPatient.getVollerName() + 
               "\n\nDiese Funktion wird in einem späteren Sprint implementiert.");
   }
   
   @FXML
   private void handleClose() {
       Stage stage = (Stage) nameLabel.getScene().getWindow();
       stage.close();
   }
   
   private void showError(String title, String content) {
       Alert alert = new Alert(Alert.AlertType.ERROR);
       alert.setTitle(title);
       alert.setHeaderText(null);
       alert.setContentText(content);
       alert.showAndWait();
   }
   
   private void showInfo(String title, String content) {
       Alert alert = new Alert(Alert.AlertType.INFORMATION);
       alert.setTitle(title);
       alert.setHeaderText(null);
       alert.setContentText(content);
       alert.showAndWait();
   }
}
