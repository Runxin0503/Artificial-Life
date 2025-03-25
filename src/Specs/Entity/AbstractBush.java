package Specs.Entity;

import java.awt.Point;
import java.util.List;

/**
 * An Immovable Plant Entity that produces berries passively <br>
 * Energy --> Berry Growth <br>
 * Health --> Infinite
 */
public interface AbstractBush extends AbstractEntity {

    /*
     * Must have
     * - An Array of Point Objects that represents berries
     * - Genetic Information: (Berries Energy, Max Berries, Size)
     * - an AbstractFixed Position Object
     * - SortedList that stores a queue for creatureInteract berry-eating
     */

    /** Returns the List of Point Object for drawing berries */
    List<Point> getBerries();

    /**
     * Returns Genetic Information: maxBerries<br>
     * States the max berries this bush can have
     */
    int getMaxBerries();

    /**
     * Returns Genetic Information: berryEnergy<br>
     * States the energy this bush's berries give
     */
    int getBerriesEnergy();

    /**
     * Clears the queue for creatureInteract berry-eating<br>
     * Gains energy dependent on size<br>
     * Attempts to grow berries if sufficient energy<br>
     * Returns true if this Entity has to be removed
     */
    boolean tick();
}
