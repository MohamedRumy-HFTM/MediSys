package ch.medisys.util;

import at.favre.lib.crypto.bcrypt.BCrypt;
import ch.medisys.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class FixPasswords {
    public static void main(String[] args) {
        try {
            // Generiere einen neuen Hash für "admin123"
            String password = "admin123";
            String newHash = BCrypt.withDefaults().hashToString(12, password.toCharArray());
            
            System.out.println("Neuer Hash für 'admin123': " + newHash);
            System.out.println("Hash-Länge: " + newHash.length());
            
            // Update in der Datenbank
            Connection conn = DatabaseConnection.getConnection();
            String updateQuery = "UPDATE Mitarbeiter SET Passwort = ? WHERE Email IN (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(updateQuery);
            stmt.setString(1, newHash);
            stmt.setString(2, "admin@medisys.ch");
            stmt.setString(3, "maria.muster@medisys.ch");
            stmt.setString(4, "hans.beispiel@medisys.ch");
            
            int updated = stmt.executeUpdate();
            System.out.println("Aktualisierte Datensätze: " + updated);
            
            stmt.close();
            conn.close();
            
            System.out.println("\nPasswörter erfolgreich aktualisiert!");
            System.out.println("Alle Benutzer können sich jetzt mit 'admin123' einloggen.");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
