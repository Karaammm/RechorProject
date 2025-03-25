package ch.epfl.rechor.timetable.mapped;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ch.epfl.rechor.timetable.mapped.BufferedRoutes;
import ch.epfl.rechor.journey.Vehicle;

public class MyBufferedRoutesTest {

    private List<String> stringTable;
    private ByteBuffer buffer;

    @BeforeEach
    public void setUp() {
        // Define a string table for route names
        stringTable = Arrays.asList(
                "Metro", "Tram", "Bus", "Train", "Ferry", "Helicopter");

        // Each route record is 4 bytes:
        // - 2 bytes for NAME_ID (U16)
        // - 2 bytes for KIND (U16)
        // We'll create a buffer for 3 routes (3 * 4 = 12 bytes)
        byte[] routeData = new byte[12];
        buffer = ByteBuffer.wrap(routeData).order(ByteOrder.BIG_ENDIAN);

        // Route 0: Name ID = 0 ("Metro"), Kind = 1 (Vehicle.Tram)
        buffer.putShort((short) 0); // NAME_ID = 0
        buffer.putShort((short) 1); // KIND = 1 (Tram)

        // Route 1: Name ID = 2 ("Bus"), Kind = 2 (Vehicle.Bus)
        buffer.putShort((short) 2);
        buffer.putShort((short) 3);

        // Route 2: Name ID = 4 ("Ferry"), Kind = 4 (Vehicle.Ferry)
        buffer.putShort((short) 4);
        buffer.putShort((short) 4);

        buffer.rewind(); // Reset buffer position for reading
    }

    @Test
    public void testSize() {
        BufferedRoutes routes = new BufferedRoutes(stringTable, buffer);
        assertEquals(3, routes.size(), "BufferedRoutes should contain 3 routes.");
    }

    @Test
    public void testRouteNames() {
        BufferedRoutes routes = new BufferedRoutes(stringTable, buffer);
        assertEquals("Metro", routes.name(0));
        assertEquals("Bus", routes.name(1));
        assertEquals("Ferry", routes.name(2));
    }

    @Test
    public void testVehicleTypes() {
        BufferedRoutes routes = new BufferedRoutes(stringTable, buffer);
        assertEquals(Vehicle.TRAM, routes.vehicle(0)); // Vehicle index 1 = TRAM
        assertEquals(Vehicle.BUS, routes.vehicle(1)); // Vehicle index 2 = BUS
        assertEquals(Vehicle.FERRY, routes.vehicle(2)); // Vehicle index 4 = FERRY
    }

    @Test
    public void testInvalidIndices() {
        BufferedRoutes routes = new BufferedRoutes(stringTable, buffer);
        assertThrows(IndexOutOfBoundsException.class, () -> routes.name(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> routes.vehicle(4));
    }
}
