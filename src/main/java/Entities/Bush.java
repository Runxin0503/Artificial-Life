package Entities;

import java.awt.*;
import java.util.List;

/**
 * An Immovable Plant Entity that produces berries passively <br>
 * Energy --> Berry Growth <br>
 * Health --> Infinite
 */
public class Bush extends Entity {

    public Bush(int id) {
        super(id);
    }

    void instantiate(Creature creature) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /*
     * Must have
     * - An Array of Point Objects that represents berries
     * - Genetic Information: (Berries Energy, Max Berries, Size)
     * - SortedList that stores a queue for creatureInteract berry-eating
     */

    /** Returns the List of Point Object for drawing berries */
    List<Point> getBerries() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Returns Genetic Information: {@code maxBerries}<br>
     * States the max berries this bush can have
     */
    int getMaxBerries() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Returns Genetic Information: {@code berryEnergy}<br>
     * States the energy this bush's berries give
     */
    int getBerriesEnergy() {
        throw new UnsupportedOperationException("Not supported yet.");
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

    /**
     * Clears the queue for creatureInteract berry-eating<br>
     * Gains energy dependent on size<br>
     * Attempts to grow berries if sufficient energy<br>
     * Returns true if this Entity has to be removed
     */
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
