package ch.epfl.rechor.journey;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.function.LongConsumer;

/**
 * The Pareto Frontier
 *
 * @author Ibrahim Khokher(361860)
 * @author Karam Fakhouri (374510)
 */
public final class ParetoFront {

    private final long[] packedCriteria;
    public static final ParetoFront EMPTY = new ParetoFront(new long[0]);

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

    public String toString() {
        StringBuilder str = new StringBuilder();
        int counter = 0;
        for (long tuple : packedCriteria) {
            str.append(counter).append(")").append(" \r\n");
            if (PackedCriteria.hasDepMins(tuple)) {
                str.append("Departure: ").append(PackedCriteria.depMins(tuple)).append(" | ");
            }
            str.append("Arrival: ")
                    .append(PackedCriteria.arrMins(tuple))
                    .append(" | ").append("Changes: ")
                    .append(PackedCriteria.changes(tuple))
                    .append("\r\n");
            counter++;
        }
        return str.toString();
    }

    public static class Builder {
        private long[] frontier;
        private int size;

        public Builder() {
            this.frontier = new long[1];
            this.size = 0;
        }

        public Builder(Builder that) {
            this.frontier = Arrays.copyOf(that.frontier, that.size);
            this.size = that.size;
        }

        public boolean isEmpty() {
            return size == 0;
        }

        public Builder clear() {
            size = 0;
            return this;
        }

        public Builder add(long packedTuple) {
            if (size == 0) {
                frontier[0] = packedTuple;
                size++;
                return this;
            }

            boolean[] keepList = new boolean[size];
            int numKept = 0;
            boolean needToInsertPacked = false;
            boolean isDominated = false;
            for (int i = 0; i < size; i++) {
                keepList[i] = !PackedCriteria.dominatesOrIsEqual(packedTuple, frontier[i]);
                needToInsertPacked = needToInsertPacked || !PackedCriteria.dominatesOrIsEqual(frontier[i], packedTuple);
                isDominated = isDominated || PackedCriteria.dominatesOrIsEqual(frontier[i], packedTuple);
                numKept += booleanToInt(keepList[i]);
            }
            if (isDominated) {
                return this;
            }
            int newSize = numKept + booleanToInt(needToInsertPacked);
            long[] newFrontier = new long[newSize];

            int insertIndex = 0;
            boolean packedInserted = !needToInsertPacked;
            for (int i = 0; i < size; i++) {
                if (keepList[i]) {
                    if ((packedTuple < frontier[i]) && !packedInserted) {
                        newFrontier[insertIndex] = packedTuple;
                        insertIndex += 1;
                        packedInserted = true;
                    }
                    newFrontier[insertIndex] = frontier[i];
                    insertIndex += 1;
                }
            }
            if (!packedInserted) {
                newFrontier[insertIndex] = packedTuple;
            }

            frontier = newFrontier;
            size = newSize;
            return this;
        }

        public Builder add(int arrMins, int changes, int payload) {
            long packedTuple = PackedCriteria.pack(arrMins, changes, payload);
            return add(packedTuple);
        }

        public Builder addAll(Builder that) {
            if (that.size == 0) {
                return this;
            }
            for (long tuple : that.frontier) {
                this.add(tuple);
            }
            return this;
        }

        public boolean fullyDominates(Builder that, int depMins) {
            boolean[] dominatedList = new boolean[that.size];
            int dominatedCount = 0;
            for (int i = 0; i < that.size; i++) {
                long thatWithDepMins = PackedCriteria.withDepMins(that.frontier[i], depMins);
                for (int j = 0; j < this.size; j++) {
                    dominatedList[i] = dominatedList[i]
                            || PackedCriteria.dominatesOrIsEqual(this.frontier[j], thatWithDepMins);
                }
                dominatedCount += booleanToInt(dominatedList[i]);
            }
            return that.size == dominatedCount;
        }

        public void forEach(LongConsumer action) {
            for (int i = 0; i < size; i++) {
                action.accept(frontier[i]);
            }
        }

        public ParetoFront build() {
            return new ParetoFront(Arrays.copyOf(frontier, size));
        }

        public String toString() {
            return this.build().toString();
        }

        private int booleanToInt(boolean b) {
            return b ? 1 : 0;
        }
    }
}