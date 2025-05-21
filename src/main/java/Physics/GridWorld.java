package Physics;

import Entities.Creature.Creature;
import Entities.Entity;
import Utils.Constants.WorldConstants;
import Utils.Pair;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

/**
 *  The main world object containing every entity existing in this world.
 * <br>A 2D world of grid cells where {@link Physics.Position} are stored and spatial partitioned
 * for more efficient collision processing. */
public class GridWorld {

    /** Keeps track of the total number of Entities in this world. */
    private int numEntities;

    /** The 2D array of ArrayLists of Positions, representing a 2D array of "Grids".<br>
     * Used for spatial partition and in searching for a Position
     * Object during creature interaction. */
    private ArrayList<Position>[][] Grids;

    /** The map that stores mappings from Entities to Position objects. */
    private HashMap<Position, Entity> positionToEntity;

    /** An Arraylist of all {@link Creature} to {@link Dynamic} object mappings present in {@code positionToEntity}.  */
    private ArrayList<Pair<Creature, Dynamic>> creatures;

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
        // Count down latch for 1 & 2
        CountDownLatch latch1 = new CountDownLatch(creatures.size() * 2),
                latch2 = new CountDownLatch(creatures.size() * 2),
                latch3 = new CountDownLatch(positionToEntity.size()),
                latch4 = new CountDownLatch(creatures.size()),
                latch5 = new CountDownLatch(positionToEntity.size()),
                latch6 = new CountDownLatch(positionToEntity.size());

        // 1. Neural Network (Brain)
        ArrayList<Pair<Creature, Dynamic>> deadCreatures = new ArrayList<>((int) Math.ceil(creatures.size() * 0.1));
        for (Pair<Creature, Dynamic> cd : creatures)
            executor.submit(() -> {
                try {
                    // 1. run brain
                    Creature.runBrain(cd.first());
                    // 2. Update velocity and Body tasks (both run parallel after runBrain())
                    executor.submit(() -> {
                        try {
                            cd.second().addVelocity(cd.first().getAcceleration()).friction();
                        } finally {
                            latch2.countDown();
                        }
                    });
                    executor.submit(() -> {
                        try {
                            if (cd.first().tick())
                                synchronized (deadCreatures) {
                                    deadCreatures.add(cd);
                                }
                        } finally {
                            latch2.countDown();
                        }
                    });
                } finally {
                    latch1.countDown();
                }
            });

        // 1. Vision (Body)
        updateVision(executor, latch1);

