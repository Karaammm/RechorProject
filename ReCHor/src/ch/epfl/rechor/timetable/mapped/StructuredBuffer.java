package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.Preconditions;

import java.nio.ByteBuffer;

/**
 * @author Karam Fakhouri (374510)
 *         Represents a structured byte array. The purpose of this class is to
 *         provide convient access to flattenned data stored in a byte array, in
 *         which the structure is described in an instance of Structure
 */
public class StructuredBuffer {

    private final Structure structure;
    private final ByteBuffer buffer;

    /**
     * Constructor a structured array whose elements have the given structure, and
     * whose bytes are stored in the "array" buffer
     * 
     * @param structure the given structure
     * @param buffer    the given buffer
     * @throws IllegalArgumentException if the number of bytes in this array is not
     *                                  a multiple of the total size of the
     *                                  structure
     */
    public StructuredBuffer(Structure structure, ByteBuffer buffer) {
        Preconditions.checkArgument((buffer.capacity() % structure.totalSize()) == 0);
        this.structure = structure;
        this.buffer = buffer;

    }

    /**
     * Returns the number of elements in the array
     * 
     * @return the number of elements in the array
     */
    public int size() {
        return buffer.capacity() / structure.totalSize();
    }

    /**
     * Returns the U8 integer corresponding to the index field of the index element
     * 
     * @param fieldIndex   the index field
     * @param elementIndex the index element
     * @return the U8 integer
     */
    public int getU8(int fieldIndex, int elementIndex) {
        int offset = structure.offset(fieldIndex, elementIndex);
        return Byte.toUnsignedInt(buffer.get(offset));
    }

    /**
     * Returns the U16 integer corresponding to the index field of the index element
     * 
     * @param fieldIndex   the index field
     * @param elementIndex the index element
     * @return the U16 integer
     */
    public int getU16(int fieldIndex, int elementIndex) {
        int offset = structure.offset(fieldIndex, elementIndex);
        return Short.toUnsignedInt(buffer.getShort(offset));
    }

    /**
     * Returns the S32 integer corresponding to the index field of the index element
     * 
     * @param fieldIndex   the index field
     * @param elementIndex the index element
     * @return the S32 integer
     */
    public int getS32(int fieldIndex, int elementIndex) {
        int offset = structure.offset(fieldIndex, elementIndex);
        return buffer.getInt(offset);
    }
}
