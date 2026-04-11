import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
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
    public static List<Train> parseSerResponse(String xmlText) {

        // System.out.println(xmlText);

        List<Train> foundTrains = new ArrayList<>();

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

                    Train train = new Train();

                    // 1. ID (JourneyRef)
                    NodeList idNodes = event.getElementsByTagName("trias:JourneyRef");
                    if (idNodes.getLength() > 0) train.setId(idNodes.item(0).getTextContent());

                    // System.out.print("[Debug] Train s-bahn found: " + train.getId());

                    // 2. Datum (OperatingDayRef - wichtig für den TIR später)
                    NodeList dateNodes = event.getElementsByTagName("trias:OperatingDayRef");
                    if (dateNodes.getLength() > 0) train.setOperatingDayRef(dateNodes.item(0).getTextContent());

                    // 3. Texte extrahieren
                    train.setLine(extractNestedText(event, "trias:PublishedLineName"));
                    train.setDirection(extractNestedText(event, "trias:DestinationText"));


                    // Vorerst leere Felder (werden durch TIR und Logik gefüllt)
                    train.setState("unknown");
                    train.setNextStation("unknown");

                    // Zug zur Liste hinzufügen
                    foundTrains.add(train);
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
    public static void parseTirAndUpdateTrain(String xmlText, Train train) {
        try {
            java.io.ByteArrayInputStream inputStream = new java.io.ByteArrayInputStream(xmlText.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            org.w3c.dom.Document document = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);
            document.getDocumentElement().normalize();

            Instant now = Instant.now();

            // Wir schauen uns NUR die OnwardCalls (kommende und aktuelle Halte) an.
            // Die API sortiert diese Liste chronologisch. Das erste Element in der Zukunft
            // ist unsere nächste Station. Das Element davor ist die letzte/aktuelle Station.
            NodeList onwardCalls = document.getElementsByTagName("trias:OnwardCall");

            for (int i = 0; i < onwardCalls.getLength(); i++) {
                Element call = (Element) onwardCalls.item(i);

                // Ankunfts- und Abfahrtszeiten an DIESER Station suchen
                Instant arrivalTime = extractTimeFromCall(call, "trias:ServiceArrival");
                Instant departureTime = extractTimeFromCall(call, "trias:ServiceDeparture");
                String stationName = extractNestedText(call, "trias:StopPointName");

                // Fallback: Manchmal fehlt die Ankunft (erste Station) oder Abfahrt (letzte Station)
                Instant timeToCompare = (arrivalTime != null) ? arrivalTime : departureTime;

                // Logik: Ist dieser Halt in der Zukunft?
                if (timeToCompare != null && timeToCompare.isAfter(now)) {

                    // Wir haben die NÄCHSTE Station in der Zukunft gefunden!
                    train.setNextStation(stationName);
                    train.setArrivalTime(arrivalTime); // Ankunft an der Zielstation

                    // Wenn es einen Halt DAVOR in der Liste gibt, ist das unsere AKTUELLE/LETZTE Station
                    if (i > 0) {
                        Element previousCall = (Element) onwardCalls.item(i - 1);
                        train.setStation(extractNestedText(previousCall, "trias:StopPointName"));
                        train.setDepartureTime(extractTimeFromCall(previousCall, "trias:ServiceDeparture"));
                    } else {
                        // Wenn es das allererste Element ist, steht der Zug noch an der Startstation
                        train.setStation(stationName);
                        train.setDepartureTime(departureTime);
                    }

                    break; // Wir haben gefunden, wo wir auf dem Zeitstrahl sind -> Abbruch!
                }
            }

            // 3. ZUSTAND BERECHNEN (Deine Logik aus dem Konzept!)
            if (train.getDepartureTime() != null && train.getArrivalTime() != null) {
                if (now.isBefore(train.getDepartureTime())) {
                    train.setState("station");
                } else if ((now.equals(train.getDepartureTime()) || now.isAfter(train.getDepartureTime()))
                        && now.isBefore(train.getArrivalTime())) {
                    train.setState("between");
                } else {
                    train.setState("station");
                }
            } else {
                // Fallback, falls Zeiten fehlen
                train.setState("station");
            }

        } catch (Exception e) {
            System.out.println("Fehler beim Parsen der TIR: " + e.getMessage());
        }
    }

    /**
     * Neue kleine Hilfsmethode, um Zeiten sauber aus einem Block zu holen
     */
    private static Instant extractTimeFromCall(Element call, String arrivalOrDepartureTag) {
        NodeList eventNodes = call.getElementsByTagName(arrivalOrDepartureTag);
        if (eventNodes.getLength() > 0) {
            Element eventElement = (Element) eventNodes.item(0);
            NodeList timeNodes = eventElement.getElementsByTagName("trias:TimetabledTime");
            if (timeNodes.getLength() > 0) {
                return Instant.parse(timeNodes.item(0).getTextContent());
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