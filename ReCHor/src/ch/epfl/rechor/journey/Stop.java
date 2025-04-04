package ch.epfl.rechor.journey;

import ch.epfl.rechor.Preconditions;

import java.util.Objects;

/**
 * A public transport stop
 * 
 * @author Karam Fakhouri (374510)
 */
public record Stop(String name, String platformName, double longitude, double latitude) {

    /**
     * Constructs a stop
     * 
     * @param name of the stop
     * @param platformName of the platform, if it exists
     * @param longitude longitude coordinate of the stop
     * @param latitude latitude coordinate of the stop
     */
    public Stop {
        Objects.requireNonNull(name);
        Preconditions.checkArgument(longitude >= -180 && longitude <= 180);
        Preconditions.checkArgument(latitude >= -90 && latitude <= 90);
    }
}
