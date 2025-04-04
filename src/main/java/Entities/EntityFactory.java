package Entities;

import Physics.Dynamic;
import Physics.Fixed;
import Utils.Pair;

import java.util.LinkedList;

/** A static Factory class that re-uses Entity and Position objects to minimize pressure on Java's garbage collector. */
public class EntityFactory {

    private LinkedList<Pair<Creature, Dynamic>> creaturePair;
    private LinkedList<Pair<Corpse, Dynamic>> corpsePair;
    private LinkedList<Pair<Bush, Dynamic>> bushPair;
    private LinkedList<Pair<Egg, Dynamic>> eggPair;

    /*
     * Must have
     * - LinkedList containing UNUSED Entity-Position pairs (using Utils.Pair)
     */

    /** Returns a pair of references to UNUSED Creature and Dynamic Objects.<br>
     * Automatically creates new ones if there are no unused Objects left. */
    public Pair<Creature, Dynamic> getCreaturePair() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** Returns a pair of references to UNUSED Corpse and Dynamic Objects.<br>
     * Automatically creates new ones if there are no unused Objects left. */
    public Pair<Corpse, Dynamic> getCorpsePair() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** Returns a pair of references to UNUSED Bush and Fixed Objects.<br>
     * Automatically creates new ones if there are no unused Objects left. */
    public Pair<Bush, Fixed> getBushPair() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** Returns a pair of references to UNUSED Egg and Fixed Objects.<br>
     * Automatically creates new ones if there are no unused Objects left. */
    public Pair<Egg, Fixed> getEggPair() {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    /** An interface that guarantees a reset() method for {@link EntityFactory} to use. */
    public abstract static class EntityFactoryObject {
        private final int ID;

        protected EntityFactoryObject(int id) {
            ID = id;
        }

        /** Resets this Object to its default values */
        protected abstract void reset();

        /** Compares the ID value of this Object to the other.<br>
         * Used in Hashmaps to allow it to hash this Object. */
        public boolean equals(Object obj) {
            return (obj instanceof EntityFactoryObject o && o.hashCode() == this.hashCode());
        }

        /** Returns the ID value of this Object, which must be unique.<br>
         * Used in Hashmaps to allow it to hash this Object */
        public int hashCode() {
            return this.ID;
        }
    }
}
