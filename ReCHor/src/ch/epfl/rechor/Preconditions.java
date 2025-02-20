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
}
