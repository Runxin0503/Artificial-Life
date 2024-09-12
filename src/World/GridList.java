package World;

import Constants.Constants.*;
import Entity.Entity;
import Entity.Movable.Movable;

import java.awt.*;
import java.awt.geom.Line2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

public class GridList implements Serializable {
    private static final List<Grid> grids = new ArrayList<Grid>();
    private static final int GRID_WIDTH = WorldConstants.GridWidth;
    private static final int GRID_HEIGHT = WorldConstants.GridHeight;
    private static final int GRID_NUM_X = (int) Math.ceil(WorldConstants.xBound * 1.0 / GRID_WIDTH);
    private static final int GRID_NUM_Y = (int) Math.ceil(WorldConstants.yBound * 1.0 / GRID_HEIGHT);

    public static void reset() {
        grids.clear();
        for (int y = 0; y < WorldConstants.yBound; y += WorldConstants.GridHeight)
            for (int x = 0; x < WorldConstants.xBound; x += WorldConstants.GridWidth)
                grids.add(new Grid(x / WorldConstants.GridWidth, y / WorldConstants.GridHeight));
    }

    @SafeVarargs
    public static void sort(ExecutorService executorService, CountDownLatch cdl, ArrayList<? extends Entity>... AllEntities) {
        for (ArrayList<? extends Entity> entities : AllEntities)
            for (Entity e : entities) {
                if (e.isBoundingBoxChange()) {
                    executorService.submit(() -> {
                        try {
                            sort(e.getPrevBoundingBox(), e.getBoundingBox(), e);
                        } finally {
                            cdl.countDown();
                        }
                    });
                } else {
                    cdl.countDown();
                }
            }
    }

    private static void sort(Rectangle prevBoundingBox, Rectangle newBoundingBox, Entity e) {
        int prevMinX = Math.max(0, prevBoundingBox.x / GRID_WIDTH), prevMaxX = Math.min(GRID_NUM_X, (int) Math.ceil(prevBoundingBox.getMaxX() / GRID_WIDTH));
        int prevMinY = Math.max(0, prevBoundingBox.y / GRID_HEIGHT), prevMaxY = Math.min(GRID_NUM_Y, (int) Math.ceil(prevBoundingBox.getMaxY() / GRID_HEIGHT));
        int newMinX = Math.max(0, newBoundingBox.x / GRID_WIDTH), newMaxX = Math.min(GRID_NUM_X, (int) Math.ceil(newBoundingBox.getMaxX() / GRID_WIDTH));
        int newMinY = Math.max(0, newBoundingBox.y / GRID_HEIGHT), newMaxY = Math.min(GRID_NUM_Y, (int) Math.ceil(newBoundingBox.getMaxY() / GRID_HEIGHT));

        int xDiff = Math.min(prevMaxX, newMaxX) - Math.max(prevMinX, newMinX) - 1;
        boolean flipper;
        for (int y = prevMinY; y < prevMaxY; y++) {
            flipper = y >= newMinY && y < newMaxY;
            for (int x = prevMinX; x < prevMaxX; x++) {
                if (flipper && x >= newMinX && x < newMaxX) x += xDiff;
                else {
                    Grid temp = grids.get(x + y * GRID_NUM_X);
                    temp.remove(e);
                    if (e instanceof Movable) ((Movable) e).getOccupiedGrids().remove(temp);
                }
            }
        }

        for (int y = newMinY; y < newMaxY; y++) {
            flipper = y >= prevMinY && y < prevMaxY;
            for (int x = newMinX; x < newMaxX; x++) {
                if (flipper && x >= prevMinX && x < prevMaxX) x += xDiff;
                else {
                    if (x + y * GRID_NUM_X > grids.size()) System.out.println(x + "," + y);
                    Grid temp = grids.get(x + y * GRID_NUM_X);
                    temp.add(e);
                    e.getOccupiedGrids().add(temp);
                }
            }
        }
    }