        // await latch 1 & 2
        try {
            latch1.await();
            latch2.await();
            // TODO remove Creatures via EntityFactory object
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // 3. Update Position (Dynamic)
        for (Position p : positionToEntity.keySet()) {
            if (p instanceof Dynamic d && d.isMoving())
                executor.submit(() -> {
                    try {
                        d.stashBoundingBox();
                        d.updatePos();
                        // TODO compare previous bounding box Grids to current bounding box Grids
                    } finally {
                        latch3.countDown();
                    }
                });
            else latch3.countDown();
        }

        // await latch 3
        try {
            latch3.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // 4. CreatureInteract
        interact(executor, latch4);

        // await latch 4
        try {
            latch4.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // 5. Tick all entities EXCEPT creature, remove
        ArrayList<Position> removedPositions = new ArrayList<>();
        for (Map.Entry<Position, Entity> entry : positionToEntity.entrySet())
            if (!(entry.getValue() instanceof Creature))
                executor.submit(() -> {
                    try {
                        if (entry.getValue().tick())
                            removedPositions.add(entry.getKey());
                    } finally {
                        latch5.countDown();
                    }
                });
            else latch5.countDown();

        // await latch 5
        try {
            latch5.await();
            // TODO remove all positions from removedPosition using EntityFactory
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // 6. Collision (Dynamic, Body? Genome?)
        handleCollision(executor, latch6);

        // await latch 5
        try {
            latch6.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Once called, iterate over every Creature object and scans their peripheral for
     * entities within their vision cone. Once complete, stashes the entities into their
     * {@link Creature#getStashedSeenEntities}.
     */
    private void updateVision(ExecutorService executor, CountDownLatch latch) {
        for (Pair<Creature, Dynamic> cd : creatures)
            executor.submit(() -> {
                try {
                    ArrayList<Line2D> visionRays = new ArrayList<>();
                    cd.first().getVisionRays(visionRays, cd.second());

                    addSeenEntities(visionRays, cd.first().getStashedSeenEntities(), cd.second());
                } finally {
                    latch.countDown();
                }
            });
    }

    /** Adds all entities intersecting with {@code visionRays} to it */
    private void addSeenEntities(ArrayList<Line2D> visionRays, ArrayList<Entity> seenEntities, Position ignore) {
        for (Line2D ray : visionRays) {
            Rectangle2D rayBounds = ray.getBounds2D();

            // Get min/max grid cells covered by the ray's bounding box
            int minGridX = (int) (rayBounds.getMinX() / WorldConstants.GridWidth);
            int maxGridX = (int) (rayBounds.getMaxX() / WorldConstants.GridWidth);
            int minGridY = (int) (rayBounds.getMinY() / WorldConstants.GridHeight);
            int maxGridY = (int) (rayBounds.getMaxY() / WorldConstants.GridHeight);

            for (int gx = minGridX; gx <= maxGridX; gx++) {
                for (int gy = minGridY; gy <= maxGridY; gy++) {
                    if (!isValidGrid(gx, gy)) continue; // Out-of-bounds safety

                    ArrayList<Position> cell = Grids[gx][gy];
                    for (Position pos : cell) {
                        Entity entity = positionToEntity.get(pos);
                        if (seenEntities.contains(entity)) continue;

                        if (ray.intersects(pos.boundingBox)) {
                            seenEntities.add(entity);
                        }
                    }
                }
            }
        }
    }

    /** Once called, iterate over all Creature objects and calls {@link Creature#getEatingHitbox}
     * to check if any Entity are inside the eating hit-box and, if so, calls creatureInteract with the
     * appropriate parameters. */
    private void interact(ExecutorService executor, CountDownLatch latch3) {
        for (Pair<Creature, Dynamic> cd : creatures) {
            Rectangle eatingHitbox = cd.first().getEatingHitbox(cd.second());
            if (eatingHitbox == null) latch3.countDown();
            else {
                executor.submit(() -> {
                    try {
                        int minX = (int) Math.floor(eatingHitbox.getMinX() / WorldConstants.GridWidth) - 1;
                        int maxX = (int) Math.ceil(eatingHitbox.getMaxX() / WorldConstants.GridWidth) + 1;
                        int minY = (int) Math.floor(eatingHitbox.getMinY() / WorldConstants.GridHeight) - 1;
                        int maxY = (int) Math.ceil(eatingHitbox.getMaxY() / WorldConstants.GridHeight) + 1;

                        if (minX < 0) minX = 0;
                        if (maxX > WorldConstants.GRID_NUM_X) maxX = WorldConstants.GRID_NUM_X;
                        if (minY < 0) minY = 0;
                        if (maxY > WorldConstants.GRID_NUM_Y) maxY = WorldConstants.GRID_NUM_Y;

                        for (int x = minX; x < maxX; x++)
                            for (int y = minY; y < maxY; y++)
                                for (Position p : Grids[x][y])
                                    if (p != cd.second())
                                        positionToEntity.get(p).creatureInteract(cd.first());
                    } finally {
                        latch3.countDown();
                    }
                });
            }
        }
    }

    /** Once called, iterate over all Dynamic objects and uses spatial partitioning to check if
     * any Dynamic Objects are overlapping with any other Position objects. If so, calls {@link Position#collision}
     * on both Position objects. */
    private void handleCollision(ExecutorService executor, CountDownLatch latch) {
        for (Position p : positionToEntity.keySet())
            if (p instanceof Dynamic d)
                executor.submit(() -> {
                    try {
                        int startGridX = d.boundingBox.x / WorldConstants.GridWidth;
                        int endGridX = (d.boundingBox.x + d.boundingBox.width) / WorldConstants.GridWidth;
                        int startGridY = d.boundingBox.y / WorldConstants.GridHeight;
                        int endGridY = (d.boundingBox.y + d.boundingBox.height) / WorldConstants.GridHeight;

                        for (int gx = startGridX; gx <= endGridX; gx++) {
                            for (int gy = startGridY; gy <= endGridY; gy++) {
                                if (!isValidGrid(gx, gy)) continue;

                                for (Position o : Grids[gx][gy]) {
                                    if (o == d) continue;
                                    // TODO test if Rectangle.intersects works as intended, chatgpt says otherwise
                                    if (d.boundingBox.intersects(o.boundingBox)) {
                                        o.collision(d);
                                    }
                                }
                            }
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            else latch.countDown();
    }

    /** Returns true if {@code gx} and {@code gy} is valid coordinates within the 2D array {@linkplain #Grids}. */
    private boolean isValidGrid(int gx, int gy) {
        return gx < 0 || WorldConstants.GRID_NUM_X <= gx ||
                gy < 0 || WorldConstants.GRID_NUM_Y <= gy;
    }

    /** Returns a read-only copy of this grid world at the current tick. */
    public ReadOnlyWorld getReadOnlyCopy() {
        return new ReadOnlyWorld(positionToEntity);
    }

    public static class ReadOnlyWorld {
        public final Entity.ReadOnlyEntity[] readOnlyData;

        public ReadOnlyWorld(HashMap<Position, Entity> positionToEntity) {
            readOnlyData = new Entity.ReadOnlyEntity[positionToEntity.size()];

            int count = 0;
            for (Map.Entry<Position, Entity> entry : positionToEntity.entrySet())
                readOnlyData[count++] = entry.getValue().getReadOnlyCopy(entry.getKey());
        }
    }
}
