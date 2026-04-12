import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class ConfigReader {

    // Hier speichern wir die geladenen Daten
    private static final Properties properties = new Properties();

    // Der static-Block wird beim Start automatisch 1x ausgeführt
    static {
        try {
            FileInputStream in = new FileInputStream("config.properties");
            properties.load(in);
            in.close();
        } catch (IOException e) {
            System.out.println("KRITISCHER FEHLER: config.properties nicht gefunden! " + e.getMessage());
        }
    }

    // --- Deine Werkzeug-Methoden ---

    public static String getApiUrl() {
        return properties.getProperty("trias.url");
    }

    public static String getApiKey() {
        return properties.getProperty("trias.key");
    }

    public static List<String> getRadars() {
        String radarString = properties.getProperty("trias.radars");

        // Sicherheits-Check: Falls die Zeile in der Datei fehlt oder leer ist
        if (radarString == null || radarString.trim().isEmpty()) {
            return Collections.emptyList(); // Gibt eine leere Liste statt "null" zurück
        }

        // 1. Am Komma teilen -> ["de:08111:6118", " de:08111:6002", ...]
        // 2. Jedes Element "trimmen" (Leerzeichen vorne/hinten entfernen)
        // 3. Als fertige Liste zurückgeben
        return Arrays.stream(radarString.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }
}