package ch.epfl.rechor.timetable.mapped;

import java.nio.ByteBuffer;
import java.util.List;

import ch.epfl.rechor.timetable.Stations;

public final class BufferedStations implements Stations {

    private final List<String> stringTable;
    private ByteBuffer buffer;

    public BufferedStations(List<String> stringTable, ByteBuffer buffer) {
        this.stringTable = stringTable;
        this.buffer = buffer;
    }

    @Override
    public int size() {
        return buffer.capacity() / 10;
    }

    @Override
    public String name(int id) {
        int offsettedIndex = Short.toUnsignedInt(buffer.getShort(id * 10));
        return stringTable.get(offsettedIndex);
    }

    @Override
    public double longitude(int id) {
        int offsettedIndex = id * 10 + 2;
        return toDegrees(buffer.getInt(offsettedIndex));
    }

    @Override
    public double latitude(int id) {
        int offsettedIndex = id * 10 + 6;
        return toDegrees(buffer.getInt(offsettedIndex));
    }

    // private double fromDegrees(double d) {
    // return StrictMath.scalb(d, 32) / 360;
    // }

    private double toDegrees(double d) {
        return StrictMath.scalb(d, -32) * 360;
    }
}
