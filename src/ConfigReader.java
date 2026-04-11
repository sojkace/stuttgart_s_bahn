import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

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
}