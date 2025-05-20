package Entities;

import Entities.Creature.Creature;
import Physics.Position;

/** The dead corpse of a {@link Creature}. Passively rots away, decreasing in size and mass.<br>
 * Energy --> Energy of the original Creature if fully consumed, decaying at some rate over time<br>
 * Health --> N/A
 * */
public class Corpse extends Entity {

    public Corpse(int id) {
        super(id);
    }

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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public record ReadOnlyCorpse() implements ReadOnlyEntity{} // TODO implement
}
