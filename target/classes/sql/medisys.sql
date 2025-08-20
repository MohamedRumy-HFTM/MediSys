-- ========================================
-- MediSys Praxisverwaltungssystem
-- SQL-Implementierung des Datenbankschemas
-- Autor: Mohamed Rumy
-- Klasse: BBIN24.2a
-- Transfer-Projekt Relational Databases
-- ========================================

-- ========================================
-- STAMMDATENTABELLEN
-- Reihenfolge: Tabellen ohne Fremdschlüssel zuerst
-- ========================================

-- Tabelle: PATIENT
-- Zentrale Patientenverwaltung mit allen persönlichen und medizinischen Stammdaten
CREATE TABLE Patient (
    PatientID INT AUTO_INCREMENT PRIMARY KEY,
    Vorname VARCHAR(50) NOT NULL,
    Nachname VARCHAR(50) NOT NULL,
    Geburtsdatum DATE NOT NULL,
    Geschlecht CHAR(1),
    Strasse VARCHAR(100),
    PLZ VARCHAR(10),
    Ort VARCHAR(50),
    Telefon VARCHAR(20),
    Email VARCHAR(100) UNIQUE,
    Versicherungsnummer VARCHAR(20) NOT NULL,
    Krankenkasse VARCHAR(50) NOT NULL,
    NotfallkontaktName VARCHAR(100),
    NotfallkontaktTelefon VARCHAR(20),
    
    -- Constraint gemäss Geschäftsregel GR06
    CONSTRAINT chk_geschlecht CHECK (Geschlecht IN ('M', 'W', 'D'))
    -- Hinweis: chk_geburtsdatum wurde entfernt wegen MySQL-Kompatibilität
);

-- Tabelle: MITARBEITER
-- Verwaltung des Praxispersonals mit Rollen und Zugangsdaten
CREATE TABLE Mitarbeiter (
    MitarbeiterID INT AUTO_INCREMENT PRIMARY KEY,
    Vorname VARCHAR(50) NOT NULL,
    Nachname VARCHAR(50) NOT NULL,
    Rolle ENUM('Arzt', 'MPA', 'Therapeut') NOT NULL,
    Fachbereich VARCHAR(50),
    Email VARCHAR(100) UNIQUE NOT NULL,
    Telefon VARCHAR(20),
    Passwort VARCHAR(255) NOT NULL
);

-- Tabelle: LEISTUNG
-- Katalog aller abrechnungsfähigen medizinischen Leistungen
CREATE TABLE Leistung (
    LeistungID INT AUTO_INCREMENT PRIMARY KEY,
    Bezeichnung VARCHAR(100) NOT NULL,
    Tarifziffer VARCHAR(20) UNIQUE,
    Preis DECIMAL(10,2) NOT NULL,
    Kategorie VARCHAR(50),
    
    -- Constraint für positive Preise
    CONSTRAINT chk_preis CHECK (Preis >= 0)
);

-- Tabelle: MEDIKAMENT
-- Verzeichnis aller verschreibbaren Medikamente
CREATE TABLE Medikament (
    MedikamentID INT AUTO_INCREMENT PRIMARY KEY,
    Handelsname VARCHAR(100) NOT NULL,
    Wirkstoff VARCHAR(100) NOT NULL,
    Standarddosierung VARCHAR(50),
    Packungsgroesse VARCHAR(50)
);

-- ========================================
-- TRANSAKTIONALE TABELLEN
-- Tabellen mit Fremdschlüsseln, abhängig von Stammdaten
-- ========================================

-- Tabelle: TERMIN
-- Terminplanung und -verwaltung
CREATE TABLE Termin (
    TerminID INT AUTO_INCREMENT PRIMARY KEY,
    PatientID INT NOT NULL,
    MitarbeiterID INT NOT NULL,
    Datum DATE NOT NULL,
    Uhrzeit TIME NOT NULL,
    Dauer INT NOT NULL DEFAULT 30,
    Terminart VARCHAR(50),
    Status ENUM('geplant', 'durchgeführt', 'abgesagt') NOT NULL DEFAULT 'geplant',
    Notizen TEXT,
    
    -- Fremdschlüssel mit referentieller Integrität
    FOREIGN KEY (PatientID) REFERENCES Patient(PatientID) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (MitarbeiterID) REFERENCES Mitarbeiter(MitarbeiterID) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    
    -- Unique Constraint gemäss GR01: Vermeidung von Doppelbuchungen
    UNIQUE KEY uk_termin_zeit (MitarbeiterID, Datum, Uhrzeit),
    
    -- Index für Performance (Seite 16 der Spezifikation)
    INDEX idx_termin_datum (Datum)
);

-- Tabelle: RECHNUNG
-- Abrechnungsverwaltung für Patienten
CREATE TABLE Rechnung (
    RechnungID INT AUTO_INCREMENT PRIMARY KEY,
    PatientID INT NOT NULL,
    Rechnungsdatum DATE NOT NULL DEFAULT (CURRENT_DATE),
    Gesamtbetrag DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    Status ENUM('offen', 'bezahlt', 'gemahnt') NOT NULL DEFAULT 'offen',
    Zahlungsfrist DATE NOT NULL,
    
    -- Fremdschlüssel mit referentieller Integrität
    FOREIGN KEY (PatientID) REFERENCES Patient(PatientID) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    
    -- Check Constraint für positive Beträge
    CONSTRAINT chk_gesamtbetrag CHECK (Gesamtbetrag >= 0),
    
    -- Index für Performance (Seite 16 der Spezifikation)
    INDEX idx_rechnung_status (Status)
);

