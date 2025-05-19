package Utils;


public class UnitVector2D extends Vector2D {

    public UnitVector2D(double angle) {
        super(Math.cos(angle), Math.sin(angle));
    }

    public void rotate(double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double newX = x * cos - y * sin;
        double newY = x * sin + y * cos;

        this.x = newX;
        this.y = newY;
    }

    @Override
    public void add(Vector2D speed) {
        throw new UnsupportedOperationException("Not supported in Unit Vector class.");
    }
}