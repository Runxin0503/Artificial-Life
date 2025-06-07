package Entities;

import Entities.Creature.Creature;
import Entities.Creature.Egg;
import Physics.Dynamic;
import Physics.Fixed;
import Utils.Constants.*;
import Utils.Pair;

import java.util.ArrayList;

/** A static Factory class that re-uses Entity and Position objects to minimize pressure on Java's garbage collector. */
public class EntityFactory {

    /** Stores how many objects this class has created that's both used and unused. */
    private int objectCounter = 0;

    /** Stores references to UNUSED Creature and Dynamic Objects. */
    private final ArrayList<Pair<Creature, Dynamic>> creaturePair;

    /** Stores references to Creature and Dynamic Objects currently being incubated in an egg. */
    private final ArrayList<Pair<Creature, Dynamic>> incubatedCreaturePair;

    /** Stores references to UNUSED Corpses and Dynamic Objects. */
    private final ArrayList<Pair<Corpse, Dynamic>> corpsePair;

    /** Stores references to UNUSED Bushes and Fixed Objects. */
    private final ArrayList<Pair<Bush, Fixed>> bushPair;

    /** Stores references to UNUSED Eggs and Fixed Objects. */
    private final ArrayList<Pair<Egg, Fixed>> eggPair;

    public EntityFactory() {
        creaturePair = new ArrayList<>();
        incubatedCreaturePair = new ArrayList<>();
        corpsePair = new ArrayList<>();
        bushPair = new ArrayList<>();
        eggPair = new ArrayList<>();
    }

    /** Returns a pair of references to UNUSED Creature and Dynamic Objects, placed
     * somewhere RANDOMLY in the world. */
    public Pair<Creature, Dynamic> getCreaturePair(Pair<Egg, Fixed> ef) {
        Creature c = ef.first().hatch();
        Pair<Creature, Dynamic> cd = null;
        for (int i = 0; i < incubatedCreaturePair.size(); i++)
            if (incubatedCreaturePair.get(i).first() == c) {
                cd = incubatedCreaturePair.remove(i);
                break;
            }
        if (cd == null) throw new RuntimeException("Unexpectedly found no Creature pair in incubatedCreaturePair.");

        return cd;
    }

    /** Returns a pair of references to UNUSED Corpse and Dynamic Objects.<br>
     * Automatically creates new ones if there are no unused Objects left. */
    public Pair<Corpse, Dynamic> getCorpsePair(Pair<Creature, Dynamic> cd) {
        if (corpsePair.isEmpty()) {
            Corpse c = new Corpse(objectCounter, cd.first());
            Dynamic d = new Dynamic(objectCounter, cd.second(), CorpseConstants.sizeMovementConstant);
            objectCounter++;
            return new Pair<>(c, d);
        } else {
            Pair<Corpse, Dynamic> cod = corpsePair.removeFirst();
            cod.first().reset(cd.first());
            cod.second().reset(cd.second(), CorpseConstants.sizeMovementConstant);
            return cod;
        }
    }

    /** Returns a pair of references to UNUSED Bush and Fixed Objects.<br>
     * Automatically creates new ones if there are no unused Objects left. */
    public Pair<Bush, Fixed> getBushPair() {
        int width = (int) (Math.random() * (BushConstants.initialMaxSize - BushConstants.initialMinSize)) + BushConstants.initialMinSize;
        int height = (int) (width * BushConstants.widthToHeight);
        int x = (int) (Math.random() * (WorldConstants.xBound - width));
        int y = (int) (Math.random() * (WorldConstants.yBound - height));

        if (bushPair.isEmpty()) {
            Bush b = new Bush(objectCounter, x, y, width, height);
            Fixed f = new Fixed(objectCounter,
                    width, height, x, y, 0);
            objectCounter++;
            return new Pair<>(b, f);
        } else {
            Pair<Bush, Fixed> bf = bushPair.removeFirst();
            bf.first().reset(x, y, width, height);
            bf.second().reset(x, y, width, height, 0);
            return bf;
        }
    }

    /** Returns a pair of references to UNUSED Egg and Fixed Objects, placed
     * somewhere RANDOMLY in the world.<br>
     * Automatically creates new ones if there are no unused Objects left. */
    public Pair<Egg, Fixed> getEggPair() {
        if (eggPair.isEmpty()) {
            Pair<Creature, Dynamic> cd = getIncubatedCreature();
            Egg e = new Egg(objectCounter, cd.first());

            Fixed f = new Fixed(objectCounter,
                    cd.second());
            objectCounter++;
            return new Pair<>(e, f);
        } else {
            Pair<Creature, Dynamic> cd = getIncubatedCreature();

            Pair<Egg, Fixed> ef = eggPair.removeFirst();
            ef.first().reset(cd.first());
            ef.second().reset(cd.second());
            return ef;
        }
    }

