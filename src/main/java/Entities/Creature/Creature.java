package Entities.Creature;


import Entities.Entity;
import Evolution.Constants;
import Genome.NN;
import Physics.Dynamic;
import Physics.Position;
import Utils.Constants.CreatureConstants.Combat;
import Utils.Constants.CreatureConstants.Energy;
import Utils.Constants.WorldConstants;
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

    //todo setup instance values and implement class
    // must have an EatingHitbox Rectangle bounding box
    // must have an ArrayList<Line2D> of fixed length rays that only changes during size change,
    // when getVisionRays is called, uses the fixed length rays' values for rotation calculation
    // must have ArrayList<Entity> for stashing entities that have been seen by this Creature, ie
    // entities that GridWorld determined to intersect with this ray-cast and passed into stashSeenEntities()
    // must have an output array buffer that stores the most recent output of this Creature's brain
    // must also have an output array buffer that stores the most recent BOID information for Velocity calculation

    /** Health and Energy of this Creature. */
    double health, energy;

    /** The maximum health and energy of this Creature. */
    private double maxHealth, maxEnergy;

    /** Stores how much energy this Creature consumes per {@linkplain #tick}. */
    private double metabolism;

    /** The size of this Creature, or the width of its hitbox in {@linkplain Dynamic}. */
    private double size;

    /** The age (maturity) of this Creature, controls various body development of this Creature such
     * as: Muscle, Strength, Armour, Size, etc. */
    private int age;

    /** Whether this Creature has its mouth open or not. That is, whether an interaction hitbox exists
     * for this Creature in the world. */
    private boolean isEating;

    /** Vision Ray Vectors centered at the creature's origin.
     * When {@linkplain #getVisionRays} is called, the function simply shifts the vectors to their
     * world-position. */
    private final ArrayList<Vector2D> relativeRays = new ArrayList<>();

    /** Entity Objects that have recently intersected with this Creature's vision rays.
     * Modified and maintained with {@link #getStashedSeenEntities}, queried by {@linkplain #runBrain}. */
    private final ArrayList<Entity> stashedSeenEntities = new ArrayList<>();

    /** The Genome component of this Creature, stores all gene data when reproduction occurs. */
    final Genome genome = new Genome();

    /** The stomach of this Creature */
    private final Stomach stomach = new Stomach(0);

    /** The Brain component of this Creature, a Neural Network that consumes more energy the bigger it is. */
    private NN brain;

    /** Stores the latest output of the brain. */
    private double[] brainOutput;

    public Creature(int id, Constants Constants) {
        super(id);
        reset(Constants);
    }

    public Creature(int id, Creature parentOne, Creature parentTwo) {
        super(id);
        reset(parentOne, parentTwo);
    }

    public void reset(Constants Constants) {
        this.brain = NN.getDefaultNeuralNet(Constants);//Math.random()<0.6?Genome.defaultGenome():
        this.genome.reset();

        this.size = genome.minSize;
        this.maxHealth = Combat.sizeToMaxHealth(size);
        this.maxEnergy = Energy.sizeToMaxEnergyFormula(size);
        this.metabolism = Energy.energyCostFormula(maxHealth, size, genome.strength, brain.getComplexity());
        this.age = 0;
        this.energy = maxEnergy;
        this.health = maxHealth;

        this.stomach.reset(size);

        resetRelativeRay();
    }

    public void reset(Creature parentOne, Creature parentTwo) {
        this.brain = NN.crossover(parentOne.brain, parentTwo.brain, parentOne.age, parentTwo.age);
        if (parentOne.age > parentTwo.age) this.genome.reset(parentOne.genome, parentTwo.genome);
        else this.genome.reset(parentTwo.genome, parentOne.genome);
        this.brain.mutate();

        this.size = genome.minSize;
        this.maxHealth = Combat.sizeToMaxHealth(size);
        this.maxEnergy = Energy.sizeToMaxEnergyFormula(size);
        this.age = 0;
        this.metabolism = Energy.energyCostFormula(maxHealth, size, genome.strength, brain.getComplexity());

        this.stomach.reset(size);

        this.energy = maxEnergy * (parentOne.genome.offspringInvestment + parentTwo.genome.offspringInvestment);
        this.health = maxHealth * (parentOne.genome.offspringInvestment + parentTwo.genome.offspringInvestment);

        resetRelativeRay();
    }


    /**
     * Once called, runs the Creature's brain with the Creature {@code c}'s current information stash.
     * <br>Builds the input array as a fixed value before returning the Runnable object, in other words,
     * even if {@code c}'s information stash changes after calling this function, the input
     * array (and hence output array) of this Creature's brain won't change.
     */
    public static void runBrain(Creature c) {
        // TODO implement
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
        // TODO implement
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** Completely resets and creates a new set of fixed relative vision rays according to the
     * current angle of {@code dynamic} and {@linkplain Genome#visionAvailable} in the {@code genome} field. */
    private void resetRelativeRay() {
        relativeRays.clear();
        for (int i = 0; i < genome.visionRayCount; i++) {
            double angle = genome.visionConeAngle * (-0.5 + (double) i / (genome.visionRayCount - 1));
            relativeRays.add(new Vector2D(Math.cos(angle) * genome.visionAvailable, Math.sin(angle) * genome.visionAvailable));
        }
    }

    /** Updates the fixed relative vision rays according to the current angle of {@code dynamic}
     * and {@linkplain Genome#visionAvailable} in the {@code genome} field. */
    private void updateRelativeRay(Dynamic dynamic) {
        //measures angle change between latest leftmost ray and the current calculated leftmost ray
        double angleDiff = relativeRays.getFirst().angle() - (dynamic.getDirection().angle() - genome.visionConeAngle * 0.5);

        //change the angle of each ray by angleDiff to update, also change the length of each ray
        for (Vector2D ray : relativeRays) {
            ray.rotate(angleDiff);
            ray.multiply(ray.length() / genome.visionAvailable);
        }
    }

    /**
     * Clears and instantiates {@code rays} with accurate coordinates of the two ends of the rays
     * according to this Creature's genome, the position {@code pos} data, and the size of this Creature.
     */
    public void getVisionRays(ArrayList<Line2D> rays, Dynamic pos) {
        for (Vector2D ray : relativeRays) {
            rays.add(new Line2D.Double(pos.x, pos.y, pos.x + ray.x, pos.y + ray.y));
            while (!WorldConstants.worldBorder.contains(rays.getLast().getP2())) {
                Line2D lineBefore = rays.getLast();
                if (WorldConstants.leftVisionBox.contains(lineBefore.getP2())) {
                    rays.add(new Line2D.Double(lineBefore.getX1() + WorldConstants.xBound, lineBefore.getY1(), lineBefore.getX2() + WorldConstants.xBound, lineBefore.getY2()));
                } else if (WorldConstants.rightVisionBox.contains(lineBefore.getP2())) {
                    rays.add(new Line2D.Double(lineBefore.getX1() - WorldConstants.xBound, lineBefore.getY1(), lineBefore.getX2() - WorldConstants.xBound, lineBefore.getY2()));
                } else if (WorldConstants.topVisionBox.contains(lineBefore.getP2())) {
                    rays.add(new Line2D.Double(lineBefore.getX1(), lineBefore.getY1() + WorldConstants.yBound, lineBefore.getX2(), lineBefore.getY2() + WorldConstants.yBound));
                } else if (WorldConstants.bottomVisionBox.contains(lineBefore.getP2())) {
                    rays.add(new Line2D.Double(lineBefore.getX1(), lineBefore.getY1() - WorldConstants.yBound, lineBefore.getX2(), lineBefore.getY2() - WorldConstants.yBound));
                } else {
                    System.out.println("Exception Occurred. Raycasting end point at " + lineBefore.getX2() + "," + lineBefore.getY2());
                }
            }
        }
    }

    /** Returns the reference to an ArrayList of the seen entities, can only be used by {@link Physics.GridWorld}. */
    public ArrayList<Entity> getStashedSeenEntities() {
        return stashedSeenEntities;
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
    public double getEnergyIfConsumed() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double getSize() {
        return size;
    }

    @Override
    public void creatureInteract(Creature c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** Compares the two Creatures' genome, returning a non-negative double describing the absolute distance
     * between the two genomes of the Creature. Takes into account both their Neural Network genome
     * and body genome. */
    public double compare(Creature o) {
        return NN.compare(o.brain, brain);
    }

    @Override
    public ReadOnlyEntity getReadOnlyCopy(Position pos) {
        if (!(pos instanceof Dynamic d)) throw new RuntimeException("Invalid position object");
        return new ReadOnlyCreature(
                d.boundingBox.x, d.boundingBox.y,
                d.boundingBox.width, d.boundingBox.height,
                d.velocity.x, d.velocity.y, d.dir.angle(),
                health, energy, genome.strength, genome.armour, genome.force,
                genome.herbivoryAffinity, genome.carnivoryAffinity, genome.offspringInvestment, age,
                genome.visionDistance, genome.boidSeparationWeight,
                genome.boidAlignmentWeight, genome.boidCohesionWeight, ID
        );
    }

    public record ReadOnlyCreature(
            int x, int y, int width, int height,
            double velocityX, double velocityY, double rotation,
            double health, double energy, double strength, double armour, double force,
            double herbivore, double carnivore, double offspringInvestment, double maturity,
            double visionRange, double separation, double alignment, double cohesion,
            int id
    ) implements ReadOnlyEntity {

        public int getSize() {
            return width;
        }

        public int getX() {
            return x + width / 2;
        }

        public int getY() {
            return y + height / 2;
        }

        @Override
        public int hashCode() {
            return id;
        }
    }
}
