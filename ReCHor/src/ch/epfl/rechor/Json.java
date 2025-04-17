package ch.epfl.rechor;

import java.util.Map;
import java.util.StringJoiner;

public sealed interface Json {
    public record JArray (Json[] jsons) implements Json{
        @Override
        public String toString(){
            StringJoiner str = new StringJoiner(",", "[", "]");
            for (Json json : jsons) {
                str.add(json.toString());
            }
            return str.toString();
        }
    }
    public record JObject(Map<String, Json> table) implements Json{
        @Override
        public String toString(){
            StringJoiner str = new StringJoiner(",","{","}");
            for(String key : table.keySet()){
                str.add(new JString(key) +
                            ":" + new JString(table.get(key).toString()));
            }
            return str.toString();
        }
    }
    public record JString(String string) implements Json{
        public String toString() {
            return "\"" + escape(string) + "\"";
        }

        private static String escape(String s) {
            return s
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
        }
    }
    public record JNumber(double number) implements Json{
        @Override
        public String toString() {
            return Double.toString(number);
        }
    }
}

