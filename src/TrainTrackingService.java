import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class TrainTrackingService {
    private Set<TrainIdentifier> activeTrains = new HashSet<>();
    private List<Train> currentTrains = new ArrayList<>();

    public void runRadar() {
        System.out.println("start Radars...");

        try {
            // 1. Das ausgelagerte XML-Template aus der Datei lesen
            String serXmlTemplate = Files.readString(Path.of("xmlRequests/ser_template.xml"));

            // 2. Dynamische Daten vorbereiten
            String apiKey = ConfigReader.getApiKey();
            String now = Instant.now().truncatedTo(ChronoUnit.SECONDS).toString();
            List<String> stopIds = ConfigReader.getRadars();

            for (String stopId : stopIds) {
                // 3. Die Platzhalter (%s) im Template füllen!
                // Reihenfolge: Timestamp, Key, StopId, DepArrTime
                String serXmlRequest = String.format(serXmlTemplate, now, apiKey, stopId, now);
                // 4. API anfunken
                System.out.println("Sende Stop Event Request (SER) for " + stopId);
                String responseXml = TriasApiClient.sendPostRequest(serXmlRequest);

                if (responseXml != null && !responseXml.isEmpty()) {
                    // Kurzer Check, ob die API einen Fehler meldet
                    if (responseXml.contains("STOPEVENT_NOEVENTFOUND")) {
                        System.out.println("no departure found at " + stopId);
                        return; // Programm beenden
                    }
                }

                System.out.println("reserved answer");

                // 5. XML parsen (Radar)
                List<TrainIdentifier> trainsIdent = TriasXmlParser.parseSerResponse(responseXml);

                activeTrains.addAll(trainsIdent);

            }

        } catch (Exception e) {
            System.out.println("[Error] runRadars");
            e.printStackTrace();
        }
    }

    public void runBinoculars() {
        try {
            // Das TIR-Template laden
            String tirXmlTemplate = Files.readString(Path.of("xmlRequests/tir_template.xml"));

            String apiKey = ConfigReader.getApiKey();
            String now = Instant.now().truncatedTo(ChronoUnit.SECONDS).toString();

            currentTrains.clear();

            // 6. Für JEDEN gefundenen Zug einen TIR abfeuern (Fernglas)
            for (TrainIdentifier trainIdent : this.activeTrains) {
                // Platzhalter füllen: Timestamp, Key, JourneyRef, OperatingDayRef
                String tirRequest = String.format(tirXmlTemplate, now, apiKey, trainIdent.getJourneyRef(), trainIdent.getOperatingDayRef());

                // API anfunken
                String tirResponse = TriasApiClient.sendPostRequest(tirRequest);

                if (tirResponse != null && !tirResponse.isEmpty()) {
                    Train newTrain = TriasXmlParser.parseTirResponse(tirResponse);

                    if (newTrain != null) {
                        currentTrains.add(newTrain);
                    }
                }

            }

        } catch (Exception e) {
            System.out.println("[Error] runBinoculars");
            e.printStackTrace();
        }

    }

    public void printTrains() {
        System.out.println("=== AKTUELL BEKANNTE ZÜGE (" + currentTrains.size() + ") ===");

        currentTrains.stream()
                // Sortiert nach departureTime. Falls departureTime null ist, ans Ende packen.
                .sorted(Comparator.comparing(Train::getDepartureTime, Comparator.nullsLast(Comparator.naturalOrder())))
                .forEach(t -> {
                    System.out.println(t.getLine() + " -> " + t.getDirection());
                    System.out.println("   📍 Aktuell: " + t.getStation());
                    System.out.println("   ⏭️ Nächster Halt: " + t.getNextStation());
                    System.out.println("   🕒 Abgefahren: " + t.getDepartureTime());
                    System.out.println("   🕒 geplante Ankunft : " + t.getArrivalTime() + "\n");
                });
    }

    public void printTrainIdentifier() {
        System.out.println("=== AKTIVE ZUG-IDs IM HASHSET (" + activeTrains.size() + ") ===");

        activeTrains.stream()
                // Alphabetisch nach der Zug-ID sortieren
                .sorted(Comparator.comparing(TrainIdentifier::getJourneyRef))
                .forEach(t -> {
                    System.out.println("ID: " + t.getJourneyRef() + " | Tag: " + t.getOperatingDayRef());
                });
        System.out.println(); // Leerzeile am Ende
    }
}
