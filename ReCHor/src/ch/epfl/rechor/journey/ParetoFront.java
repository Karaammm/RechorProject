package ch.epfl.rechor.journey;

import java.util.NoSuchElementException;
import java.util.function.LongConsumer;

/**
 * The Pareto Frontier
 *
 * @author Ibrahim Khokher(361860)
 */
public final class ParetoFront {

    private final long[] packedCriteria;
    ParetoFront EMPTY = new ParetoFront(new long[0]);

    private ParetoFront(long[] packedCriteria) {
        this.packedCriteria = packedCriteria;
    }

    public int size() {
        return packedCriteria.length;
    }

    public long get(int arrMins, int changes) {
        for (long l : packedCriteria) {
            long element = PackedCriteria.pack(arrMins, changes, PackedCriteria.payload(l));
            if (element == l) {
                return l;
            }
        }
        throw new NoSuchElementException();
    }

    public void forEach(LongConsumer action) {
        ParetoFront paretoFront = new ParetoFront(packedCriteria);
        paretoFront.forEach(value -> action.accept(value));
    }

    // public String toStrin g(){
    //     ParetoFront paretoFront = new ParetoFront(packedCriteria);
    //     paretoFront.forEach(value -> System.out.println(value));
    // }
}
