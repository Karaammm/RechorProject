package ch.epfl.rechor.journey;

import java.security.cert.CRL;
import java.time.LocalDateTime;

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

    public IcalBuilder(Name name, LocalDateTime dateTime) {

    }
}
