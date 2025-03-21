package ch.epfl.rechor.timetable.mapped;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HexFormat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.epfl.rechor.PackedRange;

class MyBufferedTransfersTest {

    // Helper method to create a ByteBuffer from a hex string
    private static ByteBuffer byteBuffer(String hex) {
        return ByteBuffer.wrap(HexFormat.ofDelimiter(" ").parseHex(hex))
                .order(ByteOrder.BIG_ENDIAN)
                .asReadOnlyBuffer();
    }

    /*
     * The flattened change table (each record is 5 bytes):
     *
     * Record 0: DEP_STATION_ID = 1, ARR_STATION_ID = 3, TRANSFER_MINUTES = 5
     * Record 1: DEP_STATION_ID = 2, ARR_STATION_ID = 3, TRANSFER_MINUTES = 8
     * Record 2: DEP_STATION_ID = 3, ARR_STATION_ID = 5, TRANSFER_MINUTES = 2
     * Record 3: DEP_STATION_ID = 4, ARR_STATION_ID = 5, TRANSFER_MINUTES = 7
     *
     * Hex string for all records:
     * "00 01 00 03 05  00 02 00 03 08  00 03 00 05 02  00 04 00 05 07"
     */
    private static final ByteBuffer TRANSFERS_BUFFER = byteBuffer(
            "00 01 00 03 05 00 02 00 03 08 00 03 00 05 02 00 04 00 05 07");

    @BeforeEach
    public void setUp() {
        // Ensure that the buffer's position is reset before each test.
        TRANSFERS_BUFFER.rewind();
    }

    @Test
    void testSize() {
        BufferedTransfers transfers = new BufferedTransfers(TRANSFERS_BUFFER);
        // There are 20 bytes; each record is 5 bytes → 4 records
        assertEquals(4, transfers.size(), "BufferedTransfers should contain 4 records.");
    }

    @Test
    void testDepStationIdAndMinutes() {
        BufferedTransfers transfers = new BufferedTransfers(TRANSFERS_BUFFER);
        // Record 0:
        assertEquals(1, transfers.depStationId(0), "Record 0 DEP_STATION_ID should be 1.");
        assertEquals(5, transfers.minutes(0), "Record 0 TRANSFER_MINUTES should be 5.");
        // Record 1:
        assertEquals(2, transfers.depStationId(1), "Record 1 DEP_STATION_ID should be 2.");
        assertEquals(8, transfers.minutes(1), "Record 1 TRANSFER_MINUTES should be 8.");
        // Record 2:
        assertEquals(3, transfers.depStationId(2), "Record 2 DEP_STATION_ID should be 3.");
        assertEquals(2, transfers.minutes(2), "Record 2 TRANSFER_MINUTES should be 2.");
        // Record 3:
        assertEquals(4, transfers.depStationId(3), "Record 3 DEP_STATION_ID should be 4.");
        assertEquals(7, transfers.minutes(3), "Record 3 TRANSFER_MINUTES should be 7.");
    }

    @Test
    void testMinutesBetween() {
        BufferedTransfers transfers = new BufferedTransfers(TRANSFERS_BUFFER);
        // Test that minutesBetween returns the TRANSFER_MINUTES of the first matching
        // record.
        // For example, for DEP_STATION_ID = 1 and ARR_STATION_ID = 3, record 0 should
        // match.
        assertEquals(5, transfers.minutesBetween(1, 3), "minutesBetween(1,3) should be 5.");
        // For DEP_STATION_ID = 2 and ARR_STATION_ID = 3, record 1 should match.
        assertEquals(8, transfers.minutesBetween(2, 3), "minutesBetween(2,3) should be 8.");
        // For DEP_STATION_ID = 3 and ARR_STATION_ID = 5, record 2 should match.
        assertEquals(2, transfers.minutesBetween(3, 5), "minutesBetween(3,5) should be 2.");
        // If no matching change exists, the method should throw an exception.
        assertThrows(IndexOutOfBoundsException.class, () -> transfers.minutesBetween(1, 5),
                "No change should match DEP_STATION_ID 1 and ARR_STATION_ID 5.");
    }

    @Test
    void testArrivingAt() {
        BufferedTransfers transfers = new BufferedTransfers(TRANSFERS_BUFFER);
        // Our test data: records 0 and 1 have ARR_STATION_ID = 3, and records 2 and 3
        // have ARR_STATION_ID = 5.
        // Use your PackedRange utility to unpack the interval.
        int packedIntervalFor3 = transfers.arrivingAt(3);
        int start3 = PackedRange.startInclusive(packedIntervalFor3);
        int end3 = PackedRange.endExclusive(packedIntervalFor3);
        // We expect the range [0,2): records 0 and 1.
        assertEquals(0, start3, "For station 3, the first record index should be 0.");
        assertEquals(2, end3, "For station 3, the range should cover indices 0 and 1.");

        int packedIntervalFor5 = transfers.arrivingAt(5);
        int start5 = PackedRange.startInclusive(packedIntervalFor5);
        int end5 = PackedRange.endExclusive(packedIntervalFor5);
        // We expect the range [2,4): records 2 and 3.
        assertEquals(2, start5, "For station 5, the first record index should be 2.");
        assertEquals(4, end5, "For station 5, the range should cover indices 2 and 3.");

        // For a station with no arrivals, for example station 0 or 2, arrivingAt should
        // return 0.
        // Here we test for station 0:
        assertEquals(0, transfers.arrivingAt(0), "For station 0, no changes should yield a 0 interval.");
    }

    @Test
    void testInvalidIndices() {
        BufferedTransfers transfers = new BufferedTransfers(TRANSFERS_BUFFER);
        // Negative record index should throw exception in depStationId, minutes, etc.
        assertThrows(IndexOutOfBoundsException.class, () -> transfers.depStationId(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> transfers.minutes(-1));
        // For arrivingAt, if we pass an invalid station id (e.g., greater than the
        // maximum station id found),
        // it should throw an exception.
        // Our test data has ARR_STATION_ID values of 3 and 5, so station id 6 should be
        // invalid.
        assertThrows(IndexOutOfBoundsException.class, () -> transfers.arrivingAt(6));
    }
}
