package ch.epfl.rechor.timetable.mapped;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;

import ch.epfl.rechor.timetable.mapped.Structure.FieldType;

public class MyStructuredBufferTest {

    private static StructuredBuffer initialise() {
        Structure stationStructure = new Structure(
                Structure.field(0, FieldType.U8),
                Structure.field(1, FieldType.U16),
                Structure.field(2, FieldType.S32));

        byte[] bytes = new byte[stationStructure.totalSize() * 2];
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.put((byte) 4);
        buffer.putShort((short) 0x7A2F);
        buffer.putInt(0x21141FA1);

        buffer.put((byte) 6);
        buffer.putShort((short) 0xDCCC);
        buffer.putInt(0x2118DA03);

        StructuredBuffer sb = new StructuredBuffer(stationStructure, buffer);
        return sb;
    }

    StructuredBuffer structuredBuffer = initialise();

    @Test
    void sizeWorks() {
        assertEquals(2, structuredBuffer.size());
    }

    @Test
    void gettersWork() {
        assertEquals(4, structuredBuffer.getU8(0, 0));
        assertEquals(0x7A2F, structuredBuffer.getU16(1, 0));
        assertEquals(0x21141FA1, structuredBuffer.getS32(2, 0));

        assertEquals(6, structuredBuffer.getU8(0, 1));
        assertEquals(0xDCCC, structuredBuffer.getU16(1, 1));
        assertEquals(0x2118DA03, structuredBuffer.getS32(2, 1));
    }

    @Test
    void gettersThrow() {
        assertThrows(IndexOutOfBoundsException.class, () -> {
            int u8 = structuredBuffer.getU8(-1, 0);
            int u8_2 = structuredBuffer.getU8(3, 0);
            int u16 = structuredBuffer.getU16(-1, 0);
            int s32 = structuredBuffer.getS32(-1, 0);

        });
    }
}
