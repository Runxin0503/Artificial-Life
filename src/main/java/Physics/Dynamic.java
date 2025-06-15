package Physics;

import Utils.Constants;
import Utils.Constants.CreatureConstants;
import Utils.Constants.Physics;
import Utils.Constants.WorldConstants;
import Utils.UnitVector2D;
import Utils.Vector2D;

import java.awt.*;

/**
 * Represents a dynamic physical entity in the simulation.
 * This object supports motion, orientation, and collisions, and is designed to be reused by an object factory.
 */
public final class Dynamic extends Position {

    /** The bounding box last stashed by calling {@link #stashBoundingBox} */
    final Rectangle prevBoundingBox = new Rectangle();

    /** A 2D Vector representing both the direction and magnitude of the velocity of this Dynamic Object. */
    public Vector2D velocity;

    /** A 2D Unit Vector representing the direction this Dynamic Object is facing. */
    public final UnitVector2D dir = new UnitVector2D(0);

    /** The velocity of how many radians Counterclockwise to turn per tick. */
    public double angularSpeed;

    /** Constructs a new {@code Dynamic} object with assigned position. */
    public Dynamic(int id, double width, double height, int x, int y, double dir, double sizeToMass) {
        super(id, width, height);
        reset(width, height, x, y, dir, sizeToMass);
    }

    /** Resets this {@code Dynamic} object for reuse with new size, image, direction, and position. */
    public void reset(double width, double height, int x, int y, double dir, double sizeToMass) {
        boundingBox.setRect(x - width / 2, y - height / 2, width, height);
        prevBoundingBox.setRect(boundingBox);
        this.dir.x = Math.cos(dir);
        this.dir.y = Math.sin(dir);
        this.x = x;
        this.y = y;
        this.sizeToMass = sizeToMass;
    }

    /** Constructs a new {@code Dynamic} object with assigned position. */
    public Dynamic(int id, Dynamic d, double sizeToMass) {
        this(id, d.boundingBox.width, d.boundingBox.height, d.x, d.y, d.dir.angle(), sizeToMass);
    }

    /** Resets this {@code Dynamic} object for reuse with new size, image, direction, and position. */
    public void reset(Dynamic d, double sizeToMass) {
        reset(d.boundingBox.width, d.boundingBox.height, d.x, d.y, d.dir.angle(), sizeToMass);
    }

    /** Stashes the current bounding box's dimension and position to the previous bounding Box Object. */
    public void stashBoundingBox() {
        prevBoundingBox.setRect(boundingBox);
    }

    /** Updates the x,y position according to the velocity vector */
    public void updatePos() {
        x += (int) Math.round(velocity.x);
        y += (int) Math.round(velocity.x);
        dir.rotate(angularSpeed);
        boundingBox.x = x - boundingBox.width / 2;
        boundingBox.y = y - boundingBox.height / 2;
    }

    /** Updates the velocity vector according to {@link Utils.Constants} */
    public void friction() {
        double dotProduct = velocity.x * dir.x + velocity.y * dir.y;
        Vector2D parallel = dir.multiplied(dotProduct);
        Vector2D perpendicular = velocity.subtracted(parallel);

        velocity = parallel.multiplied(1 - Physics.frictionParallel).added(perpendicular.multiplied(1 - Physics.frictionPerpendicular)).maxVectored(WorldConstants.Settings.maxSpeed);
        if (Math.abs(velocity.x) < 0.1) velocity.x = 0;
        if (Math.abs(velocity.y) < 0.1) velocity.y = 0;

        angularSpeed *= (1 - Physics.frictionAngular);
    }

    /** Returns the direction vector */
    public Vector2D getDirection() {
        return dir;
    }

    /** Adds {@code deltaVelocity} to the velocity of this Dynamic object. Returns a reference to itself. */
    public Dynamic addVelocity(Vector2D deltaVelocity) {
        velocity.add(deltaVelocity);
        return this;
    }

    /** Returns whether the velocity is 0 */
    public boolean isMoving() {
        return velocity.x == 0 && velocity.y == 0;
    }

    @Override
    boolean isBoundingBoxChange() {
        return prevBoundingBox.equals(boundingBox);
    }

    @Override
    public double getMass() {
        return sizeToMass * boundingBox.width * boundingBox.height;
    }

    @Override
    public double getDamage() {
        double temp = damage;
        damage = 0;
        return temp;
    }

    @Override
    public void setSize(double newSize) {
        boundingBox.setSize((int) Math.round(newSize), (int) Math.round(boundingBox.height * boundingBox.width / newSize));
    }

    public void setSizeToMass(double newSize) {
        sizeToMass = newSize;
    }

    @Override
    Vector2D collision(Dynamic movable) {
        if (movable.equals(this)) throw new RuntimeException("Can't collide with itself");
        double minSpeedX = 0, minSpeedY = 0;
        Rectangle eBoundingBox = movable.boundingBox;
        Rectangle boundingBox = this.boundingBox;
        if (Math.min(Math.abs(eBoundingBox.getMinX() - boundingBox.getMaxX()), Math.abs(eBoundingBox.getMaxX() - boundingBox.getMinX())) < Math.min(Math.abs(eBoundingBox.getMinY() - boundingBox.getMaxY()), Math.abs(eBoundingBox.getMaxY() - boundingBox.getMinY()))) {
            if (Math.abs(eBoundingBox.getMinX() - boundingBox.getMaxX()) < Math.abs(eBoundingBox.getMaxX() - boundingBox.getMinX()))
                minSpeedX = eBoundingBox.getMinX() - boundingBox.getMaxX();
            else
                minSpeedX = eBoundingBox.getMaxX() - boundingBox.getMinX();
        } else {
            if (Math.abs(eBoundingBox.getMinY() - boundingBox.getMaxY()) < Math.abs(eBoundingBox.getMaxY() - boundingBox.getMinY()))
                minSpeedY = eBoundingBox.getMinY() - boundingBox.getMaxY();
            else
                minSpeedY = eBoundingBox.getMaxY() - boundingBox.getMinY();
        }
        Vector2D[] velocities = Physics.elasticCollision(getMass(), movable.getMass(), velocity, movable.velocity);
        Vector2D speed = velocities[0].multiplied(Physics.impulseVelocityLoss).minVectored(minSpeedX, minSpeedY);
        Vector2D otherSpeed = velocities[1].multiplied(Physics.impulseVelocityLoss).minVectored(-minSpeedX, -minSpeedY);

        double mass = movable.getMass() + getMass();
        double damage = (getMass() * this.velocity.length() + movable.getMass() * movable.velocity.length()) * CreatureConstants.Movement.momentumToDamage;
        movable.damage += damage * getMass() / mass;
        this.damage += damage * movable.getMass() / mass;

        this.velocity.add(speed);
        return otherSpeed;
    }
}
