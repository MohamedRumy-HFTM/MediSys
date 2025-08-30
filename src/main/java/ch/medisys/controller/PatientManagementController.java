package ch.medisys.controller;

import ch.medisys.dao.PatientDAO;
import ch.medisys.model.Patient;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PatientManagementController {
    
    @FXML private TableView<Patient> patientTable;
    @FXML private TableColumn<Patient, Integer> patientIdColumn;
    @FXML private TableColumn<Patient, String> nachnameColumn;
    @FXML private TableColumn<Patient, String> vornameColumn;
    @FXML private TableColumn<Patient, LocalDate> geburtsdatumColumn;
    @FXML private TableColumn<Patient, String> geschlechtColumn;
    @FXML private TableColumn<Patient, String> telefonColumn;
    @FXML private TableColumn<Patient, String> emailColumn;
    @FXML private TableColumn<Patient, String> krankenkasseColumn;
    
    @FXML private TextField searchField;
    @FXML private CheckBox activeOnlyCheckbox;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private VBox detailPanel;
    @FXML private VBox detailsContent;
    @FXML private Label statusLabel;
    @FXML private Label patientCountLabel;
    
    private ObservableList<Patient> allPatients;
    private FilteredList<Patient> filteredPatients;
    
    @FXML
    public void initialize() {
        setupTableColumns();
        setupTableSelection();
        setupSearch();
        loadPatients();
    }
    
    private void setupTableColumns() {
        patientIdColumn.setCellValueFactory(new PropertyValueFactory<>("patientID"));
        nachnameColumn.setCellValueFactory(new PropertyValueFactory<>("nachname"));
        vornameColumn.setCellValueFactory(new PropertyValueFactory<>("vorname"));
        geburtsdatumColumn.setCellValueFactory(new PropertyValueFactory<>("geburtsdatum"));
        geschlechtColumn.setCellValueFactory(new PropertyValueFactory<>("geschlecht"));
        telefonColumn.setCellValueFactory(new PropertyValueFactory<>("telefon"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        krankenkasseColumn.setCellValueFactory(new PropertyValueFactory<>("krankenkasse"));
        
        // Formatierung für Geburtsdatum
        geburtsdatumColumn.setCellFactory(column -> new TableCell<Patient, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
                }
            }
        });
    }
    
    private void setupTableSelection() {
        patientTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            editButton.setDisable(!hasSelection);
            deleteButton.setDisable(!hasSelection);
            
            if (hasSelection) {
                showPatientDetails(newSelection);
            } else {
                hidePatientDetails();
            }
        });
        
        // Doppelklick zum Bearbeiten
        patientTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && patientTable.getSelectionModel().getSelectedItem() != null) {
                handleEditPatient();
            }
        });
    }
    
    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterPatients();
        });
        
        activeOnlyCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            filterPatients();
        });
    }
    
    private void loadPatients() {
        try {
            allPatients = PatientDAO.getAllPatients();
            filteredPatients = new FilteredList<>(allPatients);
            patientTable.setItems(filteredPatients);
            updatePatientCount();
            statusLabel.setText("Patienten geladen");
        } catch (SQLException e) {
            showError("Fehler", "Patienten konnten nicht geladen werden: " + e.getMessage());
        }
    }
    
    private void filterPatients() {
        if (filteredPatients == null) return;
        
        String searchText = searchField.getText().toLowerCase();
        
        filteredPatients.setPredicate(patient -> {
            // Aktiv-Filter (hier könnten Sie ein aktiv-Flag im Patient-Model hinzufügen)
            // Für jetzt nehmen wir alle als aktiv
            
            // Text-Suche
            if (searchText.isEmpty()) {
                return true;
            }
            
            return patient.getNachname().toLowerCase().contains(searchText) ||
                   patient.getVorname().toLowerCase().contains(searchText) ||
                   patient.getVersicherungsnummer().toLowerCase().contains(searchText) ||
                   (patient.getEmail() != null && patient.getEmail().toLowerCase().contains(searchText)) ||
                   (patient.getTelefon() != null && patient.getTelefon().contains(searchText));
        });
        
        updatePatientCount();
    }
    
    private void showPatientDetails(Patient patient) {
        detailPanel.setVisible(true);
        detailPanel.setManaged(true);
        
        detailsContent.getChildren().clear();
        
        // Details anzeigen
        addDetailRow("ID:", String.valueOf(patient.getPatientID()));
        addDetailRow("Name:", patient.getVollerName());
        addDetailRow("Geburtsdatum:", patient.getGeburtsdatum().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        addDetailRow("Alter:", patient.getAlter() + " Jahre");
        addDetailRow("Geschlecht:", patient.getGeschlecht());
        
        detailsContent.getChildren().add(new Separator());
        
        addDetailRow("Strasse:", patient.getStrasse());
        addDetailRow("PLZ/Ort:", patient.getPlz() + " " + patient.getOrt());
        addDetailRow("Telefon:", patient.getTelefon());
        addDetailRow("Email:", patient.getEmail());
        
        detailsContent.getChildren().add(new Separator());
        
        addDetailRow("Versicherung:", patient.getVersicherungsnummer());
        addDetailRow("Krankenkasse:", patient.getKrankenkasse());
        
        if (patient.getNotfallkontaktName() != null && !patient.getNotfallkontaktName().isEmpty()) {
            detailsContent.getChildren().add(new Separator());
            addDetailRow("Notfallkontakt:", patient.getNotfallkontaktName());
            addDetailRow("Notfall-Tel:", patient.getNotfallkontaktTelefon());
        }
    }
    
    private void addDetailRow(String label, String value) {
        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-font-weight: bold;");
        Label valueNode = new Label(value != null ? value : "-");
        VBox row = new VBox(2, labelNode, valueNode);
        detailsContent.getChildren().add(row);
    }
    
    private void hidePatientDetails() {
        detailPanel.setVisible(false);
        detailPanel.setManaged(false);
    }
    
    @FXML
    private void handleNewPatient() {
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
            
            if (controller.isSaved()) {
                loadPatients();
            }
        } catch (IOException e) {
            showError("Fehler", "Formular konnte nicht geladen werden.");
        }
    }
    
    @FXML
    private void handleEditPatient() {
        Patient selected = patientTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/patient-form.fxml"));
            Parent root = loader.load();
            
            PatientFormController controller = loader.getController();
            controller.setEditMode(selected);
            
            Stage stage = new Stage();
            stage.setTitle("Patient bearbeiten");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
            if (controller.isSaved()) {
                loadPatients();
            }
        } catch (IOException e) {
            showError("Fehler", "Formular konnte nicht geladen werden.");
        }
    }
    
    @FXML
    private void handleDeletePatient() {
        Patient selected = patientTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Patient löschen");
        alert.setHeaderText("Möchten Sie diesen Patienten wirklich löschen?");
        alert.setContentText("Patient: " + selected.getVollerName() + "\n" +
                           "Geburtsdatum: " + selected.getGeburtsdatum());
        
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                PatientDAO.deletePatient(selected.getPatientID());
                loadPatients();
                showInfo("Erfolgreich", "Patient wurde gelöscht.");
            } catch (SQLException e) {
                showError("Fehler", "Patient konnte nicht gelöscht werden: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleSearch() {
        filterPatients();
    }
    
    @FXML
    private void handleResetSearch() {
        searchField.clear();
        activeOnlyCheckbox.setSelected(true);
    }
    
    @FXML
    private void handleExport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Patientenliste exportieren");
        fileChooser.setInitialFileName("patienten_" + LocalDate.now() + ".csv");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Dateien", "*.csv")
        );
        
        File file = fileChooser.showSaveDialog(patientTable.getScene().getWindow());
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println("ID,Nachname,Vorname,Geburtsdatum,Geschlecht,Telefon,Email,Krankenkasse");
                
                for (Patient p : filteredPatients) {
                    writer.printf("%d,\"%s\",\"%s\",%s,%s,\"%s\",\"%s\",\"%s\"%n",
                        p.getPatientID(),
                        p.getNachname(),
                        p.getVorname(),
                        p.getGeburtsdatum(),
                        p.getGeschlecht(),
                        p.getTelefon() != null ? p.getTelefon() : "",
                        p.getEmail() != null ? p.getEmail() : "",
                        p.getKrankenkasse()
                    );
                }
                showInfo("Export erfolgreich", "Patientenliste wurde exportiert.");
            } catch (IOException e) {
                showError("Export fehlgeschlagen", e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleShowHistory() {
        Patient selected = patientTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showInfo("Historie", "Patientenhistorie für " + selected.getVollerName() + 
                    "\n(Diese Funktion wird in einem späteren Release implementiert)");
        }
    }
    
    @FXML
    private void handleShowAppointments() {
        Patient selected = patientTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // TODO: Termine des Patienten anzeigen
            showInfo("Termine", "Termine für " + selected.getVollerName());
        }
    }
    
    @FXML
    private void handleShowTreatments() {
        Patient selected = patientTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // TODO: Behandlungen des Patienten anzeigen
            showInfo("Behandlungen", "Behandlungen für " + selected.getVollerName());
        }
    }
    
    private void updatePatientCount() {
        if (filteredPatients != null) {
            patientCountLabel.setText(filteredPatients.size() + " von " + allPatients.size() + " Patienten");
        }
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
