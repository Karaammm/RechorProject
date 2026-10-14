package ch.epfl.rechor.timetable.demo;

import ch.epfl.rechor.journey.Vehicle;
import ch.epfl.rechor.timetable.Connections;
import ch.epfl.rechor.timetable.Platforms;
import ch.epfl.rechor.timetable.Routes;
import ch.epfl.rechor.timetable.StationAliases;
import ch.epfl.rechor.timetable.Stations;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.Transfers;
import ch.epfl.rechor.timetable.Trips;

import java.time.LocalDate;
import java.util.List;

/**
 * Small in-memory timetable used when no external snapshot is supplied.
 *
 * <p>The network is synthetic and deliberately small: EPFL, Renens, Lausanne,
 * and Geneva are connected by a bus and two trains. It is intended for trying
 * the application after cloning the repository, not for real journey planning.
 */
public final class DemoTimeTable implements TimeTable {
    private static final Stations STATIONS = new DemoStations();
    private static final StationAliases ALIASES = new EmptyAliases();
    private static final Platforms PLATFORMS = new EmptyPlatforms();
    private static final Routes ROUTES = new DemoRoutes();
    private static final Transfers TRANSFERS = new DemoTransfers();
    private static final Trips TRIPS = new DemoTrips();
    private static final Connections CONNECTIONS = new DemoConnections();

    private DemoTimeTable() {
    }

    public static TimeTable create() {
        return new DemoTimeTable();
    }

    @Override
    public Stations stations() {
        return STATIONS;
    }

    @Override
    public StationAliases stationAliases() {
        return ALIASES;
    }

    @Override
    public Platforms platforms() {
        return PLATFORMS;
    }

    @Override
    public Routes routes() {
        return ROUTES;
    }

    @Override
    public Transfers transfers() {
        return TRANSFERS;
    }

    @Override
    public Trips tripsFor(LocalDate date) {
        return TRIPS;
    }

    @Override
    public Connections connectionsFor(LocalDate date) {
        return CONNECTIONS;
    }

    private static final class DemoStations implements Stations {
        private static final List<String> NAMES =
                List.of("Ecublens VD, EPFL", "Renens VD", "Lausanne", "Genève");
        private static final double[] LONGITUDES = {6.566, 6.578, 6.633, 6.142};
        private static final double[] LATITUDES = {46.522, 46.537, 46.517, 46.210};

        @Override
        public String name(int id) {
            return NAMES.get(id);
        }

        @Override
        public double longitude(int id) {
            return LONGITUDES[id];
        }

        @Override
        public double latitude(int id) {
            return LATITUDES[id];
        }

        @Override
        public int size() {
            return NAMES.size();
        }
    }

    private static final class EmptyAliases implements StationAliases {
        @Override
        public String alias(int id) {
            throw new IndexOutOfBoundsException();
        }

        @Override
        public String stationName(int id) {
            throw new IndexOutOfBoundsException();
        }

        @Override
        public int size() {
            return 0;
        }
    }

    private static final class EmptyPlatforms implements Platforms {
        @Override
        public String name(int id) {
            throw new IndexOutOfBoundsException();
        }

        @Override
        public int stationId(int id) {
            throw new IndexOutOfBoundsException();
        }

        @Override
        public int size() {
            return 0;
        }
    }

    private static final class DemoRoutes implements Routes {
        private static final List<Vehicle> VEHICLES =
                List.of(Vehicle.BUS, Vehicle.TRAIN);
        private static final List<String> NAMES = List.of("Demo bus 1", "Demo train 1");

        @Override
        public Vehicle vehicle(int id) {
            return VEHICLES.get(id);
        }

        @Override
        public String name(int id) {
            return NAMES.get(id);
        }

        @Override
        public int size() {
            return NAMES.size();
        }
    }

    private static final class DemoTransfers implements Transfers {
        private static final int STATION_COUNT = 4;
        private static final int[] DEPARTURE_STATIONS =
                {1, 2, 3, 0, 2, 3, 0, 1, 3, 0, 1, 2};

        @Override
        public int depStationId(int id) {
            checkIndex(id);
            return DEPARTURE_STATIONS[id];
        }

        @Override
        public int minutes(int id) {
            checkIndex(id);
            return 5 + Math.abs(depStationId(id) - arrivalStationId(id)) * 5;
        }

        @Override
        public int arrivingAt(int stationId) {
            if (stationId < 0 || stationId >= STATION_COUNT) {
                throw new IndexOutOfBoundsException();
            }
            return (stationId * (STATION_COUNT - 1) << 8) | (STATION_COUNT - 1);
        }

        @Override
        public int minutesBetween(int depStationId, int arrStationId) {
            if (depStationId < 0 || depStationId >= STATION_COUNT
                    || arrStationId < 0 || arrStationId >= STATION_COUNT
                    || depStationId == arrStationId) {
                throw new IndexOutOfBoundsException();
            }
            return 5 + Math.abs(depStationId - arrStationId) * 5;
        }

        @Override
        public int size() {
            return STATION_COUNT * (STATION_COUNT - 1);
        }

        private int arrivalStationId(int id) {
            return id / (STATION_COUNT - 1);
        }

        private void checkIndex(int id) {
            if (id < 0 || id >= size()) {
                throw new IndexOutOfBoundsException();
            }
        }
    }

    private static final class DemoTrips implements Trips {
        @Override
        public int routeId(int id) {
            if (id < 0 || id >= size()) {
                throw new IndexOutOfBoundsException();
            }
            return id;
        }

        @Override
        public String destination(int id) {
            if (id < 0 || id >= size()) {
                throw new IndexOutOfBoundsException();
            }
            return id == 0 ? "Lausanne" : "Genève";
        }

        @Override
        public int size() {
            return 2;
        }
    }

    private static final class DemoConnections implements Connections {
        private static final int[] DEP_STOPS = {2, 1, 0};
        private static final int[] DEP_MINS = {570, 552, 540};
        private static final int[] ARR_STOPS = {3, 2, 1};
        private static final int[] ARR_MINS = {600, 562, 550};
        private static final int[] TRIP_IDS = {1, 0, 0};
        private static final int[] TRIP_POSITIONS = {0, 1, 0};
        private static final int[] NEXT_CONNECTIONS = {0, 2, 1};

        @Override
        public int depStopId(int id) {
            return value(DEP_STOPS, id);
        }

        @Override
        public int depMins(int id) {
            return value(DEP_MINS, id);
        }

        @Override
        public int arrStopId(int id) {
            return value(ARR_STOPS, id);
        }

        @Override
        public int arrMins(int id) {
            return value(ARR_MINS, id);
        }

        @Override
        public int tripId(int id) {
            return value(TRIP_IDS, id);
        }

        @Override
        public int tripPos(int id) {
            return value(TRIP_POSITIONS, id);
        }

        @Override
        public int nextConnectionId(int id) {
            return value(NEXT_CONNECTIONS, id);
        }

        @Override
        public int size() {
            return DEP_STOPS.length;
        }

        private int value(int[] values, int id) {
            return values[id];
        }
    }
}
