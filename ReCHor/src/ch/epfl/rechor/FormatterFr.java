package ch.epfl.rechor;

import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.Stop;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

/**
 * Formatter for representing different types of data
 * 
 * @author Karam Fakhouri (374510)
 */
public final class FormatterFr {

    private FormatterFr() {

    }

    /**
     * Formats the given duration to the form **h**min
     * 
     * @param duration given duration to be formatted
     * @return String of the formatted duration
     */
    public static String formatDuration(Duration duration) {
        long totalMins = duration.toMinutes();
        long hours = totalMins / 60;
        long mins = totalMins % 60;

        if (hours == 0) {
            return mins + " min";
        } else {
            return hours + " h " + mins + " min";
        }
    }

    /**
     * Formats given dateTime
     * 
     * @param dateTime given dateTime to be formatted
     * @return String of the formatted datetime
     */
    public static String formatTime(LocalDateTime dateTime) {
        DateTimeFormatter fmt = new DateTimeFormatterBuilder()
                .appendValue(ChronoField.HOUR_OF_DAY)
                .appendLiteral('h')
                .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
                .toFormatter();
        return fmt.format(dateTime);

    }

    /**
     * Formats the platform name (if it exists)
     * 
     * @param stop current stop
     * @return String with the formatted platform name
     */
    public static String formatPlatformName(Stop stop) {
        if (stop.platformName() == null || stop.platformName().isEmpty()) {
            return "";
        }
        if (Character.isDigit(stop.platformName().charAt(0))) {
            return "voie " + stop.platformName();
        }
        return "quai " + stop.platformName();
    }

    /**
     * Formats the current Foot leg
     * 
     * @param footLeg current leg
     * @return String formatted for a foot step
     */
    public static String formatLeg(Journey.Leg.Foot footLeg) {
        long duration = Duration.between(footLeg.depTime(), footLeg.arrTime()).toMinutes();
        if (footLeg.isTransfer()) {
            return "changement (" + duration + " min)";
        }
        return "trajet à pied (" + duration + " min)";
    }

    /**
     * Formats the current transport leg
     * 
     * @param Leg current leg
     * @return String formatted for a transport leg
     */
    public static String formatLeg(Journey.Leg.Transport Leg) {
        StringBuilder sb = new StringBuilder();

        sb.append(formatTime(Leg.depTime())).append(" ").append(Leg.depStop().name()).append(" ");
        if (Leg.depStop().platformName() != null) {
            sb.append("(")
                    .append(formatPlatformName(Leg.depStop()))
                    .append(") ");
        }
        sb.append("\u2192 ").append(Leg.arrStop().name()).append(" ").append("(arr. ")
          .append(formatTime(Leg.arrTime()));
        if (Leg.arrStop().platformName() != null) {
            sb.append(" ").append(formatPlatformName(Leg.arrStop()));
        }
        sb.append(")");

        return sb.toString();
    }

    /**
     * Formats the direction and line of the leg
     * 
     * @param transportLeg current leg
     * @return String of the formatted route destination
     */
    public static String formatRouteDestination(Journey.Leg.Transport transportLeg) {
        return transportLeg.route() + " Direction " + transportLeg.destination();
    }
}
