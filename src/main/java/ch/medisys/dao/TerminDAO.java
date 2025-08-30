package ch.medisys.dao;

import ch.medisys.model.Termin;
import ch.medisys.util.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object für Termin-Datenbankoperationen
 */
public class TerminDAO {
    
    /**
     * Lädt alle Termine
     */
    public static ObservableList<Termin> getAllTermine() throws SQLException {
        String query = "SELECT t.*, " +
                "CONCAT(p.Nachname, ', ', p.Vorname) as PatientName, " +
                "CONCAT(m.Nachname, ', ', m.Vorname) as MitarbeiterName " +
                "FROM Termin t " +
                "LEFT JOIN Patient p ON t.PatientID = p.PatientID " +
                "LEFT JOIN Mitarbeiter m ON t.MitarbeiterID = m.MitarbeiterID " +
                "ORDER BY t.Datum, t.Uhrzeit";
        
        return executeQuery(query);
    }
    
    /**
     * Lädt Termine für ein bestimmtes Datum
     */
    public static ObservableList<Termin> getTermineByDate(LocalDate date) throws SQLException {
        String query = "SELECT t.*, " +
                "CONCAT(p.Nachname, ', ', p.Vorname) as PatientName, " +
                "CONCAT(m.Nachname, ', ', m.Vorname) as MitarbeiterName " +
                "FROM Termin t " +
                "LEFT JOIN Patient p ON t.PatientID = p.PatientID " +
                "LEFT JOIN Mitarbeiter m ON t.MitarbeiterID = m.MitarbeiterID " +
                "WHERE t.Datum = ? " +
                "ORDER BY t.Uhrzeit";
        
        ObservableList<Termin> termine = FXCollections.observableArrayList();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setDate(1, Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                termine.add(createTerminFromResultSet(rs));
            }
        }
        
