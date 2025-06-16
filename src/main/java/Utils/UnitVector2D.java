package Utils;


public final class UnitVector2D extends Vector2D {

    public UnitVector2D(double angle) {
        super(Math.cos(angle), Math.sin(angle));
    }

    /** NOT SUPPORTED in Unit Vector Class since it may violate the class invariant. */
    @Override
    public void multiply(double multiplier) {
        throw new UnsupportedOperationException("Not supported in Unit Vector class.");
    }

    /** NOT SUPPORTED in Unit Vector Class since it may violate the class invariant. */
    @Override
    public void add(Vector2D speed) {
        throw new UnsupportedOperationException("Not supported in Unit Vector class.");
    }
}