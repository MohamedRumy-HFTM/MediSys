package ch.medisys.controller;

import ch.medisys.dao.PatientDAO;
import ch.medisys.model.Patient;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Controller für die Patientenliste
 */
public class PatientListController {
    
    @FXML private TextField searchField;
    @FXML private TableView<Patient> patientTable;
    @FXML private TableColumn<Patient, Integer> idColumn;
    @FXML private TableColumn<Patient, String> nachnameColumn;
    @FXML private TableColumn<Patient, String> vornameColumn;
    @FXML private TableColumn<Patient, LocalDate> geburtsdatumColumn;
    @FXML private TableColumn<Patient, Integer> alterColumn;
    @FXML private TableColumn<Patient, String> geschlechtColumn;
    @FXML private TableColumn<Patient, String> telefonColumn;
    @FXML private TableColumn<Patient, String> emailColumn;
    @FXML private TableColumn<Patient, String> krankenkasseColumn;
    @FXML private TableColumn<Patient, Void> aktionenColumn;
    @FXML private Label statusLabel;
    
    private ObservableList<Patient> patientList;
    
    @FXML
    public void initialize() {
        setupTableColumns();
        loadPatients();
        
        // Enter-Taste im Suchfeld löst Suche aus
        searchField.setOnAction(e -> handleSearch());
        
        // Doppelklick öffnet Patient
        patientTable.setRowFactory(tv -> {
            TableRow<Patient> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleViewPatient(row.getItem());
                }
            });
            return row;
        });
    }
    
    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("patientID"));
        nachnameColumn.setCellValueFactory(new PropertyValueFactory<>("nachname"));
        vornameColumn.setCellValueFactory(new PropertyValueFactory<>("vorname"));
        geburtsdatumColumn.setCellValueFactory(new PropertyValueFactory<>("geburtsdatum"));
        alterColumn.setCellValueFactory(new PropertyValueFactory<>("alter"));
        geschlechtColumn.setCellValueFactory(new PropertyValueFactory<>("geschlecht"));
        telefonColumn.setCellValueFactory(new PropertyValueFactory<>("telefon"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        krankenkasseColumn.setCellValueFactory(new PropertyValueFactory<>("krankenkasse"));
        
        // Formatierung für Geburtsdatum
        geburtsdatumColumn.setCellFactory(column -> new TableCell<Patient, LocalDate>() {
            private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(formatter.format(date));
                }
            }
        });
        
        // Aktionen-Spalte mit Buttons
        setupActionsColumn();
    }
    
    private void setupActionsColumn() {
        Callback<TableColumn<Patient, Void>, TableCell<Patient, Void>> cellFactory = 
            new Callback<TableColumn<Patient, Void>, TableCell<Patient, Void>>() {
            @Override
            public TableCell<Patient, Void> call(final TableColumn<Patient, Void> param) {
                final TableCell<Patient, Void> cell = new TableCell<Patient, Void>() {
                    
                    private final Button viewBtn = new Button("👁");
                    private final Button editBtn = new Button("✏");
                    private final Button deleteBtn = new Button("🗑");
                    
                    {
                        viewBtn.setTooltip(new Tooltip("Details anzeigen"));
                        editBtn.setTooltip(new Tooltip("Bearbeiten"));
                        deleteBtn.setTooltip(new Tooltip("Löschen"));
                        
                        viewBtn.setOnAction(event -> {
                            Patient patient = getTableView().getItems().get(getIndex());
                            handleViewPatient(patient);
                        });
                        
                        editBtn.setOnAction(event -> {
                            Patient patient = getTableView().getItems().get(getIndex());
                            handleEditPatient(patient);
                        });
                        
                        deleteBtn.setOnAction(event -> {
                            Patient patient = getTableView().getItems().get(getIndex());
                            handleDeletePatient(patient);
                        });
                    }
                    
                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            HBox buttons = new HBox(5);
                            buttons.setAlignment(Pos.CENTER);
                            buttons.getChildren().addAll(viewBtn, editBtn, deleteBtn);
                            setGraphic(buttons);
                        }
                    }
                };
                return cell;
            }
        };
        
        aktionenColumn.setCellFactory(cellFactory);
    }
    
    private void loadPatients() {
        try {
            patientList = PatientDAO.getAllPatients();
            patientTable.setItems(patientList);
            updateStatusLabel();
        } catch (SQLException e) {
            showError("Fehler beim Laden der Patienten", e.getMessage());
        }
    }
    
    @FXML
    private void handleSearch() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            loadPatients();
            return;
        }
        
        try {
            patientList = PatientDAO.searchPatients(searchTerm);
            patientTable.setItems(patientList);
            updateStatusLabel();
        } catch (SQLException e) {
            showError("Fehler bei der Suche", e.getMessage());
        }
    }
    
    @FXML
    private void handleShowAll() {
        searchField.clear();
        loadPatients();
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
            
            // Nach dem Schließen des Dialogs die Liste aktualisieren
            if (controller.isSaved()) {
                loadPatients();
            }
        } catch (IOException e) {
            showError("Fehler", "Formular konnte nicht geladen werden.");
        }
    }
    
    private void handleViewPatient(Patient patient) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/patient-detail.fxml"));
            Parent root = loader.load();
            
            PatientDetailController controller = loader.getController();
            controller.setPatient(patient);
            
            Stage stage = new Stage();
            stage.setTitle("Patient: " + patient.getVollerName());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            showError("Fehler", "Details konnten nicht geladen werden.");
        }
    }
    
    private void handleEditPatient(Patient patient) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/patient-form.fxml"));
            Parent root = loader.load();
            
            PatientFormController controller = loader.getController();
            controller.setEditMode(patient);
            
            Stage stage = new Stage();
            stage.setTitle("Patient bearbeiten: " + patient.getVollerName());
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
    
    private void handleDeletePatient(Patient patient) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Patient löschen");
        alert.setHeaderText("Möchten Sie diesen Patienten wirklich löschen?");
        alert.setContentText(patient.getVollerName() + "\n\nHinweis: Patienten mit Terminen oder Behandlungen können nicht gelöscht werden.");
        
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
           try {
               if (PatientDAO.deletePatient(patient.getPatientID())) {
                   patientList.remove(patient);
                   updateStatusLabel();
                   showInfo("Erfolgreich", "Patient wurde gelöscht.");
               }
           } catch (SQLException e) {
               showError("Löschen nicht möglich", e.getMessage());
           }
       }
   }
   
   @FXML
   private void handleRefresh() {
       loadPatients();
   }
   
   private void updateStatusLabel() {
       statusLabel.setText(patientList.size() + " Patient(en) geladen");
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
