package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.Transfers;

import java.nio.ByteBuffer;

public class BufferedTransfers implements Transfers {

    // Constants for the fields in the transfer structure
    private static final int DEP_STATION_ID = 0;
    private static final int ARR_STATION_ID = 1;
    private static final int TRANSFER_MINUTES = 2;
    

    // Buffers for transfer data
    private final StructuredBuffer structuredBuffer;

    /**
     * Constructs a BufferedTransfers object with access to the flattened transfer data.
     *
     * @param buffer the ByteBuffer containing the transfer data
     */
    public BufferedTransfers(ByteBuffer buffer) {
        Structure transferStructure = new Structure(
                Structure.field(DEP_STATION_ID, Structure.FieldType.U16),
                Structure.field(ARR_STATION_ID, Structure.FieldType.U16),
                Structure.field(TRANSFER_MINUTES, Structure.FieldType.U16)

        );

        this.structuredBuffer = new StructuredBuffer(transferStructure, buffer);
    }
    @Override
    public int depStationId(int id) {
        if(id < 0 || id > size()){
            throw new IndexOutOfBoundsException();
        }

        return structuredBuffer.getU16(DEP_STATION_ID, id);
    }

    @Override
    public int minutes(int id) {
        if(id < 0 || id > size()){
            throw new IndexOutOfBoundsException();
        }

        return structuredBuffer.getU16(TRANSFER_MINUTES, id);
    }

    @Override
    public int arrivingAt(int stationId) {
        if(stationId < 0 || stationId > size()){
            throw new IndexOutOfBoundsException();
        }
        return 0;
    }

    @Override
    public int minutesBetween(int depStationId, int arrStationId) {
        if(depStationId < 0 || arrStationId > size()){
            throw new IndexOutOfBoundsException();
        }
        for (int i = 0; i < size(); i++) {
            if (structuredBuffer.getU16(i, DEP_STATION_ID) == depStationId &&
                    structuredBuffer.getU16(i, ARR_STATION_ID) == arrStationId) {
                return structuredBuffer.getU16(i, TRANSFER_MINUTES);
            }
        }
        throw new IndexOutOfBoundsException();

    }

    @Override
    public int size() {
        return structuredBuffer.size();
    }
}
