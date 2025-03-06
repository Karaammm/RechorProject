package ch.epfl.rechor.journey;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class MyParetoFrontTest {
    ParetoFront.Builder builder = new ParetoFront.Builder();
    long t1 = PackedCriteria.pack(720, 3, 0);
    long t2 = PackedCriteria.pack(720, 4, 0);
    long t3 = PackedCriteria.pack(721, 2, 0);
    long t4 = PackedCriteria.pack(722, 1, 0);
    long t5 = PackedCriteria.pack(723, 0, 0);
    long t6 = PackedCriteria.pack(724, 1, 0);

    @Test
    void builderIsEmptyInitially() {
        assertTrue(builder.isEmpty());
    }

    @Test
    void addLongWorks() {
        builder.add(t1);
        builder.add(t2);
        builder.add(t6);
        builder.add(t3);
        builder.add(t4);
        builder.add(t5);
        System.out.println(builder);
    }

    @Test
    void clearWorks() {
        builder.add(t1);
        assertTrue(!builder.isEmpty());
        builder.clear();
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
        builder.add(t2);
        builder.add(t6);
        ParetoFront.Builder builderBetter = new ParetoFront.Builder();
        builderBetter.add(t4);
        builderBetter.add(t1);

        // For debugging, uncomment the prints
        // System.out.println(builder);
        // System.out.println(builderBetter);

        assertTrue(builder.fullyDominates(builderBetter, 240));
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

}
