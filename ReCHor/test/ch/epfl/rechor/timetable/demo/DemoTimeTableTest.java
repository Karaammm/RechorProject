package ch.epfl.rechor.timetable.demo;

import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.JourneyExtractor;
import ch.epfl.rechor.journey.Router;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class DemoTimeTableTest {
    @Test
    void exposesAUsableSyntheticNetwork() {
        var table = DemoTimeTable.create();

        assertEquals(4, table.stations().size());
        assertEquals(2, table.routes().size());
        assertEquals(2, table.tripsFor(LocalDate.now()).size());
        assertEquals(3, table.connectionsFor(LocalDate.now()).size());
        assertEquals(10, table.transfers().minutesBetween(0, 1));
    }

    @Test
    void routesAJourneyAcrossTheDemoNetwork() {
        var table = DemoTimeTable.create();
        var journeys = JourneyExtractor.journeys(
                new Router(table).profile(LocalDate.of(2025, 4, 28), 3), 0);

        assertFalse(journeys.isEmpty());
        List<Journey.Leg> legs = journeys.getLast().legs();
        assertEquals("Ecublens VD, EPFL", legs.getFirst().depStop().name());
        assertEquals("Genève", legs.getLast().arrStop().name());
    }
}
