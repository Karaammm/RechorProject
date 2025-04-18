package ch.epfl.rechor;

import java.util.Map;
import java.util.StringJoiner;

public sealed interface Json {
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
    public record JString(String string) implements Json{
        public String toString() {
            return  new StringJoiner("", "\"", "\"").add(string).toString();
        }

    }
    public record JNumber(double number) implements Json{
        @Override
        public String toString() {
            return Double.toString(number);
        }
    }
}

