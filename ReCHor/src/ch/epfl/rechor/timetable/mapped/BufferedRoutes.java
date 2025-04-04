package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.Preconditions;
import ch.epfl.rechor.journey.Vehicle;
import ch.epfl.rechor.timetable.Routes;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * @author Karam Fakhouri (374510)
 *         Class that provides access to a table of routes represented in a
 *         flattened manner
 */
public final class BufferedRoutes implements Routes {
    private static final int NAME_ID = 0;
    private static final int KIND = 1;
    private final List<String> stringTable;
    private final StructuredBuffer buffer;

    /**
     * 
     * @param stringTable the provided string table, which is used to determine the
     *                    value of strings referenced by the data in the buffer
     * @param buffer      flatenned data in a ByteBuffer
     */
    public BufferedRoutes(List<String> stringTable, ByteBuffer buffer) {
        this.stringTable = stringTable;
        this.buffer = new StructuredBuffer(new Structure(Structure.field(NAME_ID, Structure.FieldType.U16),
                Structure.field(KIND, Structure.FieldType.U8)), buffer);
    }

    /**
     * 
     * @return number of elements routes
     */
    @Override
    public int size() {
        return buffer.size();
    }

    /**
     * 
     * @param id the index
     * @return the type of vehicle serving the given index line
     * @throws IndexOutOfBoundsException if the index is less than 0 or greater than
     *                                   or equal to the size returned by size()
     */
    @Override
    public Vehicle vehicle(int id) {
        Preconditions.checkIndex(size(), id);
        int vehicleIndex = buffer.getU8(KIND, id);
        return Vehicle.values()[vehicleIndex];
    }

    /**
     * 
     * @param id the index
     * @return the name of the given index row
     * @throws IndexOutOfBoundsException if the index is less than 0 or greater than
     *                                   or equal to the size returned by size()
     */
    @Override
    public String name(int id) {
        Preconditions.checkIndex(size(), id);
        int stringIndex = buffer.getU16(NAME_ID, id);
        return stringTable.get(stringIndex);
    }

}
