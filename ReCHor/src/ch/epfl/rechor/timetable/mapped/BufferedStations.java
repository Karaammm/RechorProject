package ch.epfl.rechor.timetable.mapped;

import java.nio.ByteBuffer;
import java.util.List;

import ch.epfl.rechor.Preconditions;
import ch.epfl.rechor.timetable.Stations;

/**
 * @author Karam Fakhouri (374510)
 * 
 *         Class that provides access to a table of stations represented in a
 *         flattened manner
 */
public final class BufferedStations implements Stations {

    private final List<String> stringTable;
    private StructuredBuffer buffer;
    private static final int NAME_ID = 0;
    private static final int LON = 1;
    private static final int LAT = 2;
    private static final double TO_DEGREES = (StrictMath.scalb((double) 1, -32) * 360.0);

    /**
     * Constructor of BufferedStations
     * 
     * @param stringTable the provided string table, which is used to determine the
     *                    value of strings referenced by the data in the buffer
     * @param buffer      flatenned data in a ByteBuffer
     */
    public BufferedStations(List<String> stringTable, ByteBuffer buffer) {
        this.stringTable = stringTable;
        this.buffer = new StructuredBuffer(new Structure(Structure.field(NAME_ID, Structure.FieldType.U16),
                Structure.field(LON, Structure.FieldType.S32), Structure.field(LAT, Structure.FieldType.S32)), buffer);
    }

    /**
     * Returns the number of elements in the array
     * 
     * @return number of elements
     */
    @Override
    public int size() {
        return buffer.size();
    }

    /**
     * Returns the name of the station with the given index
     * 
     * @return the name of the station with the given index
     * @param id the index
     * @throws IndexOutOfBoundsException if the index is less than 0 or greater than
     *                                   or equal to the size returned by size()
     */
    @Override
    public String name(int id) {
        Preconditions.checkIndex(size(), id);
        int stringIndex = buffer.getU16(NAME_ID, id);
        return stringTable.get(stringIndex);
    }

    /**
     * Returns the longitude in degrees, of the given index station
     * 
     * @return the name of the station with the given index
     * 
     * @param id the index
     * @throws IndexOutOfBoundsException if the index is less than 0 or greater than
     *                                   or equal to the size returned by size()
     */
    @Override
    public double longitude(int id) {
        Preconditions.checkIndex(size(), id);
        return buffer.getS32(LON, id) * TO_DEGREES;
    }

    /**
     * Returns the latitude in degrees, of the given index station
     * 
     * @return the name of the station with the given index
     * 
     * @param id the index
     * @throws IndexOutOfBoundsException if the index is less than 0 or greater than
     *                                   or equal to the size returned by size()
     */
    @Override
    public double latitude(int id) {
        Preconditions.checkIndex(size(), id);
        return buffer.getS32(LAT, id) * TO_DEGREES;
    }
}
