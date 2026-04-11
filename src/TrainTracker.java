import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class TrainTracker {

    public static void main(String[] args) {
        System.out.println("🚀 Starte S-Bahn Live-Tracker...\n");

        try {
            // 1. Das ausgelagerte XML-Template aus der Datei lesen
            String serXmlTemplate = Files.readString(Path.of("ser_template.xml"));

            // 2. Dynamische Daten vorbereiten
            String apiKey = ConfigReader.getApiKey();
            String now = Instant.now().truncatedTo(ChronoUnit.SECONDS).toString();
            String stopId = "de:08111:6118";

            // 3. Die Platzhalter (%s) im Template füllen!
            // Reihenfolge: Timestamp, Key, StopId, DepArrTime
            String serXmlRequest = String.format(serXmlTemplate, now, apiKey, stopId, now);

            System.out.println(serXmlRequest);

            // 4. API anfunken
            System.out.println("📡 Sende Stop Event Request (SER) an TRIAS...");
            String responseXml = TriasApiClient.sendPostRequest(serXmlRequest);

            if (responseXml != null && !responseXml.isEmpty()) {

                // Kurzer Check, ob die API einen Fehler meldet
                if (responseXml.contains("STOPEVENT_NOEVENTFOUND")) {
                    System.out.println("ℹ️ Keine Abfahrten gefunden. (Ist es vielleicht mitten in der Nacht?)");
                    return; // Programm beenden
                }

                System.out.println("✅ Antwort erhalten! Parse Daten...\n");


                // 5. XML parsen (Radar)
                List<Train> trains = TriasXmlParser.parseSerResponse(responseXml);

                // NEU: Das TIR-Template laden
                String tirXmlTemplate = Files.readString(Path.of("tir_template.xml"));

                System.out.println("\n🔍 Analysiere Fahrtrouten (TIR) für " + trains.size() + " Züge...");

                // 6. Für JEDEN gefundenen Zug einen TIR abfeuern (Fernglas)
                for (Train t : trains) {
                    // Platzhalter füllen: Timestamp, Key, JourneyRef, OperatingDayRef
                    String tirRequest = String.format(tirXmlTemplate, now, apiKey, t.getId(), t.getOperatingDayRef());

                    // API anfunken
                    String tirResponse = TriasApiClient.sendPostRequest(tirRequest);

                    if (tirResponse != null && !tirResponse.isEmpty()) {
                        // Parser füllt die nächste Station in unser Objekt
                        TriasXmlParser.parseTirAndUpdateTrain(tirResponse, t);
                    }
                }

                // 7. Finales Ergebnis anzeigen
                System.out.println("\n🚆 --- AKTUELLE S-BAHN ZUSTÄNDE --- 🚆");

                for (Train t : trains) {
                    System.out.println(t);
                    System.out.println(t.getLine() + " -> " + t.getDirection());
                    System.out.println("   📍 Aktuell: " + t.getStation());
                    System.out.println("   ⏭️ Nächster Halt: " + t.getNextStation());
                    System.out.println("   🕒 Geplante Abfahrt: " + t.getDepartureTime() + "\n");
                }

            } else {
                System.out.println("❌ Keine oder fehlerhafte Antwort von der API erhalten.");
            }

        } catch (Exception e) {
            System.out.println("💥 Ein Fehler ist aufgetreten: " + e.getMessage());
            e.printStackTrace();
        }
    }
}