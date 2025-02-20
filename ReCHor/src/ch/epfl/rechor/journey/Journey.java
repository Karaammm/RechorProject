package ch.epfl.rechor.journey;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import ch.epfl.rechor.Preconditions;
import ch.epfl.rechor.journey.Journey.Leg.IntermediateStop;

/**
 * A journey
 * 
 * @author Karam Fakhouri (374510)
 */
public record Journey(List<Leg> legs) {

    /**
     * Constructs a journey with the list of "legs" given
     * The legs list is copied to ensure immutability
     * 
     * @param legs the list of legs
     * @throws NullPointerException     if the list is either empty or null
     * @throws IllegalArgumentException if the arrival stop of the previous leg is
     *                                  not identical to the departing stop of the
     *                                  current
     * @throws IllegalArgumentException if the start time of the current step
     *                                  precedes the arrival time of the previous
     *                                  step
     * @throws IllegalArgumentException if the stages do not alternate between foot
     *                                  and transport
     *
     */
    public Journey {
        legs = List.copyOf(legs);
        Objects.requireNonNull(legs);
        Preconditions.checkArgument(!(legs.isEmpty()));

        for (int i = 1; i < legs.size(); i++) {
            Leg prev = legs.get(i - 1);
            Leg current = legs.get(i);
            Preconditions.checkArgument((prev.arrStop()).equals(current.depStop()));
            Preconditions.checkArgument(
                    (current.depTime().isAfter(prev.arrTime())) || current.depTime().equals(prev.arrTime()));
            Preconditions.checkArgument((prev instanceof Leg.Foot) != (current instanceof Leg.Foot));
        }
    }

    /**
     * Returns the departure stop of this journey
     * 
     * @return the departure stop of this journey
     */
    public Stop depStop() {
        return this.legs.getFirst().depStop();
    }

    /**
     * Returns the arrival stop of this journey
     * 
     * @return the arrival stop of this journey
     */
    public Stop arrStop() {
        return legs.getLast().arrStop();
    }

    /**
     * Returns the local departure time of this journey
     * 
     * @return the local departure time of this journey
     */
    public LocalDateTime depTime() {
        return legs.getFirst().depTime();
    }

    /**
     * Returns the local arrival time of this journey
     * 
     * @return the local arrival time of this journey
     */
    public LocalDateTime arrTime() {
        return legs.getLast().arrTime();
    }

    /**
     * Returns the duration of this journey
     * 
     * @return the duration of this journey
     */
    public Duration duration() {
        return Duration.between(legs.getFirst().depTime(), legs.getLast().arrTime());
    }

    /**
     * A "leg" or a step of a journey
     * 
     * @author Karam Fakhouri (374510)
     */
    public sealed interface Leg {

        /**
         * Returns the departure stop of this leg
         * 
         * @return the departure stop of this leg
         */
        public Stop depStop();

        /**
         * Returns the departure time of this leg
         * 
         * @return the departure time of this leg
         */
        public LocalDateTime depTime();

        /**
         * Returns the arrival stop of this leg
         * 
         * @return the arrival stop of this leg
         */
        public Stop arrStop();

        /**
         * Returns the arrival time of this leg
         * 
         * @return the arrival time of this leg
         */
        public LocalDateTime arrTime();

        /**
         * returns the list of stops throughout the leg
         * 
         * @return the list of stops throughout the leg
         */
        public List<IntermediateStop> intermediateStops();

        /**
         * returns the duration of the leg
         * 
         * @return the duration of the leg
         */
        default Duration duration() {
            return Duration.between(depTime(), arrTime());
        }

        /**
         * An intermediate stop
         * 
         * @author Karam Fakhouri (374510)
         * 
         * @param stop    current stop
         * @param arrTime arrival time of this stage
         * @param depTime departure time of this stage
         */
        public record IntermediateStop(Stop stop, LocalDateTime arrTime, LocalDateTime depTime) {

