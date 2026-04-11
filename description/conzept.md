# 🧾 Projektbeschreibung: S-Bahn Live-Netzkarte Stuttgart

---

## 🎯 Ziel des Projekts

Ziel dieses Projekts ist die Entwicklung einer Software-Anwendung, die das S-Bahn-Netz des Verkehrs- und Tarifverbunds Stuttgart (VVS) in Form einer **schematischen Netzkarte** visualisiert und diese Karte in Echtzeit mit Betriebsdaten anreichert.

Die Anwendung soll es ermöglichen, den aktuellen Betriebszustand des S-Bahn-Netzes intuitiv zu erfassen, indem sichtbar gemacht wird, **an welcher Position sich einzelne Züge im Netz befinden**.

Dabei liegt der Fokus nicht auf geographischer Genauigkeit, sondern auf einer abstrahierten, leicht verständlichen Darstellung — vergleichbar mit klassischen Liniennetzplänen.

---

## 🗺️ Art der Darstellung

Die Visualisierung basiert auf einer **schematischen Netzkarte**, die sich optisch an der offiziellen VVS-Netzkarte orientiert.

Folgende Eigenschaften sind vorgesehen:

- Jede S-Bahn-Linie wird durch ihre **typische Linienfarbe** dargestellt
- Haltestellen werden als feste Punkte im Netz visualisiert
- Streckenverläufe sind abstrahiert (keine echte Geografie)
- Die Darstellung ist für eine Anzeige auf einem Bildschirm optimiert

---

## 🚆 Darstellung der Züge

Züge werden als Zustände im Netz dargestellt.

Dabei gilt:

Ein Zug befindet sich immer entweder:

- **an einer Station**
- oder **zwischen zwei Stationen**

Wichtige Vereinfachung:

- Es wird **keine exakte Position berechnet**
- Stattdessen wird nur unterschieden:
    - „Zug steht“
    - „Zug fährt gerade“

Das System ist damit ideal geeignet für:

- einfache Visualisierung
- LED-Hardware (ein Licht pro Station / Strecke)

---

## 📡 Datenquelle

Die Anwendung nutzt die TRIAS-API (Version 1.2) der EFA Baden-Württemberg.

Verwendet werden:

- Stop Event Requests (SER)
- Trip Information Requests (TIR)

Wichtige Einschränkung:

- Keine GPS-Daten
- Keine kontinuierlichen Positionen

Die Position wird aus Zeitinformationen abgeleitet.

---

## 🧠 Logik der Positionsbestimmung (vereinfacht)

Für jede Fahrt werden folgende Zeiten betrachtet:

- Abfahrtszeit an Station A
- Ankunftszeit an Station B
- aktuelle Zeit

### Entscheidungslogik:

- Wenn aktuelle Zeit < Abfahrtszeit:
  → Zug ist an Station A

- Wenn aktuelle Zeit >= Abfahrtszeit UND < Ankunftszeit:
  → Zug ist zwischen A und B

- Wenn aktuelle Zeit >= Ankunftszeit:
  → Zug ist an Station B

---

## 🧱 Systemarchitektur

Die Anwendung besteht aus zwei Hauptkomponenten:

### Backend (Java)

Aufgaben:

- TRIAS API abfragen
- XML-Daten parsen
- Fahrten analysieren
- Zustand jedes Zuges bestimmen:
    - Station
    - Strecke
- Daten als JSON bereitstellen

---

### Frontend (Web-App)

Aufgaben:

- Netzkarte anzeigen
- Linien farbig darstellen
- Züge visualisieren:
    - Punkt auf Station
    - oder Marker auf Strecke
- alle 5 Sekunden aktualisieren

---

## 🔄 Datenfluss

1. Backend fragt TRIAS API ab (alle 5 Sekunden)

2. Daten werden verarbeitet:
    - Linien
    - Haltestellen
    - Fahrten
    - Zeiten

3. Backend bestimmt Zustand jedes Zuges:

- an Station
- zwischen zwei Stationen

# 🧾 Projektbeschreibung – Frontend & Technologien

---

## 🖥️ Frontend-Architektur

Das Frontend ist eine Webanwendung zur Visualisierung des S-Bahn-Netzes und der aktuellen Zugpositionen.

Ziel ist eine **übersichtliche, schematische Darstellung**, ähnlich einem klassischen Liniennetzplan.

---

## 🗺️ Darstellung der Netzkarte

Die Netzkarte bildet das S-Bahn-Netz abstrahiert ab.

### Eigenschaften:

- Jede Station hat eine feste Position (x, y)
- Linien verbinden die Stationen in definierter Reihenfolge
- Jede Linie hat eine eigene Farbe (z. B. S1 = grün, S2 = rot)

### Technische Umsetzung:

Die Karte wird als **SVG (Scalable Vector Graphics)** umgesetzt.

Vorteile von SVG:

- Skalierbar ohne Qualitätsverlust
- Elemente (Linien, Punkte) sind einzeln ansprechbar
- Einfach zu manipulieren mit JavaScript

---

## 🚆 Darstellung der Züge

Züge werden als einfache visuelle Marker dargestellt.

### Zustände:

#### 1. Zug an Station
- Darstellung:
    - Punkt direkt auf der Station
    - optional: größer oder hervorgehoben

#### 2. Zug zwischen zwei Stationen
- Darstellung:
    - Punkt auf der Verbindungslinie
    - z. B. mittig zwischen zwei Stationen

---

## 🔄 Update-Mechanismus

Das Frontend aktualisiert die Anzeige regelmäßig.

### Ablauf:

1. Alle 5 Sekunden:
    - Anfrage an Backend API

2. Empfang der Daten (JSON)

3. Aktualisierung der Anzeige:
    - alte Marker entfernen
    - neue Marker setzen

---

## 🌐 Kommunikation mit Backend

### API-Aufruf:

```javascript
fetch("/api/trains")
  .then(response => response.json())
  .then(data => {
    // Züge aktualisieren
  });