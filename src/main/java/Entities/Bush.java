package Entities;

import Entities.Creature.Creature;
import Physics.Position;
import Utils.Constants.BushConstants;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * An Immovable Plant Entity that produces berries passively <br>
 */
public class Bush extends Entity {

    /** The max amount of berries possible. */
    private int maxBerries;

    /** The (effectively FINAL) fields for the bounding box of this Bush. Used to generate Berry positions. */
    private int x, y, width, height;

    /** The amount of energy each berries give */
    private double berryEnergy;

    /** Stores absolute world positions of the berries themselves. */
    private final ArrayList<Point> berries = new ArrayList<>();

    /** The list of Creatures (sorted by descending size) of the creatures that
     * interacted with this bush. */
    private final ArrayList<Creature> queuedBerryEating = new ArrayList<>();

    public Bush(int id, int x, int y, int width, int height) {
        super(id);
        reset(x, y, width, height);
    }

    public void reset(int x, int y, int width, int height) {
        this.maxBerries = width / BushConstants.initialMaxSize * BushConstants.maxBerries;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        int currentBerries = (int) (Math.random() * maxBerries);
        for (int i = 0; i < currentBerries; i++)
            growBerry();

        berries.clear();
        queuedBerryEating.clear();
    }

    private void growBerry() {
        berries.add(new Point(
                x + BushConstants.berriesWidth + (int) (Math.random() * (width - 2 * BushConstants.berriesWidth)),
                y + BushConstants.berriesHeight + (int) (Math.random() * (height - 2 * BushConstants.berriesHeight))));
    }

    /**
     * Clears the queue for creatureInteract berry-eating<br>
     * Gains energy dependent on size<br>
     * Attempts to grow berries if sufficient energy<br>
     * Returns true if this Entity has to be removed
     */
    public boolean tick() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** Returns the List of Point Object for drawing berries */
    List<Point> getBerries() {
        return berries;
    }

    /**
     * Returns Genetic Information: {@code maxBerries}<br>
     * States the max berries this bush can have.
     */
    int getMaxBerries() {
        return maxBerries;
    }

    /**
     * Returns Genetic Information: {@code berryEnergy}<br>
     * States the energy this bush's berries give
     */
    double getBerriesEnergy() {
        return berryEnergy;
    }

    @Override
    public double getEnergyIfConsumed() {
        return berryEnergy * berries.size();
    }

    @Override
    public void creatureInteract(Creature c) {
        queuedBerryEating.add(c);
    }

    @Override
    public ReadOnlyEntity getReadOnlyCopy(Position pos) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public record ReadOnlyBush() implements ReadOnlyEntity {
    } // TODO implement
}
