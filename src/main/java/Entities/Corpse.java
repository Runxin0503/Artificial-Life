package Entities;

/** The dead corpse of a {@link Creature}. Passively rots away, decreasing in size and mass.<br>
 * Energy --> Energy of the original Creature if fully consumed, decaying at some rate over time<br>
 * Health --> N/A
 * */
public class Corpse extends Entity {

    public Corpse(int id) {
        super(id);
    }

    @Override
    public int getHealth() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getEnergy() {
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
    protected void reset() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
