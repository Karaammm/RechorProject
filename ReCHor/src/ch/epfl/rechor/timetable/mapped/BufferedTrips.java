package ch.epfl.rechor.timetable.mapped;

import java.nio.ByteBuffer;
import java.util.List;

import ch.epfl.rechor.Preconditions;
import ch.epfl.rechor.timetable.Trips;

/**
 * @author Karam Fakhouri (374510)
 * 
 *         Class that provides access to a table of trips represented in a
 *         flattened manner
 */
public final class BufferedTrips implements Trips {

    private static final int ROUTE_ID = 0;
    private static final int DESTINATION_ID = 1;
    private List<String> stringTable;
    private StructuredBuffer buffer;

    /**
     * 
     * @param stringTable the provided string table, which is used to determine the
     *                    value of strings referenced by the data in the buffer
     * @param buffer      flatenned data in a ByteBuffer
     */
    public BufferedTrips(List<String> stringTable, ByteBuffer buffer) {
        this.stringTable = stringTable;
        this.buffer = new StructuredBuffer(new Structure(Structure.field(ROUTE_ID, Structure.FieldType.U16),
                Structure.field(DESTINATION_ID, Structure.FieldType.U16)), buffer);
    }

    /**
     * 
     * @return number of elements trips
     */
    @Override
    public int size() {
        return buffer.size();
    }

    /**
     * @returnthe index of the row to which the given index trip belongs
     */
    @Override
    public int routeId(int id) {
        Preconditions.checkIndex(size(), id);
        return buffer.getU16(ROUTE_ID, id);
    }

    /**
     * @return the name of the final destination of the trip
     */
    @Override
    public String destination(int id) {
        Preconditions.checkIndex(size(), id);
        return stringTable.get(buffer.getU16(DESTINATION_ID, id));

    }

}
