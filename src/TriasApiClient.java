import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TriasApiClient {

    /**
     * Schickt ein fertiges XML-Dokument an die TRIAS-API und gibt die Antwort als Text zurück.
     */
    public static String sendPostRequest(String xmlBody) {
        try {
            // 1. URL aus der Config laden
            String apiUrl = ConfigReader.getApiUrl();
            if (apiUrl == null || apiUrl.isEmpty()) {
                System.out.println("FEHLER: Keine API-URL in der config.properties gefunden!");
                return null;
            }

            // 2. Den HTTP-Client erstellen
            HttpClient client = HttpClient.newHttpClient();

            // 3. Den Request bauen (POST-Methode, korrekter Header)
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "text/xml")
                    .POST(HttpRequest.BodyPublishers.ofString(xmlBody))
                    .build();

            // 4. Request abschicken und auf die Antwort (Body) warten
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return response.body();

        } catch (Exception e) {
            System.out.println("Fehler beim API-Aufruf: " + e.getMessage());
            return null;
        }
    }
}