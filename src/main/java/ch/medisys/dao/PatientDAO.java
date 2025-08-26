package ch.medisys.dao;

import ch.medisys.model.Patient;
import ch.medisys.util.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object für Patienten-Datenbankoperationen
 */
public class PatientDAO {
    
    /**
     * Lädt alle Patienten aus der Datenbank
     */
    public static ObservableList<Patient> getAllPatients() throws SQLException {
        ObservableList<Patient> patients = FXCollections.observableArrayList();
        String query = "SELECT * FROM Patient ORDER BY Nachname, Vorname";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                patients.add(createPatientFromResultSet(rs));
            }
        }
        
        return patients;
    }
    
    /**
     * Sucht Patienten nach verschiedenen Kriterien
     */
    public static ObservableList<Patient> searchPatients(String searchTerm) throws SQLException {
        ObservableList<Patient> patients = FXCollections.observableArrayList();
        String query = "SELECT * FROM Patient WHERE " +
                "Vorname LIKE ? OR " +
                "Nachname LIKE ? OR " +
                "Email LIKE ? OR " +
                "Telefon LIKE ? OR " +
                "Versicherungsnummer LIKE ? " +
                "ORDER BY Nachname, Vorname";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            String searchPattern = "%" + searchTerm + "%";
            for (int i = 1; i <= 5; i++) {
                stmt.setString(i, searchPattern);
            }
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                patients.add(createPatientFromResultSet(rs));
            }
        }
        
        return patients;
    }
    
    /**
     * Lädt einen Patienten anhand der ID
     */
    public static Patient getPatientById(int patientId) throws SQLException {
        String query = "SELECT * FROM Patient WHERE PatientID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return createPatientFromResultSet(rs);
            }
        }
        
        return null;
    }
    
    /**
     * Fügt einen neuen Patienten ein
     */
    public static int insertPatient(Patient patient) throws SQLException {
        String query = "INSERT INTO Patient (Vorname, Nachname, Geburtsdatum, Geschlecht, " +
                "Strasse, PLZ, Ort, Telefon, Email, Versicherungsnummer, Krankenkasse, " +
                "NotfallkontaktName, NotfallkontaktTelefon) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            setPatientParameters(stmt, patient);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }
        
        return -1;
    }
    
    /**
     * Aktualisiert einen bestehenden Patienten
     */
    public static boolean updatePatient(Patient patient) throws SQLException {
        String query = "UPDATE Patient SET Vorname = ?, Nachname = ?, Geburtsdatum = ?, " +
                "Geschlecht = ?, Strasse = ?, PLZ = ?, Ort = ?, Telefon = ?, Email = ?, " +
                "Versicherungsnummer = ?, Krankenkasse = ?, NotfallkontaktName = ?, " +
                "NotfallkontaktTelefon = ? WHERE PatientID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            setPatientParameters(stmt, patient);
            stmt.setInt(14, patient.getPatientID());
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Löscht einen Patienten (Vorsicht: Aufgrund von Constraints möglicherweise nicht möglich)
     */
    public static boolean deletePatient(int patientId) throws SQLException {
        // Prüfe erst, ob der Patient gelöscht werden kann
        if (hasRelatedRecords(patientId)) {
            throw new SQLException("Patient kann nicht gelöscht werden, da zugehörige Termine oder Behandlungen existieren.");
        }
        
        String query = "DELETE FROM Patient WHERE PatientID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, patientId);
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Prüft, ob ein Patient verknüpfte Datensätze hat
     */
    public static boolean hasRelatedRecords(int patientId) throws SQLException {
        String query = "SELECT COUNT(*) FROM Termin WHERE PatientID = ? " +
                "UNION ALL " +
                "SELECT COUNT(*) FROM Rechnung WHERE PatientID = ? " +
                "UNION ALL " +
                "SELECT COUNT(*) FROM Dokument WHERE PatientID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, patientId);
            stmt.setInt(2, patientId);
            stmt.setInt(3, patientId);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                if (rs.getInt(1) > 0) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Prüft, ob eine E-Mail bereits existiert (für Validierung)
     */
    public static boolean emailExists(String email, Integer excludePatientId) throws SQLException {
        String query = "SELECT COUNT(*) FROM Patient WHERE Email = ?";
        if (excludePatientId != null) {
            query += " AND PatientID != ?";
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, email);
            if (excludePatientId != null) {
                stmt.setInt(2, excludePatientId);
            }
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        
        return false;
    }
    
    /**
     * Erstellt ein Patient-Objekt aus einem ResultSet
     */
    private static Patient createPatientFromResultSet(ResultSet rs) throws SQLException {
        Patient patient = new Patient();
        patient.setPatientID(rs.getInt("PatientID"));
        patient.setVorname(rs.getString("Vorname"));
        patient.setNachname(rs.getString("Nachname"));
        
        Date geburtsdatum = rs.getDate("Geburtsdatum");
        if (geburtsdatum != null) {
            patient.setGeburtsdatum(geburtsdatum.toLocalDate());
        }
        
        patient.setGeschlecht(rs.getString("Geschlecht"));
        patient.setStrasse(rs.getString("Strasse"));
        patient.setPlz(rs.getString("PLZ"));
        patient.setOrt(rs.getString("Ort"));
        patient.setTelefon(rs.getString("Telefon"));
        patient.setEmail(rs.getString("Email"));
        patient.setVersicherungsnummer(rs.getString("Versicherungsnummer"));
        patient.setKrankenkasse(rs.getString("Krankenkasse"));
        patient.setNotfallkontaktName(rs.getString("NotfallkontaktName"));
        patient.setNotfallkontaktTelefon(rs.getString("NotfallkontaktTelefon"));
        
        return patient;
    }
    
    /**
     * Setzt die PreparedStatement-Parameter für einen Patienten
     */
    private static void setPatientParameters(PreparedStatement stmt, Patient patient) throws SQLException {
        stmt.setString(1, patient.getVorname());
        stmt.setString(2, patient.getNachname());
        
        if (patient.getGeburtsdatum() != null) {
            stmt.setDate(3, Date.valueOf(patient.getGeburtsdatum()));
        } else {
            stmt.setNull(3, Types.DATE);
        }
        
        stmt.setString(4, patient.getGeschlecht());
        stmt.setString(5, patient.getStrasse());
        stmt.setString(6, patient.getPlz());
        stmt.setString(7, patient.getOrt());
        stmt.setString(8, patient.getTelefon());
        stmt.setString(9, patient.getEmail());
        stmt.setString(10, patient.getVersicherungsnummer());
        stmt.setString(11, patient.getKrankenkasse());
        stmt.setString(12, patient.getNotfallkontaktName());
        stmt.setString(13, patient.getNotfallkontaktTelefon());
    }
    
    /**
     * Gibt Statistiken zurück
     */
    public static int getTotalPatientCount() throws SQLException {
        String query = "SELECT COUNT(*) FROM Patient";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        
        return 0;
    }
}
