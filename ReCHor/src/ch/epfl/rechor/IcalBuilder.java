package ch.epfl.rechor;

import java.security.cert.CRL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;

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
    private static final String CRLF = "\r\n";
    private ArrayList<Component> components = new ArrayList<Component>();

    public IcalBuilder add(Name name, String value) {
        StringBuilder line = new StringBuilder(name.toString()).append(":").append(value);
        if (line.length() <= 75) {
            builder.append(line).append(CRLF);
        } else {
            builder.append(line, 0, 75);
            int i = 75;
            while (i < line.length()) {
                builder.append(CRLF).append(" ");
                builder.append(line, i, Math.min(i + 75, line.length()));
                i += 75;
            }
            builder.append(CRLF);

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
        builder.append(name.toString()).append(":").append(line.format(dateTime)).append(CRLF);

        return this;
    }

    public IcalBuilder begin(Component component) {
        StringBuilder line = new StringBuilder("BEGIN:" + component.name());
        components.add(component);
        builder.append(line).append(CRLF);
        return this;
    }

    public IcalBuilder end() {
        Preconditions.checkArgument(!components.isEmpty());
        builder.append("END:").append(components.get(components.size() - 1)).append(CRLF);
        components.remove(components.size() - 1);
        return this;
    }

    public String build() {
        Preconditions.checkArgument(components.isEmpty());
        return builder.toString();
    }
}
