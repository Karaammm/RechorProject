package ch.epfl.rechor.timetable.mapped;

import java.util.Objects;

import ch.epfl.rechor.Preconditions;

public final class Structure {

    public enum FieldType {
        U8,
        U16,
        S32;
    }

    public record Field(int index, FieldType type) {
        public Field {
            Objects.requireNonNull(type);
        }
    }

    public static Field field(int index, FieldType type) {
        return new Field(index, type);
    }

    private final Field[] fields;

    public Structure(Field... fields) {
        for (int i = 1; i < fields.length; i++) {
            Preconditions.checkArgument(fields[i - 1].index < fields[i].index);
        }
        this.fields = fields.clone();
    }

    public int totalSize() {
        int size = 0;
        for (Field field : fields) {
            size += size(field);
        }
        return size;
    }

    public int offset(int fieldIndex, int elementIndex) {
        if (fieldIndex < 0 || fieldIndex >= fields.length) {
            throw new IndexOutOfBoundsException(fieldIndex);
        }
        return (elementIndex * totalSize()) + fieldOffset(fieldIndex);
    }

    private int fieldOffset(int fieldIndex) {
        int computedOffset = 0;
        for (int i = 0; i < fieldIndex; i++) {
            computedOffset += size(fields[i]);
        }
        return computedOffset;
    }

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
