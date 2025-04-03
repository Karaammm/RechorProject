package ch.epfl.rechor.journey;

import ch.epfl.rechor.Bits32_24_8;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class JourneyExtractor {

    public static List<Journey> journeys(Profile profile, int depStationId) {
        List<Journey> journeys = new ArrayList<>();
        ParetoFront pf = profile.forStation(depStationId);
        System.out.println("Number of elements in ParetoFront: " + pf.size());
        pf.forEach((long criteria) -> {
            journeys.add(buildJourney(profile, criteria, depStationId));
            System.out.println("journey added");
        });
        journeys.sort(
            Comparator.comparing(Journey::depTime).thenComparing(Journey::arrTime));
        return journeys;
    }

    private static int stationOf(Profile profile, int connectionId, boolean isDepStation) {
        return (isDepStation) ?
            profile.timeTable().stationId(profile.connections().depStopId(connectionId))
            : profile.timeTable().stationId(profile.connections().arrStopId(connectionId));
    }
    private static Stop buildStop(Profile profile, int stationId) {
        String stopName = profile.timeTable().stations().name(stationId);
        String platformName = profile.timeTable().isPlatformId(stationId) ?
            profile.timeTable().platformName(stationId) :
            "";
        double longitude = profile.timeTable().stations().longitude(stationId);
        double latitude = profile.timeTable().stations().latitude(stationId);
        return new Stop(stopName, platformName, longitude, latitude);
    }

    private static Journey.Leg.Foot buildFootLeg(Profile profile, int depStationId, int depMins, int connectionId) {
        Stop depStop = buildStop(profile, depStationId);
        int arrStationId = stationOf(profile, connectionId, true);
        Stop arrStop = buildStop(profile, arrStationId);
        LocalDateTime depTime = initialiseDateTime(profile, depMins);
        int minsBetween = profile.timeTable().transfers().minutesBetween(depStationId, arrStationId);
        LocalDateTime arrTime =depTime.plusMinutes(minsBetween);

        return new Journey.Leg.Foot(depStop, depTime, arrStop, arrTime);
    }
    private static LocalDateTime initialiseDateTime(Profile profile, int mins) {
        return profile.date().plusDays(mins / (24 * 60))
                      .atTime((mins / 60) % 24, mins % 60);
    }

    private static Journey.Leg.IntermediateStop buildIntermediateStop(Profile profile,
                                                                      int connectionId) {
        int stationId = stationOf(profile, connectionId, false);
        Stop stop = buildStop(profile, stationId);
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

    private static int finalStopId(Profile profile, int connectionId, int numOfStops) {
        int finalConnectionId = connectionId;
        for (int i = 0; i < numOfStops; i++) {
            finalConnectionId = profile.connections().nextConnectionId(finalConnectionId);
        }
        finalConnectionId = profile.connections().nextConnectionId(finalConnectionId);
        return stationOf(profile, finalConnectionId, false);
    }

    private static Journey.Leg.Transport buildTransport(Profile profile, int connectionId,
                                                        long criteria) {
        int depStationId = stationOf(profile, connectionId, true);
        Stop depStop = buildStop(profile, depStationId);
        int numOfStops = Bits32_24_8.unpack8(PackedCriteria.payload(criteria));
        int lastStopId = finalStopId(profile,connectionId,numOfStops);
        int arrStationId = stationOf(profile, lastStopId, false);
        Stop arrStop = buildStop(profile, arrStationId);

        int depMins = profile.connections().depMins(connectionId);
        int arrMins = profile.connections().arrMins(connectionId);
        LocalDateTime depTime = initialiseDateTime(profile, depMins);
        LocalDateTime arrTime = initialiseDateTime(profile, arrMins);
        System.out.println("transport dep time: " + depTime);

        List<Journey.Leg.IntermediateStop> intermediateStops;
        intermediateStops = buildIntermediateStops(profile, connectionId, numOfStops);
        int tripId = profile.connections().tripId(connectionId);
        int routeId = profile.trips().routeId(tripId);
        Vehicle vehicle = profile.timeTable().routes().vehicle(routeId);
        String route = profile.timeTable().routes().name(routeId);
        String destination = profile.trips().destination(tripId);
        return new Journey.Leg.Transport(depStop,depTime,arrStop,arrTime
                                    ,intermediateStops,vehicle,route,destination);
    }

    private static Journey buildJourney(Profile profile, Long criteria, int depStationId) {
        List<Journey.Leg> legs = new ArrayList<>();

        int payload = PackedCriteria.payload(criteria);
        int connectionId = Bits32_24_8.unpack24(payload);
        int connectionDepMins = profile.connections().depMins(connectionId);
        int stationId = stationOf(profile, connectionId, true);
        int depMins = PackedCriteria.depMins(criteria);
        boolean lastLegIsFoot = false;

        System.out.println("Debug Info:");
        System.out.println("DepStationId: " + depStationId);
        System.out.println("FirstConnectionDepStationId: " + stationId);
        if(depStationId != stationId && depMins != connectionDepMins) {
            System.out.println("first station is foot");
            legs.add(buildFootLeg(profile,depStationId, depMins,connectionId));
            legs.add(buildTransport(profile,connectionId,criteria));
        }
        for(Journey.Leg leg : legs) {
            if(leg instanceof Journey.Leg.Transport) {
                System.out.println("this leg is transport");
            } else {
                System.out.println("this leg is foot");
            }
        }
        System.out.println("Number of changes:" + PackedCriteria.changes(criteria));
        return new Journey(legs);
    }
}


