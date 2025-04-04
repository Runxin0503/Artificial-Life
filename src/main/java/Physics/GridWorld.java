package Physics;

import Entities.Creature;
import Entities.Entity;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

/**
 *  The main world object containing every entity existing in this world.
 * <br>A 2D world of grid cells where {@link Physics.Position} are stored and spatial partitioned
 * for more efficient collision processing. */
public class GridWorld {

    /** The 2D array of ArrayLists of Entities, representing a 2D array of "Grids".<br>
     * Used in spatial partition and searching for Entity that correspond with a Position
     * Object during creature interaction. */
    private ArrayList<Entity>[][] Grids;

    /** The map that stores Entity to Position objects. */
    private HashMap<Entity, Position> entityToPosition;

    public GridWorld() {
        // TODO implement
    }

    public static GridWorld loadWorld(String filePath) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** Given the coordinates of a point P, searches the related grid cell for any
     * {@link Entity} and returns the one with the smallest hit-box, or null
     * if no entities are found. */
    public Entity get(int x, int y) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** Completes all actions required of the world in one tick. */
    public void tick(ExecutorService executor) {
        // TODO 1. Vision (Body)
        updateVision(executor);

        // TODO 1. Neural Network (Brain)


        // TODO 2. Update Velocity (Brain)


        // TODO 2. Body Tasks (Body, Dynamic)


        // TODO 3. Update Position (Dynamic)
        updatePositions();
        interact();

        // TODO 4. Collision (Dynamic, Body? Genome?)


    }

    /**
     * Once called, iterate over every Creature object and scans their peripheral for
     * entities within their vision cone. Once complete, stashes the entities into
     */
    private void updateVision(ExecutorService executor) {
        for (Entity e : entityToPosition.keySet())
            if (e instanceof Creature c)
                executor.submit(() -> {
                    ArrayList<Line2D> visionRays = new ArrayList<>();
                    c.getVisionRays(visionRays,entityToPosition.get(e));

                    ArrayList<Entity> seenEntities = new ArrayList<>();
                    addSeenEntities(visionRays,c.getStashedSeenEntities());
                });
    }

    /**
     * Clears {@code seenEntities} before adding all entities intersecting with {@code visionRays} to it.
     */
    private void addSeenEntities(ArrayList<Line2D> visionRays, ArrayList<Entity> seenEntities) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Once called, iterate over every position object currently residing in this Grid world
     * and updates their position and references in {@code Grid} according to their
     * velocity vectors.
     */
    private void updatePositions() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** Once called, iterate over all Creature objects and calls {@link Creature#getEatingHitbox}
     * to check if any Entity are inside the eating hit-box and, if so, calls creatureInteract with the
     * appropriate parameters. */
    private void interact() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Returns a read-only copy of this grid world at the current tick.
     */
    public ReadOnlyWorld getReadOnlyCopy() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public static class ReadOnlyWorld {
        // TODO implement
    }
}
