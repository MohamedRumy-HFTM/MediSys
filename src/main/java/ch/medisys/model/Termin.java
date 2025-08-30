package ch.medisys.model;

import javafx.beans.property.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Model-Klasse für Termine mit JavaFX Properties
 */
public class Termin {
    
    private final IntegerProperty terminID;
    private final IntegerProperty patientID;
    private final IntegerProperty mitarbeiterID;
    private final ObjectProperty<LocalDate> datum;
    private final ObjectProperty<LocalTime> uhrzeit;
    private final IntegerProperty dauer;
    private final StringProperty terminart;
    private final StringProperty status;
    private final StringProperty notizen;
    
    // Zusätzliche Properties für die Anzeige
    private final StringProperty patientName;
    private final StringProperty mitarbeiterName;
    private final ObjectProperty<LocalDateTime> terminZeit;
    
    // Konstruktor
    public Termin() {
        this.terminID = new SimpleIntegerProperty();
        this.patientID = new SimpleIntegerProperty();
        this.mitarbeiterID = new SimpleIntegerProperty();
        this.datum = new SimpleObjectProperty<>();
        this.uhrzeit = new SimpleObjectProperty<>();
        this.dauer = new SimpleIntegerProperty(30); // Standard 30 Minuten
        this.terminart = new SimpleStringProperty();
        this.status = new SimpleStringProperty("geplant");
        this.notizen = new SimpleStringProperty();
        
        this.patientName = new SimpleStringProperty();
        this.mitarbeiterName = new SimpleStringProperty();
        this.terminZeit = new SimpleObjectProperty<>();
        
        // Automatische Berechnung der TerminZeit
        datum.addListener((obs, oldVal, newVal) -> updateTerminZeit());
        uhrzeit.addListener((obs, oldVal, newVal) -> updateTerminZeit());
    }
    
    private void updateTerminZeit() {
        if (datum.get() != null && uhrzeit.get() != null) {
            terminZeit.set(LocalDateTime.of(datum.get(), uhrzeit.get()));
        }
    }
    
    // Getter und Setter
    public int getTerminID() { return terminID.get(); }
    public void setTerminID(int value) { terminID.set(value); }
    public IntegerProperty terminIDProperty() { return terminID; }
    
    public int getPatientID() { return patientID.get(); }
    public void setPatientID(int value) { patientID.set(value); }
    public IntegerProperty patientIDProperty() { return patientID; }
    
    public int getMitarbeiterID() { return mitarbeiterID.get(); }
    public void setMitarbeiterID(int value) { mitarbeiterID.set(value); }
    public IntegerProperty mitarbeiterIDProperty() { return mitarbeiterID; }
    
    public LocalDate getDatum() { return datum.get(); }
    public void setDatum(LocalDate value) { datum.set(value); }
    public ObjectProperty<LocalDate> datumProperty() { return datum; }
    
    public LocalTime getUhrzeit() { return uhrzeit.get(); }
    public void setUhrzeit(LocalTime value) { uhrzeit.set(value); }
    public ObjectProperty<LocalTime> uhrzeitProperty() { return uhrzeit; }
    
    public int getDauer() { return dauer.get(); }
    public void setDauer(int value) { dauer.set(value); }
    public IntegerProperty dauerProperty() { return dauer; }
    
    public String getTerminart() { return terminart.get(); }
    public void setTerminart(String value) { terminart.set(value); }
    public StringProperty terminartProperty() { return terminart; }
    
    public String getStatus() { return status.get(); }
    public void setStatus(String value) { status.set(value); }
    public StringProperty statusProperty() { return status; }
    
    public String getNotizen() { return notizen.get(); }
    public void setNotizen(String value) { notizen.set(value); }
    public StringProperty notizenProperty() { return notizen; }
    
    public String getPatientName() { return patientName.get(); }
    public void setPatientName(String value) { patientName.set(value); }
    public StringProperty patientNameProperty() { return patientName; }
    
    public String getMitarbeiterName() { return mitarbeiterName.get(); }
    public void setMitarbeiterName(String value) { mitarbeiterName.set(value); }
    public StringProperty mitarbeiterNameProperty() { return mitarbeiterName; }
    
    public LocalDateTime getTerminZeit() { return terminZeit.get(); }
    public ObjectProperty<LocalDateTime> terminZeitProperty() { return terminZeit; }
    
    // Hilfsmethoden
    public LocalTime getEndzeit() {
        if (uhrzeit.get() != null) {
            return uhrzeit.get().plusMinutes(dauer.get());
        }
        return null;
    }
    
    public String getZeitspanne() {
        if (uhrzeit.get() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            return formatter.format(uhrzeit.get()) + " - " + formatter.format(getEndzeit());
        }
        return "";
    }
    
    public String getTerminInfo() {
        return String.format("%s - %s (%s)", 
            getZeitspanne(), 
            getPatientName() != null ? getPatientName() : "Kein Patient",
            getTerminart() != null ? getTerminart() : "Termin");
    }
    
    // Farbe basierend auf Terminart
    public String getTerminFarbe() {
        if (terminart.get() == null) return "#2196F3"; // Standard Blau
        
        switch (terminart.get()) {
            case "Erstuntersuchung": return "#4CAF50"; // Grün
            case "Nachkontrolle": return "#2196F3"; // Blau
            case "Notfall": return "#F44336"; // Rot
            case "Behandlung": return "#FF9800"; // Orange
            case "Beratung": return "#9C27B0"; // Lila
            case "Impfung": return "#00BCD4"; // Cyan
            default: return "#607D8B"; // Grau
        }
    }
    
    @Override
    public String toString() {
        return getTerminInfo();
    }
}
