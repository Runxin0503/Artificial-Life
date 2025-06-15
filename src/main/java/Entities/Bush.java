package Entities;

import Entities.Creature.Creature;
import Physics.Position;
import Utils.Constants;
import Utils.Constants.BushConstants;

import java.util.ArrayList;

/**
 * An Immovable Plant Entity that produces berries passively <br>
 */
public class Bush extends Entity {

    /** The max amount of berries possible. */
    private int maxBerries;

    /** The amount of energy each berries give. TODO implement this in the future. */
    private double berryEnergy = BushConstants.energy;

    /** Stores absolute world positions of the berries themselves. */
    private int numBerries = 0;

    /** The list of Creatures (sorted by descending size) of the creatures that
     * interacted with this bush. */
    private final ArrayList<Creature> queuedBerryEating = new ArrayList<>();

    public Bush(int id, int maxBerries) {
        super(id);
        reset(maxBerries);
    }

    public void reset(int maxBerries) {
        this.maxBerries = maxBerries;

        numBerries = (int) (Math.random() * maxBerries);

        queuedBerryEating.clear();
    }

    /**
     * Clears the queue for creatureInteract berry-eating<br>
     * Gains energy dependent on size<br>
     * Attempts to grow berries if sufficient energy<br>
     * Returns true if this Entity has to be removed
     */
    public boolean tick(Position pos) {
        if (numBerries < maxBerries && Math.random() < BushConstants.berriesGrowProbability * maxBerries) {
            numBerries++;
        }

        queuedBerryEating.sort((o1, o2) -> (int) (100 * (o1.getSize() - o2.getSize())));
        while (numBerries > 0 && !queuedBerryEating.isEmpty()) {
            Creature c = queuedBerryEating.removeLast();
            // TODO add berry to Creature Stomach
//            if (c.addPlantMass(BushConstants.energy / Constants.CreatureConstants.Digestion.plantMassToEnergy))
//                numBerries--;
        }
        queuedBerryEating.clear();

        return false;
    }

    @Override
    public double getEnergyIfConsumed() {
        return berryEnergy * numBerries;
    }

    @Override
    public void creatureInteract(Creature c) {
        queuedBerryEating.add(c);
    }

    @Override
    public ReadOnlyEntity getReadOnlyCopy(Position pos) {
        return new ReadOnlyBush(
                pos.boundingBox.x, pos.boundingBox.y,
                pos.boundingBox.width, pos.boundingBox.height,
                numBerries, ID);
    }

    public record ReadOnlyBush(
            int x, int y, int width, int height,
            int numBerries, int ID
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

        public double getStoredEnergy() {
            return numBerries * BushConstants.energy;
        }

        @Override
        public int hashCode() {
            return ID;
        }
    }
}
