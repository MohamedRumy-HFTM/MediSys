package ch.medisys.controller;

import ch.medisys.dao.TerminDAO;
import ch.medisys.model.Termin;
import ch.medisys.util.SessionManager;
import ch.medisys.util.DatabaseConnection;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.PrintWriter;
import java.util.Optional;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

import java.io.IOException;
import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller für die Kalenderansicht
 */
public class AppointmentCalendarController {

    @FXML private Label monthYearLabel;
    @FXML private GridPane calendarGrid;
    @FXML private ToggleButton monthViewButton;
    @FXML private ToggleButton weekViewButton;
    @FXML private ToggleButton dayViewButton;
    @FXML private ComboBox<String> mitarbeiterFilter;
    @FXML private ComboBox<String> terminartFilter;
    @FXML private CheckBox showCancelledCheckbox;
    @FXML private VBox dayDetailPanel;
    @FXML private VBox dayAppointmentsList;
    @FXML private Label statusLabel;
    @FXML private Label appointmentCountLabel;

    private LocalDate currentDate;
    private YearMonth currentYearMonth;
    private ObservableList<Termin> allTermine;
    private DateTimeFormatter monthYearFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.GERMAN);

    @FXML
    public void initialize() {
        currentDate = LocalDate.now();
        currentYearMonth = YearMonth.from(currentDate);

        setupViewToggle();
        setupFilters();
        loadMitarbeiterFilter();
        loadTermine();
        buildMonthView();
    }

    private void setupViewToggle() {
        monthViewButton.setOnAction(e -> buildMonthView());
        weekViewButton.setOnAction(e -> buildWeekView());
        dayViewButton.setOnAction(e -> buildDayView());
    }

    private void setupFilters() {
        // Terminarten
        List<String> terminarten = Arrays.asList(
                "Alle", "Erstuntersuchung", "Nachkontrolle", "Notfall",
                "Behandlung", "Beratung", "Impfung"
        );
        terminartFilter.getItems().addAll(terminarten);
        terminartFilter.setValue("Alle");
        terminartFilter.setOnAction(e -> applyFilters());

        // Mitarbeiter initial setup
        mitarbeiterFilter.getItems().add("Alle");
        mitarbeiterFilter.setValue("Alle");
        mitarbeiterFilter.setOnAction(e -> applyFilters());

        showCancelledCheckbox.setOnAction(e -> applyFilters());
    }

    private void loadMitarbeiterFilter() {
        System.out.println("=== Lade Mitarbeiter-Filter ===");
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT DISTINCT CONCAT(m.Nachname, ', ', m.Vorname) as Name " +
                    "FROM Mitarbeiter m " +
                    "ORDER BY m.Nachname";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            mitarbeiterFilter.getItems().clear();
            mitarbeiterFilter.getItems().add("Alle");

            while (rs.next()) {
                String name = rs.getString("Name");
                mitarbeiterFilter.getItems().add(name);
                System.out.println("Mitarbeiter hinzugefügt: " + name);
            }

            System.out.println("Filter Items: " + mitarbeiterFilter.getItems());
            mitarbeiterFilter.setValue("Alle");

        } catch (SQLException e) {
            System.err.println("SQL-Fehler: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("=== Mitarbeiter-Filter Ende ===");
    }

    private void loadTermine() {
        try {
            LocalDate startDate = currentYearMonth.atDay(1);
            LocalDate endDate = currentYearMonth.atEndOfMonth();
            allTermine = TerminDAO.getTermineByDateRange(startDate, endDate);
            updateAppointmentCount();
        } catch (SQLException e) {
            showError("Fehler", "Termine konnten nicht geladen werden: " + e.getMessage());
        }
    }

    private void buildMonthView() {
        dayDetailPanel.setVisible(false);
        dayDetailPanel.setManaged(false);

        calendarGrid.getChildren().clear();
        calendarGrid.getColumnConstraints().clear();
        calendarGrid.getRowConstraints().clear();

        // Header mit Wochentagen
        String[] weekDays = {"Mo", "Di", "Mi", "Do", "Fr", "Sa", "So"};
        for (int i = 0; i < 7; i++) {
            Label dayLabel = new Label(weekDays[i]);
            dayLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
            dayLabel.setMaxWidth(Double.MAX_VALUE);
            dayLabel.setAlignment(Pos.CENTER);
            dayLabel.setStyle("-fx-background-color: #e0e0e0; -fx-padding: 5;");
            calendarGrid.add(dayLabel, i, 0);

            ColumnConstraints col = new ColumnConstraints();
            col.setHgrow(Priority.ALWAYS);
            col.setMinWidth(100);
            calendarGrid.getColumnConstraints().add(col);
        }

        // Kalender-Tage
        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int dayOfWeekOfFirst = firstOfMonth.getDayOfWeek().getValue();
        LocalDate calendarDate = firstOfMonth.minusDays(dayOfWeekOfFirst - 1);

        monthYearLabel.setText(currentYearMonth.format(monthYearFormatter));

        for (int week = 1; week <= 6; week++) {
            for (int day = 0; day < 7; day++) {
                VBox dayBox = createDayBox(calendarDate, currentYearMonth);
                calendarGrid.add(dayBox, day, week);
                calendarDate = calendarDate.plusDays(1);
            }

            RowConstraints row = new RowConstraints();
            row.setVgrow(Priority.ALWAYS);
            row.setMinHeight(80);
            calendarGrid.getRowConstraints().add(row);
        }
    }

    private VBox createDayBox(LocalDate date, YearMonth yearMonth) {
        VBox dayBox = new VBox(2);
        dayBox.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1; -fx-background-color: white;");
        dayBox.setPadding(new Insets(2));

        // Datum-Label
        Label dateLabel = new Label(String.valueOf(date.getDayOfMonth()));
        dateLabel.setFont(Font.font("System", FontWeight.BOLD, 11));

        if (!date.getMonth().equals(yearMonth.getMonth())) {
            dateLabel.setTextFill(Color.GRAY);
            dayBox.setStyle(dayBox.getStyle() + "-fx-background-color: #f5f5f5;");
        }

        if (date.equals(LocalDate.now())) {
            dayBox.setStyle(dayBox.getStyle() + "-fx-background-color: #e3f2fd;");
        }

        dayBox.getChildren().add(dateLabel);

        // Termine für diesen Tag
        List<Termin> dayTermine = getFilteredTermineForDate(date);
        for (Termin termin : dayTermine.stream().limit(3).collect(Collectors.toList())) {
            Label terminLabel = new Label(termin.getZeitspanne() + " " + termin.getPatientName());
            terminLabel.setFont(Font.font("System", 9));
            terminLabel.setMaxWidth(Double.MAX_VALUE);
            terminLabel.setStyle("-fx-background-color: " + termin.getTerminFarbe() +
                    "; -fx-text-fill: white; -fx-padding: 1 3; -fx-background-radius: 2;");
            dayBox.getChildren().add(terminLabel);
        }

        if (dayTermine.size() > 3) {
            Label moreLabel = new Label("+" + (dayTermine.size() - 3) + " weitere");
            moreLabel.setFont(Font.font("System", 9));
            moreLabel.setTextFill(Color.GRAY);
            dayBox.getChildren().add(moreLabel);
        }

        // Klick-Event
        dayBox.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                currentDate = date;
                dayViewButton.setSelected(true);
                buildDayView();
            }
        });

        return dayBox;
    }

    private void buildWeekView() {
        dayDetailPanel.setVisible(false);
        dayDetailPanel.setManaged(false);

        calendarGrid.getChildren().clear();
        calendarGrid.getColumnConstraints().clear();
        calendarGrid.getRowConstraints().clear();

        // Berechne Wochenbeginn (Montag)
        LocalDate weekStart = currentDate.with(DayOfWeek.MONDAY);
        monthYearLabel.setText("Woche vom " + weekStart.format(DateTimeFormatter.ofPattern("d. MMMM yyyy", Locale.GERMAN)));

        // Header mit Wochentagen
        calendarGrid.add(new Label("Zeit"), 0, 0);
        for (int i = 0; i < 7; i++) {
            LocalDate day = weekStart.plusDays(i);
            Label dayLabel = new Label(day.format(DateTimeFormatter.ofPattern("EEE dd.MM", Locale.GERMAN)));
            dayLabel.setStyle("-fx-font-weight: bold; -fx-alignment: center;");
            if (day.equals(LocalDate.now())) {
                dayLabel.setStyle(dayLabel.getStyle() + "-fx-text-fill: #2196F3;");
            }
            calendarGrid.add(dayLabel, i + 1, 0);
        }

        // Spalten-Constraints
        calendarGrid.getColumnConstraints().add(new ColumnConstraints(60)); // Zeit-Spalte
        for (int i = 0; i < 7; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setHgrow(Priority.ALWAYS);
            col.setMinWidth(100);
            calendarGrid.getColumnConstraints().add(col);
        }

        // Zeitslots
        for (int hour = 8; hour < 18; hour++) {
            int row = hour - 7;

            // Zeit-Label
            Label timeLabel = new Label(String.format("%02d:00", hour));
            timeLabel.setStyle("-fx-padding: 5;");
            calendarGrid.add(timeLabel, 0, row);

            // Für jeden Tag der Woche
            for (int day = 0; day < 7; day++) {
                LocalDate date = weekStart.plusDays(day);
                VBox daySlot = new VBox(1);
                daySlot.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 0.5; -fx-min-height: 40;");

                // Termine für diesen Tag und diese Stunde laden
                List<Termin> dayTermine = getFilteredTermineForDate(date);

                for (Termin termin : dayTermine) {
                    if (termin.getUhrzeit() != null && termin.getUhrzeit().getHour() == hour) {
                        Label terminLabel = new Label(termin.getUhrzeit().format(DateTimeFormatter.ofPattern("HH:mm")) +
                                " " + termin.getPatientName().split(",")[0]);
                        terminLabel.setStyle("-fx-background-color: " + termin.getTerminFarbe() +
                                "; -fx-text-fill: white; -fx-padding: 2; -fx-font-size: 9;");
                        daySlot.getChildren().add(terminLabel);
                    }
                }

                calendarGrid.add(daySlot, day + 1, row);
            }

            RowConstraints rowC = new RowConstraints();
            rowC.setMinHeight(40);
            calendarGrid.getRowConstraints().add(rowC);
        }
    }

    private void buildDayView() {
        dayDetailPanel.setVisible(true);
        dayDetailPanel.setManaged(true);

        calendarGrid.getChildren().clear();
        calendarGrid.getColumnConstraints().clear();
        calendarGrid.getRowConstraints().clear();

        monthYearLabel.setText(currentDate.format(DateTimeFormatter.ofPattern("EEEE, d. MMMM yyyy", Locale.GERMAN)));

        // Setup Spalten
        ColumnConstraints timeCol = new ColumnConstraints(60);
        ColumnConstraints appointmentCol = new ColumnConstraints();
        appointmentCol.setHgrow(Priority.ALWAYS);
        calendarGrid.getColumnConstraints().addAll(timeCol, appointmentCol);

        // Lade Termine für den Tag - DIESE ZEILE FEHLTE!
        List<Termin> dayTermine = getFilteredTermineForDate(currentDate);

        // Zeitslots von 8:00 bis 18:00
        for (int hour = 8; hour < 18; hour++) {
            Label timeLabel = new Label(String.format("%02d:00", hour));
            timeLabel.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0; -fx-padding: 5;");

            HBox hourContainer = new HBox(5);
            hourContainer.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0; -fx-min-height: 60;");

            VBox hourBox = new VBox(2);
            hourBox.setStyle("-fx-min-width: 200;");
            HBox.setHgrow(hourBox, Priority.ALWAYS);

            // Quick-Add Button
            final int finalHour = hour;
            Button addButton = new Button("+");
            addButton.setStyle("-fx-font-size: 10; -fx-padding: 2;");
            addButton.setOnAction(e ->
                    handleQuickAddAppointment(currentDate, LocalTime.of(finalHour, 0))
            );

            // Prüfe ob Termine in dieser Stunde sind
            for (Termin termin : dayTermine) {
                if (termin.getUhrzeit() != null && termin.getUhrzeit().getHour() == hour) {
                    VBox terminBlock = new VBox(1);
                    terminBlock.setStyle(
                            "-fx-background-color: " + termin.getTerminFarbe() + ";" +
                                    "-fx-text-fill: white; -fx-padding: 3; -fx-background-radius: 3;"
                    );

                    Label timeSpan = new Label(termin.getZeitspanne());
                    timeSpan.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10;");

                    Label patient = new Label(termin.getPatientName());
                    patient.setStyle("-fx-text-fill: white; -fx-font-size: 9;");

                    terminBlock.getChildren().addAll(timeSpan, patient);
                    hourBox.getChildren().add(terminBlock);

                    terminBlock.setOnMouseClicked(e -> {
                        if (e.getClickCount() == 2) {
                            handleEditAppointment(termin);
                        }
                    });
                }
            }

            hourContainer.getChildren().addAll(hourBox, addButton);

            calendarGrid.add(timeLabel, 0, hour - 8);
            calendarGrid.add(hourContainer, 1, hour - 8);

            RowConstraints row = new RowConstraints();
            row.setMinHeight(60);
            calendarGrid.getRowConstraints().add(row);
        }

        // Termine in Seitenleiste anzeigen
        loadDayAppointments();
    }

    private void loadDayAppointments() {
        dayAppointmentsList.getChildren().clear();

        List<Termin> dayTermine = getFilteredTermineForDate(currentDate);

        if (dayTermine.isEmpty()) {
            Label noAppointments = new Label("Keine Termine");
            noAppointments.setTextFill(Color.GRAY);
            dayAppointmentsList.getChildren().add(noAppointments);
            return;
        }

        for (Termin termin : dayTermine) {
            VBox terminCard = createTerminCard(termin);
            dayAppointmentsList.getChildren().add(terminCard);
        }
    }

    private VBox createTerminCard(Termin termin) {
        VBox card = new VBox(5);
        card.setStyle("-fx-background-color: white; -fx-border-color: " + termin.getTerminFarbe() +
                "; -fx-border-width: 0 0 0 3; -fx-padding: 8;");

        Label timeLabel = new Label(termin.getZeitspanne());
        timeLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

        Label patientLabel = new Label(termin.getPatientName());
        Label artLabel = new Label(termin.getTerminart());
        artLabel.setTextFill(Color.GRAY);

        HBox buttons = new HBox(5);
        Button editBtn = new Button("✏");
        editBtn.setTooltip(new Tooltip("Bearbeiten"));
        editBtn.setOnAction(e -> handleEditAppointment(termin));

        Button deleteBtn = new Button("🗑");
        deleteBtn.setTooltip(new Tooltip("Löschen"));
        deleteBtn.setOnAction(e -> handleDeleteAppointment(termin));

        buttons.getChildren().addAll(editBtn, deleteBtn);

        card.getChildren().addAll(timeLabel, patientLabel, artLabel, buttons);

        return card;
    }

    private List<Termin> getFilteredTermineForDate(LocalDate date) {
        if (allTermine == null) return new ArrayList<>();

        return allTermine.stream()
                .filter(t -> t.getDatum().equals(date))
                .filter(t -> showCancelledCheckbox.isSelected() || !"abgesagt".equals(t.getStatus()))
                .filter(t -> {
                    String artFilter = terminartFilter.getValue();
                    return "Alle".equals(artFilter) || artFilter.equals(t.getTerminart());
                })
                .filter(t -> {
                    String mitarbeiterFilterValue = mitarbeiterFilter.getValue();
                    if ("Alle".equals(mitarbeiterFilterValue)) {
                        return true;
                    }
                    return t.getMitarbeiterName() != null &&
                            t.getMitarbeiterName().startsWith(mitarbeiterFilterValue);
                })
                .sorted(Comparator.comparing(Termin::getUhrzeit))
                .collect(Collectors.toList());
    }

    @FXML
    private void handlePreviousMonth() {
        if (monthViewButton.isSelected()) {
            currentYearMonth = currentYearMonth.minusMonths(1);
            loadTermine();
            buildMonthView();
        } else if (weekViewButton.isSelected()) {
            currentDate = currentDate.minusWeeks(1);
            currentYearMonth = YearMonth.from(currentDate);
            loadTermine();
            buildWeekView();
        } else if (dayViewButton.isSelected()) {
            currentDate = currentDate.minusDays(1);
            currentYearMonth = YearMonth.from(currentDate);
            loadTermine();
            buildDayView();
        }
    }

    @FXML
    private void handleNextMonth() {
        if (monthViewButton.isSelected()) {
            currentYearMonth = currentYearMonth.plusMonths(1);
            loadTermine();
            buildMonthView();
        } else if (weekViewButton.isSelected()) {
            currentDate = currentDate.plusWeeks(1);
            currentYearMonth = YearMonth.from(currentDate);
            loadTermine();
            buildWeekView();
        } else if (dayViewButton.isSelected()) {
            currentDate = currentDate.plusDays(1);
            currentYearMonth = YearMonth.from(currentDate);
            loadTermine();
            buildDayView();
        }
    }

    @FXML
    private void handleToday() {
        currentDate = LocalDate.now();
        currentYearMonth = YearMonth.from(currentDate);
        loadTermine();
        refreshCurrentView();
    }

    @FXML
    private void handleNewAppointment() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/appointment-form.fxml"));
            Parent root = loader.load();

            AppointmentFormController controller = loader.getController();
            controller.setNewAppointmentMode(currentDate);

            Stage stage = new Stage();
            stage.setTitle("Neuer Termin");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            if (controller.isSaved()) {
                loadTermine();
                refreshCurrentView();
            }
        } catch (IOException e) {
            showError("Fehler", "Formular konnte nicht geladen werden.");
        }
    }

    private void handleEditAppointment(Termin termin) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/appointment-form.fxml"));
            Parent root = loader.load();

            AppointmentFormController controller = loader.getController();
            controller.setEditMode(termin);

            Stage stage = new Stage();
            stage.setTitle("Termin bearbeiten");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            if (controller.isSaved()) {
                loadTermine();
                refreshCurrentView();
            }
        } catch (IOException e) {
            showError("Fehler", "Formular konnte nicht geladen werden.");
        }
    }

    private void handleDeleteAppointment(Termin termin) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Termin löschen");
        alert.setHeaderText("Möchten Sie diesen Termin wirklich löschen?");
        alert.setContentText(termin.getTerminInfo());

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                if (TerminDAO.deleteTermin(termin.getTerminID())) {
                    showInfo("Erfolgreich", "Termin wurde gelöscht.");
                    loadTermine();
                    refreshCurrentView();
                }
            } catch (SQLException e) {
                showError("Fehler", e.getMessage());
            }
        }
    }

    private void handleQuickAddAppointment(LocalDate date, LocalTime time) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/appointment-form.fxml"));
            Parent root = loader.load();

            AppointmentFormController controller = loader.getController();
            controller.setNewAppointmentMode(date);
            if (time != null) {
                controller.setDefaultTime(time);
            }

            Stage stage = new Stage();
            stage.setTitle("Schnelltermin - " + date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            if (controller.isSaved()) {
                loadTermine();
                refreshCurrentView();
            }
        } catch (IOException e) {
            showError("Fehler", "Formular konnte nicht geladen werden.");
        }
    }

    @FXML
    private void handleResetFilter() {
        mitarbeiterFilter.setValue("Alle");
        terminartFilter.setValue("Alle");
        showCancelledCheckbox.setSelected(false);
        applyFilters();
    }

    private void applyFilters() {
        loadTermine();
        refreshCurrentView();
    }

    private void refreshCurrentView() {
        if (monthViewButton.isSelected()) {
            buildMonthView();
        } else if (weekViewButton.isSelected()) {
            buildWeekView();
        } else if (dayViewButton.isSelected()) {
            buildDayView();
        }
    }

    private void updateAppointmentCount() {
        if (allTermine != null) {
            long activeCount = allTermine.stream()
                    .filter(t -> !"abgesagt".equals(t.getStatus()))
                    .count();
            appointmentCountLabel.setText(activeCount + " Termine");
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

    @FXML
    private void handleExportCalendar() {
        List<String> choices = Arrays.asList("CSV (Excel)", "ICS (Kalender)");
        ChoiceDialog<String> dialog = new ChoiceDialog<>("CSV (Excel)", choices);
        dialog.setTitle("Kalender exportieren");
        dialog.setHeaderText("Wählen Sie das Export-Format:");
        dialog.setContentText("Format:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                if (result.get().startsWith("CSV")) {
                    exportToCSV();
                } else {
                    exportToICS();
                }
            } catch (IOException e) {
                showError("Export fehlgeschlagen", e.getMessage());
            }
        }
    }

    private void exportToCSV() throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Termine als CSV speichern");
        fileChooser.setInitialFileName("termine_" + currentYearMonth + ".csv");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Dateien", "*.csv")
        );

        File file = fileChooser.showSaveDialog(calendarGrid.getScene().getWindow());
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println("Datum,Zeit,Dauer,Patient,Mitarbeiter,Terminart,Status,Notizen");

                for (Termin t : allTermine) {
                    writer.printf("%s,%s,%d,\"%s\",\"%s\",%s,%s,\"%s\"%n",
                            t.getDatum(),
                            t.getZeitspanne(),
                            t.getDauer(),
                            t.getPatientName().replace("\"", "\"\""),
                            t.getMitarbeiterName().replace("\"", "\"\""),
                            t.getTerminart(),
                            t.getStatus(),
                            t.getNotizen() != null ? t.getNotizen().replace("\"", "\"\"") : ""
                    );
                }
                showInfo("Export erfolgreich", "Termine wurden exportiert nach:\n" + file.getAbsolutePath());
            }
        }
    }

    private void exportToICS() throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Termine als ICS speichern");
        fileChooser.setInitialFileName("termine_" + currentYearMonth + ".ics");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("iCalendar Dateien", "*.ics")
        );

        File file = fileChooser.showSaveDialog(calendarGrid.getScene().getWindow());
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println("BEGIN:VCALENDAR");
                writer.println("VERSION:2.0");
                writer.println("PRODID:-//MediSys//Praxisverwaltung//DE");

                for (Termin t : allTermine) {
                    if (!"abgesagt".equals(t.getStatus())) {
                        writer.println("BEGIN:VEVENT");
                        writer.println("UID:" + t.getTerminID() + "@medisys.local");
                        writer.println("DTSTART:" + t.getDatum().format(DateTimeFormatter.BASIC_ISO_DATE) +
                                "T" + t.getUhrzeit().format(DateTimeFormatter.ofPattern("HHmmss")));
                        writer.println("DTEND:" + t.getDatum().format(DateTimeFormatter.BASIC_ISO_DATE) +
                                "T" + t.getUhrzeit().plusMinutes(t.getDauer()).format(DateTimeFormatter.ofPattern("HHmmss")));
                        writer.println("SUMMARY:" + t.getPatientName() + " - " + t.getTerminart());
                        writer.println("DESCRIPTION:" + (t.getNotizen() != null ? t.getNotizen() : ""));
                        writer.println("END:VEVENT");
                    }
                }

                writer.println("END:VCALENDAR");
                showInfo("Export erfolgreich", "Termine wurden exportiert nach:\n" + file.getAbsolutePath());
            }
        }
    }
}