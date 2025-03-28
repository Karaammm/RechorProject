package ch.epfl.rechor.journey;

import ch.epfl.rechor.timetable.Connections;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.Trips;

import java.awt.event.PaintEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class JourneyExtractor {

    private JourneyExtractor(){}

    public static List<Journey> journeys(Profile profile, int depStationId){
        List<Journey> journeys = new ArrayList<>();
        ParetoFront paretoFront = profile.forStation(depStationId);
        TimeTable timetable = profile.timeTable();
        int finalArrivalStationId = profile.arrStationId();

        Trips trips = timetable.tripsFor(profile.date());
        Connections connections = profile.connections();

        paretoFront.forEach((long criteria) -> {
            List<Journey.Leg> legs = new ArrayList<>();

            int arrMins = PackedCriteria.arrMins(criteria);
            int changes = PackedCriteria.changes(criteria);
            int payload = PackedCriteria.payload(criteria);
            int depMins = PackedCriteria.hasDepMins(criteria) ? PackedCriteria.depMins(criteria) : 0;

           int firstStation = depStationId;
           int isFirst = 0;

            while(payload != 0){
                int firstConnectionId  = payload >> 8;
                int numOfPasses = payload & 0x000000FF;

                if (firstConnectionId != firstStation && isFirst == 0) {
                    String platformName = timetable.isPlatformId(firstStation) ? timetable.platformName(firstStation) : "";
                    String platformNameS = timetable.isPlatformId(firstConnectionId) ? timetable.platformName(firstConnectionId) : "";

                    int timeBetween = depMins + timetable.transfers().minutesBetween(firstStation, firstConnectionId);

                    Journey.Leg.Foot foot = new Journey.Leg.Foot(
                            new Stop(
                                    timetable.stations().name(firstStation),
                                    platformName,
                                    timetable.stations().longitude(firstStation),
                                    timetable.stations().latitude(firstStation)
                            ),
                            profile.date().atTime(depMins / 60, depMins % 60),
                            new Stop(
                                    timetable.stations().name(firstConnectionId),
                                    platformNameS,
                                    timetable.stations().longitude(firstConnectionId),
                                    timetable.stations().latitude(firstConnectionId)
                            ),
                            profile.date().atTime(timeBetween / 60, timeBetween % 60)
                    );


                    firstStation = firstConnectionId;
                    legs.add(foot);
                    isFirst++;
                }

                int intermediateArrivalStationId = 0;

                while(changes != 0){
                    List<Journey.Leg.IntermediateStop> intermediateStops = new ArrayList<>();

                    int nextStationID = connections.nextConnectionId(firstConnectionId);
                    int nextArrivalTime = connections.arrMins(firstConnectionId);

                    String platformName = timetable.isPlatformId(firstConnectionId) ? timetable.platformName(firstConnectionId) : "";
                    String secondPlatformName = timetable.isPlatformId(nextStationID) ? timetable.platformName(nextStationID) : "";

                    if(firstConnectionId != nextStationID){
                        Journey.Leg.Transport transport = new Journey.Leg.Transport(
                                new Stop(
                                        timetable.stations().name(firstConnectionId),
                                        platformName,
                                        timetable.stations().longitude(firstConnectionId),
                                        timetable.stations().latitude(firstConnectionId)
                                ),
                                profile.date().atTime(connections.depMins(firstConnectionId) / 60, connections.depMins(firstConnectionId) & 60),
                                new Stop(
                                        timetable.stations().name(nextStationID),
                                        secondPlatformName,
                                        timetable.stations().longitude(nextStationID),
                                        timetable.stations().latitude(nextStationID)
                                ),
                                profile.date().atTime(nextArrivalTime / 60, nextArrivalTime % 60),
                                intermediateStops,
                                timetable.routes().vehicle(firstConnectionId),
                                timetable.routes().toString(),
                                trips.destination(firstConnectionId)
                        );
                        changes -= 1;
                        legs.add(transport);
                    }else{
                        Journey.Leg.Foot foot = new Journey.Leg.Foot(
                                new Stop(
                                        timetable.stations().name(firstConnectionId),
                                        platformName,
                                        timetable.stations().longitude(firstConnectionId),
                                        timetable.stations().latitude(firstConnectionId)
                                ),
                                profile.date().atTime(connections.depMins(firstConnectionId) / 60, connections.depMins(firstConnectionId) & 60),
                                new Stop(
                                        timetable.stations().name(nextStationID),
                                        secondPlatformName,
                                        timetable.stations().longitude(nextStationID),
                                        timetable.stations().latitude(nextStationID)
                                ),
                                profile.date().atTime(nextArrivalTime / 60, nextArrivalTime % 60)
                        );

                        legs.add(foot);
                    }
                    changes -= 1;
                    long updatedFront = paretoFront.get(finalArrivalStationId, changes);
                    firstConnectionId = nextStationID;
                    depMins = nextArrivalTime;
                    nextStationID = connections.nextConnectionId(firstConnectionId);
                    nextArrivalTime = PackedCriteria.arrMins(updatedFront);
                }

                if(intermediateArrivalStationId != finalArrivalStationId){
                    String platformName = timetable.isPlatformId(intermediateArrivalStationId) ? timetable.platformName(intermediateArrivalStationId) : "";

                    Journey.Leg.Foot foot = new Journey.Leg.Foot(
                            new Stop(
                                    timetable.stations().name(intermediateArrivalStationId),
                                    platformName,
                                    timetable.stations().longitude(intermediateArrivalStationId),
                                    timetable.stations().latitude(intermediateArrivalStationId)
                            ),
                            profile.date().atTime(arrMins / 60, arrMins % 60),
                            new Stop(
                                    timetable.stations().name(finalArrivalStationId),
                                    "",
                                    timetable.stations().longitude(finalArrivalStationId),
                                    timetable.stations().latitude(finalArrivalStationId)
                            ),
                            profile.date().atTime(arrMins / 60, arrMins % 60)
                    );
                    legs.add(foot);
                }


            }
            journeys.add(new Journey(legs));
        });

        journeys.sort(Comparator.comparing(Journey::depTime).thenComparing(Journey::arrTime));

        return journeys;
    }


}
