🧾 Projektbeschreibung – Datenstruktur & Logik (Aktualisiert)

🚆 Train-Objekt

Das Train-Objekt ist extrem leichtgewichtig und enthält nur einfache Datentypen (Strings und Instants), um es für das Frontend und mögliche Hardware-Erweiterungen simpel zu halten.

Allgemeine Felder
* id (String) → Eindeutige Fahrt-ID (aus TRIAS: JourneyRef)
* line (String) → Linienname (z. B. "S1")
* direction (String) → Zielbahnhof (z. B. "Kirchheim (Teck)")
* operatingDayRef (String) → Das Betriebsdatum (intern nötig für API-Abfragen)

Zustands-Feld
* state (String) → Der aktuelle Zustand des Zuges. Nimmt exakt zwei Werte an: "station" oder "between".

🟢 Zustand: "station"
* Bedeutung: Zug befindet sich aktuell an einer Station
* Frontend: Punkt direkt auf der Station
* Hardware: LED der Station leuchtet

🔵 Zustand: "between"
* Bedeutung: Zug befindet sich zwischen zwei Stationen
* Frontend: Punkt auf der Strecke zwischen `station` und `nextStation`
* Hardware: Strecken-LED leuchtet

Positions- und Zeitdaten (UTC)
Wir arbeiten im Backend strikt mit `java.time.Instant` (UTC / Zulu-Zeit), um Zeitzonen-Probleme zu vermeiden.
* station (String) → Die Station, an der der Zug steht ODER die er zuletzt verlassen hat
* nextStation (String) → Die nächste Station (nur relevant, wenn Zug fährt)
* departureTime (Instant) → Abfahrtszeit an `station`
* arrivalTime (Instant) → Ankunftszeit an `nextStation`

---

🧠 Ableitungslogik (Backend Pipeline)

Die Datenbeschaffung erfolgt in einer Zwei-Stufen-Pipeline:

1. Der Radar (Stop Event Request - SER)
* Fragt allgemeine Abfahrten an einer Station ab (z. B. Stuttgart Hbf).
* Ziel: Findet heraus, WELCHE Züge in der Nähe sind.
* Füllt: `id`, `line`, `direction`, `operatingDayRef`.

2. Das Fernglas (Trip Information Request - TIR)
* Fragt für jeden gefundenen Zug den exakten, individuellen Fahrtverlauf ab.
* Ziel: Findet heraus, WO GENAU auf dem Zeitstrahl sich der Zug JETZT befindet.
* Füllt: `station`, `nextStation`, `departureTime`, `arrivalTime`.

Entscheidungslogik für den Zustand (State)
Gegeben sind die Zeiten aus dem TIR und die aktuelle Zeit (`Instant.now()`).

Regeln:
* Wenn aktuelle Zeit < Abfahrtszeit: → state = "station" (Zug wartet noch)
* Wenn aktuelle Zeit >= Abfahrtszeit UND < Ankunftszeit: → state = "between" (Zug fährt gerade)
* Wenn aktuelle Zeit >= Ankunftszeit: → state = "station" (Zug ist bereits angekommen)

---

🧩 Designentscheidungen

Warum keine exakte Position (GPS)?
* TRIAS liefert keine GPS-Daten
* Für schematische Visualisierung und LEDs nicht notwendig

Warum UTC (Instant)?
* Verhindert Bugs bei Sommer-/Winterzeit.
* Das Frontend (Browser) kann UTC bei Bedarf automatisch in die lokale User-Zeit umwandeln.

⚠️ Regeln für die Implementierung
* Keine verschachtelten komplexen Objekte im Train-Modell
* Klare Zustände verwenden ("station", "between")