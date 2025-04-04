package ch.epfl.rechor.journey;

import ch.epfl.rechor.Bits32_24_8;
import ch.epfl.rechor.timetable.TimeTable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
/**
 * Extracting Journeys
 *
 * @author Karam Fakhouri (374510)
 */
public abstract class JourneyExtractor {

    /**
     * @param profile      given profile
     * @param depStationId ID of departure station
     * @return List of Journeys from a given profile and a departure point
     */
    public static List<Journey> journeys(Profile profile, int depStationId) {
        List<Journey> journeys = new ArrayList<>();
        ParetoFront pf = profile.forStation(depStationId);
        int counter = 0;
        pf.forEach((long criteria) -> {
            journeys.add(buildJourney(profile, criteria, depStationId));
            System.out.println("journey added");

        });
        journeys.sort(
            Comparator.comparing(Journey::depTime).thenComparing(Journey::arrTime));
        return journeys;
    }

    private static Stop buildStop(TimeTable table, int stopId) {
        int stationId;
        String platformName;
        if(table.isPlatformId(stopId)) {
            stationId = table.stationId(stopId);
            platformName = table.platformName(stopId);
        } else {
            stationId = stopId;
            platformName = "";
        }
        String stationName = table.stations().name(stationId);
        double longitude = table.stations().longitude(stationId);
        double latitude = table.stations().latitude(stationId);
        return new Stop(stationName, platformName, longitude, latitude);
    }

    private static Journey.Leg.Foot buildFootLeg(Profile profile, int depStopId,
                                                 int depMins, int arrStopId) {
        Stop depStop = buildStop(profile.timeTable(), depStopId);
        Stop arrStop = buildStop(profile.timeTable(), arrStopId);
        LocalDateTime depTime = initialiseDateTime(profile,depMins);
        //this is almost always throwing an IndexOutOfBoundsException
        System.out.println("depStopId:" + depStopId);
        System.out.println("arrStopId:" + arrStopId);
        int minsBetween = profile.timeTable().transfers().minutesBetween(depStopId,arrStopId);
        LocalDateTime arrTime = depTime.plusMinutes(minsBetween);
        System.out.println(depTime);
        System.out.println(arrTime);
        return new Journey.Leg.Foot(depStop, depTime, arrStop, arrTime);
    }

    private static LocalDateTime initialiseDateTime(Profile profile, int mins) {
        //This method is to make initialising dates for stops easier, but im not sure if
        // the "plusDays" part is correct.
        return profile.date().atTime((mins / 60) % 24, mins % 60).plusMinutes(mins / 1440);
    }

    private static Journey.Leg.IntermediateStop buildIntermediateStop(Profile profile,
                                                                      int connectionId) {
        int stopId = profile.connections().arrStopId(connectionId);
        int stationId = profile.timeTable().stationId(stopId);
        Stop stop = buildStop(profile.timeTable(), stationId);
        int depMins = profile.connections().depMins(connectionId);
        int arrMins = profile.connections().arrMins(connectionId);
        LocalDateTime depTime = initialiseDateTime(profile, depMins);
        LocalDateTime arrTime = initialiseDateTime(profile, arrMins);
        return new Journey.Leg.IntermediateStop(stop, depTime, arrTime);
    }

    private static List<Journey.Leg.IntermediateStop> buildIntermediateStops(
        Profile profile, int connectionId, int NumOfStops) {
        List<Journey.Leg.IntermediateStop> stops = new ArrayList<>();
        int nextConnectionId = connectionId;
        for (int i = 0; i < NumOfStops; i++) {
            stops.add(buildIntermediateStop(profile, nextConnectionId));
            nextConnectionId = profile.connections().nextConnectionId(nextConnectionId);
        }
        return stops;
    }
    private static int finalConnectionId(Profile profile, int connectionid, int numOfStops) {
        int finalConnectionId = connectionid;
        for (int i = 0; i < numOfStops; i++) {
            finalConnectionId = profile.connections().nextConnectionId(finalConnectionId);
        }
        return finalConnectionId;
    }

    private static Journey.Leg.Transport buildTransport(Profile profile, int connectionId,
                                                        long criteria) {
        TimeTable timeTable = profile.timeTable();
        int depStopId = profile.connections().depStopId(connectionId);
        int depStationId = timeTable.stationId(depStopId);
        Stop depStop = buildStop(timeTable, depStationId);
        int numOfStops = Bits32_24_8.unpack8(PackedCriteria.payload(criteria));
        int lastConnectionId = finalConnectionId(profile, connectionId, numOfStops);
        int lastStopId = profile.connections().arrStopId(lastConnectionId);
        int arrStationId = timeTable.stationId(lastStopId);
        Stop arrStop = buildStop(timeTable, arrStationId);

        int depMins = profile.connections().depMins(connectionId);
        int arrMins = profile.connections().arrMins(lastConnectionId);
        LocalDateTime depTime = initialiseDateTime(profile, depMins);
        LocalDateTime arrTime = initialiseDateTime(profile, arrMins);
        System.out.println(depTime);
        System.out.println(arrTime);

        List<Journey.Leg.IntermediateStop> intermediateStops;
        intermediateStops = buildIntermediateStops(profile, connectionId, numOfStops);
        int tripId = profile.connections().tripId(connectionId);
        int routeId = profile.trips().routeId(tripId);
        Vehicle vehicle = profile.timeTable().routes().vehicle(routeId);
        String route = profile.timeTable().routes().name(routeId);
        String destination = profile.trips().destination(tripId);
        return new Journey.Leg.Transport(depStop, depTime, arrStop, arrTime,
                                         intermediateStops, vehicle, route, destination);
    }

    private static Journey buildJourney(Profile profile, Long criteria,
                                        int depStationId) {
        List<Journey.Leg> legs = new ArrayList<>();
        TimeTable timeTable = profile.timeTable();
        int payload = PackedCriteria.payload(criteria);
        int connectionId = Bits32_24_8.unpack24(payload);
        int stopId = profile.connections().depStopId(connectionId);
        int stationId = timeTable.stationId(stopId);
        int depMins = PackedCriteria.depMins(criteria);
        int changes = PackedCriteria.changes(criteria);
        boolean lastLegIsFoot = false;

        if(depStationId != stationId) {
            legs.add(buildFootLeg(profile, depStationId, depMins, stationId));
            System.out.println("foot added");
            legs.add(buildTransport(profile, connectionId, criteria));
            changes--;
        }
        while(changes >= 0){
            if(lastLegIsFoot || legs.isEmpty()) {
                legs.add(buildTransport(profile, connectionId, criteria));
                System.out.println("transport added");
                lastLegIsFoot = false;
            } else {
                buildFootLeg(profile,depStationId, depMins, stationId);
                System.out.println("foot added");
                lastLegIsFoot = true;
            }
                changes--;
        }
        //adjust criteria

        // now we check if the finalstation is the same as the arrivalstation and add a
        // foot leg accordingly

        return new Journey(legs);
    }
}


