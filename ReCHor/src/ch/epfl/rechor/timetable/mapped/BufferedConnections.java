package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.Preconditions;
import ch.epfl.rechor.timetable.Connections;
import java.nio.ByteBuffer;

/**
 * Implementation of the Connections interface for accessing flattened timetable
 * data.
 *
 * @author Ibrahim Khokher (361860)
 */
public final class BufferedConnections implements Connections {

    /**
     * Constants for the fields in the connection structure
     */
    private static final int DEP_STOP_ID = 0;
    private static final int DEP_MINUTES = 1;
    private static final int ARR_STOP_ID = 2;
    private static final int ARR_MINUTES = 3;
    private static final int TRIP_POS_ID = 4;

    /**
     * Constant for the 'Next Connection' field in the auxiliary table
     */
    private static final int NEXT_CONNECTION_ID = 0;

    /**
     * Buffers for connection data and successor connections
     */
    private final StructuredBuffer structuredBuffer;
    private final StructuredBuffer succStructuredBuffer;

    /**
     * Constructs a BufferedConnections object with access to the flattened
     * connection and successor data.
     *
     * @param buffer     the ByteBuffer containing the connection data
     * @param succBuffer the ByteBuffer containing the next connection data
     */
    public BufferedConnections(ByteBuffer buffer, ByteBuffer succBuffer) {

        Structure connectionStructure = new Structure(
                Structure.field(DEP_STOP_ID, Structure.FieldType.U16),
                Structure.field(DEP_MINUTES, Structure.FieldType.U16),
                Structure.field(ARR_STOP_ID, Structure.FieldType.U16),
                Structure.field(ARR_MINUTES, Structure.FieldType.U16),
                Structure.field(TRIP_POS_ID, Structure.FieldType.S32));
        this.structuredBuffer = new StructuredBuffer(connectionStructure, buffer);

        Structure succStructure = new Structure(Structure.field(NEXT_CONNECTION_ID, Structure.FieldType.S32));
        this.succStructuredBuffer = new StructuredBuffer(succStructure, succBuffer);
    }

    /**
     *
     * @param id the index
     * @return the index of the starting stop of the index
     * @throws IndexOutOfBoundsException if the index is less than 0 or greater than or equal to the size returned by
     * size()
     */
    @Override
    public int depStopId(int id) {
        Preconditions.checkIndex(size(), id);
        return structuredBuffer.getU16(DEP_STOP_ID, id);
    }

    /**
     *
     * @param id the index
     * @return the start time of the given index link (in minutes after midnight)
     * @throws IndexOutOfBoundsException if the index is less than 0 or greater than or equal to the size returned by
     * size()
     */
    @Override
    public int depMins(int id) {
        Preconditions.checkIndex(size(), id);
        return structuredBuffer.getU16(DEP_MINUTES, id);
    }

    /**
     *
     * @param id the index
     * @return the index of the arrival time of the index
     * @throws IndexOutOfBoundsException if the index is less than 0 or greater than or equal to the size returned by
     * size()
     */
    @Override
    public int arrStopId(int id) {
        Preconditions.checkIndex(size(), id);
        return structuredBuffer.getU16(ARR_STOP_ID, id);
    }

    /**
     *
     * @param id the index
     * @return the arrival time of the index (in minutes after midnight)
     * @throws IndexOutOfBoundsException if the index is less than 0 or greater than or equal to the size returned by
     * size()
     */
    @Override
    public int arrMins(int id) {
        Preconditions.checkIndex(size(), id);
        return structuredBuffer.getU16(ARR_MINUTES, id);
    }

    /**
     *
     * @param id the index
     * @return the index of the line of which the given index belongs to
     * @throws IndexOutOfBoundsException if the index is less than 0 or greater than or equal to the size returned by
     * size()
     */
    @Override
    public int tripId(int id) {
        Preconditions.checkIndex(size(), id);
        return structuredBuffer.getS32(TRIP_POS_ID, id) >> 8;
    }

    /**
     *
     * @param id the index
     * @return the position of the connection with the given index in the line
     * @throws IndexOutOfBoundsException if the index is less than 0 or greater than or equal to the size returned by
     * size()
     */
    @Override
    public int tripPos(int id) {
        Preconditions.checkIndex(size(), id);
        return structuredBuffer.getS32(TRIP_POS_ID, id) & 0xFF;
    }

    /**
     *
     * @param id the index
     * @return the index of the connection following the one with the given index in the line to which it belongs, or
     * the index of the first connection in the line if the connection with the given index is the last in the line
     * @throws IndexOutOfBoundsException if the index is less than 0 or greater than or equal to the size returned by
     * size()
     */
    @Override
    public int nextConnectionId(int id) {
        Preconditions.checkIndex(size(), id);
        return succStructuredBuffer.getS32(NEXT_CONNECTION_ID, id);
    }

    /**
     * Returns the number of elements of the buffer
     *
     * @return number of elements
     */
    @Override
    public int size() {
        return structuredBuffer.size();
    }
}