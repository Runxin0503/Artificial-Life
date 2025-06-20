package Entities.Creature;


import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.util.ArrayList;

import Entities.Entity;
import Genome.NN;
import Physics.Dynamic;
import Physics.GridWorld;
import Physics.Position;
import Utils.Constants.CreatureConstants;
import Utils.Constants.CreatureConstants.Combat;
import Utils.Constants.CreatureConstants.Energy;
import Utils.Constants.NeuralNet;
import Utils.Constants.Physics;
import Utils.Constants.WorldConstants;
import Utils.Equations;
import Utils.Pair;
import Utils.Vector2D;

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

    /** Entity Objects that have intersected with this Creature's vision rays, organized by type.
     * Modified and maintained by {@link GridWorld}, queried by {@linkplain #runBrain}. */
    private final Pair<? extends Entity, ? extends Position>[] rayHits = new Pair[4];
    
    /** Count of entities that have intersected with this Creature's vision rays, organized by type.
     * Modified and maintained by {@link GridWorld}, queried by {@linkplain #runBrain}. */
    private final int[] rayHitCounts = new int[4];

    /** The Genome component of this Creature, stores all gene data when reproduction occurs. */
    final Genome genome = new Genome();

    /** The stomach of this Creature */
    private final Stomach stomach = new Stomach(0);

    /** The Brain component of this Creature, a Neural Network that consumes more energy the bigger it is. */
    private NN brain;

    /** Stores the latest output of the brain. */
    private double[] brainOutput;

    private double internalClockCountdown = 0;

    public Creature(int id) {
        super(id);
        reset();
    }

    public Creature(int id, Creature parentOne, Creature parentTwo) {
        super(id);
        reset(parentOne, parentTwo);
    }

    public void reset() {
        this.brain = NN.getDefaultNeuralNet(NeuralNet.EvolutionConstants);//Math.random()<0.6?Genome.defaultGenome():
        this.genome.reset();

        this.age = 0;
        this.size = genome.updateCreatureSize(age);
        this.maxHealth = Combat.sizeToMaxHealth(size);
        this.maxEnergy = Energy.sizeToMaxEnergyFormula(size);
        this.metabolism = Energy.energyCostFormula(maxHealth, size, genome.strength, brain.getComplexity());
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

        this.age = 0;
        this.size = genome.updateCreatureSize(age);
        this.maxHealth = Combat.sizeToMaxHealth(size);
        this.maxEnergy = Energy.sizeToMaxEnergyFormula(size);
        this.metabolism = Energy.energyCostFormula(maxHealth, size, genome.strength, brain.getComplexity());

        this.layingEgg = false;

        this.stomach.reset(size);

        this.energy = maxEnergy * (parentOne.genome.offspringInvestment + parentTwo.genome.offspringInvestment);
        this.health = maxHealth * (parentOne.genome.offspringInvestment + parentTwo.genome.offspringInvestment);

        resetRelativeRay();
    }

    /**
     * Once called, builds the Creature's brain input array with the Creature {@code c}'s current information stash.
     */
    public void runBrain(Dynamic pos) {
        // TODO check all this vvv
        double[] input = new double[NeuralNet.EvolutionConstants.getInputNum()];
        Pair<? extends Entity, ? extends Position> zero = rayHits[0], one = rayHits[1], two = rayHits[2], three = rayHits[3];

        // Use pos to get x, y, direction
        double x = pos.x;
        double y = pos.y;
        double direction = pos.dir.angle();

        if (zero != null) {
            Position zeroPos = zero.second();
            input[0] = Equations.dist(pos.x, pos.y, zeroPos.x, zeroPos.y) - 1;
            input[1] = ((Dynamic) zeroPos).velocity.length();
            double objAngle = (Math.atan2((zeroPos.y - y), (zeroPos.x - x)) + Math.PI * 2) % (Math.PI * 2);
            input[2] = (direction - objAngle) / (Math.PI * 2);
            input[3] = (objAngle - direction) / (Math.PI * 2);
            input[4] = zero.first().getEnergyIfConsumed();
            input[5] = ((Creature) zero.first()).health / Math.max(0.01, ((Creature) zero.first()).genome.armourAvailable - genome.strengthAvailable);
            input[6] = rayHitCounts[0];
        }
        if (one != null) {
            Position onePos = rayHits[1].second();
            input[7] = Equations.dist(pos.x, pos.y, onePos.x, onePos.y) - 1;
            double objAngle = (Math.atan2((onePos.y - y), (onePos.x - x)) + Math.PI * 2) % (Math.PI * 2);
            input[8] = (direction - objAngle) / (Math.PI * 2);
            input[9] = (objAngle - direction) / (Math.PI * 2);
            input[10] = one.first().getEnergyIfConsumed();
            input[11] = one.second().boundingBox.width;
            input[12] = rayHitCounts[1];
        }
        if (two != null) {
            Position twoPos = rayHits[2].second();
            input[13] = Equations.dist(pos.x, pos.y, twoPos.x, twoPos.y) - 1;
            if (input[13] < size * 1.5) input[13] = -1;
            double objAngle = (Math.atan2((twoPos.y - y), (twoPos.x - x)) + Math.PI * 2) % (Math.PI * 2);
            input[14] = (direction - objAngle) / (Math.PI * 2);
            input[15] = (objAngle - direction) / (Math.PI * 2);
            input[16] = two.first().getEnergyIfConsumed();
            input[17] = two.second().boundingBox.width;
            input[18] = rayHitCounts[2];
        }
        if (three != null) {
            Position threePos = rayHits[3].second();
            input[19] = Equations.dist(pos.x, pos.y, threePos.x, threePos.y) - 1;
            double objAngle = (Math.atan2((threePos.y - y), (threePos.x - x)) + Math.PI * 2) % (Math.PI * 2);
            input[20] = (direction - objAngle) / (Math.PI * 2);
            input[21] = (objAngle - direction) / (Math.PI * 2);
            input[22] = three.first().getEnergyIfConsumed();
            input[23] = ((Egg) three.first()).timeLeft;
            input[24] = rayHitCounts[3];
        }

        input[25] = Math.min(1.0, pos.velocity.length() / genome.forceAvailable * (pos.getMass() * Physics.frictionParallel)) * 2 - 1;
        input[26] = pos.angularSpeed / (Math.PI * 2);

        input[27] = (energy / maxEnergy - 0.5) * 2;
        input[28] = (health / maxHealth - 0.5) * 2;
        input[29] = (size / genome.maxSize - 0.5) * 2;

        input[30] = metabolism;
        input[31] = genome.strengthAvailable;
        input[32] = ((stomach.plantMass + stomach.meatMass) / stomach.stomachSize - 0.5) * 2;
        input[33] = stomach.isStarving() ? 1 : -1;
        input[34] = Math.sin(internalClockCountdown / NeuralNet.internalClockPeriod * Math.PI * 2); // internal clock sine wave

            System.out.println("composed");
        //output: Move Forward | Move Backward | Turn Left | Turn Right | Mate | Regenerate Health | Eat | Digestion Rate | Herd | Separate
        this.brainOutput = brain.calculateWeightedOutput(input);
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

        //run this parallel with brain calculation
        //change speed & boid herding
        double mass = pos.getMass();

        double deltaVelocity = (brainOutput[1] > 0.5 || brainOutput[0] > 0.5)
            ? genome.forceAvailable / mass / 2
            : 0;

        double deltaOmega = (brainOutput[2] > 0.5 || brainOutput[3] > 0.5)
            ? genome.forceAvailable * (Math.PI / 180) / mass
            : 0;

        deltaEnergy -= 0.5 * mass * (deltaVelocity * deltaVelocity + deltaOmega * deltaOmega);


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
        if (health <= 0 || age >= CreatureConstants.Reproduce.maturityDeath)
            return true;

        //try to reproduce
        layingEgg = brainOutput[4] > 0.5;
        
        internalClockCountdown++;
        if (internalClockCountdown >= NeuralNet.internalClockPeriod)
            internalClockCountdown = 0;
    

        return false;
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
            if (!Double.isFinite(pos.x) || !Double.isFinite(pos.y) || !Double.isFinite(ray.x) || !Double.isFinite(ray.y))
                System.out.println(pos.x + "," + pos.y + ". " + ray.x + "," + ray.y);
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
                    relativeRays.forEach(r -> System.out.println(r));
                    System.out.println("genome.visionAvailable:" +genome.visionAvailable);
                    break;
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
            ray.multiply(genome.visionAvailable / ray.length());
        }
    }

    /**
     * Attempts to reproduce if the creature is currently laying an egg and has enough energy.
     * @return true if reproduction occurs (energy is deducted and layingEgg is reset), false otherwise.
     */
    public boolean tryReproduce() {
        if (layingEgg) {
            double cost = genome.getReproductionCost();
            if (energy < cost * 1.1) {
                this.energy -= cost;
                this.layingEgg = false;
                return true;
            } return false;
        } else return false;
    }

    /** Returns the reference to an ArrayList of the seen entities, can only be used by {@link GridWorld}. */
    public ArrayList<Entity> getStashedSeenEntities() {
        return stashedSeenEntities;
    }

    /** Returns the reference to the array storing ray hit entities, can only be used by {@link GridWorld}. */
    public Pair<? extends Entity, ? extends Position>[] getRayHits() {
        return rayHits;
    }

    /** Returns the reference to the array storing ray hit counts, can only be used by {@link GridWorld}. */
    public int[] getRayHitCounts() {
        return rayHitCounts;
    }

    /** Calculates the creature's muscle acceleration based on the last output of the brain
     * and the last BOID information stored in this Creature's buffer. Updates {@code pos} based on
     * this muscle acceleration and returns a reference to {@code pos}. */
    public Dynamic updateAcceleration(Dynamic pos) {
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
        // brainOutput[0], brainOutput[1], brainOutput[2], brainOutput[3], brainOutput[8], brainOutput[9]
        {
            if (brainOutput[0] > 0.5 && brainOutput[1] > 0.5) {
                if (brainOutput[0] < brainOutput[1])
                    deltaVelocity = -deltaVelocity / 2;
            } else if (brainOutput[1] > 0.5) deltaVelocity = -deltaVelocity / 2;
            else if (brainOutput[0] <= 0.5) deltaVelocity = 0;

            if (brainOutput[2] > 0.5 && brainOutput[3] > 0.5) {
                if (brainOutput[3] < brainOutput[2])
                    deltaOmega = -deltaOmega;
            } else if (brainOutput[2] > 0.5) deltaOmega = -deltaOmega;
            else if (brainOutput[3] <= 0.5) deltaOmega = 0;
        }

        pos.velocity.add(pos.dir.multiplied(deltaVelocity));
        pos.angularSpeed += deltaOmega;

        return pos;
    }

    /** Returns a bounding box of the size and position of this Creature's mouth, or
     * {@code Null} if the Creature isn't eating at the moment. */
    public Rectangle getEatingHitbox(Position pos) {
        if (isEating) throw new UnsupportedOperationException("Not supported yet.");//TODO implement eatingHitbox
        else return null;
    }

    @Override
    public double getEnergyIfConsumed() {
        return health + energy + size * genome.armourMultiplier + stomach.plantMass * CreatureConstants.Digestion.plantMassToEnergy + stomach.meatMass * CreatureConstants.Digestion.meatMassToEnergy;
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

        @Override
        public int getX() {
            return x + width / 2;
        }

        @Override
        public int getY() {
            return y + height / 2;
        }

        @Override
        public int hashCode() {
            return ID;
        }
    }
}
