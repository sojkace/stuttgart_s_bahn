import java.time.LocalDateTime;

public class Main {

    public static void main(String[] args) {
        // Wir nehmen die exakt jetzige Uhrzeit als unsere "aktuelle Zeit"
        LocalDateTime now = LocalDateTime.now();

        System.out.println("--- S-Bahn Tracker Testlauf ---");

        // --- Szenario 1: Zug wartet an der Station ---
        // Abfahrt ist in 5 Minuten, Ankunft in 10 Minuten
        LocalDateTime departure1 = now.plusMinutes(5);
        LocalDateTime arrival1 = now.plusMinutes(10);

        String state1 = calculateTrainState(departure1, arrival1, now);
        Train train1 = new Train("101", "S1", "Kirchheim (Teck)", state1, "Stuttgart Hbf", "Stadtmitte");
        System.out.println(train1.toString());


        // --- Szenario 2: Zug ist gerade unterwegs ---
        // Abfahrt war vor 3 Minuten, Ankunft ist in 2 Minuten
        LocalDateTime departure2 = now.minusMinutes(3);
        LocalDateTime arrival2 = now.plusMinutes(2);

        String state2 = calculateTrainState(departure2, arrival2, now);
        Train train2 = new Train("102", "S2", "Filderstadt", state2, "Rotebühlplatz", "Feuersee");
        System.out.println(train2.toString());


        // --- Szenario 3: Zug ist an der Zielstation angekommen ---
        // Abfahrt war vor 10 Minuten, Ankunft war vor 2 Minuten
        LocalDateTime departure3 = now.minusMinutes(10);
        LocalDateTime arrival3 = now.minusMinutes(2);

        String state3 = calculateTrainState(departure3, arrival3, now);
        // Da er angekommen ist, ist die "nextStation" (Feuerbach) nun die aktuelle Station.
        Train train3 = new Train("103", "S4", "Marbach", state3, "Feuerbach", "Zuffenhausen");
        System.out.println(train3.toString());
    }

    /**
     * Das ist die Kern-Logik aus deinem Konzept!
     * Hier berechnen wir den Zustand basierend auf den Zeiten.
     */
    public static String calculateTrainState(LocalDateTime departure, LocalDateTime arrival, LocalDateTime current) {

        // Regel 1: Wenn aktuelle Zeit < Abfahrtszeit -> Zug ist an Station A
        if (current.isBefore(departure)) {
            return "station";
        }
        // Regel 2: Wenn aktuelle Zeit >= Abfahrtszeit UND < Ankunftszeit -> Zug ist zwischen A und B
        else if ((current.isEqual(departure) || current.isAfter(departure)) && current.isBefore(arrival)) {
            return "between";
        }
        // Regel 3: Wenn aktuelle Zeit >= Ankunftszeit -> Zug ist an Station B
        else {
            return "station";
        }
    }
}