package ch.epfl.rechor.timetable.mapped;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MyBufferedTripsTest {

    private List<String> stringTable;
    private ByteBuffer buffer;

    @BeforeEach
    public void setUp() {
        // Create a string table with enough entries.
        // The indices we use in the flattened trips will reference these strings.
        stringTable = Arrays.asList(
                "Zero", "One", "Two", "Destination3", "Four",
                "Route5", "Six", "Destination7", "Eight", "Nine", "Route10");

        // Each trip record is 4 bytes (2 bytes for ROUTE_ID and 2 bytes for
        // DESTINATION_ID).
        // We'll create a ByteBuffer for 2 records (total 8 bytes).
        byte[] tripData = new byte[8];
        // Use BIG_ENDIAN order (commonly used for these data formats)
        buffer = ByteBuffer.wrap(tripData).order(ByteOrder.BIG_ENDIAN);

        // Record 0: ROUTE_ID = 5, DESTINATION_ID = 3
        buffer.putShort((short) 5);
        buffer.putShort((short) 3);

        // Record 1: ROUTE_ID = 10, DESTINATION_ID = 7
        buffer.putShort((short) 10);
        buffer.putShort((short) 7);

        // Reset buffer position for reading.
        buffer.rewind();
    }

    @Test
    public void testSize() {
        BufferedTrips trips = new BufferedTrips(stringTable, buffer);
        assertEquals(2, trips.size(), "There should be 2 trip records.");
    }

    @Test
    public void testRouteIdAndDestinationForRecord0() {
        BufferedTrips trips = new BufferedTrips(stringTable, buffer);
        // For record 0, ROUTE_ID should be 5.
        assertEquals(5, trips.routeId(0), "Record 0 should have routeId 5.");
        // For record 0, DESTINATION_ID is 3, so destination() should return
        // stringTable.get(3)
        assertEquals("Destination3", trips.destination(0), "Record 0 should have destination 'Destination3'.");
    }

    @Test
    public void testRouteIdAndDestinationForRecord1() {
        BufferedTrips trips = new BufferedTrips(stringTable, buffer);
        // For record 1, ROUTE_ID should be 10.
        assertEquals(10, trips.routeId(1), "Record 1 should have routeId 10.");
        // For record 1, DESTINATION_ID is 7, so destination() should return
        // stringTable.get(7)
        assertEquals("Destination7", trips.destination(1), "Record 1 should have destination 'Destination7'.");
    }

    @Test
    public void testInvalidIndices() {
        BufferedTrips trips = new BufferedTrips(stringTable, buffer);
        assertThrows(IndexOutOfBoundsException.class, () -> trips.routeId(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> trips.destination(2)); // Only records 0 and 1 exist
    }
}
