package Entities.Creature;

import Entities.Entity;
import Physics.Position;

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
        return creature.getEnergyIfConsumed() * timeCount / incubationTime;
    }

    /** Returns true if this Entity has to be removed */
    @Override
    public boolean tick(Position pos) {
        if (isEaten) return true;
        return ++timeCount == incubationTime;
    }

    @Override
    public void creatureInteract(Creature c) {
        //if it hasn't been eaten already and the creature has space for the egg's meat-mass : delete itself
//        if (!isEaten && !c.addMeatMass(getEnergyIfConsumed() / Constants.CreatureConstants.Digestion.meatMassToEnergy))
//            isEaten = true;
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
            int ID
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

        @Override
        public int hashCode() {
            return ID;
        }
    }
}
