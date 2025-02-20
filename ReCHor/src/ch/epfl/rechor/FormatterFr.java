package ch.epfl.rechor;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.Stop;

public final class FormatterFr {

    private FormatterFr() {

    }

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

    public static String formatTime(LocalDateTime dateTime) {
        DateTimeFormatter fmt = new DateTimeFormatterBuilder()
                .appendValue(ChronoField.HOUR_OF_DAY)
                .appendLiteral('h')
                .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
                .toFormatter();
        return fmt.format(dateTime);

    }

    public static String formatPlatformName(Stop stop) {
        if (stop.platformName() == null || stop.platformName().isEmpty()) {
            return "";
        }
        if (Character.isDigit(stop.platformName().charAt(0))) {
            return "voie " + stop.platformName();
        }
        return "quai " + stop.platformName();
    }

    public static String formatLeg(Journey.Leg.Foot footLeg) {
        long duration = Duration.between(footLeg.depTime(), footLeg.arrTime()).toMinutes();
        if (footLeg.isTransfer()) {
            return "changement (" + duration + " min)";
        }
        return "trajet à pied (" + duration + " min)";
    }

    public static String formatLeg(Journey.Leg.Transport Leg) {
        StringBuilder sb = new StringBuilder();

        sb.append(formatTime(Leg.depTime()) + " ")
                .append(Leg.depStop().name() + " ");
        if (Leg.depStop().platformName() != null) {
            sb.append("(")
                    .append(formatPlatformName(Leg.depStop()))
                    .append(") ");
        }
        sb.append("\u2192 ");
        sb.append(Leg.arrStop().name() + " ")
                .append("(arr. " + formatTime(Leg.arrTime()));
        if (Leg.arrStop().platformName() != null) {
            sb.append(" ").append(formatPlatformName(Leg.arrStop()));
        }
        sb.append(")");

        return sb.toString();
    }

    public static String formatRouteDestination(Journey.Leg.Transport transportLeg) {
        return transportLeg.route() + " Direction " + transportLeg.destination();
    }
}
