package ch.epfl.rechor.gui;

import ch.epfl.rechor.journey.Vehicle;
import javafx.scene.image.Image;

import java.util.EnumMap;

public abstract class VehicleIcons {
    private final static EnumMap<Vehicle,Image> vehicleMap = new EnumMap<>(Vehicle.class);
    static Image iconFor(Vehicle vehicle){
        return vehicleMap.computeIfAbsent(vehicle,v -> new Image(v.name() + ".png"));
    }
}
