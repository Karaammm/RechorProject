package ch.epfl.rechor.journey;

import java.security.cert.CRL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;

import ch.epfl.rechor.Preconditions;

public final class IcalBuilder {
    public enum Component {
        VCALENDAR,
        VEVENT;
    }

    public enum Name {
        BEGIN,
        END,
        PRODID,
        VERSION,
        UID,
        DTSTAMP,
        DTSTART,
        DTEND,
        SUMMARY,
        DESCRIPTION;
    }

    private StringBuilder builder = new StringBuilder();
    private static final String CLRF = "\r\n";
    private ArrayList<Component> components = new ArrayList<Component>();

    public IcalBuilder add(Name name, String value) {
        StringBuilder line = new StringBuilder(name.toString() + ":" + value);
        if (line.length() <= 75) {
            builder.append(line).append(CLRF);
        } else {
            builder.append(line, 0, 75);
            int i = 75;
            while (i < line.length()) {
                int end = Math.min(i + 75, line.length());
                builder.append(" ").append(line, i, end).append(CLRF);
                i = end;
            }
        }
        return this;
    }

    public IcalBuilder add(Name name, LocalDateTime dateTime) {
        DateTimeFormatter line = new DateTimeFormatterBuilder()
                .appendValue(ChronoField.YEAR, 4)
                .appendValue(ChronoField.MONTH_OF_YEAR, 2)
                .appendValue(ChronoField.DAY_OF_MONTH, 2)
                .appendLiteral("T")
                .appendValue(ChronoField.HOUR_OF_DAY, 2)
                .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
                .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
                .toFormatter();
        builder.append(line.format(dateTime));

        return this;
    }

    public IcalBuilder begin(Component component) {
        StringBuilder line = new StringBuilder("BEGIN" + component.name());
        components.add(component);
        builder.append(line + CLRF);
        return this;
    }

    public IcalBuilder end() {
        Preconditions.checkArgument(!components.isEmpty());
        components.remove(components.size() - 1);
        return this;
    }

    public String build() {
        Preconditions.checkArgument(components.isEmpty());
        return builder.toString();
    }
}
