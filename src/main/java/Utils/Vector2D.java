package Utils;

/**
 * Represents a 2D vector with standard operations such as addition,
 * subtraction, normalization, scaling, and more.
 * This class is immutable, providing methods that return new
 * vectors for functional-style usage.
 */
public class Vector2D {

    /** The X and Y components of this vector. Should be effectively IMMUTABLE. */
    public double x, y;

    /**
     * Constructs a new vector with the given x and y components.
     * @param x the x component
     * @param y the y component
     */
    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /** Rotates this vector by the specified amount in radians counterclockwise. */
    public void rotate(double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double newX = x * cos - y * sin;
        double newY = x * sin + y * cos;

        this.x = newX;
        this.y = newY;
    }

    /** Adds the given values to this vector. One of the few MUTABLE function
     * allowed in this class because of its repetitive usage. */
    public void add(Vector2D speed) {
        x += speed.x;
        y += speed.y;
    }

    /** Returns a new vector that is the sum of this vector and another vector. */
    public Vector2D added(Vector2D v) {
        return new Vector2D(this.x + v.x, this.y + v.y);
    }

    /** Returns the length (magnitude) of this vector. */
    public double length() {
        return Math.sqrt(x * x + y * y);
    }

    /** Returns the angle (in radians) of this vector from the x-axis. */
    public double angle() {
        return Math.atan2(y, x);
    }

    /** Returns a new normalized version of this vector. */
    public Vector2D normalized() {
        double strength = length();
        return strength == 0 ? new Vector2D(0, 0) : new Vector2D(x / strength, y / strength);
    }


    /** Scales this vector by the given multiplier. One of the few MUTABLE function
     * allowed in this class because of its repetitive usage. */
    public void multiply(double multiplier) {
        x *= multiplier;
        y *= multiplier;
    }

    /** Returns a new vector scaled by the given multiplier. */
    public Vector2D multiplied(double multiplier) {
        return new Vector2D(x * multiplier, y * multiplier);
    }

    /** Returns a new vector scaled by the given x and y multipliers. */
    public Vector2D multiplied(double mx, double my) {
        return new Vector2D(x * mx, y * my);
    }

    /** Returns a new vector divided by the given value. */
    public Vector2D divided(double divider) {
        return new Vector2D(x / divider, y / divider);
    }

    /** Returns a new vector with the given values subtracted. */
    public Vector2D subtracted(double dx, double dy) {
        return new Vector2D(this.x - dx, this.y - dy);
    }

    /** Returns a new vector with another vector subtracted. */
    public Vector2D subtracted(Vector2D v) {
        return new Vector2D(this.x - v.x, this.y - v.y);
    }

    /** Applies a component-wise minimum constraint to this vector. */
    public void minVector(double minX, double minY) {
    }

    /** Returns a new vector with a component-wise minimum constraint applied. */
    public Vector2D minVectored(double minX, double minY) {
        if (minX < 0 || minY < 0) throw new RuntimeException("Minimum can't be below zero");

        double x = this.x, y = this.y;
        if (Math.abs(x) < minX) x = Math.signum(x) * minX;
        if (Math.abs(y) < minY) y = Math.signum(y) * minY;
        return new Vector2D(x, y);
    }

    /** Returns a new vector with components no smaller than the given minimum. */
    public Vector2D minVectored(double min) {
        return minVectored(min, min);
    }

    /** Returns a new vector with components no larger than the given maximum. */
    public Vector2D maxVectored(double max) {
        if (max <= 0) return (Vector2D) clone();

        double x = this.x, y = this.y;
        if (Math.abs(x) > max) x = Math.signum(x) * max;
        if (Math.abs(y) > max) y = Math.signum(y) * max;
        return new Vector2D(x, y);
    }

    /** Returns a string representation of the vector as "x,y". */
    @Override
    public String toString() {
        return this.x + "," + this.y;
    }

    /** Returns a new copy of this vector. */
    @Override
    public Object clone() {
        return new Vector2D(this.x, this.y);
    }
}