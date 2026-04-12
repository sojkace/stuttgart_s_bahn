import java.util.Objects;

public class TrainIdentifier {
    private final String journeyRef;
    private final String operatingDayRef;

    public TrainIdentifier(String journeyRef, String operatingDayRef) {
        this.journeyRef = journeyRef;
        this.operatingDayRef = operatingDayRef;
    }

    public String getJourneyRef() {
        return journeyRef;
    }

    public String getOperatingDayRef() {
        return operatingDayRef;
    }

    // EXTREM WICHTIG für das HashSet! 
    // Daran erkennt das Set, ob es diesen Zug schon einmal gespeichert hat.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrainIdentifier that = (TrainIdentifier) o;
        // Zwei Züge sind gleich, wenn sowohl JourneyRef als auch OperatingDayRef übereinstimmen
        return Objects.equals(journeyRef, that.journeyRef) &&
                Objects.equals(operatingDayRef, that.operatingDayRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(journeyRef, operatingDayRef);
    }

    @Override
    public String toString() {
        return "TrainIdentifier{" +
                "journeyRef='" + journeyRef + '\'' +
                ", operatingDayRef='" + operatingDayRef + '\'' +
                '}';
    }
}