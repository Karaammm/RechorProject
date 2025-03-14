package ch.epfl.rechor.timetable.mapped;

import org.junit.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.nio.ByteBuffer;
import java.util.List;

public class MyBufferedPlatformsTest {




    BufferedPlatforms setup() {

        List<String> stringTable = List.of("Platform A", "Platform B", "Platform C");

        // Allocate buffer (2 bytes per field, 2 fields per entry, 3 entries)
        ByteBuffer buffer = ByteBuffer.allocate(2 * 2 * 3);

        buffer.putShort((short) 0); // name index 0 (Platform A)
        buffer.putShort((short) 100); // station ID 100

        buffer.putShort((short) 1); // name index 1 (Platform B)
        buffer.putShort((short) 101); // station ID 101

        buffer.putShort((short) 2); // name index 2 (Platform C)
        buffer.putShort((short) 102); // station ID 102

        buffer.flip(); // Reset position for reading

        return new BufferedPlatforms(stringTable, buffer);
    }

    BufferedPlatforms  platforms = setup();

    @Test
    public void testName() {
        setup();
        assertEquals("Platform A", platforms.name(0));
        assertEquals("Platform B", platforms.name(1));
        assertEquals("Platform C", platforms.name(2));
    }

    @Test
    public void testStationId() {
        setup();
        assertEquals(100, platforms.stationId(0));
        assertEquals(101, platforms.stationId(1));
        assertEquals(102, platforms.stationId(2));
    }

    @Test
    public void testSize() {
        setup();
        assertEquals(3, platforms.size());
    }

//    @Test
//    public void testInvalidId() {
//        setup();
//        assertThrows(IndexOutOfBoundsException.class, () -> platforms.name(5));
//        assertThrows(IndexOutOfBoundsException.class, () -> platforms.stationId(5));
//    }
}