package ch.epfl.rechor.gui_debug;

import ch.epfl.rechor.StopIndex;
import ch.epfl.rechor.gui.QueryUI;
import ch.epfl.rechor.timetable.CachedTimeTable;
import ch.epfl.rechor.timetable.StationAliases;
import ch.epfl.rechor.timetable.Stations;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.mapped.FileTimeTable;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class QueryUITest extends Application {
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
        TimeTable table = new CachedTimeTable(FileTimeTable.in(Path.of("timetable-16")));
        Stations stations = table.stations();
        StationAliases altStations = table.stationAliases();
        List<String> stopNames = generateStopNames(stations);
        Map<String, String> altNames = generateAltNames(stations, altStations);

        StopIndex stopIndex = new StopIndex(stopNames,altNames);
        QueryUI queryUI = QueryUI.create(stopIndex);
        Pane root = new BorderPane(queryUI.rootNode());
        primaryStage.setScene( new Scene(root));
        primaryStage.setMinWidth( 400 );
        primaryStage.setMinHeight( 600 );
        primaryStage.show();

        double elapsed = (System.nanoTime() - tStart) * 1e-9;
        System.out.printf("Temps écoulé : %.3f s%n", elapsed);
    }

    private List<String> generateStopNames(Stations stations) {
        List<String> list = new ArrayList<>();
        for(int i = 0; i < stations.size(); i++){
            list.add(stations.name(i));
        }
        return list;
    }

    private Map<String, String> generateAltNames(Stations stations, StationAliases altStations) {
        Map<String,String> map = new HashMap<>();
        for(int i = 0; i < altStations.size(); i++){
            map.put(altStations.stationName(i), altStations.alias(i));
        }
        return map;
    }
}
