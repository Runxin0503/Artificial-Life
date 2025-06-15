package Physics;

import Entities.Bush;
import Entities.Corpse;
import Entities.Creature.Creature;
import Entities.Creature.Egg;
import Entities.Entity;
import Entities.EntityFactory;
import Utils.Constants.WorldConstants;
import Utils.Equations;
import Utils.Pair;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

/**
 *  The main world object containing every entity existing in this world.
 * <br>A 2D world of grid cells where {@link Position} are stored and spatial partitioned
 * for more efficient collision processing. */
public class GridWorld {

    /** Keeps track of the total number of Creatures (and Eggs) in this world. */
    private int numCreatures;

    /** The 2D array of ArrayLists of Positions, representing a 2D array of "Grids".<br>
     * Used for spatial partition and in searching for a Position
     * Object during creature interaction. */
    private final ArrayList<Position>[][] Grids;

    /** The map that stores mappings from Entities to Position objects. */
    private final HashMap<Position, Pair<? extends Entity, ? extends Position>> positionToEntityPair;

    /** An Arraylist of all {@link Creature} to {@link Dynamic} object mappings present in {@code positionToEntity}.  */
    private final ArrayList<Pair<Creature, Dynamic>> creatures;

    /** The Entity Factory object used to create its factory-objects for less object-creation resource overhead. */
    private final EntityFactory entityFactory;

    /** The amount of ticks that has passed since this GridWorld's creation. */
    private final int timeElapsed;

    public GridWorld() {
        creatures = new ArrayList<>();
        positionToEntityPair = new HashMap<>();
        Grids = new ArrayList[WorldConstants.GRID_NUM_X][WorldConstants.GRID_NUM_Y];
        for (int i = 0; i < WorldConstants.GRID_NUM_X; i++)
            for (int j = 0; j < WorldConstants.GRID_NUM_Y; j++)
                Grids[i][j] = new ArrayList<>();
        entityFactory = new EntityFactory();

        // Create Bushes
        for (int i = 0; i < WorldConstants.WorldGen.numBushes; i++)
            addBush();

        timeElapsed = 0;
    }

