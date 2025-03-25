package ch.epfl.rechor;

/**
 * Preconditions for checking arguments
 * 
 * @author Karam Fakhouri (374510)
 */
public final class Preconditions {
    private Preconditions() {
    }

    /**
     * Method for checking argument
     * 
     * @param shouldBeTrue
     */
    public static void checkArgument(boolean shouldBeTrue) {
        if (!shouldBeTrue) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Method for checking index of a list
     * 
     * @param size  of the list
     * @param index index
     */
    public static void checkIndex(int size, int index) {
        if (index >= size || index < 0) {
            throw new IndexOutOfBoundsException();
        }
    }
}
