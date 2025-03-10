package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.StationAliases;
import java.nio.ByteBuffer;
import java.util.List;

public final class BufferedStationAliases implements StationAliases {

    private static final int ALIAS_ID = 0;
    private static final int STATION_NAME_ID = 1;

    private final List<String> stringTable;
    private final StructuredBuffer buffer;

    public BufferedStationAliases(List<String> stringTable, ByteBuffer buffer){
        this.stringTable = stringTable;
        this.buffer = new StructuredBuffer(new Structure(Structure.field(ALIAS_ID, Structure.FieldType.U16),
                Structure.field(STATION_NAME_ID, Structure.FieldType.U16)), buffer);
    }

    @Override
    public String alias(int id) {
        return stringTable.get(buffer.getU16(ALIAS_ID, id));
    }

    @Override
    public String stationName(int id) {
        return stringTable.get(buffer.getU16(STATION_NAME_ID, id));
    }

    @Override
    public int size() {
        return buffer.size();
    }
}
