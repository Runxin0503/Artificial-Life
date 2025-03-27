package Physics;

import Entities.Creature;
import Entities.Entity;

/** A 2D world of grid cells where {@link Physics.Position} are stored and spatial partitioned
 * for more efficient collision processing. */
public interface AbstractGridList {

    /*
     * Must have
     * - A (2D?) array of Grid objects that make up the grid world.
     * - HashMap mapping Entity to Position
     * - Arraylist containing all Entities currently present so
     *   Position's ID can be used to trace to its respective Entity Object.
     * -
     *
     */

    /** Given the coordinates of a point P, searches the related grid cell for any
     * {@link Entity} and returns the one with the smallest hit-box, or null
     * if no entities are found. */
    Entity get(int x,int y);

    /**
     * Once called, iterate over every position object currently residing in this Grid world
     * and updates their position and references in {@link AbstractGrid} according to their
     * velocity vectors.
     */
    void updatePositions();

    /** Once called, iterate over all Creature objects and calls {@link Creature#getEatingHitbox}
     * to check if any Entity are inside the eating hit-box and, if so, calls creatureInteract with the
     * appropriate parameters. */
    void interact();
}
