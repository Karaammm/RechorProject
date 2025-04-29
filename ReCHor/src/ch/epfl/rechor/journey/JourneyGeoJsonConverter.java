package ch.epfl.rechor.journey;

import ch.epfl.rechor.Json;

import java.util.*;

/**
 * Class that provides a method to convert a trip into a GeoJSON document representing its journey
 */
public abstract class JourneyGeoJsonConverter {
    /**
     * Main method to converting to GeoJson document
     * @param journey journey to convert
     * @return GeoJSON document
     */
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

    /**
     * Helper method to convert a stop into a JArray
     * @param stop to convert
     * @return JArray of the stop
     */
    private static Json.JArray stopToJArray(Stop stop){
        Json[] latAndLong = new Json[2];
        latAndLong[0] = new Json.JNumber(stop.longitude());
        latAndLong[1] = new Json.JNumber(stop.latitude());
        return new Json.JArray(latAndLong);
    }

    /**
     * Helper method that adds a stop to the list if it differs from the last stop
     * @param list list to append the stop to
     * @param stop stop to add
     */
    private static void addStopIfNew(List<Json.JArray> list, Stop stop) {
        Json.JArray stopJson = stopToJArray(stop);
        if (list.isEmpty() || !list.getLast().toString().equals(stopJson.toString())) {
            list.add(stopJson);
        }
    }
}
