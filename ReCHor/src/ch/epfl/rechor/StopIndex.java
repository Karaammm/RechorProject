package ch.epfl.rechor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A searchable index of stop names, which is immutable
 * @author Karam Fakhouri (374510)
 */
public final class StopIndex {
    private final List<String> stopNames;
    private final Map<String, String> altNames;
    static final Map<Character, String> ACCENT_EQUIV = Map.ofEntries(
        Map.entry('a', "[aàáâäãå]"),
        Map.entry('A', "[AÀÁÂÄÃÅ]"),
        Map.entry('e', "[eèéêë]"),
        Map.entry('E', "[EÈÉÊË]"),
        Map.entry('i', "[iìíîï]"),
        Map.entry('I', "[IÌÍÎÏ]"),
        Map.entry('o', "[oòóôöõ]"),
        Map.entry('O', "[OÒÓÔÖÕ]"),
        Map.entry('u', "[uùúûü]"),
        Map.entry('U', "[UÙÚÛÜ]"),
        Map.entry('c', "[cç]"),
        Map.entry('C', "[CÇ]"),
        Map.entry('n', "[nñ]"),
        Map.entry('N', "[NÑ]")
    );


    /**
     * StopIndex constructor
     * @param stopNames list of stop Names
     * @param altNames map of station alias names
     */
    public StopIndex(List<String> stopNames, Map<String,String> altNames){
        this.stopNames = List.copyOf(stopNames);
        this.altNames = Map.copyOf(altNames);
    }

    /**
     * @param query the search query
     * @param maxResults maximum number of results to return
     * @return the list of stop names matching the query, sorted by decreasing relevance and without
     * duplicates
     */
    public List<String> stopsMatching(String query, int maxResults){
        int flags = query.chars().anyMatch(Character::isUpperCase)
            ? Pattern.UNICODE_CASE
            : Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE;

        String[] subqueries = query.trim().split("\\s+");
        Pattern[] subREs = toRegEx(subqueries,flags);

        Stream<Map.Entry<String, String>> combinedStream = Stream.concat(
            stopNames.stream().map(name -> Map.entry(name, name)),
            altNames.entrySet().stream().map(e -> Map.entry(e.getValue(), e.getKey()))
        );

        return combinedStream
            .filter(entry -> matchesAllSubqueries(entry.getKey(),subREs))
            .collect(Collectors.toMap(
                Map.Entry::getValue,
                entry -> relevanceScore(entry.getKey(),subREs),
                Math::max))
            .entrySet()
            .stream()
            .sorted((Map.Entry.<String, Integer>comparingByValue().reversed()))
            .limit(maxResults)
            .map(Map.Entry::getKey)
            .toList();
    }

    /**
     * Helper method
     * @param name
     * @param subREs
     * @return boolean to check if the stop name matches all the subqueries
     */
    private boolean  matchesAllSubqueries(String name, Pattern[] subREs) {
        return Arrays.stream(subREs).allMatch(re -> re.matcher(name).find());
    }

    /**
     * Helper method  that converts an array of subqueries into an array of RegExs,
     * with the given flags
     * @param subqueries
     * @param flags
     * @return an array of RegExs
     */
    private Pattern[] toRegEx(String[] subqueries, int flags){
        return Arrays.stream(subqueries)
                     .map(sub -> sub.chars().mapToObj(c -> {
                         char ch = ((flags & Pattern.CASE_INSENSITIVE) == Pattern.CASE_INSENSITIVE)
                             ? Character.toLowerCase((char)c)
                             : (char)c;
                         return ACCENT_EQUIV.getOrDefault(ch,Pattern.quote(Character.toString(ch)));
                     })
                                    .collect(Collectors.joining()))
                     .map(regex -> Pattern.compile(regex, flags))
                     .toArray(Pattern[]::new);
    }


    /**
     * Helper method to calculate the relevance score of each stop
     * @param name the stop name
     * @param subREs
     * @return the relevance score of the given stop
     */
    private int relevanceScore(String name, Pattern[] subREs){
        int score = 0;
        for(Pattern subquery: subREs){
            Matcher matcher = subquery.matcher(name);
            if(matcher.find()){
                int matcherStart = matcher.start();
                int matcherEnd = matcher.end();
                int matchLength = matcherEnd - matcherStart;

                int subqueryScore = (matchLength * 100) / name.length();

                boolean startMatches = matcherStart == 0
                    || !Character.isLetter(name.charAt(matcherStart - 1));
                boolean endMatches = matcherEnd == name.length()
                    || !Character.isLetter(name.charAt(matcherEnd));
                int multiplier = 1;

                if(startMatches) multiplier *= 4;
                if(endMatches) multiplier *= 2;

                score += subqueryScore * multiplier;
            }
        }
        return score;
    }
}
