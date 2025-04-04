package Entities;


import Genome.NN;
import Physics.Position;
import javafx.scene.shape.Rectangle;

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

    //todo setup instance values and implement class
    // must have an EatingHitbox Rectangle bounding box
    // must have an ArrayList<Line2D> of fixed length rays that only changes during size change,
    // when getVisionRays is called, uses the fixed length rays' values for rotation calculation
    // must have ArrayList<Entity> for stashing entities that have been seen by this Creature, ie
    // entities that GridWorld determined to intersect with this ray-cast and passed into stashSeenEntities()
    private NN brain;


    public Creature(int id) {
        super(id);
    }



    /**
     * Clears and instantiates {@code rays} with accurate coordinates of the two ends of the rays
     * according to this Creature's genome, the position {@code pos} data, and the size of this Creature.
     */
    public void getVisionRays(ArrayList<Line2D> rays, Position pos) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** Returns the reference to an ArrayList of the seen entities, can only be used by {@link Physics.GridWorld}. */
    public ArrayList<Entity> getStashedSeenEntities(){
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** Returns a bounding box of the size and position of this Creature's mouth. */
    public Rectangle getEatingHitbox(Position pos) {
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

    @Override
    public boolean tick() {
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
    protected void reset() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
