package Physics;

import Utils.Vector2D;

import java.awt.*;
import java.util.List;

public class Dynamic extends Position {

    /** The bounding box last stashed by calling {@link #stashBoundingBox} */
    private Rectangle prevBoundingBox;

    /** A 2D Vector representing both the direction and magnitude of the velocity of this Dynamic Object. */
    private Vector2D velocity;

    /** A 2D Unit Vector representing the direction this Dynamic Object is facing. */
    private Vector2D direction;

    public Dynamic(int id) {
        super(id);
    }

    /** Stashes the current bounding box's dimension and position to the previous bounding Box Object. */
    public void stashBoundingBox(){
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** Updates the x,y position according to the velocity vector */
    public void updatePos(){
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** Updates the velocity vector according to {@link Utils.Constants} */
    public void friction(){
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** Returns the absolute speed of the velocity vector (length of vector) */
    public double getSpeed(){
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** Returns a clone of the velocity vector */
    public Vector2D getVelocity(){
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** Adds {@code deltaVelocity} to the velocity of this Dynamic object. Returns a reference to itself. */
    public Dynamic addVelocity(Vector2D deltaVelocity){
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** Returns whether the velocity is 0 */
    public boolean isMoving(){
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** Uses OccupiedGrids array to check for any overlapping Bounding Boxes, and, if so, calls collision on that {@link Position} object */
    public void checkCollisions(){
        throw new UnsupportedOperationException("Not supported yet.");
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

    @Override
    public void setSize(double newSize) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    @Deprecated
    public void setDimensionRatio(double widthToHeight) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    Vector2D collision(Dynamic movable) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected void reset() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
