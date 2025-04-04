package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.Preconditions;

import java.util.Objects;

/**
 * @author Karam Fakhouri(374510)
 *         The Structure of the flatenned data, composed of fields
 */
public final class Structure {

    /**
     * Enum for the type of field stored in the structure
     */
    public enum FieldType {
        U8,
        U16,
        S32
    }

    /**
     * The field stored inside the structure
     * 
     * @throws NullPointerException if given type is null
     */
    public record Field(int index, FieldType type) {
        public Field {
            Objects.requireNonNull(type);
        }
    }

    /**
     * Static method for the creation of instances of Field
     * 
     * @param index the index
     * @param type  the type of field
     * @return a field with the provided index and type
     */
    public static Field field(int index, FieldType type) {
        return new Field(index, type);
    }

    private final Field[] fields;

    /**
     * Constructor for structure
     * 
     * @param fields arbitrary number of fields
     * @throws IllegalArgumentException if the field indices are not given in order
     */
    public Structure(Field... fields) {
        Preconditions.checkArgument(fields.length > 0 && fields[0].index == 0);
        for (int i = 1; i < fields.length; i++) {
            Preconditions.checkArgument(fields[i - 1].index < fields[i].index);
        }
        this.fields = fields.clone();
    }

    /**
     * Returns the number of bytes in the structure
     * 
     * @return number of bytes in the structure
     */
    public int totalSize() {
        int size = 0;
        for (Field field : fields) {
            size += size(field);
        }
        return size;
    }

    /**
     * Returns the index, in the byte array containing the flattened data, of
     * the first byte of the index field of the index element
     * 
     * @param fieldIndex   the index field
     * @param elementIndex the index element
     * @return the index of the first byte of the index field of the index element
     * @throws IndexOutOfBoundsException if the given field index is negative or
     *                                   greater than the length of the list
     */
    public int offset(int fieldIndex, int elementIndex) {
        Preconditions.checkIndex(fields.length, fieldIndex);
        return (elementIndex * totalSize()) + fieldOffset(fieldIndex);
    }

    /**
     * Helper method to calculate the offset
     * 
     * @param fieldIndex the index field
     * @return the offset of the field without the element
     */
    private int fieldOffset(int fieldIndex) {
        int computedOffset = 0;
        for (int i = 0; i < fieldIndex; i++) {
            computedOffset += size(fields[i]);
        }
        return computedOffset;
    }

    /**
     * Helper method for organizing the totalSize method
     * 
     * @param field the given field
     * @return the number of bytes in the given field
     */
    private int size(Field field) {
        return switch (field.type) {
            case FieldType.U8 -> 1;
            case FieldType.U16 -> 2;
            case FieldType.S32 -> 4;
        };
    }

    public String toString() {
        StringBuilder str = new StringBuilder();
        for (Field field : fields) {
            str.append(field.index).append(" : ").append(field.type).append("\r\n");
        }
        return str.toString();
    }
}
