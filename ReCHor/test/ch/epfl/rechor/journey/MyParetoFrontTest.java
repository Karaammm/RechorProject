package ch.epfl.rechor.journey;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import java.util.NoSuchElementException;

public class MyParetoFrontTest {
    ParetoFront.Builder builder = new ParetoFront.Builder();
    ParetoFront.Builder builder2 = new ParetoFront.Builder();
    long t1 = PackedCriteria.pack(720, 3, 1427124);
    long t2 = PackedCriteria.pack(720, 4, 331312);
    long t3 = PackedCriteria.pack(721, 2, 0);
    long t4 = PackedCriteria.pack(722, 1, 0);
    long t5 = PackedCriteria.pack(723, 0, 0);
    long t6 = PackedCriteria.pack(724, 1, 0);
    long t7 = PackedCriteria.pack(721, 0, 0);

    @Test
    void builderIsEmptyInitially() {
        assertTrue(builder.isEmpty());
    }

    @Test
    void addLongWorks() {
        builder.add(t1);
        System.out.println(builder);
        builder.add(t2);
        System.out.println(builder);
        builder.add(t6);
        System.out.println(builder);
        builder.add(t3);
        System.out.println(builder);
        builder.add(t4);
        System.out.println(builder);
        builder.add(t5);
        System.out.println(builder);
        builder.add(t7);
        System.out.println(builder);
        builder.add(721, 1, 0);
        System.out.println(builder);
    }

    @Test
    void addAllWorks() {
        builder.add(t1);
        System.out.println(builder);
        builder.add(t2);
        System.out.println(builder);
        builder.add(t6);
        System.out.println(builder);
        builder.add(t3);
        System.out.println(builder);
        builder.add(t4);
        System.out.println(builder);
        builder.add(t5);
        System.out.println(builder);
        builder.add(t7);
        System.out.println(builder);
        builder.add(721, 1, 0);
        System.out.println(builder);
        ParetoFront.Builder builder2 = new ParetoFront.Builder();
        builder2.addAll(builder);
        System.out.println(builder2);
    }

    @Test
    void addAllWorksForOneEmptyBuilder() {
        builder.add(PackedCriteria.withDepMins(t1, 0));
        builder.add(PackedCriteria.withDepMins(t3, 0));
        builder2.addAll(builder);
        assertEquals(builder.toString(), builder2.toString());
    }

    @Test
    void addAllWorksForBothBuildersEmpty() {
        assertEquals(builder.toString(), builder2.toString());
    }

    @Test
    void clearWorks() {
        builder.add(t1);
        builder.add(t2);
        assertFalse(builder.isEmpty());
        builder.clear();
        System.out.println(builder);
        assertTrue(builder.isEmpty());
    }

    @Test
    void addAndBuildSortedOrder() {
        assertTrue(builder.isEmpty());
        builder.add(t2);
        builder.add(t3);
        builder.add(t4);
        System.out.println(builder);
    }

    @Test
    void fullyDominatesWorks() {
        builder.add(PackedCriteria.withDepMins(t1, 0));
        builder.add(PackedCriteria.withDepMins(t4, 0));
        builder2.add(PackedCriteria.withDepMins(t2, 0));
        builder2.add(PackedCriteria.withDepMins(t6, 0));
        // For debugging, uncomment the prints
        System.out.println(builder);
        System.out.println(builder2);
        assertTrue(builder.fullyDominates(builder2, 0));
        assertFalse(builder2.fullyDominates(builder, 0));
    }

    @Test
    void fullyDominatesEmptyBuilder() {
        builder.add(PackedCriteria.withDepMins(t1, 0));
        builder.add(PackedCriteria.withDepMins(t3, 0));
        builder.add(PackedCriteria.withDepMins(t4, 0));
        assertTrue(builder.fullyDominates(builder2, 0));
    }

    @Test
    void fullyDominatesWorksForDiffEmptyBuilder() {
        builder.add(PackedCriteria.withDepMins(t1, 0));
        builder.add(PackedCriteria.withDepMins(t3, 0));
        builder.add(PackedCriteria.withDepMins(t4, 0));
        assertFalse(builder2.fullyDominates(builder, 0));
    }

    @Test
    void sizeWorksForEmptyFrontier() {
        ParetoFront prtFrnt = builder.build();
        assertEquals(0, prtFrnt.size());
    }

    @Test
    void sizeWorksForNonEmpty() {
        builder.add(t1);
        builder.add(t3);
        ParetoFront prtFrnt = builder.build();
        assertEquals(2, prtFrnt.size());
    }

    @Test
    void getWorksForNonEmpty() {
        builder.add(t1);
        builder.add(t3);
        ParetoFront prtFrnt = builder.build();
        assertEquals(t1, prtFrnt.get(720, 3));
        assertEquals(t3, prtFrnt.get(721, 2));
    }

    @Test
    void getThrowsWhenNotFound() {
        builder.add(t1);
        ParetoFront prtFrnt = builder.build();
        assertThrows(NoSuchElementException.class, () -> {
            prtFrnt.get(721, 127);
        });
    }
}
