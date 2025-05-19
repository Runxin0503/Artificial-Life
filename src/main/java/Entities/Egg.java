package Entities;

import Entities.Creature.Creature;
import Physics.Position;

/**
 * An Immovable Creature in the fragile form of an Egg<br>
 * Energy --> incubationTime<br>
 * Health --> Decreases with damage
 */
public class Egg extends Entity {

    private Creature creature;

    public Egg(int id) {
        super(id);
    }

    /*
     * Must have
     * - A Creature Object that stores the Genetic Material of this Egg: (maxEnergy,maxHealth,metabolism,Size)
     */

    @Override
    public double getHealth() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double getEnergy() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double getEnergyIfConsumed() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** Returns true if this Entity has to be removed */
    public boolean tick() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void creatureInteract(Creature c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ReadOnlyEntity getReadOnlyCopy(Position pos) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void reset() {

    }

    public record ReadOnlyEgg() implements ReadOnlyEntity{} // TODO implement
}
