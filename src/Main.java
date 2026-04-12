import java.time.LocalDateTime;

public class Main {

    public static void main(String[] args) {
        TrainTrackingService trainTrackingService = new TrainTrackingService();
        trainTrackingService.runRadar();

        trainTrackingService.printTrainIdentifier();

        trainTrackingService.runBinoculars();

        trainTrackingService.printTrains();

    }
}