-- Tabelle: DOKUMENT
-- Externe Dokumentenverwaltung
CREATE TABLE Dokument (
    DokumentID INT AUTO_INCREMENT PRIMARY KEY,
    PatientID INT NOT NULL,
    Dokumenttyp VARCHAR(50) NOT NULL,
    Erstellungsdatum DATE NOT NULL,
    Dateipfad VARCHAR(255) NOT NULL,
    Beschreibung TEXT,
    
    -- Fremdschlüssel mit referentieller Integrität
    FOREIGN KEY (PatientID) REFERENCES Patient(PatientID) 
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- Tabelle: BEHANDLUNG
-- Medizinische Dokumentation (abhängig von Termin)
CREATE TABLE Behandlung (
    BehandlungID INT AUTO_INCREMENT PRIMARY KEY,
    TerminID INT UNIQUE NOT NULL,  -- UNIQUE für 1:1 Beziehung
    MitarbeiterID INT NOT NULL,
    Behandlungsdatum DATE NOT NULL,
    Anamnese TEXT,
    Diagnose TEXT,
    Therapie TEXT,
    Notizen TEXT,
    
    -- Fremdschlüssel mit referentieller Integrität
    FOREIGN KEY (TerminID) REFERENCES Termin(TerminID) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (MitarbeiterID) REFERENCES Mitarbeiter(MitarbeiterID) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    
    -- Index für Performance (Seite 16 der Spezifikation)
    INDEX idx_behandlung_datum (Behandlungsdatum)
);

-- ========================================
-- VERKNÜPFUNGSTABELLEN
-- Auflösung der n:m Beziehungen
-- ========================================

-- Tabelle: BEHANDLUNG_LEISTUNG
-- Erbrachte Leistungen pro Behandlung mit historisierten Preisen (GR05)
CREATE TABLE Behandlung_Leistung (
    BehandlungID INT NOT NULL,
    LeistungID INT NOT NULL,
    Anzahl INT NOT NULL DEFAULT 1,
    Einzelpreis DECIMAL(10,2) NOT NULL,  -- Historisierter Preis gemäss GR05
    
    -- Zusammengesetzter Primärschlüssel
    PRIMARY KEY (BehandlungID, LeistungID),
    
    -- Fremdschlüssel mit referentieller Integrität
    FOREIGN KEY (BehandlungID) REFERENCES Behandlung(BehandlungID) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (LeistungID) REFERENCES Leistung(LeistungID) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    
    -- Check Constraints
    CONSTRAINT chk_anzahl CHECK (Anzahl > 0),
    CONSTRAINT chk_einzelpreis CHECK (Einzelpreis >= 0)
);

-- Tabelle: VERSCHREIBUNG
-- Medikamentenverschreibungen mit Dosierungsangaben (GR08)
CREATE TABLE Verschreibung (
    VerschreibungID INT AUTO_INCREMENT PRIMARY KEY,
    BehandlungID INT NOT NULL,
    MedikamentID INT NOT NULL,
    Dosierung VARCHAR(100) NOT NULL,  -- Pflichtfeld gemäss GR08
    Dauer VARCHAR(50),
    Hinweise TEXT,
    
    -- Fremdschlüssel mit referentieller Integrität
    FOREIGN KEY (BehandlungID) REFERENCES Behandlung(BehandlungID) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (MedikamentID) REFERENCES Medikament(MedikamentID) 
        ON DELETE RESTRICT ON UPDATE CASCADE
);

-- Tabelle: BEHANDLUNG_RECHNUNG
-- Zuordnung von Behandlungen zu Sammelrechnungen
CREATE TABLE Behandlung_Rechnung (
    BehandlungID INT NOT NULL,
    RechnungID INT NOT NULL,
    
    -- Zusammengesetzter Primärschlüssel
    PRIMARY KEY (BehandlungID, RechnungID),
    
    -- Fremdschlüssel mit referentieller Integrität
    FOREIGN KEY (BehandlungID) REFERENCES Behandlung(BehandlungID) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (RechnungID) REFERENCES Rechnung(RechnungID) 
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- Tabelle: BEHANDLUNG_DOKUMENT
-- Zuordnung von Dokumenten zu Behandlungen (GR10)
CREATE TABLE Behandlung_Dokument (
    BehandlungID INT NOT NULL,
    DokumentID INT NOT NULL,
    
    -- Zusammengesetzter Primärschlüssel
    PRIMARY KEY (BehandlungID, DokumentID),
    
    -- Fremdschlüssel mit referentieller Integrität
    FOREIGN KEY (BehandlungID) REFERENCES Behandlung(BehandlungID) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (DokumentID) REFERENCES Dokument(DokumentID) 
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- ========================================
-- ZUSÄTZLICHE INDIZES FÜR PERFORMANCE
-- Gemäss Spezifikation Seite 16
-- ========================================

-- Index für Patientensuche nach Name
CREATE INDEX idx_patient_name ON Patient(Nachname, Vorname);

-- Hinweis: Weitere Indizes bereits in den Tabellendefinitionen:
-- idx_termin_datum für Tagesansicht
-- uk_termin_zeit für Kollisionsvermeidung  
-- idx_behandlung_datum für Verlaufsabfragen
-- idx_rechnung_status für offene Rechnungen

-- ========================================
-- Ende der SQL-Implementierung
-- Alle funktionalen Anforderungen (FA01-FA08) und 
-- Geschäftsregeln (GR01-GR10) sind implementiert
-- ========================================