        return termine;
    }
    
    /**
     * Lädt Termine für einen Datumsbereich
     */
    public static ObservableList<Termin> getTermineByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        String query = "SELECT t.*, " +
                "CONCAT(p.Nachname, ', ', p.Vorname) as PatientName, " +
                "CONCAT(m.Nachname, ', ', m.Vorname) as MitarbeiterName " +
                "FROM Termin t " +
                "LEFT JOIN Patient p ON t.PatientID = p.PatientID " +
                "LEFT JOIN Mitarbeiter m ON t.MitarbeiterID = m.MitarbeiterID " +
                "WHERE t.Datum BETWEEN ? AND ? " +
                "ORDER BY t.Datum, t.Uhrzeit";
        
        ObservableList<Termin> termine = FXCollections.observableArrayList();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                termine.add(createTerminFromResultSet(rs));
            }
        }
        
        return termine;
    }
    
    /**
     * Lädt Termine für einen bestimmten Mitarbeiter
     */
    public static ObservableList<Termin> getTermineByMitarbeiter(int mitarbeiterID, LocalDate date) throws SQLException {
        String query = "SELECT t.*, " +
                "CONCAT(p.Nachname, ', ', p.Vorname) as PatientName, " +
                "CONCAT(m.Nachname, ', ', m.Vorname) as MitarbeiterName " +
                "FROM Termin t " +
                "LEFT JOIN Patient p ON t.PatientID = p.PatientID " +
                "LEFT JOIN Mitarbeiter m ON t.MitarbeiterID = m.MitarbeiterID " +
                "WHERE t.MitarbeiterID = ? AND t.Datum = ? " +
                "ORDER BY t.Uhrzeit";
        
        ObservableList<Termin> termine = FXCollections.observableArrayList();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, mitarbeiterID);
            stmt.setDate(2, Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                termine.add(createTerminFromResultSet(rs));
            }
        }
        
        return termine;
    }
    
    /**
     * Prüft auf Terminkollisionen (gemäß GR01)
     */
    public static boolean hasCollision(int mitarbeiterID, LocalDate datum, LocalTime startZeit, 
                                      int dauer, Integer excludeTerminID) throws SQLException {
        LocalTime endZeit = startZeit.plusMinutes(dauer);
        
        String query = "SELECT COUNT(*) FROM Termin " +
                "WHERE MitarbeiterID = ? AND Datum = ? AND Status != 'abgesagt' " +
                "AND ((Uhrzeit < ? AND ADDTIME(Uhrzeit, SEC_TO_TIME(Dauer * 60)) > ?) " +
                "OR (Uhrzeit >= ? AND Uhrzeit < ?))";
        
        if (excludeTerminID != null) {
            query += " AND TerminID != ?";
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, mitarbeiterID);
            stmt.setDate(2, Date.valueOf(datum));
            stmt.setTime(3, Time.valueOf(endZeit));
            stmt.setTime(4, Time.valueOf(startZeit));
            stmt.setTime(5, Time.valueOf(startZeit));
            stmt.setTime(6, Time.valueOf(endZeit));
            
            if (excludeTerminID != null) {
                stmt.setInt(7, excludeTerminID);
            }
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        
        return false;
    }
    
    /**
     * Erstellt einen neuen Termin
     */
    public static int insertTermin(Termin termin) throws SQLException {
        // Prüfe erst auf Kollision
        if (hasCollision(termin.getMitarbeiterID(), termin.getDatum(), 
                        termin.getUhrzeit(), termin.getDauer(), null)) {
            throw new SQLException("Terminüberschneidung! Der Mitarbeiter hat zu dieser Zeit bereits einen Termin.");
        }
        
        String query = "INSERT INTO Termin (PatientID, MitarbeiterID, Datum, Uhrzeit, " +
                "Dauer, Terminart, Status, Notizen) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, termin.getPatientID());
            stmt.setInt(2, termin.getMitarbeiterID());
            stmt.setDate(3, Date.valueOf(termin.getDatum()));
            stmt.setTime(4, Time.valueOf(termin.getUhrzeit()));
            stmt.setInt(5, termin.getDauer());
            stmt.setString(6, termin.getTerminart());
            stmt.setString(7, termin.getStatus());
            stmt.setString(8, termin.getNotizen());
            
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
     * Aktualisiert einen Termin
     */
    public static boolean updateTermin(Termin termin) throws SQLException {
        // Prüfe auf Kollision (außer mit sich selbst)
        if (hasCollision(termin.getMitarbeiterID(), termin.getDatum(), 
                        termin.getUhrzeit(), termin.getDauer(), termin.getTerminID())) {
            throw new SQLException("Terminüberschneidung! Der Mitarbeiter hat zu dieser Zeit bereits einen Termin.");
        }
        
        String query = "UPDATE Termin SET PatientID = ?, MitarbeiterID = ?, Datum = ?, " +
                "Uhrzeit = ?, Dauer = ?, Terminart = ?, Status = ?, Notizen = ? " +
                "WHERE TerminID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, termin.getPatientID());
            stmt.setInt(2, termin.getMitarbeiterID());
            stmt.setDate(3, Date.valueOf(termin.getDatum()));
            stmt.setTime(4, Time.valueOf(termin.getUhrzeit()));
            stmt.setInt(5, termin.getDauer());
            stmt.setString(6, termin.getTerminart());
            stmt.setString(7, termin.getStatus());
            stmt.setString(8, termin.getNotizen());
            stmt.setInt(9, termin.getTerminID());
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Aktualisiert nur den Status eines Termins
     */
    public static boolean updateTerminStatus(int terminID, String status) throws SQLException {
        String query = "UPDATE Termin SET Status = ? WHERE TerminID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, status);
            stmt.setInt(2, terminID);
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Löscht einen Termin
     */
    public static boolean deleteTermin(int terminID) throws SQLException {
        // Prüfe ob Behandlung existiert
        if (hasBehandlung(terminID)) {
            throw new SQLException("Termin kann nicht gelöscht werden, da bereits eine Behandlung dokumentiert wurde.");
        }
        
        String query = "DELETE FROM Termin WHERE TerminID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, terminID);
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Prüft ob eine Behandlung zum Termin existiert
     */
    private static boolean hasBehandlung(int terminID) throws SQLException {
        String query = "SELECT COUNT(*) FROM Behandlung WHERE TerminID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, terminID);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        
        return false;
    }
    
    /**
     * Lädt verfügbare Zeitslots für einen Mitarbeiter an einem Tag
     */
    public static List<LocalTime> getAvailableTimeSlots(int mitarbeiterID, LocalDate date, 
                                                        int slotDuration) throws SQLException {
        List<LocalTime> availableSlots = new ArrayList<>();
        LocalTime workStart = LocalTime.of(8, 0);  // Arbeitsbeginn 8:00
        LocalTime workEnd = LocalTime.of(18, 0);   // Arbeitsende 18:00
        
        // Hole alle Termine des Mitarbeiters für den Tag
        ObservableList<Termin> termine = getTermineByMitarbeiter(mitarbeiterID, date);
        
        LocalTime currentTime = workStart;
        
        while (currentTime.plusMinutes(slotDuration).isBefore(workEnd) || 
               currentTime.plusMinutes(slotDuration).equals(workEnd)) {
            
            boolean isAvailable = true;
            LocalTime slotEnd = currentTime.plusMinutes(slotDuration);
            
            // Prüfe ob Slot mit existierenden Terminen kollidiert
            for (Termin termin : termine) {
                if (!"abgesagt".equals(termin.getStatus())) {
                    LocalTime terminStart = termin.getUhrzeit();
                    LocalTime terminEnd = termin.getEndzeit();
                    
                    // Prüfe auf Überschneidung
                    if (!(slotEnd.isBefore(terminStart) || slotEnd.equals(terminStart) ||
                          currentTime.isAfter(terminEnd) || currentTime.equals(terminEnd))) {
                        isAvailable = false;
                        break;
                    }
                }
            }
            
            if (isAvailable) {
                availableSlots.add(currentTime);
            }
            
            currentTime = currentTime.plusMinutes(15); // 15-Minuten-Schritte
        }
        
        return availableSlots;
    }
    
    /**
     * Hilfsmethode zum Ausführen von Queries
     */
    private static ObservableList<Termin> executeQuery(String query) throws SQLException {
        ObservableList<Termin> termine = FXCollections.observableArrayList();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                termine.add(createTerminFromResultSet(rs));
            }
        }
        
        return termine;
    }
    
    /**
     * Erstellt ein Termin-Objekt aus einem ResultSet
     */
    private static Termin createTerminFromResultSet(ResultSet rs) throws SQLException {
        Termin termin = new Termin();
        
        termin.setTerminID(rs.getInt("TerminID"));
        termin.setPatientID(rs.getInt("PatientID"));
        termin.setMitarbeiterID(rs.getInt("MitarbeiterID"));
        
        Date datum = rs.getDate("Datum");
        if (datum != null) {
            termin.setDatum(datum.toLocalDate());
        }
        
        Time uhrzeit = rs.getTime("Uhrzeit");
        if (uhrzeit != null) {
            termin.setUhrzeit(uhrzeit.toLocalTime());
        }
        
        termin.setDauer(rs.getInt("Dauer"));
        termin.setTerminart(rs.getString("Terminart"));
        termin.setStatus(rs.getString("Status"));
        termin.setNotizen(rs.getString("Notizen"));
        
        // Zusätzliche Felder wenn vorhanden
        try {
            termin.setPatientName(rs.getString("PatientName"));
        } catch (SQLException e) {
            // Feld nicht vorhanden
        }
        
        try {
            termin.setMitarbeiterName(rs.getString("MitarbeiterName"));
        } catch (SQLException e) {
            // Feld nicht vorhanden
        }
        
        return termin;
    }
    
    /**
     * Statistik: Anzahl heutiger Termine
     */
    public static int getTodayAppointmentCount() throws SQLException {
        String query = "SELECT COUNT(*) FROM Termin WHERE Datum = CURRENT_DATE AND Status = 'geplant'";
        
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
