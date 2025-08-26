package ch.medisys.model;

import javafx.beans.property.*;
import java.time.LocalDate;
import java.time.Period;

/**
 * Model-Klasse für Patienten mit JavaFX Properties für Data Binding
 */
public class Patient {
    
    private final IntegerProperty patientID;
    private final StringProperty vorname;
    private final StringProperty nachname;
    private final ObjectProperty<LocalDate> geburtsdatum;
    private final StringProperty geschlecht;
    private final StringProperty strasse;
    private final StringProperty plz;
    private final StringProperty ort;
    private final StringProperty telefon;
    private final StringProperty email;
    private final StringProperty versicherungsnummer;
    private final StringProperty krankenkasse;
    private final StringProperty notfallkontaktName;
    private final StringProperty notfallkontaktTelefon;
    
    // Berechnetes Feld
    private final IntegerProperty alter;
    
    // Konstruktor für neue Patienten
    public Patient() {
        this.patientID = new SimpleIntegerProperty();
        this.vorname = new SimpleStringProperty();
        this.nachname = new SimpleStringProperty();
        this.geburtsdatum = new SimpleObjectProperty<>();
        this.geschlecht = new SimpleStringProperty();
        this.strasse = new SimpleStringProperty();
        this.plz = new SimpleStringProperty();
        this.ort = new SimpleStringProperty();
        this.telefon = new SimpleStringProperty();
        this.email = new SimpleStringProperty();
        this.versicherungsnummer = new SimpleStringProperty();
        this.krankenkasse = new SimpleStringProperty();
        this.notfallkontaktName = new SimpleStringProperty();
        this.notfallkontaktTelefon = new SimpleStringProperty();
        this.alter = new SimpleIntegerProperty();
        
        // Automatische Altersberechnung bei Änderung des Geburtsdatums
        this.geburtsdatum.addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                this.alter.set(Period.between(newVal, LocalDate.now()).getYears());
            }
        });
    }
    
    // Konstruktor mit allen Parametern
    public Patient(int patientID, String vorname, String nachname, LocalDate geburtsdatum,
                   String geschlecht, String strasse, String plz, String ort, String telefon,
                   String email, String versicherungsnummer, String krankenkasse,
                   String notfallkontaktName, String notfallkontaktTelefon) {
        this();
        setPatientID(patientID);
        setVorname(vorname);
        setNachname(nachname);
        setGeburtsdatum(geburtsdatum);
        setGeschlecht(geschlecht);
        setStrasse(strasse);
        setPlz(plz);
        setOrt(ort);
        setTelefon(telefon);
        setEmail(email);
        setVersicherungsnummer(versicherungsnummer);
        setKrankenkasse(krankenkasse);
        setNotfallkontaktName(notfallkontaktName);
        setNotfallkontaktTelefon(notfallkontaktTelefon);
    }
    
    // Getter und Setter
    public int getPatientID() { return patientID.get(); }
    public void setPatientID(int value) { patientID.set(value); }
    public IntegerProperty patientIDProperty() { return patientID; }
    
    public String getVorname() { return vorname.get(); }
    public void setVorname(String value) { vorname.set(value); }
    public StringProperty vornameProperty() { return vorname; }
    
    public String getNachname() { return nachname.get(); }
    public void setNachname(String value) { nachname.set(value); }
    public StringProperty nachnameProperty() { return nachname; }
    
    public LocalDate getGeburtsdatum() { return geburtsdatum.get(); }
    public void setGeburtsdatum(LocalDate value) { geburtsdatum.set(value); }
    public ObjectProperty<LocalDate> geburtsdatumProperty() { return geburtsdatum; }
    
    public String getGeschlecht() { return geschlecht.get(); }
    public void setGeschlecht(String value) { geschlecht.set(value); }
    public StringProperty geschlechtProperty() { return geschlecht; }
    
    public String getStrasse() { return strasse.get(); }
    public void setStrasse(String value) { strasse.set(value); }
    public StringProperty strasseProperty() { return strasse; }
    
    public String getPlz() { return plz.get(); }
    public void setPlz(String value) { plz.set(value); }
    public StringProperty plzProperty() { return plz; }
    
    public String getOrt() { return ort.get(); }
    public void setOrt(String value) { ort.set(value); }
    public StringProperty ortProperty() { return ort; }
    
    public String getTelefon() { return telefon.get(); }
    public void setTelefon(String value) { telefon.set(value); }
    public StringProperty telefonProperty() { return telefon; }
    
    public String getEmail() { return email.get(); }
    public void setEmail(String value) { email.set(value); }
    public StringProperty emailProperty() { return email; }
    
    public String getVersicherungsnummer() { return versicherungsnummer.get(); }
    public void setVersicherungsnummer(String value) { versicherungsnummer.set(value); }
    public StringProperty versicherungsnummerProperty() { return versicherungsnummer; }
    
    public String getKrankenkasse() { return krankenkasse.get(); }
    public void setKrankenkasse(String value) { krankenkasse.set(value); }
    public StringProperty krankenkasseProperty() { return krankenkasse; }
    
    public String getNotfallkontaktName() { return notfallkontaktName.get(); }
    public void setNotfallkontaktName(String value) { notfallkontaktName.set(value); }
    public StringProperty notfallkontaktNameProperty() { return notfallkontaktName; }
    
    public String getNotfallkontaktTelefon() { return notfallkontaktTelefon.get(); }
    public void setNotfallkontaktTelefon(String value) { notfallkontaktTelefon.set(value); }
    public StringProperty notfallkontaktTelefonProperty() { return notfallkontaktTelefon; }
    
    public int getAlter() { return alter.get(); }
    public IntegerProperty alterProperty() { return alter; }
    
    // Hilfsmethoden
    public String getVollerName() {
        return getNachname() + ", " + getVorname();
    }
    
    public String getAdresse() {
        return getStrasse() + ", " + getPlz() + " " + getOrt();
    }
    
    @Override
    public String toString() {
        return getVollerName();
    }
}
