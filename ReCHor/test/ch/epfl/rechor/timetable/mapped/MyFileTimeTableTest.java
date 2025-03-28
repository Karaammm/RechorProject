package ch.epfl.rechor.timetable.mapped;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import ch.epfl.rechor.timetable.*;

public class MyFileTimeTableTest {

    Path timetablePath = Path.of("timetable");
    Path stringsPath = timetablePath.resolve("strings.txt");
    TimeTable table;
    List<String> stringTable;

    private void setup() throws IOException {
        stringTable = Files.readAllLines(stringsPath, StandardCharsets.ISO_8859_1);
        table = FileTimeTable.in(timetablePath);
    }

    @Test
    void fileTimeTableLoadsWithoutErrors() throws IOException {
        assertDoesNotThrow(() -> FileTimeTable.in(timetablePath));
    }

    @Test
    void fileTimeTableLoadsStationsCorrectly() throws IOException {
        setup();
        Stations stations = table.stations();
        // StringBuilder str = new StringBuilder();
        try (InputStream stream = new FileInputStream("timetable/stations.bin")) {
            byte[] allBytes = stream.readAllBytes();

            BufferedStations bufferedStations = new BufferedStations(stringTable, ByteBuffer.wrap(allBytes));
            // System.out.println(Arrays.toString(firstTen));
            for (int i = 0; i < bufferedStations.size(); i++) {
                // str.append(bufferedStations.name(i)).append(" |
                // ").append(stations.name(i)).append("\r\n");
                assertEquals(bufferedStations.name(i), stations.name(i));
                // assertEquals(bufferedStations.longitude(i), stations.longitude(i), 0);
                // assertEquals(bufferedStations.latitude(i), stations.latitude(i), 0);
                // str.append(bufferedStations.name(i)).append(" |
                // ").append(stations.name(i)).append("\r\n");
            }
            // System.out.println(str);
        }
    }

    @Test
    void fileTimeTableLoadsStationAliasesCorrectly() throws IOException {
        setup();
        StationAliases stationAliases = table.stationAliases();
        try (InputStream stream = new FileInputStream("timetable/station-aliases.bin")) {
            byte[] allBytes = stream.readAllBytes();
            BufferedStationAliases bufferedStationAliases = new BufferedStationAliases(stringTable,
                    ByteBuffer.wrap(allBytes));
            for (int i = 0; i < bufferedStationAliases.size(); i++) {
                assertEquals(bufferedStationAliases.alias(i), stationAliases.alias(i));
                assertEquals(bufferedStationAliases.stationName(i), stationAliases.stationName(i));
            }
        }
    }

    @Test
    void fileTimeTableLoadsPlatformsCorrectly() throws IOException {
        setup();
        Platforms platforms = table.platforms();
        try (InputStream stream = new FileInputStream("timetable/platforms.bin")) {
            byte[] allBytes = stream.readAllBytes();
            BufferedPlatforms bufferedPlatforms = new BufferedPlatforms(stringTable,
                    ByteBuffer.wrap(allBytes));
            for (int i = 0; i < bufferedPlatforms.size(); i++) {
                assertEquals(bufferedPlatforms.name(i), platforms.name(i));
                assertEquals(bufferedPlatforms.stationId(i), platforms.stationId(i));
            }
        }
    }

    @Test
    void fileTimeTableLoadsRoutesCorrectly() throws IOException {
        setup();
        Routes routes = table.routes();
        try (InputStream stream = new FileInputStream("timetable/routes.bin")) {
            byte[] allBytes = stream.readAllBytes();
            BufferedRoutes bufferedRoutes = new BufferedRoutes(stringTable, ByteBuffer.wrap(allBytes));
            for (int i = 0; i < bufferedRoutes.size(); i++) {
                assertEquals(bufferedRoutes.name(i), routes.name(i));
                assertEquals(bufferedRoutes.vehicle(i), routes.vehicle(i));
            }
        }
    }

    @Test
    void fileTimeTableLoadsTransfersCorrectly() throws IOException {
        setup();
        Transfers transfers = table.transfers();
        try (InputStream stream = new FileInputStream("timetable/transfers.bin")) {
            byte[] allBytes = stream.readAllBytes();
            BufferedTransfers bufferedTransfers = new BufferedTransfers(ByteBuffer.wrap(allBytes));
            for (int i = 0; i < bufferedTransfers.size(); i++) {
                assertEquals(bufferedTransfers.depStationId(i), transfers.depStationId(i));
                assertEquals(bufferedTransfers.minutes(i), transfers.minutes(i));
            }
        }
    }

    @Test
    void fileTimeTableLoadsTripsCorrectly() throws IOException {
        setup();
        Trips trips = table.tripsFor(LocalDate.of(2025, 03, 30));
        try (InputStream stream = new FileInputStream("timetable/2025-03-30/trips.bin")) {
            byte[] allBytes = stream.readAllBytes();
            BufferedTrips bufferedTrips = new BufferedTrips(stringTable, ByteBuffer.wrap(allBytes));
            for (int i = 0; i < bufferedTrips.size(); i++) {
                assertEquals(bufferedTrips.destination(i), trips.destination(i));
                assertEquals(bufferedTrips.routeId(i), trips.routeId(i));
            }
        }
    }

    @Test
    void fileTimeTableLoadsConnnectionsCorrectly() throws IOException {
        setup();
        Connections connections = table.connectionsFor(LocalDate.of(2025, 03, 30));
        try (InputStream stream1 = new FileInputStream("timetable/2025-03-30/connections.bin");
                InputStream stream2 = new FileInputStream("timetable/2025-03-30/connections-succ.bin")) {
            byte[] allBytes1 = stream1.readAllBytes();
            byte[] allBytes2 = stream2.readAllBytes();
            BufferedConnections bufferedConnections = new BufferedConnections(ByteBuffer.wrap(allBytes1),
                    ByteBuffer.wrap(allBytes2));
            for (int i = 0; i < bufferedConnections.size(); i++) {
                assertEquals(bufferedConnections.depStopId(i), connections.depStopId(i));
                assertEquals(bufferedConnections.depMins(i), connections.depMins(i));
                assertEquals(bufferedConnections.arrMins(i), connections.arrMins(i));
                assertEquals(bufferedConnections.arrStopId(i), connections.arrStopId(i));
                assertEquals(bufferedConnections.tripId(i), connections.tripId(i));
                assertEquals(bufferedConnections.tripPos(i), connections.tripPos(i));
                assertEquals(bufferedConnections.nextConnectionId(i), connections.nextConnectionId(i));
            }
        }
    }
}
