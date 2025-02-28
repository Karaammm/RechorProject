package ch.epfl.rechor.journey;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.StringJoiner;
import java.util.UUID;

import ch.epfl.rechor.FormatterFr;
import ch.epfl.rechor.IcalBuilder;
import ch.epfl.rechor.IcalBuilder.Component;
import ch.epfl.rechor.IcalBuilder.Name;
import ch.epfl.rechor.journey.Journey.Leg;

public abstract class JourneyIcalConverter {
    public static String toIcalendar(Journey journey) {
        IcalBuilder builder = new IcalBuilder();
        builder.begin(Component.VCALENDAR);
        builder.add(Name.VERSION, "2.0");
        builder.add(Name.PRODID, "ReCHor");
        builder.begin(Component.VEVENT);
        builder.add(Name.UID, UUID.randomUUID().toString());
        builder.add(Name.DTSTAMP, LocalDateTime.now());
        builder.add(Name.DTSTART, journey.depTime());
        builder.add(Name.DTEND, journey.arrTime());
        builder.add(Name.SUMMARY, journey.depStop().name() + " \u2192 " + journey.arrStop().name());
        StringJoiner desc = new StringJoiner(" \r\n ");
        for (Leg leg : journey.legs()) {
            switch (leg) {
                case Journey.Leg.Foot f ->
                    desc.add(FormatterFr.formatLeg(f));
                case Journey.Leg.Transport t ->
                    desc.add(FormatterFr.formatLeg(t));
            }
        }
        builder.add(Name.DESCRIPTION, desc.toString());
        builder.end();
        builder.end();

        return builder.toString();
    }
}
