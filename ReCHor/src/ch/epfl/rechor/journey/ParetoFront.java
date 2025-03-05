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

        public Builder add(long packedTuple){
            int pos1 = 0;
            int pos2 = 0;
            boolean dominates = false;
            for(long l : frontier){
                pos1++;
                if(PackedCriteria.dominatesOrIsEqual(packedTuple, l)){
                    frontier[pos1] = packedTuple;
                    dominates = true;
//                    frontier = Arrays.copyOf(frontier, size + 1);
//                    System.arraycopy(frontier, pos, frontier, pos + 1, size - pos);
//                    frontier[pos] = packedTuple;
                }
            }
            if(!dominates){

            }
            return this;
        }

    }
}
