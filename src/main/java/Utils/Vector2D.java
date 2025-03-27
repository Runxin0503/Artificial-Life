package Utils;

public class Vector2D {
    public double x, y;

    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2D() {
        this.x = 0;
        this.y = 0;
    }

    public Vector2D add(double dx, double dy) {
        this.x += dx;
        this.y += dy;
        return this;
    }

    public Vector2D add(Vector2D v) {
        this.x += v.x;
        this.y += v.y;
        return this;
    }

    public double length() {
        return Math.sqrt(x * x + y * y);
    }

    public double angle() {
        return Math.atan2(y, x);
    }

    public Vector2D normalize() {
        double strength = length();
        if (strength == 0) return this;
        this.x /= strength;
        this.y /= strength;
        return this;
    }

    public Vector2D multiply(double multiplier) {
        this.x *= multiplier;
        this.y *= multiplier;
        return this;
    }

    public Vector2D multiply(double mx, double my) {
        this.x *= mx;
        this.y *= my;
        return this;
    }

    public Vector2D divide(double divider) {
        this.x /= divider;
        this.y /= divider;
        return this;
    }

    public Vector2D subtract(double dx, double dy) {
        this.x -= dx;
        this.y -= dy;
        return this;
    }

    public Vector2D subtract(Vector2D v) {
        this.x -= v.x;
        this.y -= v.y;
        return this;
    }

    public Vector2D minVector(double minX, double minY) {
        if (minX != 0) if ((minX < 0 == x < 0) || Math.abs(x) < Math.abs(minX)) this.x = minX;
        if (minY != 0) if ((minY < 0 == y < 0) || Math.abs(y) < Math.abs(minY)) this.y = minY;
        return this;
    }

    public Vector2D min(double min) {
        if (min == 0) return this;
        if (Math.abs(x) < Math.abs(min)) this.x = x < 0 ? -min : min;
        if (Math.abs(y) < Math.abs(min)) this.y = y < 0 ? -min : min;
        return this;
    }

    public Vector2D max(double max) {
        if (max == 0) return this;
        if (Math.abs(x) > max) this.x = x < 0 ? -max : max;
        if (Math.abs(y) > max) this.y = y < 0 ? -max : max;
        return this;
    }

    @Override
    public String toString() {
        return this.x + "," + this.y;
    }
}