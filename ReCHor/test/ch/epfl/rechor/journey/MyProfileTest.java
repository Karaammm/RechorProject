package ch.epfl.rechor.journey;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;

import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.*;
import ch.epfl.rechor.timetable.mapped.BufferedConnections;
import ch.epfl.rechor.timetable.mapped.BufferedTransfers;
import ch.epfl.rechor.timetable.mapped.FileTimeTable;

public class MyProfileTest {

    Path timetablePath = Path.of("timetable");
    Path stringsPath = timetablePath.resolve("strings.txt");
    TimeTable timeTable;
    List<String> stringTable;
    int arrStationId = 0;
    LocalDate date = LocalDate.of(2025, 03, 30);
    Profile profile;
    Profile.Builder builder;

    private void setup() throws IOException {
        stringTable = Files.readAllLines(stringsPath, StandardCharsets.ISO_8859_1);
        timeTable = FileTimeTable.in(timetablePath);
        builder = new Profile.Builder(timeTable, date, arrStationId);
        profile = builder.build();
    }

    @Test
    void profileConnectionsWorks() throws IOException {
        setup();
        assertEquals(timeTable.connectionsFor(date).depStopId(arrStationId),
                profile.connections().depStopId(arrStationId));

        assertEquals(timeTable.connectionsFor(date).depMins(arrStationId),
                profile.connections().depMins(arrStationId));
    }

    @Test
    void profileTripsWorks() throws IOException {
        setup();
        assertEquals(timeTable.tripsFor(date).destination(arrStationId), profile.trips().destination(arrStationId));
        assertEquals(timeTable.tripsFor(date).routeId(arrStationId), profile.trips().routeId(arrStationId));
    }

    // @Test
    void forStationThrowsCorrectly() throws IOException {
        // should throw IOOBE, but throws null pointer because builder isnt completed
        // yet
        assertThrows(IndexOutOfBoundsException.class, () -> {
            profile.forStation(arrStationId);
        });
    }
}