    /** TODO write specs */
    public static GridWorld loadWorld(String filePath) {
        // TODO implement this
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** Given the coordinates of a point P, searches the related grid cell for any
     * {@link Entity} and returns the one with the smallest hit-box, or null
     * if no entities are found. */
    public Entity get(int x, int y) {
        ArrayList<Position> gridPosition = Grids[x / WorldConstants.GridWidth][y / WorldConstants.GridHeight];
        if (gridPosition.isEmpty()) return null;

        double minArea = Double.MAX_VALUE;
        Position minAreaPosition = null;
        for (Position pos : gridPosition) {
            double area = pos.boundingBox.width * pos.boundingBox.height;
            if (area < minArea) {
                minArea = area;
                minAreaPosition = pos;
            }
        }

        return positionToEntityPair.get(minAreaPosition).first();
    }

    /** Completes all actions required of the world in one tick. */
    public void tick(ExecutorService executor) {
        // Count down latch for 1 & 2
        CountDownLatch latch1 = new CountDownLatch(creatures.size() * 2), latch2 = new CountDownLatch(creatures.size() * 2), latch3 = new CountDownLatch(positionToEntityPair.size()), latch4 = new CountDownLatch(creatures.size()), latch5 = new CountDownLatch(positionToEntityPair.size()), latch6 = new CountDownLatch(positionToEntityPair.size());

        ArrayList<Pair<? extends Entity, ? extends Position>> newEntities = new ArrayList<>();

        // naturally spawn eggs
        if (numCreatures < WorldConstants.WorldGen.naturalSpawningThreshold)
            if (Math.random() < WorldConstants.WorldGen.naturalSpawningProbability)
                newEntities.add(entityFactory.getEggPair());

        // 1. Neural Network (Brain)
        ArrayList<Pair<Creature, Dynamic>> deadCreatures = new ArrayList<>((int) Math.ceil(creatures.size() * 0.01));
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
                            if (cd.first().tick(cd.second())) {
                                synchronized (deadCreatures) {
                                    deadCreatures.add(cd);
                                }
                            }
                            // TODO check if Creature wants to lay an egg with cd.first().layingEgg boolean var
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
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // 3. Update Position (Dynamic)
        for (Position p : positionToEntityPair.keySet()) {
            if (p instanceof Dynamic d && d.isMoving()) executor.submit(() -> {
                try {
                    d.stashBoundingBox();
                    d.updatePos();
                    updatePosition(d);
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
        for (Pair<? extends Entity, ? extends Position> value : positionToEntityPair.values())
            if (!(value.first() instanceof Creature)) executor.submit(() -> {
                try {
                    if (value.first().tick(value.second())) removedPositions.add(value.second());
                } finally {
                    latch5.countDown();
                }
            });
            else latch5.countDown();

        // await latch 5
        try {
            latch5.await();
            // remove all positions from removedPosition using EntityFactory
            //  and add in new ones with newEntities.

            for (Position p : removedPositions) {
                Pair<? extends Entity, ? extends Position> pair = positionToEntityPair.remove(p);

                switch (pair.first()) {
                    case Creature c -> {
                        if (!creatures.remove(pair))
                            throw new RuntimeException("Unexpectedly found no creature pair when removing dead creatures.");

                        numCreatures--;
                        newEntities.add(entityFactory.getCorpsePair((Pair<Creature, Dynamic>) pair));
                    }
                    case Corpse c -> {
                        // nothing
                    }
                    case Bush b -> {
                        // nothing
                    }
                    case Egg egg -> {
                        // determine if it should hatch or not
                        if (egg.isHatchable()) newEntities.add(entityFactory.getCreaturePair((Pair<Egg, Fixed>) pair));
                        else numCreatures--;
                    }
                    default ->
                            throw new IllegalStateException("Unexpected class Entity: " + pair.first().getClass().getName());
                }
                entityFactory.recycle(pair);

                removePosition(p);
            }

            for (Pair<Creature, Dynamic> cd : deadCreatures) {
                numCreatures--;
                creatures.remove(cd);
                entityFactory.recycle(cd);
                if (positionToEntityPair.remove(cd.second()).first() != cd.first())
                    throw new RuntimeException("positionToEntity in GridWorld doesn't match up with expected Pairing.");
                newEntities.add(entityFactory.getCorpsePair(cd));
                removePosition(cd.second());
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // 6. Collision (Dynamic, Body? Genome?)
        handleCollision(executor, latch6);

        // await latch 6
        try {
            latch6.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // spawn in all Entities in queue
        for (Pair<? extends Entity, ? extends Position> pair : newEntities) {
            addPosition(pair.second());
            positionToEntityPair.put(pair.second(), pair);
            if (pair.first() instanceof Creature) {
                creatures.add((Pair<Creature, Dynamic>) pair);
                numCreatures++;
            } else if (pair.first() instanceof Egg) numCreatures++;
        }
    }

    /** Adds the {@code pos} position object to Grid. In other words,
     * adds references to {@code pos} in every Grid cell that intersects / contains
     * {@code pos's} bounding box. */
    private void addPosition(Position pos) {
        int minX = Math.max(0, pos.boundingBox.x / WorldConstants.GridWidth), maxX = Math.min(WorldConstants.GRID_NUM_X - 1, (int) Math.ceil(pos.boundingBox.getMaxX() / WorldConstants.GridWidth));
        int minY = Math.max(0, pos.boundingBox.y / WorldConstants.GridHeight), maxY = Math.min(WorldConstants.GRID_NUM_Y - 1, (int) Math.ceil(pos.boundingBox.getMaxY() / WorldConstants.GridHeight));

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                if (isValidGrid(x, y)) Grids[x][y].add(pos);
            }
        }
    }

    /** Removes the {@code pos} position object to Grid. In other words,
     * removes all references to {@code pos} in every Grid cell that intersects / contains
     * {@code pos's} bounding box. */
    private void removePosition(Position pos) {
        int minX = Math.max(0, pos.boundingBox.x / WorldConstants.GridWidth), maxX = Math.min(WorldConstants.GRID_NUM_X, (int) Math.ceil(pos.boundingBox.getMaxX() / WorldConstants.GridWidth));
        int minY = Math.max(0, pos.boundingBox.y / WorldConstants.GridHeight), maxY = Math.min(WorldConstants.GRID_NUM_Y, (int) Math.ceil(pos.boundingBox.getMaxY() / WorldConstants.GridHeight));

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                if (isValidGrid(x, y)) Grids[x][y].remove(pos);
            }
        }
    }

    /** Updates the {@code pos} position object to Grid. In other words,
     * adds references to {@code pos} in every Grid cell that intersects / contains
     * {@code pos's} bounding box and removes all references in Grid cells that intersects
     * with {@code pos's} prev bounding box. */
    private void updatePosition(Dynamic d) {
        int prevMinX = Math.max(0, d.prevBoundingBox.x / WorldConstants.GridWidth), prevMaxX = Math.min(WorldConstants.GRID_NUM_X, (int) Math.ceil(d.prevBoundingBox.getMaxX() / WorldConstants.GridWidth));
        int prevMinY = Math.max(0, d.prevBoundingBox.y / WorldConstants.GridHeight), prevMaxY = Math.min(WorldConstants.GRID_NUM_Y, (int) Math.ceil(d.prevBoundingBox.getMaxY() / WorldConstants.GridHeight));
        int newMinX = Math.max(0, d.boundingBox.x / WorldConstants.GridWidth), newMaxX = Math.min(WorldConstants.GRID_NUM_X, (int) Math.ceil(d.boundingBox.getMaxX() / WorldConstants.GridWidth));
        int newMinY = Math.max(0, d.boundingBox.y / WorldConstants.GridHeight), newMaxY = Math.min(WorldConstants.GRID_NUM_Y, (int) Math.ceil(d.boundingBox.getMaxY() / WorldConstants.GridHeight));

        int xDiff = Math.min(prevMaxX, newMaxX) - Math.max(prevMinX, newMinX) - 1;
        boolean flipper;
        for (int y = prevMinY; y < prevMaxY; y++) {
            flipper = y >= newMinY && y < newMaxY;
            for (int x = prevMinX; x < prevMaxX; x++) {
                if (flipper && x >= newMinX && x < newMaxX) x += xDiff;
                else {
                    Grids[x][y].remove(d);
                }
            }
        }
        for (int y = newMinY; y < newMaxY; y++) {
            flipper = y >= prevMinY && y < prevMaxY;
            for (int x = newMinX; x < newMaxX; x++) {
                if (flipper && x >= prevMinX && x < prevMaxX) x += xDiff;
                else {
                    Grids[x][y].add(d);
                }
            }
        }
    }

    /**
     * Generates and adds a new {@code Bush} entity to the world, ensuring it is not placed
     * too close to any existing bushes.<br>
     * <br>
     * The method repeatedly requests a new {@linkplain Bush} and its associated {@linkplain Fixed} position
     * from {@linkplain EntityFactory} until it finds a position that is at least
     * {@link WorldConstants.WorldGen#bushRadius} away from all existing entities.<br>
     * <br>
     * Once a valid location is found, the bush's position is added to the world’s spatial structure
     * and the pair is registered in {@code positionToEntityPair}.<br>
     * <br>
     * If a bush is generated too close to another entity, it is recycled to avoid memory waste.
     */
    private void addBush() {
        Pair<Bush, Fixed> bushPair = entityFactory.getBushPair();
        boolean tooClose = true;

        while (tooClose) {
            tooClose = false;
            for (Pair<? extends Entity, ? extends Position> pair : positionToEntityPair.values()) {
                if (!(pair.first() instanceof Bush)) continue;
                if (Equations.dist(pair.second().x, pair.second().y, bushPair.second().x, bushPair.second().y) < WorldConstants.WorldGen.bushRadius) {
                    tooClose = true;
                    break;
                }
            }
            if (tooClose) {
                entityFactory.recycle(bushPair);
                bushPair = entityFactory.getBushPair();
            }
        }

        addPosition(bushPair.second());
        positionToEntityPair.put(bushPair.second(), bushPair);
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
                        Entity entity = positionToEntityPair.get(pos).first();
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

                        for (int x = minX; x <= maxX; x++)
                            for (int y = minY; y <= maxY; y++)
                                for (Position p : Grids[x][y])
                                    if (p != cd.second())
                                        positionToEntityPair.get(p).first().creatureInteract(cd.first());
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
        for (Pair<? extends Entity, ? extends Position> pair : positionToEntityPair.values())
            if (!(pair.first() instanceof Bush) && pair.second() instanceof Dynamic d) executor.submit(() -> {
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
        return 0 <= gx && gx < WorldConstants.GRID_NUM_X && 0 <= gy && gy < WorldConstants.GRID_NUM_Y;
    }

    /** Returns a read-only copy of this grid world at the current tick. */
    public ReadOnlyWorld getReadOnlyCopy() {
        return new ReadOnlyWorld(this);
    }

    /**
     * A read-only, immutable snapshot of the world state at a specific moment in time.
     * <p>
     * This class is used to safely expose the current state of the world to the view layer
     * without allowing any mutation. It contains a flattened list of all entities and a spatial
     * partitioning grid to optimize querying by bounding box.
     * </p>
     */
    public static class ReadOnlyWorld {

        /**
         * A flat array containing all read-only entities present in the world at this snapshot.
         * Intended for full iteration.
         */
        public final Entity.ReadOnlyEntity[] entities;

        /**
         * A 2D spatial partition grid storing references to entities based on their position,
         * used to efficiently query regions of the world.
         */
        private final ArrayList<Entity.ReadOnlyEntity>[][] Grids;

        /** The amount of ticks that has passed since this GridWorld's creation. */
        public final int timeElapsed;

        /**
         * Constructs a new {@code ReadOnlyWorld} snapshot from the current model.
         *
         * @param GridWorld the GridWorld this Read-Only copy is made from.
         */
        private ReadOnlyWorld(GridWorld GridWorld) {
            this.Grids = new ArrayList[GridWorld.Grids.length][GridWorld.Grids[0].length];

            HashMap<Position, Entity.ReadOnlyEntity> posToReadOnlyEntity = new HashMap<>();
            for (Pair<? extends Entity, ? extends Position> value : GridWorld.positionToEntityPair.values())
                posToReadOnlyEntity.put(value.second(), value.first().getReadOnlyCopy(value.second()));

            this.entities = posToReadOnlyEntity.values().toArray(new Entity.ReadOnlyEntity[0]);

            for (int i = 0; i < Grids.length; i++) {
                for (int j = 0; j < Grids[0].length; j++) {
                    this.Grids[i][j] = new ArrayList<>();
                    for (Position p : GridWorld.Grids[i][j])
                        this.Grids[i][j].add(posToReadOnlyEntity.get(p));
                }
            }

            this.timeElapsed = GridWorld.timeElapsed;
        }

        /**
         * Returns a collection of read-only entities within the specified rectangular region.
         * The coordinates are grid cell indices, not pixel or world space units.
         *
         * @param minX Minimum X coordinate in world (inclusive)
         * @param minY Minimum Y coordinate in world (inclusive)
         * @param maxX Maximum X coordinate in world (exclusive)
         * @param maxY Maximum Y coordinate in world (exclusive)
         * @return An array of entity lists located in the specified bounding box.
         */
        @SuppressWarnings("unchecked")
        public ArrayList<Entity.ReadOnlyEntity>[] getEntities(int minX, int minY, int maxX, int maxY) {
            ArrayList<ArrayList<Entity.ReadOnlyEntity>> result = new ArrayList<>();

            int minXGrid = minX / WorldConstants.GridWidth, minYGrid = minY / WorldConstants.GridHeight, maxXGrid = maxX / WorldConstants.GridWidth, maxYGrid = maxY / WorldConstants.GridHeight;

            for (int i = minXGrid; i < maxXGrid; i++)
                for (int j = minYGrid; j < maxYGrid; j++)
                    if (!Grids[i][j].isEmpty()) result.add(Grids[i][j]);

            return result.toArray(new ArrayList[0]);
        }

        /**
         * Returns the entity at the given pixel coordinate (x, y),
         * prioritizing entities with smaller hitboxes if multiple are found.
         *
         * @param x The x-coordinate in world space (pixels)
         * @param y The y-coordinate in world space (pixels)
         * @return The topmost entity at the given point, or null if none found
         */
        public Entity.ReadOnlyEntity getEntity(int x, int y) {
            int gridX = x / WorldConstants.GridWidth;
            int gridY = y / WorldConstants.GridHeight;

            if (gridX < 0 || gridX >= WorldConstants.GRID_NUM_X || gridY < 0 || gridY >= WorldConstants.GRID_NUM_Y)
                return null;

            Entity.ReadOnlyEntity selected = null;
            double smallestArea = Double.MAX_VALUE;

            for (Entity.ReadOnlyEntity entity : Grids[gridX][gridY]) {
                int ex = entity.x();
                int ey = entity.y();
                int w = entity.width();
                int h = entity.height();

                if (ex <= x && x <= ex + w && ey <= y && y <= ey + h) {
                    double area = w * h;
                    if (area < smallestArea) {
                        smallestArea = area;
                        selected = entity;
                    }
                }
            }

            return selected;
        }
    }
}
