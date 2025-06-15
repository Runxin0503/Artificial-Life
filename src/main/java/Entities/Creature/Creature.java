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

    /** Whether this Creature wants to lay an egg or not. */
    public boolean layingEgg;

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

        this.layingEgg = false;

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

        this.layingEgg = false;

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
    public boolean tick(Position pos) {
        age++;
        double deltaEnergy = -metabolism;

        //see
//        stashedSeenEntities

        //run this parallel with brain calculation
        //change speed & boid herding
        deltaEnergy -= updateVel(brainOutput[0], brainOutput[1], brainOutput[2], brainOutput[3], brainOutput[8], brainOutput[9], (Dynamic) pos);

        if (health < maxHealth && brainOutput[5] > 0.5) deltaEnergy -= regenHealth();

        //grow size
        deltaEnergy -= growSize((Dynamic) pos);

        //interact with entity (Eat or Attack)
        isEating = brainOutput[6] > 0.5;

        //digest food
        deltaEnergy += stomach.digest(brainOutput[7]);

        //update metabolism
//        metabolism = Energy.energyCostFormula(stomachFluid, size, strengthAvailable, brain.nodes.size(), brain.synapses.size());

        //update stomach size
        stomach.updateSize(size);

        //reset clock when brainOutput[10] > 0.5

        //add delta energy
        energy = Math.min(maxEnergy, energy + deltaEnergy);
        if (energy < 0) {
            health += energy;
            energy = 0;
        }

        //check for death (Health 0?)
//        if (!isPlayer)
        if (health <= 0 || age >= Utils.Constants.CreatureConstants.Reproduce.maturityDeath)
            return true;

        //try to reproduce
        layingEgg = brainOutput[4] > 0.5;

        return false;
    }

    /**
     * Updates the creature's linear and angular velocity based on the neural network outputs.
     *
     * <p>This method interprets brain output signals to adjust the creature's movement:
     * forward/backward movement, turning left/right, and herding behavior.
     * Depending on the values passed, it modifies the velocity vector and angular speed
     * of the {@link Dynamic} position object.</p>
     *
     * @param moveForward Brain output signal for moving forward.
     * @param moveBackward Brain output signal for moving backward.
     * @param turnLeft Brain output signal for turning left.
     * @param turnRight Brain output signal for turning right.
     * @param herdYes Brain output signal for initiating herding behavior.
     * @param herdNo Brain output signal for resisting herding behavior.
     * @param pos The dynamic position and motion state of the creature to be updated.
     * @return The kinetic energy cost incurred from the applied movement update.
     */
    private double updateVel(double moveForward, double moveBackward, double turnLeft, double turnRight, double herdYes, double herdNo, Dynamic pos) {
        double mass = pos.getMass();

        double deltaVelocity = genome.forceAvailable / mass;
        double deltaOmega = genome.forceAvailable * (Math.PI / 180) / mass;

//        if (((herdNo > 0.5 && !boids.isEmpty()) || herdYes > 0.5)) {
//            Vector2D herdingVector = getHerdingVector(boids, (herdYes > 0.5 && herdNo > 0.5) ? (herdYes > herdNo) : (herdYes > 0.5));
//            double deltaTheta = Equations.angleDistance(pos.dir.angle(), herdingVector.angle());
//            //if deltaTheta is far away from angularSpeed, adjust angularSpeed towards deltaTheta
//            if (deltaTheta < 0 != pos.angularSpeed < 0 || Math.abs(deltaTheta) > Boids.angleAdjustmentThreshold) {
//                //turns to the direction of the herding vector
//                if (deltaTheta > 0 && pos.angularSpeed < deltaTheta) {
//                }//keep accelerating positive
//                else if (deltaTheta < 0 && pos.angularSpeed > deltaTheta) deltaOmega *= -1;
//            } else {
//                deltaOmega = 0;
//            }
//
//            //decelerate when velocity vector length is bigger than herding vector length
//            if (getVelocity() > herdingVector.length() && Math.abs(Equations.angleDistance(speed.angle(), herdingVector.angle())) < Math.toRadians(10))
//                deltaVelocity *= -1;
//                //decelerate when velocity vector and herding vector are too far apart
//            else if (Math.abs(deltaTheta) > Math.toRadians(90) && Math.abs(Equations.angleDistance(direction, speed.angle())) < Math.toRadians(10))
//                deltaVelocity *= -1;
//                //stop accelerating when velocity vector and herding velocity vector are close to the same
//            else if (Math.abs(herdingVector.length() - speed.length()) < deltaVelocity) {
//                deltaVelocity = 0;
//            }
//        } else
        {
            if (moveForward > 0.5 && moveBackward > 0.5) {
                if (moveForward < moveBackward)
                    deltaVelocity = -deltaVelocity / 2;
            } else if (moveBackward > 0.5) deltaVelocity = -deltaVelocity / 2;
            else if (moveForward <= 0.5) deltaVelocity = 0;

            if (turnLeft > 0.5 && turnRight > 0.5) {
                if (turnRight < turnLeft)
                    deltaOmega = -deltaOmega;
            } else if (turnLeft > 0.5) deltaOmega = -deltaOmega;
            else if (turnRight <= 0.5) deltaOmega = 0;
        }

        pos.velocity.add(pos.dir.multiplied(deltaVelocity));
        pos.angularSpeed += deltaOmega;
        return 0.5 * mass * (deltaVelocity * deltaVelocity + deltaOmega * deltaOmega);
    }

    /**
     * Regenerates the creature's health based on a constant health regeneration rate.
     *
     * <p>The method increases the creature's health by a fixed amount,
     * but clamps it to {@code maxHealth} if the regenerated health exceeds the maximum.
     * It returns the effective energy cost of the health regeneration.</p>
     *
     * @return The energy cost spent on regenerating health.
     */
    private double regenHealth() {
        health += Combat.healthRegen;
        double cost = Combat.healthRegen - Math.max(0, health - maxHealth);
        health = Math.min(maxHealth, health);
        return cost;
    }

    /**
     * Grows the creature's size based on its age and genetic growth configuration.
     *
     * <p>This method adjusts the creature's size using its genome's logic,
     * recalculates its maximum health and energy, and applies momentum conservation
     * by adjusting the velocity according to the mass change.
     * It returns the energy cost associated with the size increase.</p>
     *
     * @param pos The dynamic position of the creature which contains its size and velocity.
     * @return The energy cost of growing in size, scaled by an armor multiplier.
     */
    private double growSize(Dynamic pos) {
        double sizeBefore = size;
        double massBefore = pos.getMass();
        maxEnergy = Energy.sizeToMaxEnergyFormula(size);
        maxHealth = Combat.sizeToMaxHealth(size);
        pos.setSize(genome.updateCreatureSize(age));
        pos.velocity.multiply(pos.getMass() / massBefore);
        return (size - sizeBefore) * genome.armourMultiplier;
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

    /**
     * Clears and instantiates {@code rays} with accurate coordinates of the two ends of the rays
     * according to this Creature's genome, the position {@code pos} data, and the size of this Creature.
     */
    public void getVisionRays(ArrayList<Line2D> rays, Dynamic pos) {
        updateRelativeRay(pos);
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
        // TODO implement
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double getSize() {
        return size;
    }

    @Override
    public void creatureInteract(Creature c) {
        // TODO implement
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
        return new ReadOnlyCreature(d.boundingBox.x, d.boundingBox.y, d.boundingBox.width, d.boundingBox.height, d.velocity.x, d.velocity.y, d.dir.angle(), health, energy, genome.strength, genome.armour, genome.force, genome.herbivoryAffinity, genome.carnivoryAffinity, genome.offspringInvestment, age, genome.visionDistance, genome.boidSeparationWeight, genome.boidAlignmentWeight, genome.boidCohesionWeight, ID);
    }

    public record ReadOnlyCreature(int x, int y, int width, int height, double velocityX, double velocityY,
                                   double rotation, double health, double energy, double strength, double armour,
                                   double force, double herbivore, double carnivore, double offspringInvestment,
                                   double maturity, double visionRange, double separation, double alignment,
                                   double cohesion, int ID) implements ReadOnlyEntity {

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
            return ID;
        }
    }
}
