import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
public class TriasXmlParser {

    /**
     * Liest die SER-XML-Datei und gibt eine Liste von S-Bahnen zurück.
     * (Zustand und nächste Station sind hier noch leer, die füllen wir später via TIR).
     */
    public static List<TrainIdentifier> parseSerResponse(String xmlText) {

        // System.out.println(xmlText);

        List<TrainIdentifier> foundTrains = new ArrayList<>();

        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(xmlText.getBytes(StandardCharsets.UTF_8));

            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);
            document.getDocumentElement().normalize();

            NodeList stopEvents = document.getElementsByTagName("trias:StopEvent");

            for (int i = 0; i < stopEvents.getLength(); i++) {
                Element event = (Element) stopEvents.item(i);

                // System.out.print("[Debug] Train found: " + event.getElementsByTagName("trias:JourneyRef"));

                // Check: Ist es eine S-Bahn?
                NodeList submodeNodes = event.getElementsByTagName("trias:RailSubmode");
                if (submodeNodes.getLength() > 0 && "suburbanRailway".equals(submodeNodes.item(0).getTextContent())) {

                    // 1. ID (JourneyRef)
                    String newTrainJourneyRef = "xxx";
                    NodeList idNodes = event.getElementsByTagName("trias:JourneyRef");
                    if (idNodes.getLength() > 0) {
                        newTrainJourneyRef = idNodes.item(0).getTextContent();
                    } else {
                        System.out.println("JourneyRef problem by radar search");
                    }

                    // System.out.print("[Debug] Train s-bahn found: " + train.getId());

                    // 2. Datum (OperatingDayRef - wichtig für den TIR später)
                    String newTrainOperatingDayRef = "xxx";
                    NodeList dateNodes = event.getElementsByTagName("trias:OperatingDayRef");
                    if (dateNodes.getLength() > 0) {
                        newTrainOperatingDayRef = dateNodes.item(0).getTextContent();
                    } else {
                        System.out.println("OperatingDayRef problem by radar search");
                    }


                    // Zug zur Liste hinzufügen
                    foundTrains.add(new TrainIdentifier(newTrainJourneyRef, newTrainOperatingDayRef));
                }
            }
        } catch (Exception e) {
            System.out.println("Fehler beim Parsen der XML: " + e.getMessage());
        }

        return foundTrains; // Gibt die gesammelte Liste zurück an das Hauptprogramm!
    }

    /**
     * Liest die TIR-XML-Antwort und füllt die nächste Station in das Train-Objekt.
     */
    public static Train parseTirResponse(String xmlText) {
        Train train = new Train();
        try {
            // System.out.println(xmlText);
            java.io.ByteArrayInputStream inputStream = new java.io.ByteArrayInputStream(xmlText.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            org.w3c.dom.Document document = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);
            document.getDocumentElement().normalize();

            Instant now = Instant.now();

            // --------------------------------------------------------
            // NEU: Allgemeine Zug-Metadaten aus dem <trias:Service> Block auslesen
            // --------------------------------------------------------
            NodeList serviceNodes = document.getElementsByTagName("trias:Service");
            if (serviceNodes.getLength() > 0) {
                Element serviceElement = (Element) serviceNodes.item(0);

                // 1. ID (JourneyRef)
                NodeList journeyRefNodes = serviceElement.getElementsByTagName("trias:JourneyRef");
                if (journeyRefNodes.getLength() > 0) {
                    train.setId(journeyRefNodes.item(0).getTextContent());
                }

                // 2. OperatingDayRef
                NodeList opDayNodes = serviceElement.getElementsByTagName("trias:OperatingDayRef");
                if (opDayNodes.getLength() > 0) {
                    train.setOperatingDayRef(opDayNodes.item(0).getTextContent());
                }

                // 3. Linie (z.B. "S1") - Nutzt deine Hilfsmethode!
                train.setLine(extractNestedText(serviceElement, "trias:PublishedLineName"));

                // 4. Richtung/Ziel (z.B. "Kirchheim (T)") - Nutzt deine Hilfsmethode!
                train.setDirection(extractNestedText(serviceElement, "trias:DestinationText"));
            }
            // --------------------------------------------------------

            // Beide Listen auslesen
            NodeList previousCalls = document.getElementsByTagName("trias:PreviousCall");
            NodeList onwardCalls = document.getElementsByTagName("trias:OnwardCall");

            int prevCount = previousCalls.getLength();
            int onwardCount = onwardCalls.getLength();

            // FALL 1: Zug hat sein Ziel erreicht (Es gibt keine Zukunft mehr)
            if (onwardCount == 0) {
                return null;
            }

            // FALL 2: Zug ist noch nicht abgefahren (Es gibt keine Vergangenheit)
            if (prevCount == 0) {
                Element firstOnward = (Element) onwardCalls.item(0);

                // Wir lesen die Abfahrtszeit erst in eine lokale Variable, um sie zu prüfen
                Instant departureTime = extractTimeFromCall(firstOnward, "trias:ServiceDeparture");

                // --- NEU: DER 5-MINUTEN FILTER ---
                if (departureTime != null) {
                    Instant inFiveMinutes = now.plus(Duration.ofMinutes(8));

                    if (departureTime.isAfter(inFiveMinutes)) {
                        // Die Abfahrt liegt weiter als 5 Minuten in der Zukunft.
                        // Wir geben false/null zurück -> Zug wird nicht aufs Frontend geschickt!
                        return null; // (bzw. return false; falls deine Methode boolean zurückgibt)
                    }
                }

                // Wenn der Zug den Filter bestanden hat, speichern wir die Daten im Objekt
                train.setStation(extractNestedText(firstOnward, "trias:StopPointName"));
                train.setDepartureTime(departureTime);

                // Gibt es schon eine zweite Station für die Vorschau?
                if (onwardCount > 1) {
                    Element secondOnward = (Element) onwardCalls.item(1);
                    train.setNextStation(extractNestedText(secondOnward, "trias:StopPointName"));
                    train.setArrivalTime(extractTimeFromCall(secondOnward, "trias:ServiceArrival"));
                }

                train.setState("station"); // Steht logischerweise am Start
            }
            // FALL 3: Zug ist unterwegs (Er hat eine Vergangenheit und eine Zukunft)
            else {
                // Letzte Station der Vergangenheit
                Element lastPrevious = (Element) previousCalls.item(prevCount - 1);
                // Erste Station der Zukunft
                Element firstOnward = (Element) onwardCalls.item(0);

                train.setStation(extractNestedText(lastPrevious, "trias:StopPointName"));
                train.setDepartureTime(extractTimeFromCall(lastPrevious, "trias:ServiceDeparture"));

                train.setNextStation(extractNestedText(firstOnward, "trias:StopPointName"));
                train.setArrivalTime(extractTimeFromCall(firstOnward, "trias:ServiceArrival"));

                train.setState("between"); // Zug ist strukturell auf der Strecke

                // --- DEINE GEWÜNSCHTE ZEIT-VALIDIERUNG FÜRS TERMINAL ---
                Instant dep = train.getDepartureTime();
                Instant arr = train.getArrivalTime();

                if (dep != null && arr != null) {
                    if (now.isBefore(dep)) {
                        System.out.println("⚠️ WARNUNG: API sagt Zug ist zwischen " + train.getStation() +
                                " und " + train.getNextStation() + ", aber aktuelle Zeit ist VOR der Abfahrt! (API-Lag?)");
                    } else if (now.isAfter(arr)) {
                        System.out.println("⚠️ WARNUNG: Zug auf dem Weg nach " + train.getNextStation() +
                                " sollte schon da sein! (Zug verspätet sich oder API hängt)");
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("Fehler beim Parsen der TIR: " + e.getMessage());
        }

        return train;
    }

    /**
     * Neue kleine Hilfsmethode, um Zeiten sauber aus einem Block zu holen
     */
    private static Instant extractTimeFromCall(Element call, String arrivalOrDepartureTag) {
        NodeList eventNodes = call.getElementsByTagName(arrivalOrDepartureTag);
        if (eventNodes.getLength() > 0) {
            Element eventElement = (Element) eventNodes.item(0);

            // 1. PRIORITÄT: Wir suchen zuerst nach der Echtzeit (Verspätung)!
            NodeList estimatedNodes = eventElement.getElementsByTagName("trias:EstimatedTime");
            if (estimatedNodes.getLength() > 0) {
                return Instant.parse(estimatedNodes.item(0).getTextContent());
            }

            // 2. FALLBACK: Nur wenn es keine Echtzeit gibt, nehmen wir den Fahrplan
            NodeList timetabledNodes = eventElement.getElementsByTagName("trias:TimetabledTime");
            if (timetabledNodes.getLength() > 0) {
                return Instant.parse(timetabledNodes.item(0).getTextContent());
            }
        }
        return null;
    }

    // Die Hilfsmethode bleibt unverändert, verliert aber das "public" (braucht draußen niemand)
    private static String extractNestedText(Element parent, String parentTagName) {
        NodeList parentNodes = parent.getElementsByTagName(parentTagName);
        if (parentNodes.getLength() > 0) {
            Element parentElement = (Element) parentNodes.item(0);
            NodeList textNodes = parentElement.getElementsByTagName("trias:Text");
            if (textNodes.getLength() > 0) {
                return textNodes.item(0).getTextContent();
            }
        }
        return "Unbekannt";
    }
}