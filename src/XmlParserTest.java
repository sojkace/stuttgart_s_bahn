import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.time.Instant;

public class XmlParserTest {

    public static void main(String[] args) {
        try {
            File xmlFile = new File("xmlResponses/SER_response.xml");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);
            document.getDocumentElement().normalize();

            System.out.println("XML geladen! Extrahiere Train-Daten...\n");

            NodeList stopEvents = document.getElementsByTagName("trias:StopEvent");

            for (int i = 0; i < stopEvents.getLength(); i++) {
                Element event = (Element) stopEvents.item(i);

                // 1. Check: Ist es eine S-Bahn? (Offizieller Weg)
                NodeList submodeNodes = event.getElementsByTagName("trias:RailSubmode");
                if (submodeNodes.getLength() > 0 && "suburbanRailway".equals(submodeNodes.item(0).getTextContent())) {

                    // 2. Daten für das Train-Objekt sammeln

                    // ID (JourneyRef hat kein verschachteltes Text-Tag)
                    String id = "Unbekannt";
                    NodeList idNodes = event.getElementsByTagName("trias:JourneyRef");
                    if (idNodes.getLength() > 0) id = idNodes.item(0).getTextContent();

                    // Linie, Richtung und Station (haben alle ein verschachteltes <trias:Text> Tag)
                    String line = extractNestedText(event, "trias:PublishedLineName");
                    String direction = extractNestedText(event, "trias:DestinationText");
                    String station = extractNestedText(event, "trias:StopPointName");

                    // Abfahrtszeit in UTC (Instant)
                    Instant departureTime = null;
                    NodeList timeNodes = event.getElementsByTagName("trias:TimetabledTime");
                    if (timeNodes.getLength() > 0) {
                        departureTime = Instant.parse(timeNodes.item(0).getTextContent());
                    }

                    // 3. Ausgabe zur Kontrolle
                    System.out.println("🚆 ZUG GEFUNDEN:");
                    System.out.println("   ID:        " + id);
                    System.out.println("   Linie:     " + line);
                    System.out.println("   Richtung:  " + direction);
                    System.out.println("   Station:   " + station);
                    System.out.println("   Abfahrt:   " + departureTime + " (UTC)");
                    System.out.println("-------------------------------------------------");
                }
            }
        } catch (Exception e) {
            System.out.println("Fehler: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Hilfsmethode: Sucht nach einem Tag (z.B. trias:DestinationText) und
     * holt den Text aus dem darin liegenden <trias:Text> Tag.
     */
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