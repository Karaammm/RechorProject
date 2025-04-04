package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.Preconditions;
import ch.epfl.rechor.timetable.StationAliases;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Implementation of the StationAliases interface for accessing flattened timetable
 * data.
 */
public final class BufferedStationAliases implements StationAliases {

    /**
     * Constants for the fields in the station aliases structure
     */
    private static final int ALIAS_ID = 0;
    private static final int STATION_NAME_ID = 1;

    /**
     * Buffers for connection data and successor connections
     */
    private final List<String> stringTable;
    private final StructuredBuffer buffer;

    /**
     * Constructs a BufferedStationAliases object with access to the flattened
     * station aliases data.
     *
     * @param stringTable the list of strings associated
     * @param buffer     the ByteBuffer containing the connection data
     */
    public BufferedStationAliases(List<String> stringTable, ByteBuffer buffer) {
        this.stringTable = stringTable;
        this.buffer = new StructuredBuffer(new Structure(Structure.field(ALIAS_ID, Structure.FieldType.U16),
                Structure.field(STATION_NAME_ID, Structure.FieldType.U16)), buffer);
    }

    /**
     * Returns the alternative name of given index station
     *
     * @return the alternative name of the station with the given index
     * @param id the index
     * @throws IndexOutOfBoundsException if the index is less than 0 or greater than
     *                                   or equal to the size returned by size()
     */
    @Override
    public String alias(int id) {
        Preconditions.checkIndex(size(), id);
        return stringTable.get(buffer.getU16(ALIAS_ID, id));
    }

    /**
     * Returns the name of given index station
     *
     * @return the name of the station with the given index
     * @param id the index
     * @throws IndexOutOfBoundsException if the index is less than 0 or greater than
     *                                   or equal to the size returned by size()
     */
    @Override
    public String stationName(int id) {
        Preconditions.checkIndex(size(), id);
        return stringTable.get(buffer.getU16(STATION_NAME_ID, id));
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
