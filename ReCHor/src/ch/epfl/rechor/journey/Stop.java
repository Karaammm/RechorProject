package ch.epfl.rechor.journey;

import java.util.Objects;

import ch.epfl.rechor.Preconditions;

// public record Stop(String name, String platformName, Double longitude, Double latitude) {
//     public Stop {
//         Objects.requireNonNull(name);
//         Preconditions.checkArgument((longitude > 180 || longitude < -180));
//         Preconditions.checkArgument((latitude > 90 || latitude < -90));
//     }
// }

public record Stop(String name, String platformName, double longitude, double latitude) {

    public Stop {
        Objects.requireNonNull(name);
        Preconditions.checkArgument(longitude >= -180 && longitude <= 180);
        Preconditions.checkArgument(latitude >= -90 && latitude <= 90);
    }
}
