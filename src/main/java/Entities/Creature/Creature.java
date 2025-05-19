package Entities.Creature;


import Entities.Egg;
import Entities.Entity;
import Genome.NN;
import Physics.Position;
import Utils.Vector2D;

import java.awt.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;

/** A Creature Entity that harbors a brain, stomach, and vision system and is the main
 * living organism of this world.<br>
 * Loses energy passively and from exerting force to move around.<br>
 * Can interact with any other {@link Entity} class and gain energy.<br>
 * Able to reproduce by laying {@link Egg} that incubates into more Creatures.<br>
 * Energy --> Simply Energy<br>
 * Health --> Simply Health
 */
public class Creature extends Entity {

    private double health, energy;
    private boolean isEating;

    //todo setup instance values and implement class
    // must have an EatingHitbox Rectangle bounding box
    // must have an ArrayList<Line2D> of fixed length rays that only changes during size change,
    // when getVisionRays is called, uses the fixed length rays' values for rotation calculation
    // must have ArrayList<Entity> for stashing entities that have been seen by this Creature, ie
    // entities that GridWorld determined to intersect with this ray-cast and passed into stashSeenEntities()
    // must have an output array buffer that stores the most recent output of this Creature's brain
    // must also have an output array buffer that stores the most recent BOID information for Velocity calculation
    private NN brain;

    public Creature(int id) {
        super(id);
    }

    /**
     * Once called, builds a {@link Runnable} object that runs the Creature's brain with
     * the Creature {@code c}'s current information stash.
     * <br>Builds the input array as a fixed value before returning the Runnable object, in other words,
     * even if {@code c}'s information stash changes after calling this function, the input
     * array (and hence output array) of this Creature's brain won't change.
     */
    public static Runnable runBrain(Creature c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** Allow this Creature to perform all actions in a tick,
     * assuming {@link Creature#runBrain} has been called on this Creature
     * and a brain output array has been stashed.
     * <br>If the above condition is fulfilled, {@code tick()} does tasks
     * like digesting, regenerating, metabolizing, growing stomach, etc.
     * <br>Returns true if this Creature has died. */
    @Override
    public boolean tick() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Clears and instantiates {@code rays} with accurate coordinates of the two ends of the rays
     * according to this Creature's genome, the position {@code pos} data, and the size of this Creature.
     */
    public void getVisionRays(ArrayList<Line2D> rays, Position pos) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** Returns the reference to an ArrayList of the seen entities, can only be used by {@link Physics.GridWorld}. */
    public ArrayList<Entity> getStashedSeenEntities() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** Calculates the creature's muscle acceleration vector based on the last output of the brain
     * and the last BOID information stored in this Creature's buffer. Returns a Vector2D representing
     * the combined deltaVelocities of both vectors. */
    public Vector2D getAcceleration() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** Returns a bounding box of the size and position of this Creature's mouth, or
     * {@code Null} if the Creature isn't eating at the moment. */
    public Rectangle getEatingHitbox(Position pos) {
        if (isEating) throw new UnsupportedOperationException("Not supported yet.");//TODO implement eatingHitbox
        else return null;
    }

    @Override
    public double getHealth() {
        return health;
    }

    @Override
    public double getEnergy() {
        return energy;
    }

    @Override
    public double getEnergyIfConsumed() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void creatureInteract(Creature c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** Compares the two Creatures' genome, returning a non-negative double describing the absolute distance
     * between the two genomes of the Creature. Takes into account both their Neural Network genome
     * and body genome. */
    public double compare(Creature o) {
        return o.brain.compare(brain);
    }

    @Override
    public ReadOnlyEntity getReadOnlyCopy(Position pos) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void reset() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public record ReadOnlyCreature() implements ReadOnlyEntity {
    } // TODO implement
}
