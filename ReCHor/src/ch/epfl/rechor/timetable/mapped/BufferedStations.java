package ch.epfl.rechor.timetable.mapped;

import java.nio.ByteBuffer;
import java.util.List;

import ch.epfl.rechor.timetable.Stations;

/**
 * @author Karam Fakhouri (374510)
 * 
 *         Class that provides access to a table of stations represented in a
 *         flattened manner
 */
public final class BufferedStations implements Stations {

    private final List<String> stringTable;
    private ByteBuffer buffer;
    private static final int RECORD_SIZE = 10;
    private static final int NAME_ID_OFFSET = 0;
    private static final int LONGITUDE_OFFSET = 2;
    private static final int LATITUDE_OFFSET = 6;
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
        this.buffer = buffer;
    }

    /**
     * Returns the number of elements in the array
     * 
     * @return number of elements
     */
    @Override
    public int size() {
        return buffer.capacity() / RECORD_SIZE;
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
        if (id < 0 || id >= size()) {
            throw new IndexOutOfBoundsException();
        }
        int offset = id * RECORD_SIZE + NAME_ID_OFFSET;
        int nameIndex = Short.toUnsignedInt(buffer.getShort(offset));
        return stringTable.get(nameIndex);
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
        if (id < 0 || id >= size()) {
            throw new IndexOutOfBoundsException();
        }
        int offsettedIndex = id * RECORD_SIZE + LONGITUDE_OFFSET;
        return buffer.getInt(offsettedIndex) * TO_DEGREES;
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
        if (id < 0 || id >= size()) {
            throw new IndexOutOfBoundsException();
        }
        int offsettedIndex = id * RECORD_SIZE + LATITUDE_OFFSET;
        return buffer.getInt(offsettedIndex) * TO_DEGREES;
    }

    public String toString() {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < stringTable.size(); i++) {
            str.append(i).append(" | ").append(stringTable.get(i)).append("\r\n");
        }
        buffer = buffer.rewind();
        for (int i = 0; i < size(); i++) {
            str.append(String.valueOf(buffer.getShort()))
                    .append(" | ")
                    .append(String.valueOf(buffer.getInt()))
                    .append(" | ")
                    .append(String.valueOf(buffer.getInt()))
                    .append("\r\n");
        }
        return str.toString();
    }
}
