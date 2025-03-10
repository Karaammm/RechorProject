package ch.epfl.rechor.timetable;

import ch.epfl.rechor.journey.Vehicle;

/**
 * Public transport lines
 * 
 * @author Karam Fakhouri (374510)
 */
public interface Routes extends Indexed {

    /**
     * returns the type of vehicle serving the given index line
     * 
     * @param id the index
     * @return the type of vehicle serving the given index line
     * @throws IndexOutOfBoundsException if the index is less than 0 or greater than
     *                                   or equal to the size returned by size()
     */
    Vehicle vehicle(int id);

    /**
     * returns the name of the given index row
     * 
     * @param id the index
     * @return the name of the given index row
     * @throws IndexOutOfBoundsException if the index is less than 0 or greater than
     *                                   or equal to the size returned by size()
     */
    String name(int id);
}
