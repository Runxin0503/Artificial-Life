package Physics;

import Utils.Vector2D;

public class Fixed extends Position {

    public Fixed(int id) {
        super(id);
    }

    @Override
    boolean isBoundingBoxChange() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    double getMass() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double getDamage() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setSize(double newSize) {
        throw new UnsupportedOperationException("Fixed Position is immutable");
    }

    @Override
    @Deprecated
    public void setDimensionRatio(double widthToHeight) {
        throw new UnsupportedOperationException("Fixed Position is immutable");
    }

    @Override
    Vector2D collision(Dynamic movable) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected void reset() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
