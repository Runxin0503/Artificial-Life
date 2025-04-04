package Physics;

import Entities.EntityFactory;
import Utils.Vector2D;

import java.awt.*;

/**
 * An interface wrapper for the {@link Rectangle} class to use for the Bounding Box of class {@link Entities}<br>
 * Stores an {@link Image} to use for visualization in {@code javaFX}.
 */
public abstract class Position extends EntityFactory.EntityFactoryObject {

    /** The int coordinate of this Position */
    int x,y;

    /** The bounding box of this Position, used to dictate if two Position objects collide or not. */
    Rectangle boundingBox;

    /** The image of this particular Position object, used for rendering in {@code JavaFX}. */
    public Image image;

    /** The ratio value used in calculating the mass for {@link #getMass()}. */
    double sizeToMass;

    /** The damage value from the most recent collision, used in {@link #getDamage()}. */
    double damage;

    protected Position(int id) {
        super(id);
    }

    /** Returns if bounding box has changed from where it was in the previous tick */
    abstract boolean isBoundingBoxChange();

    /** Returns the mass of this Position for collision calculation. */
    abstract double getMass();

    /**
     * Returns the damage to this Position object from recent collisions.<br>
     * The damage value will reset to 0 once this function is called.
     */
    public abstract double getDamage();

    /**
     * Sets the width and height of the Bounding Box to the new size according to the Dimension Ratio.<br>
     * Resizes the Image object accordingly.
     */
    public abstract void setSize(double newSize);

    /**
     * Sets the ratio of the width and height of the Bounding Box to the new ratio
     * Resizes the Image object accordingly
     *  EX: widthToHeight of 3 means width = height / 3
     */
    @Deprecated
    public abstract void setDimensionRatio(double widthToHeight);

    /**
     * Calculates the various effects of a collision based on mass, velocity, etc
     * @param movable a dynamic instance of Position with velocity and impulse
     * @return the impulse vector generated between the two {@link Position} classes
     */
    abstract Vector2D collision(Dynamic movable);
}
