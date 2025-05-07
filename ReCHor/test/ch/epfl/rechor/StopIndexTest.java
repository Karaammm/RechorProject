package ch.epfl.rechor;

import ch.epfl.rechor.timetable.CachedTimeTable;
import ch.epfl.rechor.timetable.StationAliases;
import ch.epfl.rechor.timetable.Stations;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.mapped.FileTimeTable;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StopIndexTest {

    @Test
    void testingPattern(){
        Pattern pattern = Pattern.compile("[A-Z][a-z]");
        Matcher matcher = pattern.matcher("GeEkIfic");
        for (int i = 0; i < 5; i++){
            System.out.println(matcher.find());
        }
    }

    @Test
    void givenTest(){
        List<String> stopNames = new ArrayList<>();
        stopNames.add("Mézières FR, village");
        stopNames.add("Mézières VD, village");
        stopNames.add("Mézery-près-Donneloye, village");
        stopNames.add("Charleville-Mézières");
        Map<String,String> altNames = new HashMap<>();
        altNames.put("Lausanne","Losana");
        StopIndex stopIndex = new StopIndex(stopNames,altNames);
        List<String> matches = stopIndex.stopsMatching("mez vil", 4);
        for(int i = 0; i < 4; i++){
            System.out.println(matches.get(i));
        }
    }

    @Test
    void largeTest(){
        try {
            TimeTable table = new CachedTimeTable(FileTimeTable.in(Path.of("timetable-16")));
            Stations stations = table.stations();
            StationAliases altStations = table.stationAliases();
            List<String> stopNames = generateStopNames(stations);
            Map<String, String> altNames = generateAltNames(stations,altStations);
            StopIndex stopIndex = new StopIndex(stopNames,altNames);
            List<String> results =  stopIndex.stopsMatching("Renens Gare", 12);
            for(String str : results) {
                System.out.println(str);
            }
        } catch (IOException e) {
            System.out.println(1);
            throw new RuntimeException(e);
        }
    }

    private Map<String, String> generateAltNames(Stations stations, StationAliases altStations) {
        Map<String,String> map = new HashMap<>();
        for(int i = 0; i < altStations.size(); i++){
            map.put(altStations.stationName(i), altStations.alias(i));
        }
        return map;
    }

    private List<String> generateStopNames(Stations stations) {
        List<String> list = new ArrayList<>();
        for(int i = 0; i < stations.size(); i++){
            list.add(stations.name(i));
        }
        return list;
    }
}
