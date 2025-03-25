package Specs.Entity;

/** An Immovable Creature in the fragile form of an Egg
 * Energy --> incubationTime
 * Health --> Damage
 */
public interface AbstractEgg extends AbstractEntity {

    /*
     * Must have
     * - A Creature Object that stores the Genetic Material of this Egg: (maxEnergy,maxHealth,metabolism,Size)
     * - an AbstractFixed Position Object
     */

    /**
     *
     * Returns true if this Entity has to be removed
     */
    boolean tick();
}
