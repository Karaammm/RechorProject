package ch.epfl.rechor.journey;

import java.util.List;

public enum Vehicle {
    TRAM,
    METRO,
    TRAIN,
    BUS,
    FERRY,
    AERIAL_LIFT,
    FENICULAR;

    public static final List<Vehicle> ALL = List.of(values());
}
