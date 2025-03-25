package Specs.Entity;

import Specs.Physics.AbstractDynamic;
import Specs.Physics.AbstractFixed;

/**
 * A static Factory class that re-uses Entity and Position objects to minimize pressure on Java's garbage collector
 */
public interface AbstractEntityFactory {

    /*
     * Must have
     * For every type of Entity and Position subclass:
     *      - A HashTable containing ALL subclass instances CURRENTLY BEING USED (instance variables)
     *      - A LinkedList QUEUE containing ALL subclass instances CURRENTLY NOT BEING USED (static variables)
     *
     */

    AbstractCreature getCreature();
    AbstractEgg getEgg();
    AbstractBush getBush();
    AbstractCorpse getCorpse();
    AbstractDynamic getDynamic();
    AbstractFixed getFixed();

    /** An interface that guarantees a reset() method for {@link AbstractEntityFactory} to use */
    @FunctionalInterface
    interface AbstractFactoryObject {
        /** Resets this Object to its default values */
        void reset();
    }
}
