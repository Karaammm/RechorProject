package ch.epfl.rechor.gui_debug;

import ch.epfl.rechor.gui.SummaryUI;
import ch.epfl.rechor.journey.*;
import ch.epfl.rechor.timetable.CachedTimeTable;
import ch.epfl.rechor.timetable.Stations;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.mapped.FileTimeTable;
import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.util.List;

public final class SummaryUITest extends Application {
    int stationId(Stations stations, String stationName) {
        for(int i = 0; i < stations.size(); i++){
            if(stations.name(i).equals(stationName)){
                return i;
            }
        }
        return -1;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        TimeTable timeTable = new CachedTimeTable(
            FileTimeTable.in(Path.of("timetable-18" )));
        Stations stations = timeTable.stations();
        LocalDate date = LocalDate.of(2025 , Month.APRIL, 29 );
        int depStationId = stationId(stations, "Ecublens VD, EPFL" );
        int arrStationId = stationId(stations, "Renens VD");
        Router router = new Router (timeTable);
        Profile profile = router.profile(date, arrStationId);

        List<Journey> journeys = JourneyExtractor
            .journeys(profile, depStationId);
        System.out.println(journeys.size());
        System.out.println(JourneyIcalConverter.toIcalendar(journeys.getLast()));
        ObservableValue<List<Journey>> journeysO =
            new SimpleObjectProperty<>(journeys);
        ObservableValue<LocalTime> depTimeO =
            new SimpleObjectProperty<>(LocalTime.of(0, 0));
        SummaryUI summaryUI = SummaryUI.create(journeysO, depTimeO);
        Pane root = new BorderPane(summaryUI.rootNode());
        primaryStage.setScene( new Scene(root));
        primaryStage.setMinWidth( 400 );
        primaryStage.setMinHeight( 600 );
        primaryStage.show();
    }
}
