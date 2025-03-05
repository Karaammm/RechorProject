package ch.epfl.rechor.journey;

import java.util.Arrays;
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

    private ParetoFront(long[] packedCriteria){
        this.packedCriteria = packedCriteria;
    }

    public int size(){
        return packedCriteria.length;
    }

    public long get(int arrMins, int changes){
        for(long l : packedCriteria){
            long element = PackedCriteria.pack(arrMins, changes, PackedCriteria.payload(l));
            if(element == l){
                return l;
            }
        }
        throw new NoSuchElementException();
    }

    public void forEach(LongConsumer action){
        ParetoFront paretoFront = new ParetoFront(packedCriteria);
        paretoFront.forEach(value -> action.accept(value));
    }

//    public String toString(){
//        ParetoFront paretoFront = new ParetoFront(packedCriteria);
//        paretoFront.forEach(value -> String.valueOf(value));
//
//    }

    public class Builder{
        private long[] frontier;
        private int size;

        public Builder(){
            this.frontier = new long[2];
            this.size = frontier.length;
        }

        public Builder(Builder that){
            this.frontier = Arrays.copyOf(that.frontier, that.size);
            this.size = that.size;
        }

        public boolean isEmpty(){
            return size == 0;
        }

        public Builder clear(){
            size = 0;
            return this;
        }

        public Builder add(long packedTuple) {
            int pos1 = 0;
            int countDominated = 0;

            // Find insertion position and count dominated elements
            while (pos1 < size) {
                long current = frontier[pos1];

                if (PackedCriteria.dominatesOrIsEqual(current, packedTuple)) {
                    return this; // An existing tuple dominates, so do nothing
                }

                if (PackedCriteria.dominatesOrIsEqual(packedTuple, current)) {
                    countDominated++;
                } else if (packedTuple < current) {
                    break; // Found correct insertion position
                }

                pos1++;
            }

            // If the frontier is full, resize
            if (size == frontier.length) {
                long[] newFrontier = new long[(int) (frontier.length * 1.5)];
                System.arraycopy(frontier, 0, newFrontier, 0, frontier.length);
                frontier = newFrontier;
            }

            // If any dominated tuples exist, overwrite and shift others
            int remaining = size - (pos1 + countDominated);
            System.arraycopy(frontier, pos1 + countDominated, frontier, pos1 + 1, remaining);

            // Insert the new tuple
            frontier[pos1] = packedTuple;
            size -= countDominated;
            size++;

            return this;
        }

        public Builder addAll(Builder that){
            if(that.isEmpty()){
                return this;
            }
            else if(this.isEmpty()){
                this.frontier = Arrays.copyOf(that.frontier, that.size);
                this.size = that.size;
                return this;
            }else{
                long[] combFrontier = new long[this.size + that.size];
                int i = 0, j = 0, k = 0;

                while (i < this.size && j < that.size) {
                    long t1 = this.frontier[i];
                    long t2 = that.frontier[j];

                    if (PackedCriteria.dominatesOrIsEqual(t1, t2)) {
                        // t1 dominates or equals t2, skip t2
                        j++;
                    } else if (PackedCriteria.dominatesOrIsEqual(t2, t1)) {
                        // t2 dominates t1, take t2 instead
                        i++;
                        combFrontier[k++] = t2;
                        j++;
                    } else {
                        // Insert the smaller one in lexicographic order
                        if (t1 < t2) {
                            combFrontier[k++] = t1;
                            i++;
                        } else {
                            combFrontier[k++] = t2;
                            j++;
                        }
                    }
                }

                // Copy remaining elements
                while (i < this.size) combFrontier[k++] = this.frontier[i++];
                while (j < that.size) combFrontier[k++] = that.frontier[j++];

                // Update this frontier
                this.frontier = Arrays.copyOf(combFrontier, k);
                this.size = k;

                return this;
            }
        }

        public boolean fullyDominates(Builder that, int depMins){
            boolean dominatesCheck = false;
            for(int i = 0; i <= that.size; i++){
                that.frontier[i] = PackedCriteria.withDepMins(that.frontier[i],depMins);
                for(long l : this.frontier){
                    if(PackedCriteria)
                }
            }
        }

    }
}