    /** Returns a pair of references to UNUSED Creature and Dynamic Objects, placed
     * somewhere randomly in the world.<br>
     * Automatically creates new ones if there are no unused Objects left. */
    public Pair<Egg, Fixed> getEggPair(Pair<Creature, Dynamic> cd1, Pair<Creature, Dynamic> cd2) {
        if (eggPair.isEmpty()) {
            Pair<Creature, Dynamic> cd = getIncubatedCreature(cd1, cd2);
            Egg e = new Egg(objectCounter, cd.first());

            Fixed f = new Fixed(objectCounter,
                    cd.second());
            objectCounter++;
            return new Pair<>(e, f);
        } else {
            Pair<Creature, Dynamic> cd = getIncubatedCreature(cd1, cd2);

            Pair<Egg, Fixed> ef = eggPair.removeFirst();
            ef.first().reset(cd.first());
            ef.second().reset(cd.second());
            return ef;
        }
    }

    /** Returns a pair of references to UNUSED Creature and Dynamic Objects, placed
     * somewhere RANDOMLY in the world.<br>
     * Automatically creates new ones if there are no unused Objects left. */
    private Pair<Creature, Dynamic> getIncubatedCreature() {
        double randAngle = Math.random() * 2 * Math.PI;
        int x = (int) (Math.random() * WorldConstants.xBound);
        int y = (int) (Math.random() * WorldConstants.yBound);

        if (creaturePair.isEmpty()) {
            Creature c = new Creature(objectCounter, NeuralNet.EvolutionConstants);
            Egg e = new Egg(objectCounter, c);

            Dynamic d = new Dynamic(objectCounter,
                    c.getSize(), c.getSize(),
                    x, y, randAngle, CreatureConstants.Movement.sizeMovementConstant);
            objectCounter++;
            Pair<Creature, Dynamic> cd = new Pair<>(c, d);
            incubatedCreaturePair.add(cd);
            return cd;
        } else {
            Pair<Creature, Dynamic> cd = creaturePair.removeFirst();
            cd.first().reset(NeuralNet.EvolutionConstants);
            cd.second().reset(cd.first().getSize(), cd.first().getSize(),
                    x, y, randAngle, CreatureConstants.Movement.sizeMovementConstant);
            incubatedCreaturePair.add(cd);
            return cd;
        }
    }

    /** Returns a pair of references to UNUSED Creature and Dynamic Objects, placed
     * somewhere randomly in the world.<br>
     * Automatically creates new ones if there are no unused Objects left. */
    private Pair<Creature, Dynamic> getIncubatedCreature
    (Pair<Creature, Dynamic> cd1, Pair<Creature, Dynamic> cd2) {
        double randAngle = Math.random() * 2 * Math.PI;

        if (creaturePair.isEmpty()) {
            Creature c = new Creature(objectCounter, cd1.first(), cd2.first());
            Dynamic d = new Dynamic(objectCounter,
                    c.getSize(), c.getSize(),
                    cd1.second().x, cd1.second().y, randAngle, CreatureConstants.Movement.sizeMovementConstant);
            objectCounter++;
            Pair<Creature, Dynamic> cd = new Pair<>(c, d);
            incubatedCreaturePair.add(cd);
            return cd;
        } else {
            Pair<Creature, Dynamic> cd = creaturePair.removeFirst();
            cd.first().reset(cd1.first(), cd2.first());
            cd.second().reset(cd.first().getSize(), cd.first().getSize(),
                    cd1.second().x, cd1.second().y, randAngle, CreatureConstants.Movement.sizeMovementConstant);
            return cd;
        }
    }

    /**
     * Recycles a previously used entity pair by returning it to the appropriate object pool.
     *
     * @param pair the Pair of an entity and its physics object to recycle.
     * @throws RuntimeException if the Pair type is not recognized.
     */
    public void recycle(Pair<?, ?> pair) {
        if (pair.first() instanceof Creature && pair.second() instanceof Dynamic)
            creaturePair.add((Pair<Creature, Dynamic>) (pair));
        else if (pair.first() instanceof Corpse && pair.second() instanceof Dynamic)
            corpsePair.add((Pair<Corpse, Dynamic>) (pair));
        else if (pair.first() instanceof Bush && pair.second() instanceof Fixed)
            bushPair.add((Pair<Bush, Fixed>) (pair));
        else if (pair.first() instanceof Egg && pair.second() instanceof Fixed)
            eggPair.add((Pair<Egg, Fixed>) (pair));
        else
            throw new RuntimeException("Unknown Pair type: " +
                    pair.first().getClass().getName() + ", " + pair.second().getClass().getName());
    }

    /** An interface that must have at least one reset() method that takes in some for {@link EntityFactory} to use. */
    public abstract static class EntityFactoryObject {
        protected final int ID;

        protected EntityFactoryObject(int id) {
            ID = id;
        }

        /** Compares the ID value of this Object to the other.<br>
         * Used in Hashmaps to allow it to hash this Object. */
        @Override
        public boolean equals(Object obj) {
            return (obj instanceof EntityFactoryObject o && o.hashCode() == this.hashCode());
        }

        /** Returns the ID value of this Object, which must be unique.<br>
         * Used in Hashmaps to allow it to hash this Object */
        @Override
        public int hashCode() {
            return this.ID;
        }
    }
}
