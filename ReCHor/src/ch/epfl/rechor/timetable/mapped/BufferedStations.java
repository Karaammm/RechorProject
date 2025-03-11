package ch.epfl.rechor.timetable.mapped;

import java.nio.ByteBuffer;
import java.util.List;

import ch.epfl.rechor.timetable.Stations;

public final class BufferedStations implements Stations {

    private final List<String> stringTable;
    private ByteBuffer buffer;
    private static final int RECORD_SIZE = 10;
    private static final int NAME_ID_OFFSET = 0;
    private static final int LONGITUDE_OFFSET = 2;
    private static final int LATITUDE_OFFSET = 6;

    public BufferedStations(List<String> stringTable, ByteBuffer buffer) {
        this.stringTable = stringTable;
        this.buffer = buffer;
    }

    @Override
    public int size() {
        return buffer.capacity() / RECORD_SIZE;
    }

    @Override
    public String name(int id) {
        int offset = id * RECORD_SIZE + NAME_ID_OFFSET;
        int nameIndex = Short.toUnsignedInt(buffer.getShort(offset));
        return stringTable.get(nameIndex);
    }

    @Override
    public double longitude(int id) {
        int offsettedIndex = id * RECORD_SIZE + LONGITUDE_OFFSET;
        return toDegrees(buffer.getInt(offsettedIndex));
    }

    @Override
    public double latitude(int id) {
        int offsettedIndex = id * RECORD_SIZE + LATITUDE_OFFSET;
        return toDegrees(buffer.getInt(offsettedIndex));
    }

    private double toDegrees(int raw) {
        return (StrictMath.scalb((double) raw, -32) * 360.0);
    }

    public String toString() {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < stringTable.size(); i++) {
            str.append(i).append(" | ").append(stringTable.get(i)).append("\r\n");
        }
        buffer = buffer.rewind();
        for (int i = 0; i < size(); i++) {
            str.append(String.valueOf(buffer.getShort()))
                    .append(" | ")
                    .append(String.valueOf(buffer.getInt()))
                    .append(" | ")
                    .append(String.valueOf(buffer.getInt()))
                    .append("\r\n");
        }
        return str.toString();
    }
}