            /**
             * Compact constructor of the intermediate stop
             * 
             * @param stop    current stop (must be nonNull)
             * @param arrTime arrival time (must be before or the same as the departure
             *                time)
             * @param depTime departure time (must be after or the same as the arrival
             *                time)
             * @throws NullPointerException     if stop is null
             * @throws IllegalArgumentException if departure time is strictly before the
             *                                  arrival time
             */
            public IntermediateStop {
                Objects.requireNonNull(stop);
                Preconditions.checkArgument(depTime.isAfter(arrTime) || depTime.isEqual(arrTime));
            }

        }

        /**
         * A step performed in public transport
         * 
         * @author Karam Fakhouri (374510)
         * 
         */
        public record Transport(Stop depStop, LocalDateTime depTime, Stop arrStop, LocalDateTime arrTime,
                List<IntermediateStop> intermediateStops, Vehicle vehicle, String route,
                String destination) implements Leg {

            /**
             * Compact constructor of the intermediate stop
             * Copies intermediateStops to ensure immutability
             * 
             * @param depStop           departure stop (must be nonNull)
             * @param depTime           departure time (must be nonNull and before/same as
             *                          the arrival
             *                          time)
             * @param arrStop           arrival stop (must be nonNull)
             * @param arrTime           arrival time (must be nonNull and after/same as the
             *                          departure
             *                          time)
             * @param intermediateStops any intermediate
             *                          stop during this stage
             * @param vehicle           the type of vehicle used for this stage
             * @param route             the name of the line on which the vehicle used for
             *                          this
             *                          stage operates
             * @param destination       the name of the final destinatio of the vehicle used
             *                          for
             *                          this stage
             * 
             * @throws NullPointerException     if
             *                                  depStop, depTime, arrStop, arrTime, vehicle,
             *                                  route,
             *                                  or destination is null
             * @throws IllegalArgumentException if departure time is strictly before the
             *                                  arrival time
             */
            public Transport {
                Objects.requireNonNull(depStop);
                Objects.requireNonNull(depTime);
                Objects.requireNonNull(arrStop);
                Objects.requireNonNull(arrTime);
                Preconditions.checkArgument(depTime.isBefore(arrTime) || depTime.isEqual(arrTime));
                Objects.requireNonNull(vehicle);
                Objects.requireNonNull(route);
                Objects.requireNonNull(destination);
                intermediateStops = List.copyOf(intermediateStops);

            }

            /**
             * @return any intermediate steps
             */
            public List<IntermediateStop> intermediateStops() {
                return intermediateStops;
            }

        }

        /**
         * A step performed by walking or changing
         * 
         * * @author Karam Fakhouri (374510)
         */
        public record Foot(Stop depStop, LocalDateTime depTime, Stop arrStop, LocalDateTime arrTime) implements Leg {

            /**
             * 
             * Compact constructor of the foot stage
             * 
             * @param depStop departure stop (must be nonNull)
             * @param depTime departure time (must be nonNull and before/same as
             *                the arrival
             *                time)
             * @param arrStop arrival stop (must be nonNull)
             * @param arrTime arrival time (must be nonNull and after/same as the
             *                departure
             *                time)
             * @throws NullPointerException     if any of the parameters are null
             * @throws IllegalArgumentException if departure time is after arrival time
             */
            public Foot {
                Objects.requireNonNull(depStop);
                Objects.requireNonNull(depTime);
                Objects.requireNonNull(arrStop);
                Objects.requireNonNull(arrTime);
                Preconditions.checkArgument(depTime.isBefore(arrTime) || depTime.isEqual(arrTime));
            }

            /**
             * Returns an empty list
             * 
             * @return empty list, as there are no stops in a walking stage
             */
            @Override
            public List<IntermediateStop> intermediateStops() {
                return List.of();
            }

            /**
             * Returns true if the Foot is a change and not a walking stage
             * 
             * @return true if the Foot is a change and not a walking stage
             */
            public boolean isTransfer() {
                if ((depStop.name()).equals(arrStop.name())) {
                    return true;
                }
                return false;
            }

        }
    }
}
