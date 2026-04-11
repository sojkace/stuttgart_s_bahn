🧾 Projektbeschreibung – Teil 1: Gesamtübersicht

🎯 Ziel des Projekts

Ziel dieses Projekts ist die Entwicklung einer Software-Anwendung, die das
S-Bahn-Netz des Verkehrs- und Tarifverbunds Stuttgart (VVS) in Form einer
schematischen Netzkarte visualisiert und diese Karte in Echtzeit mit
Betriebsdaten anreichert. Die Anwendung soll es ermöglichen, den aktuellen
Betriebszustand des S-Bahn-Netzes intuitiv zu erfassen, indem sichtbar gemacht
wird, an welcher Position sich einzelne Züge im Netz befinden.
Dabei liegt der Fokus nicht auf geographischer Genauigkeit, sondern auf einer
abstrahierten, leicht verständlichen Darstellung — vergleichbar mit klassischen
Liniennetzplänen.

🗺️ Art der Darstellung

Die Visualisierung basiert auf einer schematischen Netzkarte, die sich optisch
an der offiziellen VVS-Netzkarte orientiert. Folgende Eigenschaften sind
vorgesehen:

- Jede S-Bahn-Linie wird durch ihre typische Linienfarbe dargestellt
- Haltestellen werden als feste Punkte im Netz visualisiert
- Streckenverläufe sind abstrahiert (keine echte Geografie)
- Die Darstellung ist für eine Anzeige auf einem Bildschirm optimiert

🚆 Darstellung der Züge

Züge werden als dynamische Elemente auf der Netzkarte visualisiert. Dabei gelten
folgende Prinzipien:

- Ein Zug befindet sich immer in einem von zwei Zuständen:
    - an einer Station ("station")
    - zwischen zwei Stationen ("between")
- Die Position wird aus Echtzeit-Fahrplandaten abgeleitet (nicht aus GPS)
- Verspätungen werden indirekt berücksichtigt, indem sich die Position des Zuges
  zeitlich verzögert aktualisiert.

📡 Datenquelle & Pipeline (Architektur)

Die Anwendung nutzt die TRIAS-API (Version 1.2) der EFA Baden-Württemberg als
Datenquelle. Da die API keine kontinuierlichen GPS-Positionsdaten liefert,
erfolgt die Datenbeschaffung in einer zweistufigen Pipeline:

1. Stop Event Requests (SER) → Der "Radar"
    - Fragt allgemeine Abfahrten an einer Basis-Station ab.
    - Dient dazu, die IDs (JourneyRef) aller Züge zu finden, die aktuell im Netz unterwegs sind.
2. Trip Information Requests (TIR) → Das "Fernglas"
    - Fragt für jeden gefundenen Zug den exakten, individuellen Fahrtverlauf ab.
    - Dient dazu, die exakte nächste Station und die Ankunftszeit zu ermitteln.

🧠 Grundidee der Positionsberechnung (UTC-Logik)

Die Positionsbestimmung basiert auf einem mathematischen Zeit-Modell im Backend.
Um Bugs durch Zeitzonen oder Sommer-/Winterzeit zu vermeiden, rechnet das Backend
strikt in UTC (Java `Instant`).

- Die Position wird aus folgenden Daten interpoliert:
    - geplanter oder prognostizierter Abfahrtszeitpunkt (aus SER/TIR)
    - geplanter oder prognostizierter Ankunftszeitpunkt (aus TIR)
    - exakte aktuelle Uhrzeit in UTC (`Instant.now()`)
      Das System vergleicht die aktuelle Uhrzeit mit diesen Zeitpunkten und entscheidet
      daraus den finalen Zustand (`station` oder `between`).

🖥️ Technische Umsetzung (High-Level)

Die Anwendung wird als Web-Applikation mit getrennter Architektur umgesetzt:

Backend
- Sprache: Java 11+ (mit nativem `HttpClient`)
- Framework: Spring Boot (für die REST-API)
- Aufgaben:
    - Kommunikation mit der TRIAS-API via XML
    - Verarbeitung und Zwischenspeicherung der Daten
    - Berechnung der Zugpositionen
    - Bereitstellung der fertigen Daten als JSON

Frontend
- Technologie: Web (HTML, CSS, JavaScript, SVG)
- Aufgaben:
    - Darstellung der schematischen Netzkarte
    - Visualisierung der Züge als Marker
    - Regelmäßiger Abruf der Backend-Daten (fetch API)

🔄 Aktualisierung

Die Daten werden in regelmäßigen Abständen (ca. alle 5 Sekunden) aktualisiert,
um eine nahezu Echtzeit-Darstellung bei stabiler Performance zu gewährleisten.

🔮 Erweiterbarkeit (Hardware)

Die Software wird so konzipiert, dass sie später auf Hardware erweitert werden
kann:
- Jede Station kann durch eine physische LED repräsentiert werden
- Zustände: LED an (Zug an Station), separate LED (Zug auf Strecke)