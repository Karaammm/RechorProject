package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.Platforms;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Implementation of the Platforms interface for accessing flattened timetable
 * data.
 *
 * @author Karam Fakhouri (374510)
 *
 */
public class BufferedPlatforms implements Platforms {

    /**
     * Constants for the fields in the platform structure
     */
    private static final int NAME_ID = 0;
    private static final int STATION_ID = 1;

    /**
     * Buffers for connection data and successor connections
     */
    private List<String> stringTable;
    private StructuredBuffer buffer;

    /**
     * Constructs a BufferedPlatforms object with access to the flattened
     * platform data
     *
     * @param stringTable the list of strings associated
     * @param buffer the relevant byte that stores it
     */
    public BufferedPlatforms(List<String> stringTable, ByteBuffer buffer) {
        this.stringTable = stringTable;
        this.buffer = new StructuredBuffer(new Structure(Structure.field(NAME_ID, Structure.FieldType.U16),
                Structure.field(STATION_ID, Structure.FieldType.U16)), buffer);
    }

    /**
     * Returns the name of the track/platform, which can be empty
     *
     * @return the name of the track/platform, which can be empty
     * @param id the index
     * @throws IndexOutOfBoundsException if the index is less than 0 or greater than
     *                                   or equal to the size returned by size()
     */
    @Override
    public String name(int id) {
        int stringIndex = buffer.getU16(NAME_ID, id);
        return stringTable.get(stringIndex);
    }

    /**
     * Returns the index of the station to which this track/platform belongs to
     *
     * @return the index of the station to which this track/platform belongs to
     * @param id the index
     * @throws IndexOutOfBoundsException if the index is less than 0 or greater than
     *                                   or equal to the size returned by size()
     */
    @Override
    public int stationId(int id) {
        return buffer.getU16(STATION_ID, id);
    }

    /**
     * Returns the number of elements of said data
     *
     * @return number of elements
     */
    @Override
    public int size() {
        return buffer.size();
    }
}
