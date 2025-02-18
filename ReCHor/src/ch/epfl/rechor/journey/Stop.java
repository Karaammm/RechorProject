package ch.epfl.rechor.journey;

import java.util.Objects;

import ch.epfl.rechor.Preconditions;

public record Stop(String name, String platformName, Double longitude, Double latitude) {
    public Stop {
        Preconditions.checkArgument((longitude > 180 || longitude < -180) || (latitude > 90 || latitude < -90));
        Objects.requireNonNull(name);
    }
}
