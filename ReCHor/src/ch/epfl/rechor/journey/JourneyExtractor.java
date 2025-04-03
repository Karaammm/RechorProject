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
        int minsBetween = profile.timeTable().transfers().minutesBetween(depStopId,arrStopId);
        LocalDateTime arrTime = depTime.plusMinutes(minsBetween);
        return new Journey.Leg.Foot(depStop, depTime, arrStop, arrTime);
    }

    private static LocalDateTime initialiseDateTime(Profile profile, int mins) {
        //This method is to make initialising dates for stops easier, but im not sure if
        // the "plusDays" part is correct.
        return profile.date().plusDays(mins / (24 * 60))
                      .atTime((mins / 60) % 24, mins % 60);
    }

    private static Journey.Leg.IntermediateStop buildIntermediateStop(Profile profile,
                                                                      int connectionId) {
//        int stationId = stationOf(profile, connectionId, false);
//        Stop stop = buildStop(profile, stationId);
//        int depMins = profile.connections().depMins(connectionId);
//        int arrMins = profile.connections().arrMins(connectionId);
//        LocalDateTime depTime = initialiseDateTime(profile, depMins);
//        LocalDateTime arrTime = initialiseDateTime(profile, arrMins);
//        return new Journey.Leg.IntermediateStop(stop, depTime, arrTime);
        return null;
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

    private static int finalStopId(Profile profile, int connectionId, int numOfStops) {
        //this method is to find the last stop of a connection with numOfStops intermediate
        //stops
        int finalConnectionId = connectionId;
        for (int i = 0; i < numOfStops; i++) {
            finalConnectionId = profile.connections().nextConnectionId(finalConnectionId);
        }
        finalConnectionId = profile.connections().nextConnectionId(finalConnectionId);
        return profile.timeTable().stationId(profile.connections().arrStopId(finalConnectionId));

    }

    //helper method to build a transport leg
    private static Journey.Leg.Transport buildTransport(Profile profile, int connectionId,
                                                        long criteria) {


//        Stop depStop = buildStop(profile, depStationId);
//        int numOfStops = Bits32_24_8.unpack8(PackedCriteria.payload(criteria));
//        int lastStopId = finalStopId(profile, connectionId, numOfStops);
//        int arrStationId = stationOf(profile, lastStopId, false);
//        Stop arrStop = buildStop(profile, arrStationId);
//
//        int depMins = profile.connections().depMins(connectionId);
//        int arrMins = profile.connections().arrMins(connectionId);
//        LocalDateTime depTime = initialiseDateTime(profile, depMins);
//        LocalDateTime arrTime = initialiseDateTime(profile, arrMins);
//
//        List<Journey.Leg.IntermediateStop> intermediateStops;
//        intermediateStops = buildIntermediateStops(profile, connectionId, numOfStops);
//        int tripId = profile.connections().tripId(connectionId);
//        int routeId = profile.trips().routeId(tripId);
//        Vehicle vehicle = profile.timeTable().routes().vehicle(routeId);
//        String route = profile.timeTable().routes().name(routeId);
//        String destination = profile.trips().destination(tripId);
//        return new Journey.Leg.Transport(depStop, depTime, arrStop, arrTime,
//                                         intermediateStops, vehicle, route, destination);
        return null;
    }

    private static Journey buildJourney(Profile profile, Long criteria,
                                        int depStationId) {
        List<Journey.Leg> legs = new ArrayList<>();
        TimeTable timeTable = profile.timeTable();
        int payload = PackedCriteria.payload(criteria);
        int connectionId = Bits32_24_8.unpack24(payload);
        //here i made two IDs, one for the stop in case it is a platform, and one for
        //the stop making sure it is a station
        int stopId = profile.connections().depStopId(connectionId);
        int stationId = timeTable.stationId(stopId);
        // here I do the same but with the given depStationId
        int depStopId = timeTable.stationId(depStationId);
        int realDepStationId = timeTable.stationId(depStopId);
        int depMins = PackedCriteria.depMins(criteria);
        int changes = PackedCriteria.changes(criteria);
        boolean lastLegIsFoot = false;

        // sometimes the if statement doesnt work correctly? idk why that happens
        if(stationId == realDepStationId && stopId != depStopId) {
            legs.add(buildFootLeg(profile, depStopId, depMins, stopId));
            System.out.println("first is foot leg");
            lastLegIsFoot = true;
            //here we add a transport leg right after
        }
        while(changes >= 0){
            // here we check if the last leg is foot or not, and add a transport/foot leg
            // accordingly. Then we consult the pareto front with the same arrival minutes
            // but one less change and find the next criteria from there.
            changes--;
        }
        // now we check if the finalstation is the same as the arrivalstation and add a
        // foot leg accordingly

        return new Journey(legs);
    }
}


