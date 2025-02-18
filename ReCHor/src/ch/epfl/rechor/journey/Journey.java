package ch.epfl.rechor.journey;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import ch.epfl.rechor.Preconditions;
import ch.epfl.rechor.journey.Journey.Leg.IntermediateStop;

public record Journey(List<Leg> legs) {

    public Journey {
        legs = List.copyOf(legs);
        Preconditions.checkArgument(legs == null || legs.isEmpty());
        for (int i = 1; i < legs.size(); i++) {
            Leg prev = legs.get(i - 1);
            Leg current = legs.get(i);

            // Check that the starting stop is identical to the arrival stop of the previous
            // one
            Preconditions.checkArgument((prev.arrStop()).equals(current.depStop()));

            // Check thatthe start time does not precede the arrival time of the previous
            // one
            Preconditions.checkArgument((current.depTime().isAfter(prev.arrTime())));

            // Check that the stages on foot alternate with those by transport
            Preconditions.checkArgument((prev instanceof Leg.Foot) != (current instanceof Leg.Foot));
        }
    }

    public Stop depStop() {
        return legs.getFirst().depStop();
    }

    public Stop arrStop() {
        return legs.getLast().arrStop();
    }

    public LocalDateTime depTime() {
        return legs.getFirst().depTime();
    }

    public LocalDateTime arrTime() {
        return legs.getLast().arrTime();
    }

    public Duration duration() {
        return Duration.between(legs.getFirst().depTime(), legs.getLast().arrTime());
    }

    public interface Leg {

        public Stop depStop();

        public LocalDateTime depTime();

        public Stop arrStop();

        public LocalDateTime arrTime();

        public List<IntermediateStop> intermediateStops();

        default Duration duration() {
            return Duration.between(depTime(), arrTime());
        }

        public record IntermediateStop(Stop stop, LocalDateTime arrTime, LocalDateTime depTime) {

            public IntermediateStop {
                Objects.requireNonNull(stop);
                Preconditions.checkArgument(depTime.isBefore(arrTime));
            }

        }

        public record Transport(Stop depStop, LocalDateTime depTime, Stop arrStop, LocalDateTime arrTime,
                List<IntermediateStop> intermediateStops, Vehicle vehicle, String route,
                String destination) implements Leg {

            public Transport {

            }

            @Override
            public Stop depStop() {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'depStop'");
            }

            @Override
            public LocalDateTime depTime() {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'depTime'");
            }

            @Override
            public Stop arrStop() {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'arrStop'");
            }

            @Override
            public LocalDateTime arrTime() {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'arrTime'");
            }

            @Override
            public List<IntermediateStop> intermediateStops() {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'intermediateStops'");
            }

        }

        public record Foot() implements Leg {

            @Override
            public Stop depStop() {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'depStop'");
            }

            @Override
            public LocalDateTime depTime() {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'depTime'");
            }

            @Override
            public Stop arrStop() {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'arrStop'");
            }

            @Override
            public LocalDateTime arrTime() {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'arrTime'");
            }

            @Override
            public List<IntermediateStop> intermediateStops() {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'intermediateStops'");
            }

        }
    }
}
