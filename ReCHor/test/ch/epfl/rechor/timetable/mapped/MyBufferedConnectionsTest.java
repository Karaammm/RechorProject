package ch.epfl.rechor.timetable.mapped;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.Assert.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

class MyBufferedConnectionsTest {

    ByteBuffer connectionBuffer;
    ByteBuffer successorBuffer;

    @BeforeEach
    public void setUp() {


        // Define a connection data buffer (4 routes with 10 bytes each)
        byte[] connectionData = new byte[12 * 4]; // 4 connections, 10 bytes each
        connectionBuffer = ByteBuffer.wrap(connectionData).order(ByteOrder.BIG_ENDIAN);

        // Define a successor buffer (4 connections with 4 bytes each)
        byte[] successorData = new byte[4 * 4]; // 4 connections
        successorBuffer = ByteBuffer.wrap(successorData).order(ByteOrder.BIG_ENDIAN);

        // Connection 0: depStopId = 100, depMinutes = 10, arrStopId = 200, arrMinutes = 20, tripId = 300
        connectionBuffer.putShort((short) 100); // depStopId
        connectionBuffer.putShort((short) 10);  // depMinutes
        connectionBuffer.putShort((short) 200); // arrStopId
        connectionBuffer.putShort((short) 20);  // arrMinutes
        connectionBuffer.putInt(300);           // tripId (int)

        // Connection 1: depStopId = 110, depMinutes = 15, arrStopId = 210, arrMinutes = 25, tripId = 310
        connectionBuffer.putShort((short) 110); // depStopId
        connectionBuffer.putShort((short) 15);  // depMinutes
        connectionBuffer.putShort((short) 210); // arrStopId
        connectionBuffer.putShort((short) 25);  // arrMinutes
        connectionBuffer.putInt(310);           // tripId (int)

        // Connection 2: depStopId = 120, depMinutes = 12, arrStopId = 220, arrMinutes = 22, tripId = 320
        connectionBuffer.putShort((short) 120); // depStopId
        connectionBuffer.putShort((short) 12);  // depMinutes
        connectionBuffer.putShort((short) 220); // arrStopId
        connectionBuffer.putShort((short) 22);  // arrMinutes
        connectionBuffer.putInt(320);           // tripId (int)

        // Connection 3: depStopId = 130, depMinutes = 18, arrStopId = 230, arrMinutes = 28, tripId = 330
        connectionBuffer.putShort((short) 130); // depStopId
        connectionBuffer.putShort((short) 18);  // depMinutes
        connectionBuffer.putShort((short) 230); // arrStopId
        connectionBuffer.putShort((short) 28);  // arrMinutes
        connectionBuffer.putInt(330);           // tripId (int)

        // Successor buffer (next connections for each route)
        successorBuffer.putInt(1); // Connection 0 → Connection 1
        successorBuffer.putInt(2); // Connection 1 → Connection 2
        successorBuffer.putInt(3); // Connection 2 → Connection 3
        successorBuffer.putInt(0); // Connection 3 → End (no next connection)

        connectionBuffer.rewind();  // Reset the buffer position for reading
        successorBuffer.rewind();   // Reset successor buffer for reading


    }



    @Test
    void depStopId() {
        BufferedConnections connections = new BufferedConnections(connectionBuffer, successorBuffer);
        assertEquals(100, connections.depStopId(0));
        assertEquals(110, connections.depStopId(1));
        assertEquals(120, connections.depStopId(2));
        assertEquals(130, connections.depStopId(3));
    }

    @Test
    void depMins() {
        BufferedConnections connections = new BufferedConnections(connectionBuffer, successorBuffer);
        assertEquals(10, connections.depMins(0));
        assertEquals(15, connections.depMins(1));
        assertEquals(12, connections.depMins(2));
        assertEquals(18, connections.depMins(3));
    }

    @Test
    void arrStopId() {
        BufferedConnections connections = new BufferedConnections(connectionBuffer, successorBuffer);
        assertEquals(200, connections.arrStopId(0));
        assertEquals(210, connections.arrStopId(1));
        assertEquals(220, connections.arrStopId(2));
        assertEquals(230, connections.arrStopId(3));
    }

    @Test
    void arrMins() {
        BufferedConnections connections = new BufferedConnections(connectionBuffer, successorBuffer);
        assertEquals(20, connections.arrMins(0));
        assertEquals(25, connections.arrMins(1));
        assertEquals(22, connections.arrMins(2));
        assertEquals(28, connections.arrMins(3));
    }

    @Test
    void tripId() {
        BufferedConnections connections = new BufferedConnections(connectionBuffer, successorBuffer);
        assertEquals(300, connections.tripId(0));
        assertEquals(310, connections.tripId(1));
        assertEquals(320, connections.tripId(2));
        assertEquals(330, connections.tripId(3));
    }

    @Test
    void tripPos() {
        BufferedConnections connections = new BufferedConnections(connectionBuffer, successorBuffer);
        assertEquals(0, connections.tripPos(0));
        assertEquals(1, connections.tripPos(1));
        assertEquals(255, connections.tripId(2));
        assertEquals(50, connections.tripPos(3));
    }

    @Test
    void nextConnectionId() {
        BufferedConnections connections = new BufferedConnections(connectionBuffer, successorBuffer);
        assertEquals(1, connections.nextConnectionId(0));
        assertEquals(2, connections.nextConnectionId(1));
        assertEquals(3, connections.nextConnectionId(2));
        assertEquals(0, connections.nextConnectionId(3));
    }

    @Test
    void size() {

    }

    @Test
    public void testInvalidIndices() {
        BufferedConnections connections = new BufferedConnections(connectionBuffer, successorBuffer);
        assertThrows(IndexOutOfBoundsException.class, () -> connections.depStopId(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> connections.depMins(4));
        assertThrows(IndexOutOfBoundsException.class, () -> connections.nextConnectionId(4));
    }
}