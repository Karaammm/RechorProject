package ch.epfl.rechor.gui_debug;

import ch.epfl.rechor.gui.DetailUI;
import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.JourneyExtractor;
import ch.epfl.rechor.journey.Profile;
import ch.epfl.rechor.journey.Router;
import ch.epfl.rechor.timetable.CachedTimeTable;
import ch.epfl.rechor.timetable.Stations;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.mapped.FileTimeTable;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

public final class DetailUITest extends Application {
    int stationId(Stations stations, String stationName) {
        for(int i = 0; i < stations.size(); i++){
            if(stations.name(i).equals(stationName)){
                return i;
            }
        }
        return -1;
    }
    @Override public void start (Stage primaryStage) throws Exception {
        long tStart = System.nanoTime();
        TimeTable timeTable = new CachedTimeTable(
            FileTimeTable.in(Path.of("timetable-16" )));
        Stations stations = timeTable.stations();
        LocalDate date = LocalDate.of(2025 , Month.APRIL, 15 );
        int depStationId = stationId(stations, "Bussigny" );
        int arrStationId = stationId(stations, "Gruyères" );
        Router router = new Router (timeTable);
        Profile profile = router.profile(date, arrStationId);
        List<Journey> journeys = JourneyExtractor.journeys(profile, depStationId);
//        Journey journey = null;
        Journey journey = journeys.get(32);
        SimpleObjectProperty<Journey> journeyO = new SimpleObjectProperty<>(journey);
        DetailUI detailUI = DetailUI.create(journeyO);
        Pane root = new BorderPane(detailUI.rootNode());
        primaryStage.setScene( new Scene(root));
        primaryStage.setMinWidth( 400 );
        primaryStage.setMinHeight( 600 );
        primaryStage.show();
        PauseTransition delay = new PauseTransition(Duration.seconds(2));
        delay.setOnFinished( e -> journeyO.set(null));
        delay.play();
        double elapsed = (System.nanoTime() - tStart) * 1e-9;
        System.out.printf("Temps écoulé : %.3f s%n", elapsed);
    }
}
