-- ========================================
-- MediSys Testdaten
-- ========================================

-- Mitarbeiter (mit BCrypt gehashtem Passwort für "admin123")
INSERT INTO Mitarbeiter (Vorname, Nachname, Rolle, Fachbereich, Email, Telefon, Passwort) VALUES
('Dr. Hans', 'Meier', 'Arzt', 'Allgemeinmedizin', 'hans.meier@medisys.ch', '031-111-1111', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY6MrhrG/hXrHD6'),
('Dr. Anna', 'Schmidt', 'Arzt', 'Kardiologie', 'anna.schmidt@medisys.ch', '031-222-2222', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY6MrhrG/hXrHD6'),
('Thomas', 'Weber', 'MPA', NULL, 'thomas.weber@medisys.ch', '031-333-3333', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY6MrhrG/hXrHD6'),
('Laura', 'Fischer', 'MPA', NULL, 'laura.fischer@medisys.ch', '031-444-4444', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY6MrhrG/hXrHD6'),
('Peter', 'Keller', 'Therapeut', 'Physiotherapie', 'peter.keller@medisys.ch', '031-555-5555', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY6MrhrG/hXrHD6');

-- Test-Patienten
INSERT INTO Patient (Vorname, Nachname, Geburtsdatum, Geschlecht, Strasse, PLZ, Ort, Telefon, Email, Versicherungsnummer, Krankenkasse, NotfallkontaktName, NotfallkontaktTelefon) VALUES
('Max', 'Mustermann', '1990-05-15', 'M', 'Hauptstrasse 123', '3011', 'Bern', '079-123-4567', 'max.mustermann@email.ch', 'CH1234567890', 'Helsana', 'Maria Mustermann', '079-987-6543'),
('Anna', 'Beispiel', '1985-08-22', 'W', 'Bahnhofstrasse 45', '3001', 'Bern', '078-234-5678', 'anna.beispiel@email.ch', 'CH2345678901', 'CSS', 'Peter Beispiel', '078-876-5432'),
('Hans', 'Muster', '1975-03-10', 'M', 'Marktgasse 8', '3014', 'Bern', '076-345-6789', 'hans.muster@email.ch', 'CH3456789012', 'Swica', 'Lisa Muster', '076-765-4321'),
('Marie', 'Schneider', '2000-11-28', 'W', 'Kirchweg 15', '3018', 'Bern', '077-456-7890', 'marie.schneider@email.ch', 'CH4567890123', 'Concordia', 'Paul Schneider', '077-654-3210'),
('Thomas', 'Gerber', '1968-07-05', 'M', 'Schulstrasse 22', '3006', 'Bern', '079-567-8901', 'thomas.gerber@email.ch', 'CH5678901234', 'Helsana', 'Sandra Gerber', '079-543-2109'),
('Julia', 'Zimmermann', '1992-02-14', 'W', 'Gartenweg 7', '3012', 'Bern', '078-678-9012', 'julia.zimmermann@email.ch', 'CH6789012345', 'CSS', 'Marco Zimmermann', '078-432-1098'),
('Robert', 'Brunner', '1980-09-30', 'M', 'Poststrasse 33', '3015', 'Bern', '076-789-0123', 'robert.brunner@email.ch', 'CH7890123456', 'Visana', 'Nina Brunner', '076-321-0987'),
('Sophie', 'Wagner', '1995-12-18', 'W', 'Ringstrasse 11', '3007', 'Bern', '077-890-1234', 'sophie.wagner@email.ch', 'CH8901234567', 'Sanitas', 'Felix Wagner', '077-210-9876'),
('Daniel', 'Müller', '1973-06-25', 'M', 'Bergstrasse 19', '3013', 'Bern', '079-901-2345', 'daniel.mueller@email.ch', 'CH9012345678', 'Atupri', 'Claudia Müller', '079-109-8765'),
('Emma', 'Steiner', '2005-04-08', 'W', 'Seestrasse 4', '3004', 'Bern', '078-012-3456', 'emma.steiner@email.ch', 'CH0123456789', 'KPT', 'Andreas Steiner', '078-098-7654');

-- Leistungen
INSERT INTO Leistung (Bezeichnung, Tarifziffer, Preis, Kategorie) VALUES
('Konsultation', '00.0010', 150.00, 'Grundleistung'),
('Notfallzuschlag', '00.0020', 50.00, 'Zuschläge'),
('Blutentnahme', '01.0110', 25.00, 'Labor'),
('EKG', '17.0210', 75.00, 'Diagnostik'),
('Röntgen Thorax', '39.3100', 120.00, 'Radiologie'),
('Impfung', '00.0070', 35.00, 'Prävention'),
('Wundversorgung klein', '04.0610', 45.00, 'Chirurgie'),
('Ultraschall Abdomen', '39.3600', 180.00, 'Diagnostik');

-- Medikamente
INSERT INTO Medikament (Handelsname, Wirkstoff, Standarddosierung, Packungsgroesse) VALUES
('Dafalgan', 'Paracetamol', '500mg 3x täglich', '20 Tabletten'),
('Aspirin Cardio', 'Acetylsalicylsäure', '100mg 1x täglich', '100 Tabletten'),
('Ibuprofen', 'Ibuprofen', '400mg bei Bedarf', '20 Tabletten'),
('Amoxicillin', 'Amoxicillin', '500mg 3x täglich', '20 Tabletten'),
('Pantoprazol', 'Pantoprazol', '40mg 1x täglich', '30 Tabletten'),
('Metformin', 'Metformin', '500mg 2x täglich', '120 Tabletten');

-- Termine für heute und die nächsten Tage
INSERT INTO Termin (PatientID, MitarbeiterID, Datum, Uhrzeit, Dauer, Terminart, Status, Notizen) VALUES
-- Heute
(1, 1, CURDATE(), '09:00:00', 30, 'Nachkontrolle', 'geplant', 'Blutdruck-Kontrolle'),
(2, 1, CURDATE(), '09:30:00', 45, 'Erstuntersuchung', 'geplant', 'Neue Patientin'),
(3, 2, CURDATE(), '10:00:00', 30, 'Behandlung', 'geplant', 'EKG-Kontrolle'),
(4, 1, CURDATE(), '11:00:00', 30, 'Impfung', 'geplant', 'Grippe-Impfung'),
(5, 2, CURDATE(), '14:00:00', 60, 'Behandlung', 'geplant', 'Ausführliche Untersuchung'),
-- Morgen
(6, 1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '08:00:00', 30, 'Nachkontrolle', 'geplant', NULL),
(7, 2, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:15:00', 45, 'Erstuntersuchung', 'geplant', 'Überweisung vom Hausarzt'),
(8, 1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '10:30:00', 30, 'Beratung', 'geplant', 'Ernährungsberatung'),
-- Übermorgen
(9, 2, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '08:30:00', 30, 'Nachkontrolle', 'geplant', NULL),
(10, 1, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '11:00:00', 60, 'Behandlung', 'geplant', 'Jahreskontrolle'),
-- Nächste Woche
(1, 1, DATE_ADD(CURDATE(), INTERVAL 7 DAY), '09:00:00', 30, 'Nachkontrolle', 'geplant', 'Verlaufskontrolle'),
(2, 2, DATE_ADD(CURDATE(), INTERVAL 7 DAY), '10:00:00', 45, 'Behandlung', 'geplant', NULL);

-- Beispiel-Rechnungen
INSERT INTO Rechnung (PatientID, Rechnungsdatum, Gesamtbetrag, Status, Zahlungsfrist) VALUES
(1, DATE_SUB(CURDATE(), INTERVAL 30 DAY), 225.00, 'bezahlt', DATE_SUB(CURDATE(), INTERVAL 1 DAY)),
(2, DATE_SUB(CURDATE(), INTERVAL 15 DAY), 175.00, 'offen', DATE_ADD(CURDATE(), INTERVAL 15 DAY)),
(3, DATE_SUB(CURDATE(), INTERVAL 7 DAY), 320.00, 'offen', DATE_ADD(CURDATE(), INTERVAL 23 DAY)),
(4, CURDATE(), 150.00, 'offen', DATE_ADD(CURDATE(), INTERVAL 30 DAY));

-- Beispiel-Behandlungen (für abgeschlossene Termine)
INSERT INTO Behandlung (TerminID, MitarbeiterID, Behandlungsdatum, Anamnese, Diagnose, Therapie, Notizen) VALUES
(1, 1, DATE_SUB(CURDATE(), INTERVAL 30 DAY), 
 'Patient klagt über Kopfschmerzen seit 3 Tagen', 
 'Spannungskopfschmerz', 
 'Paracetamol 500mg bei Bedarf, Entspannungsübungen empfohlen', 
 'Wiedervorstellung bei Persistenz'),
(2, 2, DATE_SUB(CURDATE(), INTERVAL 15 DAY), 
 'Routinekontrolle, keine Beschwerden', 
 'Gesund', 
 'Keine Therapie erforderlich', 
 'Nächste Kontrolle in 1 Jahr');

-- Beispiel-Dokumente
INSERT INTO Dokument (PatientID, Dokumenttyp, Erstellungsdatum, Dateipfad, Beschreibung) VALUES
(1, 'Laborbericht', DATE_SUB(CURDATE(), INTERVAL 30 DAY), '/dokumente/patient1/labor_2024.pdf', 'Blutbild vom Labor Unilabs'),
(2, 'Überweisung', DATE_SUB(CURDATE(), INTERVAL 15 DAY), '/dokumente/patient2/ueberweisung.pdf', 'Überweisung vom Hausarzt'),
(3, 'Röntgenbild', DATE_SUB(CURDATE(), INTERVAL 7 DAY), '/dokumente/patient3/roentgen_thorax.jpg', 'Röntgen Thorax');

-- Hinweis für Benutzer
SELECT 'Testdaten erfolgreich eingefügt!' as Status;
SELECT 'Alle Mitarbeiter können sich mit Passwort "admin123" anmelden.' as Info;
