package Entities.Creature;

import Entities.Entity;
import Physics.Position;

import java.awt.*;

/**
 * An Immovable Creature entity in the fragile form of an Egg.
 */
public class Egg extends Entity {

    /** The Creature this egg is incubating. */
    private Creature creature;

    /** The time (amount of ticks passed) since this Egg is created.
     * <br>Egg hatches when {@code timeCount == incubationTime}. */
    private int timeCount;

    /** The total time (ticks) it will take for this Egg to hatch.
     * <br>Egg hatches when {@code timeCount == incubationTime}. */
    private int incubationTime;
    private boolean isEaten;

    public Egg(int id, Creature c) {
        super(id);
        reset(c);
    }

    public void reset(Creature c) {
        creature = c;
        timeCount = 0;
        incubationTime = c.genome.incubationTime;
        isEaten = false;
    }

    public Creature hatch() {
        if (isHatchable())
            return creature;

        throw new RuntimeException("Called Egg.hatch() when egg is destroyed / not ready to hatch.");
    }

    public boolean isHatchable() {
        return timeCount == incubationTime && !isEaten;
    }

    @Override
    public double getEnergyIfConsumed() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** Returns true if this Entity has to be removed */
    @Override
    public boolean tick() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void creatureInteract(Creature c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ReadOnlyEntity getReadOnlyCopy(Position pos) {
        return new ReadOnlyEgg(
                pos.boundingBox.x, pos.boundingBox.y, pos.boundingBox.width, pos.boundingBox.height,
                incubationTime, creature.health, ID);
    }

    public record ReadOnlyEgg(
            int x, int y, int width, int height,
            int incubationTime, double health,
            int id
            ) implements ReadOnlyEntity {

        public int getSize() {
            return width;
        }

        public int getX() {
            return x + width / 2;
        }

        public int getY() {
            return y + height / 2;
        }

        public int getIncubationTime() {
            return incubationTime;
        }

        public double getHealth() {
            return health;
        }

        @Override
        public int hashCode() {
            return id;
        }
    }
}
