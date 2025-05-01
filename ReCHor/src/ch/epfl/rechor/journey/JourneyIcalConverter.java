package ch.epfl.rechor.journey;

import ch.epfl.rechor.FormatterFr;
import ch.epfl.rechor.IcalBuilder;
import ch.epfl.rechor.IcalBuilder.Component;
import ch.epfl.rechor.IcalBuilder.Name;
import ch.epfl.rechor.journey.Journey.Leg;

import java.time.LocalDateTime;
import java.util.StringJoiner;
import java.util.UUID;

/**
 * Journey to iCalendar converter
 *
 * @author Karam Fakhouri (374510)
 * @author Ibrahim Khokher(361860)
 */
public abstract class JourneyIcalConverter {

    /**
     *
     * @param journey The journey to be converted
     * @return returns a String in iCalendar format that represents the journey
     */
    public static String toIcalendar(Journey journey) {
        IcalBuilder builder = new IcalBuilder();
        builder.begin(Component.VCALENDAR)
               .add(Name.VERSION, "2.0")
               .add(Name.PRODID, "ReCHor")
               .begin(Component.VEVENT)
               .add(Name.UID, UUID.randomUUID().toString())
               .add(Name.DTSTAMP, LocalDateTime.now())
               .add(Name.DTSTART, journey.depTime())
               .add(Name.DTEND, journey.arrTime())
               .add(Name.SUMMARY, journey.depStop().name() + " → " + journey.arrStop().name());
        StringJoiner desc = new StringJoiner("\\n");
        for (Leg leg : journey.legs()) {
            switch (leg) {
                case Journey.Leg.Foot f ->
                    desc.add(FormatterFr.formatLeg(f));
                case Journey.Leg.Transport t ->
                    desc.add(FormatterFr.formatLeg(t));
            }
        }
        builder.add(Name.DESCRIPTION, desc.toString())
               .end()
               .end();

        return builder.build();
    }
    //check
}
