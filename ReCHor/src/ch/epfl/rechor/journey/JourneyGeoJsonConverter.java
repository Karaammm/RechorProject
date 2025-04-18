package ch.epfl.rechor.journey;

import ch.epfl.rechor.Json;

import java.util.*;

public abstract class JourneyGeoJsonConverter {
    public static Json.JObject toGeoJson(Journey journey) {
        List<Json.JArray> coordinates = new ArrayList<>();

        for (Journey.Leg leg : journey.legs()) {
            switch (leg) {
                case Journey.Leg.Foot f -> {
                    addStopIfNew(coordinates, f.depStop());
                    addStopIfNew(coordinates, f.arrStop());
                }
                case Journey.Leg.Transport t -> {
                    addStopIfNew(coordinates, t.depStop());
                    for (Journey.Leg.IntermediateStop is : t.intermediateStops()) {
                        addStopIfNew(coordinates, is.stop());
                    }
                    addStopIfNew(coordinates, t.arrStop());
                }
            }
        }
        Map<Json.JString, Json> map = new LinkedHashMap<>();
        map.put(new Json.JString("type"), new Json.JString("LineString"));
        map.put(new Json.JString("coordinates"), new Json.JArray(coordinates.toArray(new Json[0])));
        return new Json.JObject(map);
    }

    private static Json.JArray stopToJArray(Stop stop){
        Json[] latAndLong = new Json[2];
        latAndLong[0] = new Json.JNumber(stop.longitude());
        latAndLong[1] = new Json.JNumber(stop.latitude());
        return new Json.JArray(latAndLong);
    }

    private static void addStopIfNew(List<Json.JArray> list, Stop stop) {
        Json.JArray stopJson = stopToJArray(stop);
        if (list.isEmpty() || !list.getLast().toString().equals(stopJson.toString())) {
            list.add(stopJson);
        }
    }
}
