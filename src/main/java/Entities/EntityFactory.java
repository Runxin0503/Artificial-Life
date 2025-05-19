package Entities;

import Entities.Creature.Creature;
import Physics.Dynamic;
import Physics.Fixed;
import Utils.Pair;

import java.util.LinkedList;

/** A static Factory class that re-uses Entity and Position objects to minimize pressure on Java's garbage collector. */
public class EntityFactory {

    /** Stores how many objects this class has created that's both used and unused. */
    private int objectCounter = 0;

    /** Stores references to UNUSED Creature and Dynamic Objects. */
    private final LinkedList<Pair<Creature, Dynamic>> creaturePair;

    /** Stores references to UNUSED Corpses and Dynamic Objects. */
    private final LinkedList<Pair<Corpse, Dynamic>> corpsePair;

    /** Stores references to UNUSED Bushes and Fixed Objects. */
    private final LinkedList<Pair<Bush, Fixed>> bushPair;

    /** Stores references to UNUSED Eggs and Fixed Objects. */
    private final LinkedList<Pair<Egg, Fixed>> eggPair;

    public EntityFactory() {
        creaturePair = new LinkedList<>();
        corpsePair = new LinkedList<>();
        bushPair = new LinkedList<>();
        eggPair = new LinkedList<>();
    }

    /** Returns a pair of references to UNUSED Creature and Dynamic Objects.<br>
     * Automatically creates new ones if there are no unused Objects left. */
    public Pair<Creature, Dynamic> getCreaturePair() {
        if (creaturePair.isEmpty()) {
            objectCounter++;
            return new Pair<>(new Creature(), new Dynamic());
        } else {
            return creaturePair.removeFirst();
        }
    }

    /** Returns a pair of references to UNUSED Corpse and Dynamic Objects.<br>
     * Automatically creates new ones if there are no unused Objects left. */
    public Pair<Corpse, Dynamic> getCorpsePair() {
        if (creaturePair.isEmpty()) {
            objectCounter++;
            return new Pair<>(new Corpse(), new Dynamic());
        } else {
            return corpsePair.removeFirst();
        }
    }

    /** Returns a pair of references to UNUSED Bush and Fixed Objects.<br>
     * Automatically creates new ones if there are no unused Objects left. */
    public Pair<Bush, Fixed> getBushPair() {
        if (creaturePair.isEmpty()) {
            objectCounter++;
            return new Pair<>(new Bush(), new Fixed());
        } else {
            return bushPair.removeFirst();
        }
    }

    /** Returns a pair of references to UNUSED Egg and Fixed Objects.<br>
     * Automatically creates new ones if there are no unused Objects left. */
    public Pair<Egg, Fixed> getEggPair() {
        if (creaturePair.isEmpty()) {
            objectCounter++;
            return new Pair<>(new Egg(), new Fixed());
        } else {
            return eggPair.removeFirst();
        }
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
