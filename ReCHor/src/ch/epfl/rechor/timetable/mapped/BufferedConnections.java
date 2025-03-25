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

    // Constants for the fields in the connection structure
    private static final int DEP_STOP_ID = 0;
    private static final int DEP_MINUTES = 1;
    private static final int ARR_STOP_ID = 2;
    private static final int ARR_MINUTES = 3;
    private static final int TRIP_POS_ID = 4;

    // Constant for the 'Next Connection' field in the auxiliary table
    private static final int NEXT_CONNECTION_ID = 0;

    // Buffers for connection data and successor connections
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

    @Override
    public int depStopId(int id) {
        Preconditions.checkIndex(size(), id);
        return structuredBuffer.getU16(DEP_STOP_ID, id);
    }

    @Override
    public int depMins(int id) {
        Preconditions.checkIndex(size(), id);
        return structuredBuffer.getU16(DEP_MINUTES, id);
    }

    @Override
    public int arrStopId(int id) {
        Preconditions.checkIndex(size(), id);
        return structuredBuffer.getU16(ARR_STOP_ID, id);
    }

    @Override
    public int arrMins(int id) {
        Preconditions.checkIndex(size(), id);
        return structuredBuffer.getU16(ARR_MINUTES, id);
    }

    @Override
    public int tripId(int id) {
        Preconditions.checkIndex(size(), id);
        return structuredBuffer.getS32(TRIP_POS_ID, id) >> 8;
    }

    @Override
    public int tripPos(int id) {
        Preconditions.checkIndex(size(), id);
        return structuredBuffer.getS32(TRIP_POS_ID, id) & 0xFF;
    }

    @Override
    public int nextConnectionId(int id) {
        Preconditions.checkIndex(size(), id);
        return succStructuredBuffer.getS32(NEXT_CONNECTION_ID, id);
    }

    @Override
    public int size() {
        return structuredBuffer.size();
    }
}