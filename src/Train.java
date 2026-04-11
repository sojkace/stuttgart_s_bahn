import java.time.Instant;

public class Train {
    // Allgemeine Felder
    private String id;
    private String line;       // z.B. "S1"
    private String direction;  // z.B. "Kirchheim (Teck)"

    // Das Herzstück: Der Zustand ("station" oder "between")
    private String state;

    // Positionsdaten
    private String station;    // Aktuelle Station (wenn state="station") oder letzte Station
    private String nextStation; // Nur relevant, wenn state="between"

    private Instant departureTime;
    private Instant arrivalTime;
    private String operatingDayRef;

    // --- Konstruktoren ---

    // Leerer Konstruktor (wichtig für später, wenn Spring Boot daraus JSON macht)
    public Train() {
    }

    // Konstruktor mit allen Feldern für schnelles Erstellen
    public Train(String id, String line, String direction, String state, String station, String nextStation) {
        this.id = id;
        this.line = line;
        this.direction = direction;
        this.state = state;
        this.station = station;
        this.nextStation = nextStation;
    }

    // --- Getters und Setters ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getLine() { return line; }
    public void setLine(String line) { this.line = line; }

    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getStation() { return station; }
    public void setStation(String station) { this.station = station; }

    public String getNextStation() { return nextStation; }
    public void setNextStation(String nextStation) { this.nextStation = nextStation; }

    public Instant getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(Instant departureTime) {
        this.departureTime = departureTime;
    }

    public String getOperatingDayRef() {
        return operatingDayRef;
    }

    public void setOperatingDayRef(String operatingDayRef) {
        this.operatingDayRef = operatingDayRef;
    }

    public Instant getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(Instant arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    // --- toString() Methode ---
    // Diese ist extrem hilfreich, um den Zug in der Konsole lesbar auszugeben
//    @Override
//    public String toString() {
//        if ("station".equals(state)) {
//            return "Zug " + line + " (ID: " + id + ") Richtung " + direction +
//                    " -> Steht aktuell an Station: " + station;
//        } else {
//            return "Zug " + line + " (ID: " + id + ") Richtung " + direction +
//                    " -> Fährt gerade von " + station + " nach " + nextStation;
//        }
//    }


    @Override
    public String toString() {
        return "Train{" +
                "id='" + id + '\'' +
                ", line='" + line + '\'' +
                ", direction='" + direction + '\'' +
                ", state='" + state + '\'' +
                ", station='" + station + '\'' +
                ", nextStation='" + nextStation + '\'' +
                ", departureTime=" + departureTime +
                ", arrivalTime=" + arrivalTime +
                ", operatingDayRef='" + operatingDayRef + '\'' +
                '}';
    }
}