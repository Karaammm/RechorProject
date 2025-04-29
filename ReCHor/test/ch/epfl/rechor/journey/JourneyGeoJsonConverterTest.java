package ch.epfl.rechor.journey;

import ch.epfl.rechor.timetable.CachedTimeTable;
import ch.epfl.rechor.timetable.Stations;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.mapped.FileTimeTable;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

public class JourneyGeoJsonConverterTest {

    static int stationId(Stations stations, String stationName) {
        for(int i = 0; i < stations.size(); i++){
            if(stations.name(i).equals(stationName)){
                return i;
            }
        }
        return -1;
    }

    @Test
    void converterPassesGivenExample(){
        try {
            TimeTable timeTable = new CachedTimeTable(FileTimeTable.in(Path.of("timetable-16")));
            Stations stations = timeTable.stations();
            LocalDate date = LocalDate.of(2025, Month.APRIL, 16);
            int depStationId = stationId(stations, "Ecublens VD, EPFL");
            int arrStationId = stationId(stations, "Renens VD, gare");
            Router router = new Router(timeTable);
            Profile profile = router.profile(date, arrStationId);
            List<Journey> journeys = JourneyExtractor.journeys(profile, depStationId);
            Journey journey1 = journeys.get(4);
            System.out.println(JourneyIcalConverter.toIcalendar(journey1));
            System.out.println(JourneyGeoJsonConverter.toGeoJson(journey1));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void converterPassesSomeOtherExamples(){
        try {
            TimeTable timeTable = new CachedTimeTable(FileTimeTable.in(Path.of("timetable-16")));
            Stations stations = timeTable.stations();
            LocalDate date = LocalDate.of(2025, Month.APRIL, 16);
            int depStationId = stationId(stations, "Ecublens VD, EPFL");
            int arrStationId = stationId(stations, "Gruyères");
            Router router = new Router(timeTable);
            Profile profile = router.profile(date, arrStationId);
            List<Journey> journeys = JourneyExtractor.journeys(profile, depStationId);
            Journey journey1 = journeys.get(4);
            System.out.println(JourneyIcalConverter.toIcalendar(journey1));
            System.out.println(JourneyGeoJsonConverter.toGeoJson(journey1));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void javafxhasbeenadded(){
        Color c = Color.RED;
        System.out.println(c.getRed());
    }
}
