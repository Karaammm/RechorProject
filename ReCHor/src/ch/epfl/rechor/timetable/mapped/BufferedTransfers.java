package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.PackedRange;
import ch.epfl.rechor.Preconditions;
import ch.epfl.rechor.timetable.Transfers;

import java.nio.ByteBuffer;

/**
 * Implementation of the Transfers interface for accessing flattened timetable
 * data.
 *
 * @author Ibrahim Khokher (361860)
 */
public class BufferedTransfers implements Transfers {


    /**
     * Constants for the fields in the transfer structure
     */
    private static final int DEP_STATION_ID = 0;
    private static final int ARR_STATION_ID = 1;
    private static final int TRANSFER_MINUTES = 2;

    /**
     * Buffers for transfer data
     */
    private final StructuredBuffer structuredBuffer;
    private final int[] arrivingAtTable;

    /**
     * Constructs a BufferedTransfers object with access to the flattened transfer
     * data.
     *
     * @param buffer the ByteBuffer containing the transfer data
     */
    public BufferedTransfers(ByteBuffer buffer) {
        Structure transferStructure = new Structure(
                Structure.field(DEP_STATION_ID, Structure.FieldType.U16),
                Structure.field(ARR_STATION_ID, Structure.FieldType.U16),
                Structure.field(TRANSFER_MINUTES, Structure.FieldType.U8)

        );
        this.structuredBuffer = new StructuredBuffer(transferStructure, buffer);

        int totalChanges = structuredBuffer.size();
        int maxStationId = -1;
        for (int i = 0; i < totalChanges; i++) {
            int arrStation = structuredBuffer.getU16(ARR_STATION_ID, i);
            if (arrStation > maxStationId) {
                maxStationId = arrStation;
            }
        }

        this.arrivingAtTable = new int[maxStationId + 1];

        int[] firstIndex = new int[maxStationId + 1];
        int[] lastIndex = new int[maxStationId + 1];
        for (int i = 0; i <= maxStationId; i++) {
            firstIndex[i] = -1;
            lastIndex[i] = -1;
        }
        for (int i = 0; i < totalChanges; i++) {
            int arrStation = structuredBuffer.getU16(ARR_STATION_ID, i);
            if (firstIndex[arrStation] == -1) {
                firstIndex[arrStation] = i;
            }
            lastIndex[arrStation] = i;
        }

        for (int i = 0; i <= maxStationId; i++) {
            if (firstIndex[i] != -1) {
                arrivingAtTable[i] = PackedRange.pack(firstIndex[i], lastIndex[i] + 1);
            } else {
                arrivingAtTable[i] = 0;
            }
        }
    }

    /**
     *
     * @param id the index change
     * @return the index of the starting station
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    @Override
    public int depStationId(int id) {
        Preconditions.checkIndex(size(), id);
        return structuredBuffer.getU16(DEP_STATION_ID, id);
    }

    /**
     *
     * @param id the index change
     * @return the duration
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    @Override
    public int minutes(int id) {
        Preconditions.checkIndex(size(), id);

        return structuredBuffer.getU8(TRANSFER_MINUTES, id);
    }

    /**
     *
     * @param stationId the index of the arrival station
     * @return the packed interval of the index of changes
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    @Override
    public int arrivingAt(int stationId) {
        Preconditions.checkIndex(size(), stationId);
        return arrivingAtTable[stationId];
    }

    /**
     *
     * @param depStationId departure station
     * @param arrStationId arrival station
     * @return the duration in minutes of the change between the two stations
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    @Override
    public int minutesBetween(int depStationId, int arrStationId) {
        if (depStationId < 0 || arrStationId < 0) {
            throw new IndexOutOfBoundsException();
        }
        for (int i = 0; i < size(); i++) {
            if (structuredBuffer.getU16(DEP_STATION_ID, i) == depStationId &&
                    structuredBuffer.getU16(ARR_STATION_ID, i) == arrStationId) {
                return structuredBuffer.getU8(TRANSFER_MINUTES, i);
            }
        }
        throw new IndexOutOfBoundsException();

    }

    /**
     * Returns the number of elements of said data
     *
     * @return number of elements
     */
    @Override
    public int size() {
        return structuredBuffer.size();
    }
}
