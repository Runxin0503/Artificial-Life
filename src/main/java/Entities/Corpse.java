package Entities;

import Entities.Creature.Creature;
import Physics.Dynamic;
import Physics.Position;
import Utils.Constants.CorpseConstants;

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
    public boolean tick(Position pos) {
        // decay with half-life principle, then clear the queue for questionable munching
        this.energy *= CorpseConstants.corpseDecayRate;
        if (energy <= initialEnergy * CorpseConstants.corpseRottenPercentage || pos.boundingBox.width < CorpseConstants.minCorpseSize)
            return true;

        // queuedQuestionableMunching.sort((o1, o2) -> (int) (100 * (o1.getDamage() - o2.getDamage())));
        // while (energy > 0 && !queuedQuestionableMunching.isEmpty()) {
        //     Creature c = queuedQuestionableMunching.removeLast();
        //     double damageDealt = Math.min(energy, c.getDamage());
        //     if (c.addMeatMass(damageDealt / Constants.CreatureConstants.Digestion.meatMassToEnergy))
        //         setEnergy(energy - damageDealt);
        // }
        // queuedQuestionableMunching.clear();

        return false;
    }

    @Override
    public void creatureInteract(Creature c) {
        synchronized (queuedQuestionableMunching) {
            queuedQuestionableMunching.add(c);
        }
    }

    @Override
    public ReadOnlyEntity getReadOnlyCopy(Position pos) {
        if (!(pos instanceof Dynamic d)) throw new RuntimeException("Invalid position object");
        return new ReadOnlyCorpse(d.boundingBox.x, d.boundingBox.y, d.boundingBox.width, d.boundingBox.height, d.velocity.x, d.velocity.y, d.dir.angle(), energy, initialEnergy, ID);
    }

    public record ReadOnlyCorpse(int x, int y, int width, int height, double velocityX, double velocityY,
                                 double rotation, double energy, double initialEnergy,
                                 int ID) implements ReadOnlyEntity {

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

        @Override
        public int hashCode() {
            return ID;
        }
    }
}
