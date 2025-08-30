package ch.medisys.controller;

import ch.medisys.dao.PatientDAO;
import ch.medisys.dao.TerminDAO;
import ch.medisys.model.Patient;
import ch.medisys.model.Termin;
import ch.medisys.util.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * Controller für das Termin-Formular
 */
public class AppointmentFormController {
    
    @FXML private ComboBox<Patient> patientCombo;
    @FXML private ComboBox<MitarbeiterItem> mitarbeiterCombo;
    @FXML private DatePicker datumPicker;
    @FXML private ComboBox<String> uhrzeitCombo;
    @FXML private ComboBox<Integer> dauerCombo;
    @FXML private ComboBox<String> terminartCombo;
    @FXML private ComboBox<String> statusCombo;
    @FXML private TextArea notizenArea;
    @FXML private Label availabilityLabel;
    @FXML private FlowPane availableSlotsPane;
    
    private Termin currentTermin;
    private boolean editMode = false;
    private boolean saved = false;
    private ObservableList<Patient> allPatients;
    
    // Hilfsklasse für Mitarbeiter
    private static class MitarbeiterItem {
        private int id;
        private String name;
        
        public MitarbeiterItem(int id, String name) {
            this.id = id;
            this.name = name;
        }
        
        public int getId() { return id; }
        
        @Override
        public String toString() { return name; }
    }
    
    @FXML
    public void initialize() {
        setupComboBoxes();
        loadPatienten();
        loadMitarbeiter();
    }
    
    private void setupComboBoxes() {
        // Zeitslots
        ObservableList<String> timeSlots = FXCollections.observableArrayList();
        for (int hour = 8; hour < 18; hour++) {
            for (int minute = 0; minute < 60; minute += 15) {
                if (hour == 17 && minute > 30) break;
                timeSlots.add(String.format("%02d:%02d", hour, minute));
            }
        }
        uhrzeitCombo.setItems(timeSlots);
        uhrzeitCombo.setValue("08:00");
        
        // Dauer
        dauerCombo.setItems(FXCollections.observableArrayList(15, 30, 45, 60, 90, 120));
        dauerCombo.setValue(30);
        
        // Terminarten
        terminartCombo.setItems(FXCollections.observableArrayList(
            "Erstuntersuchung", "Nachkontrolle", "Notfall", 
            "Behandlung", "Beratung", "Impfung"
        ));
        
        // Status
        statusCombo.setItems(FXCollections.observableArrayList("geplant", "durchgeführt", "abgesagt"));
        statusCombo.setValue("geplant");
        
        // Patient ComboBox Setup - WICHTIG: Editable ausschalten für Stabilität
        patientCombo.setEditable(false);
        patientCombo.setConverter(new StringConverter<Patient>() {
            @Override
            public String toString(Patient patient) {
                if (patient == null) return "";
                return patient.getNachname() + ", " + patient.getVorname() + 
                       " (*" + patient.getGeburtsdatum() + ")";
            }
            
            @Override
            public Patient fromString(String string) {
                if (string == null || string.isEmpty()) return null;
                
                for (Patient p : allPatients) {
                    String display = toString(p);
                    if (display.equals(string)) {
                        return p;
                    }
                }
                return null;
            }
        });
    }
    
    private void loadPatienten() {
        try {
            allPatients = PatientDAO.getAllPatients();
            patientCombo.setItems(allPatients);
        } catch (SQLException e) {
            showError("Fehler", "Patienten konnten nicht geladen werden.");
        }
    }
    
    private void loadMitarbeiter() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT MitarbeiterID, CONCAT(Nachname, ', ', Vorname, ' (', Rolle, ')') as Name " +
                          "FROM Mitarbeiter ORDER BY Nachname";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            ObservableList<MitarbeiterItem> mitarbeiterList = FXCollections.observableArrayList();
            while (rs.next()) {
                mitarbeiterList.add(new MitarbeiterItem(
                    rs.getInt("MitarbeiterID"),
                    rs.getString("Name")
                ));
            }
            mitarbeiterCombo.setItems(mitarbeiterList);
            
