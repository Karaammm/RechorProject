package ch.epfl.rechor.journey;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import ch.epfl.rechor.Bits32_24_8;

public class MyPackedCriteriaTest {

    private long criteria1 = 417823008805420l;
    private int depMins1 = 320;
    private int arrMins1 = 380;
    private int changes1 = 2;
    private int payload1 = 315948;
    private long criteria2 = 11417823008805420l;
    private int depMins2 = 320;
    private int arrMins2 = 380;
    private int changes2 = 2;
    private int payload2 = 315948;
    private long crit3 = (1l << 51);
    private long crit4 = 0;

    @Test
    void packWorksForNormalInput() {
        assertEquals(criteria1, PackedCriteria.pack(arrMins1, changes1, payload1));
    }

    @Test
    void packThrowsWhenChangesMoreThan8Bits() {
        assertThrows(IllegalArgumentException.class, () -> {
            PackedCriteria.pack(arrMins1, payload1, payload1);
        });
    }

    @Test
    void packThrowsWhenArrivalMoreThan12Bits() {
        assertThrows(IllegalArgumentException.class, () -> {
            PackedCriteria.pack(payload1, changes1, payload1);
        });
    }

    @Test
    void hasDepMinsWorks() {
        assertEquals(false, PackedCriteria.hasDepMins(criteria1));
        assertEquals(true, PackedCriteria.hasDepMins(criteria2));
    }

    @Test
    void depMinsThrows() {
        assertThrows(IllegalArgumentException.class, () -> {
            PackedCriteria.depMins(criteria1);
        });
    }

    // TODO: idk if this is the right test
    @Test
    void depMinsWorks() {
        System.out.println(Long.toBinaryString(crit3));
        assertEquals(4334, PackedCriteria.depMins(crit3));
    }

    @Test
    void withDepMinsWorks() {
        assertEquals(320, PackedCriteria.depMins(PackedCriteria.withDepMins(crit4, depMins1)));
    }
}
