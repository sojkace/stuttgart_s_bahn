# 🧾 Projektbeschreibung – Teil 1: Gesamtübersicht

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

Züge werden als dynamische Elemente auf der Netzkarte visualisiert.

Dabei gelten folgende Prinzipien:

- Ein Zug befindet sich entweder:
    - **an einer Station** oder
    - **zwischen zwei Stationen**
- Die Position wird aus Fahrplandaten abgeleitet (nicht aus GPS)
- Verspätungen werden **indirekt berücksichtigt**, indem:
    - sich die Position des Zuges zeitlich verzögert aktualisiert
- Die Bewegung der Züge erfolgt kontinuierlich oder in diskreten Schritten entlang der Linien

---

## 📡 Datenquelle

Die Anwendung nutzt die TRIAS-API (Version 1.2) der EFA Baden-Württemberg als Datenquelle.

Verwendet werden insbesondere:

- **Stop Event Requests (SER)** → Abfahrten an Haltestellen
- **Trip Information Requests (TIR)** → Details zu einzelnen Fahrten

Wichtige Einschränkung:

- Die API liefert **keine kontinuierlichen Positionsdaten**
- Stattdessen werden:
    - Abfahrtszeiten
    - Haltestellenfolgen
    - ggf. Echtzeitprognosen bereitgestellt

Daraus wird die aktuelle Zugposition algorithmisch berechnet.

---

## 🧠 Grundidee der Positionsberechnung

Da keine GPS-Daten verfügbar sind, basiert die Positionsbestimmung auf einem Modell:

- Ein Zug bewegt sich entlang einer bekannten Strecke zwischen zwei Haltestellen
- Die Position wird aus folgenden Daten interpoliert:
    - geplanter oder prognostizierter Abfahrtszeitpunkt
    - aktuelle Uhrzeit
    - Fahrzeit zwischen Haltestellen

Das System entscheidet daraus:

- ob sich der Zug aktuell an einer Station befindet
- oder zwischen zwei Stationen unterwegs ist

---

## 🖥️ Technische Umsetzung (High-Level)

Die Anwendung wird als **Web-Applikation mit getrennter Architektur** umgesetzt:

### Backend
- Sprache: voraussichtlich Java
- Aufgaben:
    - Kommunikation mit der TRIAS-API
    - Verarbeitung und Zwischenspeicherung der Daten
    - Berechnung der Zugpositionen

### Frontend
- Technologie: Web (HTML, CSS, JavaScript)
- Aufgaben:
    - Darstellung der Netzkarte
    - Visualisierung der Züge
    - regelmäßige Aktualisierung der Anzeige

---

## 🔄 Aktualisierung

Die Daten werden in regelmäßigen Abständen aktualisiert:

- Intervall: ca. **alle 5 Sekunden**
- Ziel:
    - nahezu Echtzeit-Darstellung
    - gleichzeitig stabile Performance

---

## 🔮 Erweiterbarkeit (Hardware)

Die Software wird so konzipiert, dass sie später auf Hardware erweitert werden kann:

- Jede Station kann durch eine **physische LED** repräsentiert werden
- Zustände:
    - LED an → Zug an Station
    - separate LED → Zug zwischen Stationen

Die Software bildet dafür ein abstraktes Modell des Netzes, das unabhängig von der Darstellung (Bildschirm oder Hardware) genutzt werden kann.