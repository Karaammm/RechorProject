package ch.epfl.rechor.journey;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class JourneyExtractor {

    public List<Journey> journeys(Profile profile, int depStationId){
        List<Journey> journeys = new ArrayList<>();

        ParetoFront paretoFront = profile.forStation(depStationId);

        paretoFront.forEach((long criteria) -> {
            int arrMins = PackedCriteria.arrMins(criteria);
            int changes = PackedCriteria.changes(criteria);
            int payload = PackedCriteria.payload(criteria);


            int depMins = 0;
            if (PackedCriteria.hasDepMins(criteria)) {
                depMins = PackedCriteria.depMins(criteria);
            }

        });

        journeys.sort(Comparator.comparing(Journey::depTime).thenComparing(Journey::arrTime));

        return journeys;
    }
}
