package Entities;

import Entities.Creature.Creature;
import Physics.Position;

import java.awt.*;
import java.util.ArrayList;

/** The dead corpse of a {@link Creature}. Passively rots away, decreasing in size and mass.<br>
 * The decay rate follows some perct of the current mass, meaning it will follow the curve 1/x, decaying
 * much faster initially than at the end.<br>
 * */
public class Corpse extends Entity {

    /** The current energy bar of this Corpse, or how much nutrient is left in this Corpse before it
     * fully decays. */
    private double energy;

    /** The initial energy of this Corpse, or how much nutrient was initially in the Creature right
     * before it died. */
    private double initialEnergy;

    /** A Queue for Creatures trying to questionably munch on this corpse in a tick, sorted
     * in decreasing {@code Creature.strength}. */
    private final ArrayList<Creature> queuedQuestionableMunching = new ArrayList<>();

    /** Constructs a new {@linkplain Corpse} object corresponding to the corpse of Creature {@code c}. */
    public Corpse(int id, Creature c) {
        super(id);
        reset(c);
    }

    /** Resets this {@linkplain Corpse} object for reuse with a new Creature. */
    public void reset(Creature c) {
        this.initialEnergy = c.getEnergyIfConsumed();
        this.energy = this.initialEnergy;
        this.queuedQuestionableMunching.clear();
    }

    @Override
    public double getEnergyIfConsumed() {
        return energy;
    }

    @Override
    public boolean tick() {
        //TODO decay with half-life principle, then clear the queue for questionable munching
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void creatureInteract(Creature c) {
        synchronized (queuedQuestionableMunching) {
            queuedQuestionableMunching.add(c);
        }
    }

    @Override
    public ReadOnlyEntity getReadOnlyCopy(Position pos) {
        // TODO implement
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public record ReadOnlyCorpse(
            int x, int y, int width, int height,
            double velocityX, double velocityY, double rotation,
            double energy, double initialEnergy
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

        public double getRottenPerct() {
            return energy / initialEnergy;
        }
    }
}
