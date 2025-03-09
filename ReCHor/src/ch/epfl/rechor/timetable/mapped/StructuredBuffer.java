package ch.epfl.rechor.timetable.mapped;

import java.nio.ByteBuffer;

import ch.epfl.rechor.Preconditions;

public class StructuredBuffer {

    private Structure structure;
    private ByteBuffer buffer;

    public StructuredBuffer(Structure structure, ByteBuffer buffer) {
        Preconditions.checkArgument((buffer.capacity() % structure.totalSize()) == 0);
        this.structure = structure;
        this.buffer = buffer;

    }

    public int size() {
        return buffer.capacity() / structure.totalSize();
    }

    public int getU8(int fieldIndex, int elementIndex) {
        int offset = structure.offset(fieldIndex, elementIndex);
        return Byte.toUnsignedInt(buffer.get(offset));
    }

    public int getU16(int fieldIndex, int elementIndex) {
        int offset = structure.offset(fieldIndex, elementIndex);
        return Short.toUnsignedInt(buffer.getShort(offset));
    }

    public int getS32(int fieldIndex, int elementIndex) {
        int offset = structure.offset(fieldIndex, elementIndex);
        return buffer.getInt(offset);
    }
}
