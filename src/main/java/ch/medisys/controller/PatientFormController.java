package ch.medisys.controller;

import ch.medisys.dao.PatientDAO;
import ch.medisys.model.Patient;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;

public class PatientFormController {
    
    @FXML private TextField vornameField;
    @FXML private TextField nachnameField;
    @FXML private DatePicker geburtsdatumPicker;
    @FXML private ComboBox<String> geschlechtCombo;
    @FXML private TextField strasseField;
    @FXML private TextField plzField;
    @FXML private TextField ortField;
    @FXML private TextField telefonField;
    @FXML private TextField emailField;
    @FXML private TextField versicherungsnummerField;
    @FXML private TextField krankenkasseField;
    @FXML private TextField notfallkontaktNameField;
    @FXML private TextField notfallkontaktTelefonField;
    
    private Patient currentPatient;
    private boolean editMode = false;
    private boolean saved = false;
    
    @FXML
    public void initialize() {
        geschlechtCombo.setItems(FXCollections.observableArrayList("M", "W", "D"));
        
        // Geburtsdatum-Validator
        geburtsdatumPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date.isAfter(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffc0c0;");
                }
            }
        });
    }
    
    public void setNewPatientMode() {
        this.editMode = false;
        this.currentPatient = new Patient();
    }
    
    public void setEditMode(Patient patient) {
        this.editMode = true;
        this.currentPatient = patient;
        loadPatientData();
    }
    
    private void loadPatientData() {
        vornameField.setText(currentPatient.getVorname());
        nachnameField.setText(currentPatient.getNachname());
        geburtsdatumPicker.setValue(currentPatient.getGeburtsdatum());
        geschlechtCombo.setValue(currentPatient.getGeschlecht());
        strasseField.setText(currentPatient.getStrasse());
        plzField.setText(currentPatient.getPlz());
        ortField.setText(currentPatient.getOrt());
        telefonField.setText(currentPatient.getTelefon());
        emailField.setText(currentPatient.getEmail());
        versicherungsnummerField.setText(currentPatient.getVersicherungsnummer());
        krankenkasseField.setText(currentPatient.getKrankenkasse());
        notfallkontaktNameField.setText(currentPatient.getNotfallkontaktName());
        notfallkontaktTelefonField.setText(currentPatient.getNotfallkontaktTelefon());
    }
    
    @FXML
    private void handleSave() {
        if (!validateInput()) {
            return;
        }
        
        updatePatientFromForm();
        
        try {
            if (editMode) {
                PatientDAO.updatePatient(currentPatient);
                saved = true;
                showInfo("Erfolgreich", "Patient wurde aktualisiert.");
            } else {
                int newId = PatientDAO.insertPatient(currentPatient);
                if (newId > 0) {
                    currentPatient.setPatientID(newId);
                    saved = true;
                    showInfo("Erfolgreich", "Patient wurde erstellt.");
                }
            }
            closeWindow();
        } catch (SQLException e) {
            showError("Speichern fehlgeschlagen", e.getMessage());
        }
    }
    
    @FXML
    private void handleCancel() {
        closeWindow();
    }
    
    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();
        
        if (vornameField.getText().trim().isEmpty()) {
            errors.append("- Vorname ist erforderlich\n");
        }
        if (nachnameField.getText().trim().isEmpty()) {
            errors.append("- Nachname ist erforderlich\n");
        }
        if (geburtsdatumPicker.getValue() == null) {
            errors.append("- Geburtsdatum ist erforderlich\n");
        }
        if (geschlechtCombo.getValue() == null) {
            errors.append("- Geschlecht ist erforderlich\n");
        }
        if (versicherungsnummerField.getText().trim().isEmpty()) {
            errors.append("- Versicherungsnummer ist erforderlich\n");
        }
        if (krankenkasseField.getText().trim().isEmpty()) {
            errors.append("- Krankenkasse ist erforderlich\n");
        }
        
        // Email-Validierung wenn angegeben
        String email = emailField.getText().trim();
        if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errors.append("- Ungültiges E-Mail-Format\n");
        }
        
        if (errors.length() > 0) {
            showError("Validierungsfehler", errors.toString());
            return false;
        }
        
        return true;
    }
    
    private void updatePatientFromForm() {
        currentPatient.setVorname(vornameField.getText().trim());
        currentPatient.setNachname(nachnameField.getText().trim());
        currentPatient.setGeburtsdatum(geburtsdatumPicker.getValue());
        currentPatient.setGeschlecht(geschlechtCombo.getValue());
        currentPatient.setStrasse(strasseField.getText().trim());
        currentPatient.setPlz(plzField.getText().trim());
        currentPatient.setOrt(ortField.getText().trim());
        currentPatient.setTelefon(telefonField.getText().trim());
        currentPatient.setEmail(emailField.getText().trim());
        currentPatient.setVersicherungsnummer(versicherungsnummerField.getText().trim());
        currentPatient.setKrankenkasse(krankenkasseField.getText().trim());
        currentPatient.setNotfallkontaktName(notfallkontaktNameField.getText().trim());
        currentPatient.setNotfallkontaktTelefon(notfallkontaktTelefonField.getText().trim());
    }
    
    private void closeWindow() {
        Stage stage = (Stage) vornameField.getScene().getWindow();
        stage.close();
    }
    
    public boolean isSaved() {
        return saved;
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
