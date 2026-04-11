🧾 Tools & Technologien – Übersicht für das Projekt (Aktualisiert)

🎯 Ziel dieses Dokuments

Diese Liste beschreibt alle verwendeten Tools, Sprachen und Technologien für das
Projekt. Für jedes Tool wird erklärt:

- Wofür es genutzt wird
- Welche Funktionen verwendet werden
- Was man dafür lernen sollte
- Wie es funktioniert
- Ob es kostenlos und kommerziell nutzbar ist

🧠 PROGRAMMIERSPRACHEN

☕ Java (Version 11 oder neuer)

Verwendung:
- Backend-Logik
- API-Kommunikation (TRIAS) via nativem HttpClient
- Datenverarbeitung und XML-Parsing
- Zustandsberechnung der Züge (Radar & Fernglas Logik)

Was du konkret nutzt:
- Klassen & Objekte
- Listen (z. B. ArrayList)
- Zeitberechnung in UTC (`java.time.Instant`)
- HTTP Requests (`java.net.http.HttpClient`)

Was du lernen solltest:
- Grundlagen (Klassen, Methoden, Schleifen)
- OOP (Objektorientierung)
- Collections (Listen, Maps)

Kosten / Nutzung:
- ✅ Kostenlos (OpenJDK)
- ✅ Kommerzielle Nutzung erlaubt

🌐 JavaScript

Verwendung:
- Frontend-Logik
- Daten vom Backend abrufen (JSON)
- Anzeige (SVG-Marker) aktualisieren

Was du nutzt:
- fetch() für API Calls
- DOM-Manipulation

Kosten:
- ✅ Kostenlos
- ✅ Kommerziell nutzbar

🎨 HTML & CSS

Verwendung:
- Struktur und Styling der Webseite

Was du nutzt:
- Grundstruktur (div, svg)
- Farben & einfaches Layout

Kosten:
- ✅ Kostenlos
- ✅ Kommerziell nutzbar

⚙️ FRAMEWORKS & LIBRARIES

🚀 Spring Boot

Verwendung:
- Backend-Server
- REST API bereitstellen (JSON für das Frontend)

Was du nutzt:
- @RestController
- @GetMapping

Wie es funktioniert:
Spring Boot startet einen eingebauten Server (Tomcat) → dein Java-Code wird
direkt als Webserver ausgeführt.

Kosten:
- ✅ Kostenlos
- ✅ Kommerziell nutzbar

📡 DATEN & APIs

📡 TRIAS API (VVS / EFA Baden-Württemberg)

Verwendung:
- Echtzeit-Fahrplandaten

Was du nutzt (Zwei-Stufen-Pipeline):
- Stop Event Request (SER) → Als "Radar" zum Finden von Zug-IDs
- Trip Information Request (TIR) → Als "Fernglas" für den exakten Zeitstrahl

Was du lernen solltest:
- XML verstehen
- API Requests aufbauen

Kosten:
- ✅ Kostenlos (nach Registrierung)
- ⚠️ Kommerzielle Nutzung: → abhängig von Nutzungsbedingungen (prüfen!)

📄 DATENFORMATE

📄 JSON
Verwendung:
- Kommunikation Backend → Frontend
  Was du nutzt:
- einfache Objekte (Train)

📄 XML
Verwendung:
- TRIAS API Antworten
  Was du nutzt:
- Parsing der API Antworten im Backend (DocumentBuilder)

🖥️ VISUALISIERUNG

🧩 SVG
Verwendung:
- Darstellung der Netzkarte im Browser
  Was du nutzt:
- Linien (Strecken)
- Kreise (Stationen)
- Marker (Züge)

🧰 TOOLS

💻 IDE (Empfohlen: IntelliJ IDEA Community Edition)
- Java programmieren und Debuggen
- ✅ Kostenlos (Community Version)

🧪 Postman
- TRIAS API testen (XML Requests senden & analysieren)
- ✅ Kostenlos (Basisversion)

🌐 Browser Developer Tools (F12)
- Frontend und fetch-Requests testen
- ✅ Kostenlos