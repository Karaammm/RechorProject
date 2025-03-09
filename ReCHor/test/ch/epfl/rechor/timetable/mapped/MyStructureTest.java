package ch.epfl.rechor.timetable.mapped;

import static org.junit.Assert.assertEquals;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import ch.epfl.rechor.timetable.mapped.Structure.FieldType;

public class MyStructureTest {
    Structure stationStructure = new Structure(
            Structure.field(0, FieldType.U8),
            Structure.field(1, FieldType.U16),
            Structure.field(2, FieldType.S32),
            Structure.field(3, FieldType.U8),
            Structure.field(4, FieldType.U16),
            Structure.field(5, FieldType.S32));

    @Test
    void structureThrowsForInvalidIndex() {
        assertThrows(IllegalArgumentException.class, () -> {
            Structure invalidStructure = new Structure(Structure.field(1, FieldType.S32),
                    Structure.field(0, FieldType.U16));
        });
    }

    @Test
    void totalSizeWorks() {
        assertEquals(14, stationStructure.totalSize());
    }

    @Test
    void offsetWorks() {
        assertEquals(0, stationStructure.offset(0, 0));
        assertEquals(1, stationStructure.offset(1, 0));
        assertEquals(3, stationStructure.offset(2, 0));
        assertEquals(14, stationStructure.offset(0, 1));
        assertEquals(15, stationStructure.offset(1, 1));
        assertEquals(17, stationStructure.offset(2, 1));
    }

    @Test
    void offsetThrowsForInvalidFieldIndex() {
        assertThrows(IndexOutOfBoundsException.class, () -> {
            stationStructure.offset(-1, 0);
        });
        assertThrows(IndexOutOfBoundsException.class, () -> {
            stationStructure.offset(6, 0);
        });
    }

    @Test
    void structureToStringWorks() {
        System.out.println(stationStructure);
    }

}