            // Wähle ersten Mitarbeiter
            if (!mitarbeiterList.isEmpty()) {
                mitarbeiterCombo.setValue(mitarbeiterList.get(0));
            }
            
        } catch (SQLException e) {
            showError("Fehler", "Mitarbeiter konnten nicht geladen werden.");
        }
    }
    
    public void setNewAppointmentMode(LocalDate defaultDate) {
        this.editMode = false;
        this.currentTermin = new Termin();
        datumPicker.setValue(defaultDate != null ? defaultDate : LocalDate.now());
    }
    
    public void setEditMode(Termin termin) {
        this.editMode = true;
        this.currentTermin = termin;
        loadTerminData();
    }

    public void setDefaultTime(LocalTime time) {
        if (time != null) {
            uhrzeitCombo.setValue(time.format(DateTimeFormatter.ofPattern("HH:mm")));
        }
    }
    
    private void loadTerminData() {
        // Patient auswählen
        for (Patient p : allPatients) {
            if (p.getPatientID() == currentTermin.getPatientID()) {
                patientCombo.setValue(p);
                break;
            }
        }
        
        // Mitarbeiter auswählen
        for (MitarbeiterItem m : mitarbeiterCombo.getItems()) {
            if (m.getId() == currentTermin.getMitarbeiterID()) {
                mitarbeiterCombo.setValue(m);
                break;
            }
        }
        
        datumPicker.setValue(currentTermin.getDatum());
        
        if (currentTermin.getUhrzeit() != null) {
            uhrzeitCombo.setValue(currentTermin.getUhrzeit().format(DateTimeFormatter.ofPattern("HH:mm")));
        }
        
        dauerCombo.setValue(currentTermin.getDauer());
        terminartCombo.setValue(currentTermin.getTerminart());
        statusCombo.setValue(currentTermin.getStatus());
        notizenArea.setText(currentTermin.getNotizen());
    }
    
    @FXML
    private void handleCheckAvailability() {
        if (mitarbeiterCombo.getValue() == null || datumPicker.getValue() == null) {
            showError("Validierung", "Bitte wählen Sie Mitarbeiter und Datum aus.");
            return;
        }
        
        // Clear previous slots aber behalte Labels
        availableSlotsPane.getChildren().clear();
        
        try {
            MitarbeiterItem selected = mitarbeiterCombo.getValue();
            LocalDate datum = datumPicker.getValue();
            int dauer = dauerCombo.getValue();
            
            List<LocalTime> availableSlots = TerminDAO.getAvailableTimeSlots(
                selected.getId(), datum, dauer
            );
            
            if (availableSlots.isEmpty()) {
                availabilityLabel.setText("Keine freien Termine verfügbar");
                availabilityLabel.setTextFill(Color.RED);
            } else {
                availabilityLabel.setText(availableSlots.size() + " freie Zeitfenster:");
                availabilityLabel.setTextFill(Color.GREEN);
                
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                for (LocalTime slot : availableSlots) {
                    Button slotButton = new Button(formatter.format(slot));
                    slotButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                    slotButton.setOnAction(e -> {
                        uhrzeitCombo.setValue(formatter.format(slot));
                        availabilityLabel.setText("✓ Zeit verfügbar");
                    });
                    availableSlotsPane.getChildren().add(slotButton);
                }
            }
        } catch (SQLException e) {
            showError("Fehler", "Verfügbarkeit konnte nicht geprüft werden.");
        }
    }
    
    @FXML
    private void handleSave() {
        if (!validateInput()) {
            return;
        }
        
        updateTerminFromForm();
        
        try {
            if (editMode) {
                if (TerminDAO.updateTermin(currentTermin)) {
                    saved = true;
                    showInfo("Erfolgreich", "Termin wurde aktualisiert.");
                    closeWindow();
                }
            } else {
                int newId = TerminDAO.insertTermin(currentTermin);
                if (newId > 0) {
                    currentTermin.setTerminID(newId);
                    saved = true;
                    showInfo("Erfolgreich", "Termin wurde erstellt.");
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
        
        if (patientCombo.getValue() == null) {
            errors.append("- Patient ist erforderlich\n");
        }
        if (mitarbeiterCombo.getValue() == null) {
            errors.append("- Mitarbeiter ist erforderlich\n");
        }
        if (datumPicker.getValue() == null) {
            errors.append("- Datum ist erforderlich\n");
        }
        if (uhrzeitCombo.getValue() == null || uhrzeitCombo.getValue().isEmpty()) {
            errors.append("- Uhrzeit ist erforderlich\n");
        }
        if (terminartCombo.getValue() == null) {
            errors.append("- Terminart ist erforderlich\n");
        }
        
        if (errors.length() > 0) {
            showError("Validierungsfehler", errors.toString());
            return false;
        }
        
        return true;
    }
    
    private void updateTerminFromForm() {
        Patient selectedPatient = patientCombo.getValue();
        if (selectedPatient != null) {
            currentTermin.setPatientID(selectedPatient.getPatientID());
            currentTermin.setPatientName(selectedPatient.getVollerName());
        }
        
        MitarbeiterItem selectedMitarbeiter = mitarbeiterCombo.getValue();
        if (selectedMitarbeiter != null) {
            currentTermin.setMitarbeiterID(selectedMitarbeiter.getId());
            currentTermin.setMitarbeiterName(selectedMitarbeiter.toString());
        }
        
        currentTermin.setDatum(datumPicker.getValue());
        currentTermin.setUhrzeit(LocalTime.parse(uhrzeitCombo.getValue()));
        currentTermin.setDauer(dauerCombo.getValue());
        currentTermin.setTerminart(terminartCombo.getValue());
        currentTermin.setStatus(statusCombo.getValue());
        currentTermin.setNotizen(notizenArea.getText());
    }
    
    private void closeWindow() {
        Stage stage = (Stage) patientCombo.getScene().getWindow();
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