    public static Entity get(int x, int y) {
        ArrayList<Entity> contained = grids.get(getID(x, y)).getContainedEntities();
        for (int i = contained.size() - 1; i >= 0; i--) {
            if (!contained.get(i).getBoundingBox().contains(x, y)) {
                contained.remove(i);
            }
        }
        if (contained.isEmpty()) return null;
        if (contained.size() == 1) return contained.get(0);
        int smallestArea = Integer.MAX_VALUE;
        Entity smallestEntity = null;
        for (Entity e : contained)
            if (e.getBoundingBox().width * e.getBoundingBox().height < smallestArea) smallestEntity = e;

        return smallestEntity;
    }

    @Deprecated
    public static ArrayList<ArrayList<Entity>> get(Rectangle box) {
        ArrayList<ArrayList<Entity>> contained = new ArrayList<ArrayList<Entity>>();
        int minX = (int) Math.floor(box.getMinX() / GRID_WIDTH) - 1;
        int maxX = (int) Math.ceil(box.getMaxX() / GRID_WIDTH) + 1;
        int minY = (int) Math.floor(box.getMinY() / GRID_HEIGHT) - 1;
        int maxY = (int) Math.ceil(box.getMaxY() / GRID_HEIGHT) + 1;

        if (minX < 0) minX = 0;
        if (maxX > GRID_NUM_X) maxX = GRID_NUM_X;
        if (minY < 0) minY = 0;
        if (maxY > GRID_NUM_Y) maxY = GRID_NUM_Y;

//        System.out.println(minX*GRID_WIDTH+","+maxX*GRID_WIDTH+","+minY*GRID_HEIGHT+","+maxY*GRID_HEIGHT+"\t\t\t"+minX+","+maxX+","+minY+","+maxY);
        for (int x = minX; x < maxX; x++)
            for (int y = minY; y < maxY; y++)
                contained.add(grids.get(getID(x, y)).getContainedEntities());

        return contained;
    }

    public static void get(ArrayList<Line2D> rays, ArrayList<Grid> allVisionGrids) {
        allVisionGrids.clear();

        for (Line2D ray : rays) {
            int x1 = (int) ray.getX1() / GRID_WIDTH, x2 = (int) ray.getX2() / GRID_WIDTH, y1 = (int) ray.getY1() / GRID_HEIGHT, y2 = (int) ray.getY2() / GRID_HEIGHT;
            int dx = Math.abs(x2 - x1);
            int dy = Math.abs(y2 - y1);
            int sx = (x1 < x2) ? 1 : -1;
            int sy = (y1 < y2) ? 1 : -1;
            int err = dx - dy;

            while (true) {
                if (y1 < GRID_NUM_Y && y1 >= 0 && x1 < GRID_NUM_X && x1 >= 0)
                    allVisionGrids.add(grids.get(y1 * GRID_NUM_X + x1));
                if (x1 == x2 && y1 == y2) break;
                int e2 = 2 * err;

                if (e2 > -dy) {
                    err -= dy;
                    x1 += sx;
                } else if (e2 < dx) {
                    err += dx;
                    y1 += sy;
                } else {
                    // If the error term indicates a preference for diagonal movement,
                    // choose the direction with the larger distance to the next grid line
                    if (dx > dy) {
                        x1 += sx;
                    } else {
                        y1 += sy;
                    }
                    err += (dx - dy);
                }
            }
        }
    }

    private static int getID(int x, int y) {
        return y / GRID_HEIGHT * GRID_NUM_X + x / GRID_WIDTH;
    }

    private static int getID(Entity e) {
        return getID(e.getX(), e.getY());
    }

    public static class Grid implements Serializable {
        private final ArrayList<Entity> containedEntity = new ArrayList<Entity>();
        public final int x, y;

        private Grid(int x, int y) {
            this.x = x;
            this.y = y;
        }

        private void add(Entity e) {
            containedEntity.add(e);
        }

        private void remove(Entity e) {
            containedEntity.remove(e);
        }

        public ArrayList<Entity> getContainedEntities() {
            return containedEntity;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Grid)) return false;
            return ((Grid) o).x == x && ((Grid) o).y == y;
        }

        @Override
        public String toString() {
            return "GRID (" + x + "," + y + ")";
        }
    }
}
