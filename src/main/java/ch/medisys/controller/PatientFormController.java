package ch.medisys.controller;

import ch.medisys.dao.PatientDAO;
import ch.medisys.model.Patient;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * Controller für das Patienten-Formular (Neu/Bearbeiten)
 */
public class PatientFormController {
    
    @FXML private TextField vornameField;
    @FXML private TextField nachnameField;
    @FXML private DatePicker geburtsdatumPicker;
    @FXML private RadioButton geschlechtM;
    @FXML private RadioButton geschlechtW;
    @FXML private RadioButton geschlechtD;
    @FXML private ToggleGroup geschlechtGroup;
    
    @FXML private TextField strasseField;
    @FXML private TextField plzField;
    @FXML private TextField ortField;
    @FXML private TextField telefonField;
    @FXML private TextField emailField;
    
    @FXML private TextField versicherungsnummerField;
    @FXML private ComboBox<String> krankenkasseCombo;
    
    @FXML private TextField notfallkontaktNameField;
    @FXML private TextField notfallkontaktTelefonField;
    
    private Patient currentPatient;
    private boolean editMode = false;
    private boolean saved = false;
    
    @FXML
    public void initialize() {
        // Krankenkassen-Liste
        List<String> krankenkassen = Arrays.asList(
            "CSS Versicherung",
            "Helsana",
            "Swica",
            "Sanitas",
            "Concordia",
            "Visana",
            "Groupe Mutuel",
            "Atupri",
            "KPT",
            "Sympany"
        );
        krankenkasseCombo.setItems(FXCollections.observableArrayList(krankenkassen));
        
        // Geburtsdatum auf max. heute beschränken
        geburtsdatumPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(date.isAfter(LocalDate.now()));
            }
        });
        
        // PLZ-Validierung (nur Zahlen)
        plzField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                plzField.setText(oldVal);
            }
        });
        
        // Telefon-Formatierung
        telefonField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                formatTelefon();
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
        
        // Geschlecht
        String geschlecht = currentPatient.getGeschlecht();
        if ("M".equals(geschlecht)) {
            geschlechtM.setSelected(true);
        } else if ("W".equals(geschlecht)) {
            geschlechtW.setSelected(true);
        } else if ("D".equals(geschlecht)) {
            geschlechtD.setSelected(true);
        }
        
        strasseField.setText(currentPatient.getStrasse());
        plzField.setText(currentPatient.getPlz());
        ortField.setText(currentPatient.getOrt());
        telefonField.setText(currentPatient.getTelefon());
        emailField.setText(currentPatient.getEmail());
        
        versicherungsnummerField.setText(currentPatient.getVersicherungsnummer());
        krankenkasseCombo.setValue(currentPatient.getKrankenkasse());
        
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
                if (PatientDAO.updatePatient(currentPatient)) {
                    saved = true;
                    showInfo("Erfolgreich", "Patient wurde aktualisiert.");
                    closeWindow();
                }
            } else {
                int newId = PatientDAO.insertPatient(currentPatient);
                if (newId > 0) {
                    currentPatient.setPatientID(newId);
                    saved = true;
                    showInfo("Erfolgreich", "Neuer Patient wurde angelegt.");
                    closeWindow();
                }
            }
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
        
        // Pflichtfelder prüfen
        if (vornameField.getText().trim().isEmpty()) {
            errors.append("- Vorname ist erforderlich\n");
        }
        if (nachnameField.getText().trim().isEmpty()) {
            errors.append("- Nachname ist erforderlich\n");
        }
        if (geburtsdatumPicker.getValue() == null) {
            errors.append("- Geburtsdatum ist erforderlich\n");
        }
        if (geschlechtGroup.getSelectedToggle() == null) {
            errors.append("- Geschlecht ist erforderlich\n");
        }
        if (versicherungsnummerField.getText().trim().isEmpty()) {
            errors.append("- Versicherungsnummer ist erforderlich\n");
        }
        if (krankenkasseCombo.getValue() == null || krankenkasseCombo.getValue().trim().isEmpty()) {
            errors.append("- Krankenkasse ist erforderlich\n");
        }
        
        // E-Mail-Format prüfen (falls angegeben)
        String email = emailField.getText().trim();
        if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errors.append("- E-Mail-Format ist ungültig\n");
        }
        
        // E-Mail-Eindeutigkeit prüfen
        if (!email.isEmpty()) {
            try {
                Integer excludeId = editMode ? currentPatient.getPatientID() : null;
                if (PatientDAO.emailExists(email, excludeId)) {
                    errors.append("- Diese E-Mail-Adresse wird bereits verwendet\n");
                }
            } catch (SQLException e) {
                errors.append("- E-Mail-Prüfung fehlgeschlagen\n");
            }
        }
        
        // PLZ-Länge prüfen (Schweiz: 4 Ziffern)
        String plz = plzField.getText().trim();
        if (!plz.isEmpty() && plz.length() != 4) {
            errors.append("- PLZ muss 4 Ziffern haben\n");
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
        
        // Geschlecht
        if (geschlechtM.isSelected()) {
            currentPatient.setGeschlecht("M");
        } else if (geschlechtW.isSelected()) {
            currentPatient.setGeschlecht("W");
        } else if (geschlechtD.isSelected()) {
            currentPatient.setGeschlecht("D");
        }
        
        currentPatient.setStrasse(strasseField.getText().trim());
        currentPatient.setPlz(plzField.getText().trim());
        currentPatient.setOrt(ortField.getText().trim());
        currentPatient.setTelefon(telefonField.getText().trim());
        currentPatient.setEmail(emailField.getText().trim());
        
        currentPatient.setVersicherungsnummer(versicherungsnummerField.getText().trim());
        currentPatient.setKrankenkasse(krankenkasseCombo.getValue());
        
        currentPatient.setNotfallkontaktName(notfallkontaktNameField.getText().trim());
        currentPatient.setNotfallkontaktTelefon(notfallkontaktTelefonField.getText().trim());
    }
    
    private void formatTelefon() {
        String tel = telefonField.getText().replaceAll("[^0-9]", "");
        if (tel.length() == 10) {
            // Format: 041-123-4567
            tel = tel.substring(0, 3) + "-" + tel.substring(3, 6) + "-" + tel.substring(6);
            telefonField.setText(tel);
        }
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
