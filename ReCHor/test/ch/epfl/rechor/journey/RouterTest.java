package ch.epfl.rechor.journey;

import ch.epfl.rechor.Bits32_24_8;
import ch.epfl.rechor.PackedRange;
import ch.epfl.rechor.timetable.CachedTimeTable;
import ch.epfl.rechor.timetable.Stations;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.mapped.FileTimeTable;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.StringJoiner;

import static org.junit.Assert.assertEquals;

public class RouterTest {

    static int stationId(Stations stations, String stationName) {
        for(int i = 0; i < stations.size(); i++){
            if(stations.name(i).equals(stationName)){
                return i;
            }
        }
        return -1;
    }

    @Test
    void duplicatesTest(){
        try {
            TimeTable timeTable = FileTimeTable.in(Path.of("timetable-14"));
            Stations stations = timeTable.stations();
            int test = stationId(stations, "Renens VD, gare");
            int arrivingat = timeTable.transfers().arrivingAt(test);
            for (int i = PackedRange.startInclusive(arrivingat); i < PackedRange.endExclusive(arrivingat); i ++){
                System.out.println("transfer nb: "+i + ": from: \"" + stations.name(timeTable.transfers().depStationId(i)) + "\" to: \"" +stations.name(test) + "\"");
                System.out.println("transfer nb: " + i + ": from \"" + timeTable.transfers().depStationId(i)  + "\" to: \"" + test + "\"");
                System.out.print(timeTable.transfers().minutes(i) + " ");
                System.out.println(timeTable.transfers().minutesBetween(timeTable.transfers().depStationId(i), test));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void routerWorksAndAverageTime(){
        try {
            double sum = 0L;
            for (int i = 0;i < 100; i++) {
                long tStart = System.nanoTime();
                TimeTable timeTable = new CachedTimeTable(FileTimeTable.in(Path.of("timetable-16")));
                Stations stations = timeTable.stations();
                LocalDate date = LocalDate.of(2025, Month.APRIL, 16);
                int depStationId = stationId(stations, "Ecublens VD, EPFL");
                int arrStationId = stationId(stations, "Gruyères");
                Router router = new Router(timeTable);
                Profile profile = router.profile(date, arrStationId);
                List<Journey> journey = JourneyExtractor.journeys(profile, depStationId);
                sum += (System.nanoTime() - tStart) * 1e-9;
            }
            System.out.println("Average time:" + sum / 100.0);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    void writingProfileOfWeek12(){
        long tStart = System.nanoTime();
        try {
            TimeTable timeTable = new CachedTimeTable(FileTimeTable.in(Path.of("timetable-12")));
            Stations stations = timeTable.stations();
            LocalDate date = LocalDate.of(2025, Month.MARCH, 18);
            int depStationId = stationId(stations, "Ecublens VD, EPFL");
            int arrStationId = stationId(stations, "Gruyères");
            Router router = new Router(timeTable);
            Profile profile = router.profile(date, arrStationId);

            //File file = new File("profile12_output.txt");
            BufferedWriter writer = new BufferedWriter(new FileWriter("profile12_output.txt", false));
            for(ParetoFront f : profile.stationFront()){
                StringJoiner lineBuilder = new StringJoiner(",");
                f.forEach(value -> {
//                    lineBuilder.append(String.format("%x", value)).append(",");
                    String hexString = Long.toHexString(value);
                    lineBuilder.add(hexString);
                });
//                if (!lineBuilder.isEmpty()) {
//                    lineBuilder.setLength(lineBuilder.length() - 1);
//                }
                writer.write(lineBuilder.toString());
                writer.newLine();
            }
//            int stationid = 33358;
//            List<Journey> journeys = JourneyExtractor.journeys(profile,stationid);
//            for(int i = 0; i < journeys.size(); i++){
//                System.out.println(JourneyIcalConverter.toIcalendar(journeys.get(i)));
//            }


        } catch (IOException e) {
            System.out.println("blahblah");
        }
        double elapsed = (System.nanoTime() - tStart) * 1e-9;
        System.out.printf("Temps écoulé : %.3f s%n", elapsed);
    }
    @Test
    void comparingParetos(){
            long expectedPareto = 0x609293861852bd16L;
            long actualPareto =   0x609293851852bd16L;

            assertEquals(PackedCriteria.arrMins(expectedPareto),
                                    PackedCriteria.arrMins(actualPareto));
            assertEquals(PackedCriteria.depMins(expectedPareto),
                         PackedCriteria.depMins(actualPareto));
            assertEquals(PackedCriteria.changes(expectedPareto),
                         PackedCriteria.changes(actualPareto));
            int expectedPayload = PackedCriteria.payload(expectedPareto);
            int actualPayload = PackedCriteria.payload(actualPareto);
            assertEquals(Bits32_24_8.unpack24(expectedPayload),Bits32_24_8.unpack24(actualPayload));
            assertEquals(Bits32_24_8.unpack8(expectedPayload), Bits32_24_8.unpack8(actualPayload));
    }

    @Test
    void comparingGeneratedParetos() {
        Path expected = Path.of("profile_2025-03-18_11486.txt");
        Path actual = Path.of("profile12_output.txt");
        String expectedLine;
        String actualLine;
        try (BufferedReader expectedReader = Files.newBufferedReader(
            expected); BufferedReader actualReader = Files.newBufferedReader(actual)) {
            while ((expectedLine = expectedReader.readLine()) != null &&  (actualLine = actualReader.readLine()) != null){
                assertEquals(expectedLine,actualLine);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void comparingToSBB(){
        try {
            TimeTable timeTable =  new CachedTimeTable(FileTimeTable.in(Path.of("timetable-16" )));
            Stations stations = timeTable.stations();
            LocalDate date = LocalDate.of(2025 , Month.APRIL, 16 );
            int depStationId = stationId(stations, "Zürich HB" );
            int arrStationId = stationId(stations, "Bussigny" );
            //int arrStationId = 33333;
            Router router = new Router (timeTable);
            Profile profile = router.profile(date, arrStationId);
            List<Journey> journeys = JourneyExtractor.journeys(profile, depStationId);
            for (Journey journey : journeys) {
                System.out.println(JourneyIcalConverter.toIcalendar(journey));
                System.out.println("Number of legs:" + journey.legs().size());
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
