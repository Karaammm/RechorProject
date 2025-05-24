package ch.epfl.rechor;

import java.util.Map;
import java.util.StringJoiner;

/**
 * Represents JSON data types
 * @author Karam Fakhouri (374510)
 */
public sealed interface Json {
    /**
     * A JSON array of jsons
     * @param jsons
     */
    public record JArray (Json[] jsons) implements Json{
        @Override
        public String toString(){
            StringJoiner str = jsons.length > 0 &&
                jsons[0] instanceof JArray ? new StringJoiner(",\n", "[", "]") :
                new StringJoiner(",", "[", "]");
            for (Json json : jsons) {
                str.add(json.toString());
            }
            return str.toString();
        }
    }

    /**
     * A JSON object
     * @param table map asssociating JStrings to Jsons
     */
    public record JObject(Map<Json.JString, Json> table) implements Json{
        @Override
        public String toString(){
            StringJoiner str = new StringJoiner(",\n","{\n","\n}");
            for(Json.JString key : table.keySet()){
                str.add(key +
                            ":" + table.get(key));
            }
            return str.toString();
        }
    }

    /**
     * A JSON String
     * @param string
     */
    public record JString(String string) implements Json{
        public String toString() {
            return  new StringJoiner("", "\"", "\"").add(string).toString();
        }

    }

    /**
     * A JSON number
     * @param number of type double
     */
    public record JNumber(double number) implements Json{
        @Override
        public String toString() {
            return Double.toString(number);
        }
    }
}

