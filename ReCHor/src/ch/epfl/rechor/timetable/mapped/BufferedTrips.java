package ch.epfl.rechor.timetable.mapped;

import java.nio.ByteBuffer;
import java.util.List;

import ch.epfl.rechor.timetable.Trips;

public final class BufferedTrips implements Trips {

    private static final int ROUTE_ID = 0;
    private static final int DESTINATION_ID = 1;
    private List<String> stringTable;
    private StructuredBuffer buffer;

    public BufferedTrips(List<String> stringTable, ByteBuffer buffer) {
        this.stringTable = stringTable;
        this.buffer = new StructuredBuffer(new Structure(Structure.field(ROUTE_ID, Structure.FieldType.U16),
                Structure.field(DESTINATION_ID, Structure.FieldType.U16)), buffer);
    }

    @Override
    public int size() {
        return buffer.size();
    }

    @Override
    public int routeId(int id) {
        if (id < 0 || id >= size()) {
            throw new IndexOutOfBoundsException();
        }
        return buffer.getU16(ROUTE_ID, id);
    }

    @Override
    public String destination(int id) {
        if (id < 0 || id >= size()) {
            throw new IndexOutOfBoundsException();
        }
        return stringTable.get(buffer.getU16(DESTINATION_ID, id));

    }

}
