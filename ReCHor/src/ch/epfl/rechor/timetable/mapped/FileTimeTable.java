package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

/**
 * @author Karam Fakhouri(374510)
 *         A public transport timetable whose (flattened) data is stored in
 *         files
 *        Private constructor for FileTimeTable, is only called when in() method is
 *       called
 *
 *      @param directory      the path to the folder containing the time data files
 *      @param stringTable    the table of character strings
 *      @param stations       the stations
 *      @param stationAliases alternative names of stations
 *      @param platforms      the tracks/platform
 *      @param routes         the lines
 *      @param transfers      the changes
 *
 */
public record FileTimeTable(Path directory, List<String> stringTable, Stations stations,
                            StationAliases stationAliases, Platforms platforms,
                            Routes routes, Transfers transfers) implements TimeTable{


    /**
     * 
     * @param directory the path to the folder containing the time data files
     * @return a new instance of FileTimeTable whose flattened data was obtained fom
     *         the files in the folder with the given path
     * @throws IOException on input/output error
     */
    public static TimeTable in(Path directory) throws IOException {
        Path stringsPath = directory.resolve("strings.txt");
        Path stationsPath = directory.resolve("stations.bin");
        Path stationAliasesPath = directory.resolve("station-aliases.bin");
        Path platformsPath = directory.resolve("platforms.bin");
        Path routesPath = directory.resolve("routes.bin");
        Path transfersPath = directory.resolve("transfers.bin");

        List<String> stringTable = List.copyOf(Files.readAllLines(stringsPath, StandardCharsets.ISO_8859_1));
        BufferedStations bufferedStations = new BufferedStations(stringTable, map(stationsPath));
        BufferedStationAliases bufferedStationAliases = new BufferedStationAliases(stringTable,
                map(stationAliasesPath));
        BufferedPlatforms bufferedPlatforms = new BufferedPlatforms(stringTable, map(platformsPath));
        BufferedRoutes bufferedRoutes = new BufferedRoutes(stringTable, map(routesPath));
        BufferedTransfers bufferedTransfers = new BufferedTransfers(map(transfersPath));

        return new FileTimeTable(directory, stringTable, bufferedStations,
                                 bufferedStationAliases, bufferedPlatforms,
                                 bufferedRoutes, bufferedTransfers);
    }

    /**
     * Helper method for cleanliness of in() method
     * 
     * @param path the path to the folder containing the wanted file
     * @return bytebuffer with the contents of the file
     * @throws IOException if the path is invalid
     */
    private static ByteBuffer map(Path path) throws IOException {
        try (FileChannel channel = FileChannel.open(path)) {
            return channel.map(MapMode.READ_ONLY, 0, channel.size());
        }
    }

    /**
     *
     * @return the indexed stations of the timetable
     */
    @Override
    public Stations stations() {
        return stations;
    }

    /**
     *
     * @return the indexed alternative name of the stations in the timetable
     */
    @Override
    public StationAliases stationAliases() {
        return stationAliases;
    }

    /**
     *
     * @return the indexed tracks/platforms of the timetable
     */
    @Override
    public Platforms platforms() {
        return platforms;
    }

    /**
     *
     * @return the indexed lines of the schedule
     */
    @Override
    public Routes routes() {
        return routes;
    }

    /**
     *
     * @return the indexed changes of the timetable
     */
    @Override
    public Transfers transfers() {
        return transfers;
    }

    /**
     *
     * @param date the day
     * @return the indexed trips on the active schedule of the given day
     */
    @Override
    public Trips tripsFor(LocalDate date) {
        Path daysPath = directory.resolve(date.toString());
        Path tripsPath = daysPath.resolve("trips.bin");
        try {
            return new BufferedTrips(stringTable, map(tripsPath));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     *
     * @param date the day
     * @return the indexed connections on the active schedule of the given day
     */
    @Override
    public Connections connectionsFor(LocalDate date) {
        Path daysPath = directory.resolve(date.toString());
        Path connectionsPath = daysPath.resolve("connections.bin");
        Path succConnectionsPath = daysPath.resolve("connections-succ.bin");
        try {
            return new BufferedConnections(map(connectionsPath), map(succConnectionsPath));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}