package Specs.Physics;

import Constants.Constants.*;
import Specs.Statics.*;

import java.awt.*;
import java.util.List;

/**
 * A Dynamic instance of {@link AbstractPosition} that adds Velocity to the mix<br>
 * Co-Dependant with {@link AbstractGridList} and {@link AbstractGrid}
 */
public interface AbstractDynamic extends AbstractPosition {

    /*
     * Must have:
     * - A prevBoundingBox
     * - A Vector2D object representing Velocity
     * - An array of Grids
     */

    /** sets the previous bounding Box to the current bounding box */
    void stashBoundingBox();

    /** updates the x,y position according to the velocity vector */
    void updatePos();

    /** updates the velocity vector according to {@link AbstractConstants} */
    double friction();

    /** Returns the absolute speed of the velocity vector (length of vector) */
    double getSpeed();

    /** Returns the velocity vector */
    Vector2D getVelocity();

    /** Returns whether the velocity is 0 */
    boolean isMoving();

    /** Returns a list of Grid Objects that contains this position*/
    List<AbstractGrid> getOccupiedGrids();

    /** Uses OccupiedGrids array to check for any overlapping Bounding Boxes, and, if so, calls collision on that {@link AbstractPosition} object */
    void checkCollisions();

    /** Uses OccupiedGrids array to check for any Bounding Boxes that overlaps with {@code rect}. If so, calls {@code creatureInteract} on that {@link AbstractPosition} object */
    void checkIntersection(Rectangle rect);
}
