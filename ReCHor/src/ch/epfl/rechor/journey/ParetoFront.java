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

    /**
     * The journey details
     */
    private final long[] packedCriteria;
    /**
     * Empty Pareto Frontier
     */
    public static final ParetoFront EMPTY = new ParetoFront(new long[0]);

    /**
     * private constructor
     * @param packedCriteria the array that stores the journey details
     */
    private ParetoFront(long[] packedCriteria) {
        this.packedCriteria = packedCriteria;
    }

    /**
     *
     * @return the size of the frontier
     */
    public int size() {
        return packedCriteria.length;
    }

    /**
     *
     * @param arrMins arrival time in minutes
     * @param changes the number of changes
     * @return the packed value which contains the given information
     * @throws NoSuchElementException if there is no packed value with the given information
     */
    public long get(int arrMins, int changes) {
        for (long l : packedCriteria) {
            if (PackedCriteria.arrMins(l) == arrMins && PackedCriteria.changes(l) == changes) {
                return l;
            }
        }
        throw new NoSuchElementException();
    }

    /**
     *
     * @param action to do
     */
    public void forEach(LongConsumer action) {
        for (long value : packedCriteria) {
            action.accept(value);
        }
    }

    /**
     * Helper method for debugging
     * @return returns a string that showcases the details of each tuple
     */
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

    /**
     * Builder class for the frontier
     */
    public static class Builder {

        /**
         * The array that will store the tuples
         */
        private long[] frontier;

        /**
         * An initializer for the size of the array
         */
        private int size;

        /**
         * Default constructor that initialises the tuple array and size
         */
        public Builder() {
            this.frontier = new long[1];
            this.size = 0;
        }

        /**
         * Public constructor that initialises the frontier and size to the given instance
         * @param that an instance of the Builder class
         */
        public Builder(Builder that) {
            this.frontier = Arrays.copyOf(that.frontier, that.size);
            this.size = that.size;
        }

        /**
         *
         * @return if the frontier is empty
         */
        public boolean isEmpty() {
            return size == 0;
        }

        /**
         *
         * @return clears the frontier and returns it
         */
        public Builder clear() {
            size = 0;
            return this;
        }

        /**
         *
         * @param packedTuple packed criteria of the tuple
         * @return adds the given tuple to the frontier if it is relevant to add and returns it
         */
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
                needToInsertPacked = needToInsertPacked ||
                        !PackedCriteria.dominatesOrIsEqual(frontier[i], packedTuple);
                isDominated = isDominated || PackedCriteria.dominatesOrIsEqual(frontier[i],
                        packedTuple);
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

        /**
         *
         * @param arrMins arrival time in minutes
         * @param changes number of changes
         * @param payload given payload
         * @return adds the given criteria after packing it and then returns it
         */
        public Builder add(int arrMins, int changes, int payload) {
            long packedTuple = PackedCriteria.pack(arrMins, changes, payload);
            return add(packedTuple);
        }

        /**
         *
         * @param that given frontier builder
         * @return an instance of this with the addition of the given frontier
         */
        public Builder addAll(Builder that) {
            if (that.size == 0) {
                return this;
            }
            for (long tuple : that.frontier) {
                this.add(tuple);
            }
            return this;
        }

        /**
         *
         * @param that instance of the frontier builder
         * @param depMins departure time in minutes
         * @return true if every tuple in thisBuilder dominates the tuples in thatBuilder
         */
        public boolean fullyDominates(Builder that, int depMins) {
            boolean[] dominatedList = new boolean[that.size];
            int dominatedCount = 0;
            for (int i = 0; i < that.size; i++) {
                long thatWithDepMins = PackedCriteria.withDepMins(that.frontier[i], depMins);
                for (int j = 0; j < this.size; j++) {
                    dominatedList[i] = dominatedList[i]
                            || PackedCriteria.dominatesOrIsEqual(this.frontier[j], thatWithDepMins);
                }
                if (!dominatedList[i]) {
                    return false;
                }
                dominatedCount += booleanToInt(dominatedList[i]);
            }
            return that.size == dominatedCount;
        }

        /**
         *
         * @param action to do
         */
        public void forEach(LongConsumer action) {
            for (int i = 0; i < size; i++) {
                action.accept(frontier[i]);
            }
        }

        /**
         *
         * @return an instance of ParetoFront with the current tuple array and its size
         */
        public ParetoFront build() {
            return new ParetoFront(Arrays.copyOf(frontier, size));
        }

        /**
         *
         * @return returns a string that showcases the details of each tuple
         */
        public String toString() {
            return this.build().toString();
        }

        /**
         *
         * @param b given boolean
         * @return returns 1 if the given boolean is true, 0 otherwise
         */
        private int booleanToInt(boolean b) {
            return b ? 1 : 0;
        }
    }
}