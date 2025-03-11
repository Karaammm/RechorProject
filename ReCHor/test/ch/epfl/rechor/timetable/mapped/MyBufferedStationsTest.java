package ch.epfl.rechor.timetable.mapped;

import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class MyBufferedStationsTest {
    private List<String> stringTable = new ArrayList<>();
    private byte[] bytes = new byte[260];
    ByteBuffer buffer;
    BufferedStations bufferedStations = initialiseBufferedStations();

    private double fromDegrees(double d) {
        return (StrictMath.scalb(d, 32) / 360.0);
    }

    void initialise() {
        for (int i = 0; i < 26; i++) {
            stringTable.add(String.valueOf((char) ('A' + i)));
        }
        buffer = ByteBuffer.wrap(bytes);
        for (int i = 0; i < 260; i += 10) {
            buffer.putShort((short) (i / 10));
            buffer.putInt(2 * i);
            buffer.putInt(i + i + i);
        }

    }

    BufferedStations initialiseBufferedStations() {
        initialise();
        return new BufferedStations(stringTable, buffer);

    }

    @Test
    void sizeWorks() {

        assertEquals(26, bufferedStations.size());
    }

    @Test
    void nameWorks() {
        for (int i = 0; i < 26; i++) {
            String expected = String.valueOf((char) ('A' + i));
            assertEquals(expected, bufferedStations.name(i));
        }
    }

    @Test
    void toStringWorks() {
        System.out.println(bufferedStations.toString());
    }

    @Test
    void longitudeWorks() {
        for (int i = 0; i < 26; i++) {
            double expected = buffer.getInt((i * 10) + 2);
            assertEquals(expected, fromDegrees(bufferedStations.longitude(i)), 0);
        }
    }

    @Test
    void latitudeWorks() {
        for (int i = 0; i < 26; i++) {
            double expected = buffer.getInt((i * 10) + 6);
            assertEquals(expected, fromDegrees(bufferedStations.latitude(i)), 0);
        }
    }

}
