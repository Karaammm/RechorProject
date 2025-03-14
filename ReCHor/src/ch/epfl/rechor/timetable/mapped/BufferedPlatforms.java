package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.Preconditions;
import ch.epfl.rechor.timetable.Platforms;
import java.nio.ByteBuffer;
import java.util.List;

public class BufferedPlatforms implements Platforms {

    private static final int NAME_ID = 0;
    private static final int STATION_ID = 1;
    private List<String> stringTable;
    private StructuredBuffer buffer;


    public BufferedPlatforms(List<String> stringTable, ByteBuffer buffer) {
        this.stringTable = stringTable;
        this.buffer = new StructuredBuffer(new Structure(Structure.field(NAME_ID, Structure.FieldType.U16),
                Structure.field(STATION_ID, Structure.FieldType.U16)), buffer);
    }

    @Override
    public String name(int id) {
        int stringIndex = buffer.getU16(NAME_ID, id);
        return stringTable.get(stringIndex);
    }

    @Override
    public int stationId(int id) {
        return buffer.getU16(STATION_ID, id);
    }

    @Override
    public int size() {
        return buffer.size();
    }
}
