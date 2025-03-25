package Specs.Physics;


/**
 * A fixed immutable instance of {@link AbstractPosition}
 */
public interface AbstractFixed extends AbstractPosition{

    default void setCoord(int newX, int newY) {
        throw new UnsupportedOperationException("AbstractFixed is immutable");
    }

    default void setSize(double newSize) {
        throw new UnsupportedOperationException("AbstractFixed is immutable");
    }

    default void setDimensionRatio(double widthToHeight) {
        throw new UnsupportedOperationException("AbstractFixed is immutable");
    }
}
