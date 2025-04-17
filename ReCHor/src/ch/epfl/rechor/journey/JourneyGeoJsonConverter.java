package ch.epfl.rechor.journey;

import ch.epfl.rechor.Json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class JourneyGeoJsonConverter {
    public static Json.JObject toGeoJson(Journey journey) {

        List<Json.JArray> jsons = new ArrayList<>();
        for(int i = 1; i < journey.legs().size(); i++){
            Journey.Leg prevLeg = journey.legs().get(i);
            Stop lastArrStop = prevLeg.arrStop();
            Journey.Leg currentLeg = journey.legs().get(i-1);

            jsons.add(stopToJArray(prevLeg.depStop()));
            jsons.add(stopToJArray(prevLeg.arrStop()));

            switch (currentLeg) {
                case Journey.Leg.Foot f -> {
                    if (!lastArrStop.equals(f.depStop())){
                        jsons.add(stopToJArray(f.depStop()));
                    }
                    jsons.add(stopToJArray(f.arrStop()));
                }
                case Journey.Leg.Transport t -> {
                    if(!lastArrStop.equals(t.depStop())){
                        jsons.add(stopToJArray(t.depStop()));
                    }
                    for(int j = 1; j < t.intermediateStops().size(); j++){
                        Stop lastIntermediateStop = t.intermediateStops().get(j-1).stop();
                        Stop currentIntermediateStop = t.intermediateStops().get(j).stop();
                        if(!lastIntermediateStop.equals(currentIntermediateStop)){
                            jsons.add(stopToJArray(currentIntermediateStop));
                        }
                    }
                    jsons.add(stopToJArray(t.arrStop()));
                }
            }
        }
        Map<String, Json> map = new HashMap<>();
        map.put(new Json.JString("type").toString(), new Json.JString("LineString"));
        map.put(new Json.JString("coordinates").toString(), new Json.JArray(jsons.toArray(new Json[0])));
        return new Json.JObject(map);
    }

    private static Json.JArray stopToJArray(Stop stop){
        Json[] latAndLong = new Json[2];
        latAndLong[0] = new Json.JNumber(stop.longitude());
        latAndLong[1] = new Json.JNumber(stop.latitude());
        return new Json.JArray(latAndLong);
    }
}
