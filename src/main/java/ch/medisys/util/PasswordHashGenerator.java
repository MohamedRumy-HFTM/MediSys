package ch.medisys.util;

import at.favre.lib.crypto.bcrypt.BCrypt;

/**
 * Hilfsklasse zum Generieren von BCrypt-Passwort-Hashes
 */
public class PasswordHashGenerator {
    
    public static void main(String[] args) {
        // Generiere Hash für "admin123"
        String password = "admin123";
        String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());
        System.out.println("Original Passwort: " + password);
        System.out.println("BCrypt Hash: " + hashedPassword);
        System.out.println("\nSQL-Insert Statement:");
        System.out.println("INSERT INTO Mitarbeiter (Vorname, Nachname, Rolle, Fachbereich, Email, Telefon, Passwort)");
        System.out.println("VALUES ('Admin', 'System', 'Arzt', 'Allgemeinmedizin', 'admin@medisys.ch', '000-000-0000',");
        System.out.println("'" + hashedPassword + "');");
    }
}
