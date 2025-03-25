package Specs.Physics;

import Constants.Constants.*;
import Specs.Reloadable;
import Specs.Entity.*;

import java.awt.*;
import java.io.Serializable;

/**
 * An interface class that is a wrapper for the {@link java.awt.Rectangle} class to use for the Bounding Box of class {@link AbstractEntity}<br>
 * Stores an {@link java.awt.Image} Class used for visualization in {@code Visuals} package
 */
public interface AbstractPosition extends Serializable, Reloadable, AbstractEntityFactory.AbstractFactoryObject {

    /*
     * Must have:
     * - An int position of x and y
     * - A Rectangle object for bounding box
     * - An Image object
     * - A final sizeToMass value initialized in constructor, used in getMass()
     * - A damage value for Entity class to read
     */

    /** Returns the x position */
    int getX();

    /** Returns the y position */
    int getY();

    /** Returns if bounding box has changed from where it was in the previous tick */
    boolean isBoundingBoxChange();

    /** Returns the width of the Bounding Box */
    int getWidth();

    /** Returns the height of the Bounding Box */
    int getHeight();

    /** Returns the lowest x value of the Bounding Box */
    int getX1();

    /** Returns the lowest y value of the Bounding Box */
    int getY1();

    /** Returns the highest x value of the Bounding Box */
    int getX2();

    /** Returns the highest y value of the Bounding Box */
    int getY2();

    /** Returns the image resized to fit the bounding box */
    Image getImage();

    /** Returns the mass of this Position for collision calculation */
    double getMass();

    /**
     * Returns the damage of this Position object from recent collisions
     * The damage value will reset to 0 once this function is called
     */
    double getDamage();

    /**
     * Sets the values of x,y to newX,newY, moves the Bounding Box as well.
     * Clamps both values within world bounds
     */
    void setCoord(int newX,int newY);

    /**
     * Sets the width and height of the Bounding Box to the new size according to the Dimension Ratio
     * Resizes the Image object accordingly
     */
    void setSize(double newSize);

    /**
     * Sets the ratio of the width and height of the Bounding Box to the new ratio
     * Resizes the Image object accordingly
     *  EX: widthToHeight of 3 means width = height / 3
     */
    void setDimensionRatio(double widthToHeight);

    /**
     * Calculates the various effects of a collision based on mass, velocity, etc
     * @param movable a dynamic instance of Position with velocity and impulse
     * @return the impulse vector generated between the two {@link AbstractPosition} classes
     */
    Vector2D collision(AbstractDynamic movable);
}
