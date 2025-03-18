package ch.epfl.rechor.timetable.mapped;

import java.nio.ByteBuffer;
import java.util.List;

import ch.epfl.rechor.journey.Vehicle;
import ch.epfl.rechor.timetable.Routes;

public final class BufferedRoutes implements Routes {
    private static final int NAME_ID = 0;
    private static final int KIND = 1;
    private List<String> stringTable;
    private StructuredBuffer buffer;

    public BufferedRoutes(List<String> stringTable, ByteBuffer buffer) {
        this.stringTable = stringTable;
        this.buffer = new StructuredBuffer(new Structure(Structure.field(NAME_ID, Structure.FieldType.U16),
                Structure.field(KIND, Structure.FieldType.U16)), buffer);
    }

    @Override
    public int size() {
        return buffer.size();
    }

    @Override
    public Vehicle vehicle(int id) {
        if (id < 0 || id >= size()) {
            throw new IndexOutOfBoundsException();
        }
        int vehicleIndex = buffer.getU16(KIND, id);
        return Vehicle.values()[vehicleIndex];
    }

    @Override
    public String name(int id) {
        if (id < 0 || id >= size()) {
            throw new IndexOutOfBoundsException();
        }
        int stringIndex = buffer.getU16(NAME_ID, id);
        return stringTable.get(stringIndex);
    }

}
