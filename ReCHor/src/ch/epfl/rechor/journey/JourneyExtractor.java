package ch.epfl.rechor.journey;

import ch.epfl.rechor.timetable.Connections;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.Trips;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Extracting Journeys
 *
 * @author Ibrahim Khokher(361860)
 */
public final class JourneyExtractor {

    /**
     * Private constructor to make so it can't initialise
     */
    private JourneyExtractor() {}

    /**
     *
     * @param profile given profile
     * @param depStationId if of departure station
     * @return List of Journeys from a given profile and a departure point
     */
    public static List<Journey> journeys(Profile profile, int depStationId) {
        List<Journey> journeys = new ArrayList<>();
        ParetoFront paretoFront = profile.forStation(depStationId);
        TimeTable timetable = profile.timeTable();
        int finalArrivalStationId = profile.arrStationId();
        Trips trips = timetable.tripsFor(profile.date());
        Connections connections = profile.connections();

        paretoFront.forEach(criteria -> {
            List<Journey.Leg> legs = new ArrayList<>();
            int arrMins = PackedCriteria.arrMins(criteria);
            int changes = PackedCriteria.changes(criteria);
            int payload = PackedCriteria.payload(criteria);
            int depMins = PackedCriteria.hasDepMins(criteria) ? PackedCriteria.depMins(criteria) : 0;

            int currentStation = depStationId;
            boolean isFirstLeg = true;

            while (payload != 0) {
                int firstConnectionId = payload >> 8;
                int numOfPasses = payload & 0xFF;

                if (isFirstLeg && firstConnectionId != currentStation) {
                    legs.add(createFootLeg(timetable, profile.date(), currentStation, firstConnectionId, depMins));
                    currentStation = firstConnectionId;
                    isFirstLeg = false;
                }

                while (changes > 0) {
                    List<Journey.Leg.IntermediateStop> intermediateStops = new ArrayList<>();
                    int intermediateStopId = 0;

                    for(int i = 0; i <= numOfPasses; i++){
                        intermediateStopId = connections.nextConnectionId(firstConnectionId);

                        LocalDateTime arrtime = profile.date().atTime((depMins + timetable.transfers().minutesBetween(firstConnectionId, intermediateStopId)) / 60, (depMins + timetable.transfers().minutesBetween(firstConnectionId, intermediateStopId)) % 60);
                        Journey.Leg.IntermediateStop intstop = new Journey.Leg.IntermediateStop(createStop(timetable, intermediateStopId), arrtime, profile.date().atTime(connections.depMins(firstConnectionId) / 60, connections.depMins(firstConnectionId) % 60));
                        intermediateStops.add(intstop);
                    }

                    int nextStationId = intermediateStopId;
                    int nextArrivalTime = connections.arrMins(firstConnectionId);

                    if(nextStationId == firstConnectionId){
                        legs.add(createFootLeg(timetable, profile.date(),firstConnectionId, firstConnectionId, depMins));
                    }else{

                        legs.add(createTransportLeg(timetable, profile.date(), connections, intermediateStops, firstConnectionId, nextStationId, trips));

                    }

                    firstConnectionId = nextStationId;
                    depMins = nextArrivalTime;
                    changes--;
                }
            }

            if (currentStation != finalArrivalStationId) {
                legs.add(createFootLeg(timetable, profile.date(), currentStation, finalArrivalStationId, arrMins));
            }

            journeys.add(new Journey(legs));
        });

        journeys.sort(Comparator.comparing(Journey::depTime).thenComparing(Journey::arrTime));
        return journeys;
    }

    /**
     * Helper method to create a walking leg
     * @param timetable given timetable
     * @param date given date
     * @param from departure station id
     * @param to arrival station id
     * @param depMins departure time
     * @return an instance of a walking leg
     */
    private static Journey.Leg.Foot createFootLeg(TimeTable timetable, LocalDate date, int from, int to, int depMins) {
        return new Journey.Leg.Foot(
                createStop(timetable, from),
                date.atTime(depMins / 60, depMins % 60),
                createStop(timetable, to),
                date.atTime((depMins + timetable.transfers().minutesBetween(from, to)) / 60,
                        (depMins + timetable.transfers().minutesBetween(from, to)) % 60)
        );
    }

    /**
     * Helper method to create a transport leg
     * @param timetable given timetable
     * @param date given date
     * @param connections given connections
     * @param intermediateStops list of intermediate stops
     * @param from departure station id
     * @param to arrival station id
     * @return an instance of a transport leg
     */
    private static Journey.Leg.Transport createTransportLeg(TimeTable timetable, LocalDate date, Connections connections,
                                                            List<Journey.Leg.IntermediateStop> intermediateStops,
                                                            int from, int to, Trips trips) {
        return new Journey.Leg.Transport(
                createStop(timetable, from),
                date.atTime(connections.depMins(from) / 60, connections.depMins(from) % 60),
                createStop(timetable, to),
                date.atTime(connections.arrMins(from) / 60, connections.arrMins(from) % 60),
                intermediateStops,
                timetable.routes().vehicle(from),
                timetable.routes().toString(),
                trips.destination(from)
        );
    }

    /**
     * Helper method to create an instance of a stop
     * @param timetable given timetable
     * @param stationId given id of a station
     * @return an instance of a stop
     */
    private static Stop createStop(TimeTable timetable, int stationId) {
        return new Stop(
                timetable.stations().name(stationId),
                timetable.isPlatformId(stationId) ? timetable.platformName(stationId) : "",
                timetable.stations().longitude(stationId),
                timetable.stations().latitude(stationId)
        );
    }
}
