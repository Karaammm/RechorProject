package ch.epfl.rechor.gui;

import ch.epfl.rechor.StopIndex;
import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.JourneyExtractor;
import ch.epfl.rechor.journey.Profile;
import ch.epfl.rechor.journey.Router;
import ch.epfl.rechor.timetable.CachedTimeTable;
import ch.epfl.rechor.timetable.StationAliases;
import ch.epfl.rechor.timetable.Stations;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.mapped.FileTimeTable;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Main class serves as the entry point for the ReCHor application. It
 * initializes the graphical user interface (GUI) and sets up the necessary data
 * bindings to display journey information based on user input.
 *
 * The application allows users to query journeys between stations, view
 * summaries of available journeys, and inspect detailed information about
 * selected journeys.
 */
public class Main extends Application {

    /**
     * An observable value containing the list of journeys to be displayed in the
     * GUI.
     */
    ObservableValue<List<Journey>> journeysListObs;

    /**
     * The main method launches the JavaFX application.
     *
     * @param args Command-line arguments passed to the application.
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Initializes and starts the JavaFX application.
     *
     * @param primaryStage The primary stage for the application.
     * @throws Exception If an error occurs during initialization.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {

        Path timetablePath = Path.of("timetable-19");
        TimeTable timetable = new CachedTimeTable(FileTimeTable.in(timetablePath));
        Stations stations = timetable.stations();
        StationAliases stationAliases = timetable.stationAliases();
        List<String> stationNames = generateStationNames(stations);
        Map<String, String> altStationNames = generateAltNames(stations, stationAliases);
        StopIndex stopIndex = new StopIndex(stationNames, altStationNames);
        QueryUI queryUI = QueryUI.create(stopIndex);
        ObservableValue<Profile> profileObs = Bindings.createObjectBinding(() -> {
            String arrName = queryUI.arrStopO().getValue();
            LocalDate date = queryUI.dateO().getValue();
            int arrId = stationId(stations, arrName);
            if (arrName == null || arrName.isBlank() || arrId == -1)
                return null;
            return new Router(timetable).profile(date, arrId);
        }, queryUI.arrStopO(), queryUI.dateO());

        journeysListObs = Bindings.createObjectBinding(() -> {
            String depName = queryUI.depStopO().getValue();
            int depId = stationId(stations, depName);

            Profile profile = profileObs.getValue();
            if (profile == null || depId == -1)
                return List.of();

            return JourneyExtractor.journeys(profile, depId);
        }, queryUI.depStopO(), profileObs);

        SummaryUI summaryUI = SummaryUI.create(journeysListObs, queryUI.timeO());
        DetailUI detailUI = DetailUI.create(summaryUI.journeyObs());

        SplitPane splitPane = new SplitPane(summaryUI.rootNode(), detailUI.rootNode());
        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(splitPane);
        borderPane.setTop(queryUI.rootNode());
        Scene scene = new Scene(borderPane);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.setTitle("ReCHor");
        primaryStage.show();

        Platform.runLater(() -> scene.lookup("#depStop").requestFocus());
    }

    /**
     * Generates a list of station names from the given Stations object.
     *
     * @param stations The Stations object containing station data.
     * @return A list of station names.
     */
    private List<String> generateStationNames(Stations stations) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < stations.size(); i++) {
            list.add(stations.name(i));
        }
        return list;
    }

    /**
     * Generates a map of alternative station names (aliases) from the given
     * Stations and StationAliases objects.
     *
     * @param stations    The Stations object containing station data.
     * @param altStations The StationAliases object containing alias data.
     * @return A map where the key is the station name and the value is its alias.
     */
    private Map<String, String> generateAltNames(Stations stations, StationAliases altStations) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < altStations.size(); i++) {
            map.put(altStations.stationName(i), altStations.alias(i));
        }
        return map;
    }

    /**
     * Retrieves the ID of a station based on its name.
     *
     * @param stations    The Stations object containing station data.
     * @param stationName The name of the station.
     * @return The ID of the station, or -1 if the station name is null, blank, or
     *         not found.
     */
    private int stationId(Stations stations, String stationName) {
        if (stationName == null || stationName.isBlank())
            return -1;
        for (int i = 0; i < stations.size(); i++) {
            if (stations.name(i).equals(stationName)) {
                return i;
            }
        }
        return -1;
    }

}
