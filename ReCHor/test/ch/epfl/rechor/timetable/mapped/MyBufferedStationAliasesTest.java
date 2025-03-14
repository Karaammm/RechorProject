package ch.epfl.rechor.timetable.mapped;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.List;

public class MyBufferedStationAliasesTest {

    BufferedStationAliases setup() {
        List<String> stringTable = List.of("Alias A", "Alias B", "Station X", "Station Y");

        // Allocate buffer (2 bytes per field, 2 fields per entry, 2 entries)
        ByteBuffer buffer = ByteBuffer.allocate(2 * 2 * 2);

        buffer.putShort((short) 0); // alias index (Alias A)
        buffer.putShort((short) 2); // station name index (Station X)

        buffer.putShort((short) 1); // alias index (Alias B)
        buffer.putShort((short) 3); // station name index (Station Y)

        buffer.flip(); // Reset position for reading

        return new BufferedStationAliases(stringTable, buffer);
    }

    BufferedStationAliases stationAliases = setup();

    @Test
    void testAlias() {
        assertEquals("Alias A", stationAliases.alias(0));
        assertEquals("Alias B", stationAliases.alias(1));
    }

    @Test
    void testStationName() {
        assertEquals("Station X", stationAliases.stationName(0));
        assertEquals("Station Y", stationAliases.stationName(1));
    }

    @Test
    void testSize() {
        assertEquals(2, stationAliases.size());
    }

    @Test
    void testInvalidId() {
        assertThrows(IndexOutOfBoundsException.class, () -> stationAliases.alias(5));
        assertThrows(IndexOutOfBoundsException.class, () -> stationAliases.stationName(5));
    }
}