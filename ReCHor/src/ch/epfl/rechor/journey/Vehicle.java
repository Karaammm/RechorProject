package ch.epfl.rechor.journey;

import java.util.List;

/**
 * Types of vehicles
 * 
 * @author Karam Fakhouri (374510)
 */
public enum Vehicle {
    TRAM,
    METRO,
    TRAIN,
    BUS,
    FERRY,
    AERIAL_LIFT,
    FUNICULAR;

    /**
     * An immutable list containing all vehicles
     */
    public static final List<Vehicle> ALL = List.of(values());
}
