package Entities;

import Entities.Creature.Creature;
import Physics.Dynamic;
import Physics.Fixed;
import Utils.Constants;
import Utils.Pair;
import Utils.UnitVector2D;

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

    /** Returns a pair of references to UNUSED Creature and Dynamic Objects, placed
     * somewhere randomly in the world.<br>
     * Automatically creates new ones if there are no unused Objects left. */
    public Pair<Creature, Dynamic> getCreaturePairRandom() {
        double randAngle = Math.random() * 2 * Math.PI;
        if (creaturePair.isEmpty()) {
            Pair<Creature, Dynamic> cd = new Pair<>(
                    new Creature(objectCounter, Constants.NeuralNet.EvolutionConstants),
                    new Dynamic(objectCounter, 1,
                            Constants.ImageConstants.birdRotations[(int) Math.round(Math.toDegrees(randAngle))],
                            new UnitVector2D(randAngle)));
            objectCounter++;
            return cd;
        } else {
            Pair<Creature, Dynamic> cd = creaturePair.removeFirst();
            cd.first().reset(Constants.NeuralNet.EvolutionConstants);
            cd.second().reset(1,
                    Constants.ImageConstants.birdRotations[(int) Math.round(Math.toDegrees(randAngle))],
                    new UnitVector2D(randAngle));
            return cd;
        }
    }

    /** Returns a pair of references to UNUSED Corpse and Dynamic Objects.<br>
     * Automatically creates new ones if there are no unused Objects left. */
    public Pair<Corpse, Dynamic> getCorpsePair() {
        if (creaturePair.isEmpty()) {
            objectCounter++;
            throw new UnsupportedOperationException("Not supported yet.");
//            return new Pair<>(new Corpse(), new Dynamic());
        } else {
            return corpsePair.removeFirst();
        }
    }

    /** Returns a pair of references to UNUSED Bush and Fixed Objects.<br>
     * Automatically creates new ones if there are no unused Objects left. */
    public Pair<Bush, Fixed> getBushPair() {
        if (creaturePair.isEmpty()) {
            objectCounter++;
            throw new UnsupportedOperationException("Not supported yet.");
//            return new Pair<>(new Bush(), new Fixed());
        } else {
            return bushPair.removeFirst();
        }
    }

    /** Returns a pair of references to UNUSED Egg and Fixed Objects.<br>
     * Automatically creates new ones if there are no unused Objects left. */
    public Pair<Egg, Fixed> getEggPair() {
        if (creaturePair.isEmpty()) {
            objectCounter++;
            throw new UnsupportedOperationException("Not supported yet.");
//            return new Pair<>(new Egg(), new Fixed());
        } else {
            return eggPair.removeFirst();
        }
    }

    /** An interface that must have at least one reset() method that takes in some for {@link EntityFactory} to use. */
    public abstract static class EntityFactoryObject {
        private final int ID;

        protected EntityFactoryObject(int id) {
            ID = id;
        }

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
