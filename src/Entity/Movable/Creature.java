package Entity.Movable;

import Constants.Constants.CreatureConstants.*;
import Constants.Constants.*;

import Entity.*;
import Evolution.NN;
import World.*;
import World.GridList.Grid;
import Entity.Immovable.Bush;
import Entity.Immovable.Egg;

import java.awt.*;
import java.awt.geom.Line2D;
import java.io.Serializable;
import java.util.ArrayList;

public class Creature extends Movable implements Serializable {
    private int maturity, visionAvailable=Vision.minVisionDistance;
    private double direction, angularSpeed, maxAngularSpeed, maxSpeed;
    private double health, energy, deltaEnergy;
    private double plantMass, meatMass, stomachFluid;
    private double forceAvailable=1, strengthAvailable=1, armourAvailable=1;
    private double baseEnergyCost, maxEnergy, maxHealth, stomachSize;
    private int internalClockCountdown = 0;
    private NN brain;
    private final CreatureGenome genome;
    private boolean seekingMate = false, starving = false, eating = false;
    private Entity[] rayHits = new Entity[4];
    private int[] rayHitCounts = new int[4];
    private transient final ArrayList<Line2D> allRays = new ArrayList<>();
    private final ArrayList<GridList.Grid> allVisionGrids = new ArrayList<>();
    public boolean isPlayer = false, isSelected = false;

    //see,smell,hear,touch,taste. if no see, smell & hear better
//    private boolean canSee;

    public Creature(Point point) {
        this(point.x, point.y);
    }

    public Creature(int x, int y) {
        super(x, y);
        this.brain = NN.randomGenome();//Math.random()<0.6?Genome.defaultGenome():
        this.genome = new CreatureGenome();
        this.direction = Math.random() * 2 * Math.PI;

        super.setSize(genome.minSize, ImageConstants.getRotation(direction));
        this.maxHealth = CreatureConstants.Combat.sizeToMaxHealth(size);
        this.stomachSize = Stomach.sizeToStomachSize(size);
        this.maxEnergy = Energy.sizeToMaxEnergyFormula(size);
        this.baseEnergyCost = Energy.energyCostFormula(maxHealth, size, genome.strength, brain.nodes.size(), brain.synapses.size());
        this.angularSpeed = 0;
        this.maturity = 0;
        this.energy = maxEnergy;
        this.health = maxHealth;
        this.plantMass = 0;
        this.meatMass = 0;
        this.stomachFluid = 0;
    }

    public Creature(Creature parentOne, Creature parentTwo) {
        super(0, 0);

        this.brain = NN.crossover(parentOne.brain, parentTwo.brain, parentOne.maturity, parentTwo.maturity);
        this.genome = (parentOne.maturity > parentTwo.maturity ? new CreatureGenome(parentOne.genome, parentTwo.genome) : new CreatureGenome(parentTwo.genome, parentOne.genome));
        this.brain.mutate();
        this.direction = Math.random() * 2 * Math.PI;

        super.setSize(genome.minSize, ImageConstants.getRotation(direction));
        this.maxHealth = CreatureConstants.Combat.sizeToMaxHealth(size);
        this.stomachSize = Stomach.sizeToStomachSize(size);
        this.baseEnergyCost = Energy.energyCostFormula(maxHealth, size, genome.strength, brain.nodes.size(), brain.synapses.size());
        this.maxEnergy = Energy.sizeToMaxEnergyFormula(size);
        this.angularSpeed = 0;
        this.maturity = 0;
        this.plantMass = 0;
        this.meatMass = 0;
        this.stomachFluid = 0;
        this.energy = maxEnergy * (parentOne.genome.offspringInvestment + parentTwo.genome.offspringInvestment);
        this.health = maxHealth;
        setCoord(parentOne.getX(), parentOne.getY());
    }

    public void tick(World world) {
        double[] output = brain.output;
        deltaEnergy = -baseEnergyCost / WorldConstants.Settings.ticksToSecond;

        if (++internalClockCountdown >= 60 * WorldConstants.Settings.ticksToSecond) internalClockCountdown = 0;
        starving = energy == 0;
        if (!starving) maturity++;

        //see
        ArrayList<Creature> boids = new ArrayList<Creature>();
        see(boids);

        //run this parallel with brain calculation
        //change speed & boid herding
        deltaEnergy -= updateVel(output[0], output[1], output[2], output[3], output[8], output[9], boids);

        if (!starving && health < maxHealth && output[5] > 0.5) deltaEnergy -= regenHealth();

        //grow size
        if (!starving) deltaEnergy -= growSize();

        //interact with entity (Eat or Attack)
        eating = output[6] > 0.5;
        if (eating) eat();

        //digest food
        deltaEnergy += digest(output[7]);

        //update metabolism
        baseEnergyCost = Energy.energyCostFormula(stomachFluid, size, strengthAvailable, brain.nodes.size(), brain.synapses.size());

        //update stomach size
        stomachSize = Stomach.sizeToStomachSize(size);

        //reset clock
        if (output[10] > 0.5) internalClockCountdown = 0;

        //add delta energy
        energy = Math.min(maxEnergy, energy + deltaEnergy);
        if (energy < 0) {
            health += energy;
            energy = 0;
        }

        //check for death (Health 0?)
        if (!isPlayer)
            if (health <= 0 || maturity < 0 || size < Reproduce.babySize || !Double.isFinite(energy) || maturity >= Reproduce.maturityDeath) {
                death(world);
                return;
            }

        //try to reproduce
        if (output[4] <= 1 && output[4] > 0.5) tryReproduce(world, rayHits);
        else seekingMate = false;
    }

    private void see(ArrayList<Creature> boids) {
        double[] distance = new double[4];
        Entity[] currentRayHits = new Entity[4];
        int[] currentRayHitCounts = new int[4];
        //Nearest Edible Plant Info | Nearest Edible Entity Info | Nearest Inedible Entity Info
        allRays.clear();
        for (double i = 0; i < genome.visionRayCount; i++) {
            //direction-CreatureConstants.visionConeAngle/2;i<=direction+CreatureConstants.visionConeAngle/2;i+=CreatureConstants.visionConeAngle/(CreatureConstants.visionRayCount-1)
            double angle = direction + genome.visionConeAngle * (-0.5 + i * 1 / (genome.visionRayCount - 1));
            ArrayList<Line2D> rays = new ArrayList<Line2D>();
            rays.add(new Line2D.Double(x, y,x + Math.cos(angle) * visionAvailable,y + Math.sin(angle) * visionAvailable));
            while (!WorldConstants.worldBorder.contains(rays.get(rays.size() - 1).getP2())) {
                Line2D lineBefore = rays.get(rays.size() - 1);
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
            allRays.addAll(rays);
        }

        for (Line2D ray : allRays) {
            GridList.get(ray, allVisionGrids);
            for (Grid grid : allVisionGrids)
                for (Entity e : grid.getContainedEntities())
                    if (e != this) {
                        int id;
                        switch (e) {
                            case Creature creature -> id = 0;
                            case Bush bush -> id = 1;
                            case Corpse corpse -> id = 2;
                            case Egg egg -> id = 3;
                            default -> id = -1;
                        }
                        if (currentRayHits[id] != null && currentRayHits[id].equals(e)) continue;
                        double dist = Equations.dist(x, y, e.getX(), e.getY());
                        if (dist < Combat.sensingDistance || (e.getBoundingBox().intersectsLine(ray) || e.getBoundingBox().contains(x, y))) {
                            if (e instanceof Creature && !boids.contains((Creature) e)) boids.add((Creature) e);
                            if (currentRayHits[id] != null) {
                                if (distance[id] > Equations.dist(x, y, e.getX(), e.getY())) {
                                    distance[id] = dist;
                                    currentRayHits[id] = e;
                                }
                            } else {
                                currentRayHits[id] = e;
                                distance[id] = dist;
                            }
                            currentRayHitCounts[id]++;
                            break;
                        }
                    }
        }
        rayHits = currentRayHits;
        rayHitCounts = currentRayHitCounts;
    }

    public void thinkThonk() {
        if (isPlayer) {
            brain.output = PlayerGenome.getOutput();
            return;
        }

        //input: Nearest Creature Info | Nearest Bush Info | Nearest Corpse Info | Nearest Egg Info | Velocity & Position | Energy | Health | Size | metabolism | strength | Stomach % | starving | clock
        double[] input = new double[NeuralNet.inputNum];
        if (rayHits[0] != null) {
            input[0] = Equations.distFromRect(boundingBox, rayHits[0].getBoundingBox()) - 1;
            input[1] = ((Movable) rayHits[0]).getVelocity();
            double objAngle = (Math.atan2((rayHits[0].getY() - y), (rayHits[0].getX() - x)) + Math.PI * 2) % (Math.PI * 2);
            input[2] = (direction - objAngle) / (Math.PI * 2);
            input[3] = (objAngle - direction) / (Math.PI * 2);
            input[4] = rayHits[0].getEnergyIfConsumed();
            input[5] = ((Creature) rayHits[0]).getHealth() / Math.max(0.01, ((Creature) rayHits[0]).getArmour() - getDamage());
            input[6] = rayHitCounts[0];
        }
        if (rayHits[1] != null) {
            input[7] = Equations.distFromRect(boundingBox, rayHits[1].getBoundingBox()) - 1;
            double objAngle = (Math.atan2((rayHits[1].getY() - y), (rayHits[1].getX() - x)) + Math.PI * 2) % (Math.PI * 2);
            input[8] = (direction - objAngle) / (Math.PI * 2);
            input[9] = (objAngle - direction) / (Math.PI * 2);
            input[10] = rayHits[1].getEnergyIfConsumed();
            input[11] = rayHits[1].getSize();
            input[12] = rayHitCounts[1];
        }
        if (rayHits[2] != null) {
            input[13] = Equations.distFromRect(boundingBox, rayHits[2].getBoundingBox()) - 1;
            if (input[13] < size * 1.5) input[13] = -1;
            double objAngle = (Math.atan2((rayHits[2].getY() - y), (rayHits[2].getX() - x)) + Math.PI * 2) % (Math.PI * 2);
            input[14] = (direction - objAngle) / (Math.PI * 2);
            input[15] = (objAngle - direction) / (Math.PI * 2);
            input[16] = rayHits[2].getEnergyIfConsumed();
            input[17] = rayHits[2].getSize();
            input[18] = rayHitCounts[2];
        }
        if (rayHits[3] != null) {
            input[19] = Equations.distFromRect(boundingBox, rayHits[3].getBoundingBox()) - 1;
            double objAngle = (Math.atan2((rayHits[3].getY() - y), (rayHits[3].getX() - x)) + Math.PI * 2) % (Math.PI * 2);
            input[20] = (direction - objAngle) / (Math.PI * 2);
            input[21] = (objAngle - direction) / (Math.PI * 2);
            input[22] = rayHits[3].getEnergyIfConsumed();
            input[23] = ((Egg) rayHits[3]).getTimeLeft();
            input[24] = rayHitCounts[3];
        }

        input[25] = (getVelocity() / maxSpeed) * 2 - 1;
        input[26] = angularSpeed / maxAngularSpeed;

        input[27] = (energy / maxEnergy - 0.5) * 2;
        input[28] = (health / maxHealth - 0.5) * 2;
        input[29] = (size / genome.maxSize - 0.5) * 2;

        input[30] = baseEnergyCost;
        input[31] = strengthAvailable;
        input[32] = ((plantMass + meatMass) / stomachSize - 0.5) * 2;
        input[33] = starving ? 1 : -1;
        input[NeuralNet.inputNum - 1] = Math.sin(internalClockCountdown);

        //output: Move Forward | Move Backward | Turn Left | Turn Right | Mate | Regenerate Health | Eat | Digestion Rate | Herd | Separate
        NN.calculateWeightedOutput(brain, input);
    }

    private double updateVel(double moveForward, double moveBackward, double turnLeft, double turnRight, double herdYes, double herdNo, ArrayList<Creature> boids) {
        double deltaVelocity = forceAvailable / getMass() / WorldConstants.Settings.ticksToSecond;
        double deltaOmega = forceAvailable * (Math.PI / 180) / getMass() / WorldConstants.Settings.ticksToSecond;

        if (((herdNo > 0.5 && !boids.isEmpty()) || herdYes > 0.5)) {
            Vector2D herdingVector = getHerdingVector(boids, (herdYes > 0.5 && herdNo > 0.5) ? (herdYes > herdNo) : (herdYes > 0.5));
            double deltaTheta = Equations.angleDistance(direction, herdingVector.angle());
            //if deltaTheta is far away from angularSpeed, adjust angularSpeed towards deltaTheta
            if (deltaTheta < 0 != angularSpeed < 0 || Math.abs(deltaTheta) > Boids.angleAdjustmentThreshold) {
                //turns to the direction of the herding vector
                if (deltaTheta > 0 && angularSpeed < deltaTheta) {
                }//keep accelerating positive
                else if (deltaTheta < 0 && angularSpeed > deltaTheta) deltaOmega *= -1;
            } else {
                deltaOmega = 0;
            }

            //decelerate when velocity vector length is bigger than herding vector length
            if (getVelocity() > herdingVector.length() && Math.abs(Equations.angleDistance(speed.angle(), herdingVector.angle())) < Math.toRadians(10))
                deltaVelocity *= -1;
                //decelerate when velocity vector and herding vector are too far apart
            else if (Math.abs(deltaTheta) > Math.toRadians(90) && Math.abs(Equations.angleDistance(direction, speed.angle())) < Math.toRadians(10))
                deltaVelocity *= -1;
                //stop accelerating when velocity vector and herding velocity vector are close to the same
            else if (Math.abs(herdingVector.length() - speed.length()) < deltaVelocity) {
                deltaVelocity = 0;
            }
        } else {
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

        speed.add(Math.cos(direction) * deltaVelocity, Math.sin(direction) * deltaVelocity);
        angularSpeed += deltaOmega;
        return 0.5 * getMass() * (deltaVelocity * deltaVelocity + deltaOmega * deltaOmega) / WorldConstants.Settings.ticksToSecond;
    }

    private Vector2D getHerdingVector(ArrayList<Creature> boids, boolean positiveStimuli) {
        if (boids.isEmpty())
            return new Vector2D(Math.cos(direction) * Boids.minHerdingSpeed * forceAvailable, Math.sin(direction) * Boids.minHerdingSpeed * forceAvailable);
        Vector2D separation = new Vector2D(), alignment = new Vector2D(), cohesion = new Vector2D();
        for (Creature c : boids) {
            double distFromC = Equations.distFromRect(boundingBox, c.boundingBox), invDist = 1 / (2 * (distFromC >= 0 ? Math.max(0.01, distFromC) : Math.min(-0.01, distFromC)));
            separation.add(new Vector2D(x - c.x, y - c.y).multiply(invDist));
            alignment.add(c.speed);
            cohesion.add(c.x, c.y);
        }
        Vector2D herdingVector = new Vector2D().add(separation.normalize().multiply(genome.boidSeparationWeight));
        if (!positiveStimuli) return herdingVector;

        cohesion.divide(boids.size()).subtract(x, y);

        double herdingSpeed = Math.max(Boids.minHerdingSpeed * forceAvailable, alignment.length() / boids.size());
        herdingVector.add(alignment.normalize().multiply(genome.boidAlignmentWeight)).add(cohesion.normalize().multiply(genome.boidCohesionWeight));//.multiply(random.x,random.y)
        if (herdingVector.length() < herdingSpeed) herdingVector.multiply(herdingSpeed / herdingVector.length());

        Vector2D random = new Vector2D(herdingVector.length() * (Math.random() * 0.08 - 0.04), herdingVector.length() * (Math.random() * 0.08 - 0.04));
        herdingVector.add(random);

//        if (isSelected) {
//            System.out.println("Separation X/Y - " + separation);
//            System.out.println("Alignment X/Y - " + alignment);
//            System.out.println("Cohesion X/Y - " + cohesion);
//            System.out.println("Positive Stimuli? " + positiveStimuli);
//            System.out.println("Dir X/Y - " + herdingVector);
//        }
        return herdingVector;
    }

    @Override
    public void friction() {
        //-bv=F=ma.  v0+at=v1.  v0-bv0/m=v1.   v0*(1-b/m)=v1. m = s*s.  v0*(1-b/s/s)=v1.
        Vector2D facing = new Vector2D(Math.cos(direction), Math.sin(direction));

        double dotProduct = speed.x * facing.x + speed.y * facing.y;
        Vector2D parallel = facing.multiply(dotProduct);
        Vector2D perpendicular = speed.subtract(parallel);

        speed = parallel.multiply(1 - Physics.frictionParallel / WorldConstants.Settings.ticksToSecond).add(perpendicular.multiply(1 - Physics.frictionPerpendicular / WorldConstants.Settings.ticksToSecond)).max(WorldConstants.Settings.maxSpeed);
        if (Math.abs(speed.x) < 0.1) speed.x = 0;
        if (Math.abs(speed.y) < 0.1) speed.y = 0;

        angularSpeed *= (1 - Physics.frictionAngular / WorldConstants.Settings.ticksToSecond);
    }

    @Override
    public void updatePos() {
        super.updatePos();
        int directionBefore = (int) Math.round(Math.toDegrees(direction));
        direction += angularSpeed / WorldConstants.Settings.ticksToSecond;
        if (direction >= Math.PI * 2 || direction <= Math.PI * -2) direction %= Math.PI * 2;
        if (directionBefore != (int) Math.round(Math.toDegrees(direction))) {
            setImage(ImageConstants.getRotation(direction).getScaledInstance(getBoundingBox().width, getBoundingBox().height, ImageConstants.ResizeConstant));
        }
    }

    private double regenHealth() {
        health += Combat.healthRegen / WorldConstants.Settings.ticksToSecond;
        double cost = Combat.healthRegen / WorldConstants.Settings.ticksToSecond - Math.max(0, health - maxHealth);
        health = Math.min(maxHealth, health);
        return cost;
    }

    private double growSize() {
        maturity++;
        double sizeBefore = size;
        speed.multiply(getMass());
        maxEnergy = Energy.sizeToMaxEnergyFormula(size);
        maxHealth = Combat.sizeToMaxHealth(size);
        forceAvailable = genome.updateForce(size);
        armourAvailable = genome.updateArmour(size);
        strengthAvailable = genome.updateStrength(size);
        visionAvailable = genome.visionUpdate(size);
        maxSpeed = forceAvailable * size / (getMass() * Physics.frictionParallel);
        maxAngularSpeed = forceAvailable * (Math.PI / 180) / (getMass() * Physics.frictionAngular);
        setSize(genome.updateSize(maturity), ImageConstants.getRotation(direction));
        speed.divide(getMass());
        return (size - sizeBefore) * genome.armourMultiplier;
    }

    private void eat() {
        Rectangle eatingHitbox = getEatingHitbox();

        for (GridList.Grid grids : getOccupiedGrids())
            for (Entity e : grids.getContainedEntities())
                if (!e.equals(this) && e.getBoundingBox().intersects(eatingHitbox)) {
                    e.creatureInteract(this);
                    if (e instanceof Creature)
                        genome.biteStrengthIncrease();
                }
    }

    private double digest(double deltaStomachFluid) {
        stomachFluid = Math.max(0, Math.min(stomachSize * Stomach.stomachSizeToMaxStomachFluid, stomachFluid + deltaStomachFluid));
        double massDigested = stomachFluid / WorldConstants.Settings.ticksToSecond;
        if (massDigested <= 0) return 0;
        double nutrientsGained = 0;
        if (plantMass > 0) {
            double min = Math.min(plantMass, massDigested * Stomach.plantDigestionRate);
            plantMass -= min;
            massDigested -= min / Stomach.plantDigestionRate;
            nutrientsGained += min * Stomach.plantMassToEnergy * genome.herbivoryAffinity;
        }
        if (massDigested <= 0) return nutrientsGained;
        if (meatMass > 0) {
            double min = Math.min(meatMass, massDigested * Stomach.meatDigestionRate);
            meatMass -= min;
            massDigested -= min / Stomach.meatDigestionRate;
            nutrientsGained += min * Stomach.meatMassToEnergy * genome.carnivoryAffinity;
        }
        return nutrientsGained;
    }

    private void tryReproduce(World world, Entity[] rayHits) {
        if (energy < genome.getReproductionCost() * 1.1) return;
        Creature offspring = this.mate(this);
        energy -= offspring.getEnergyIfConsumed();
        world.add.add(new Egg(offspring, x - Math.cos(direction) * (size + genome.minSize / 2), y - Math.sin(direction) * (size + genome.minSize / 2)));
        seekingMate = false;
    }

    private void death(World world) {
        health = Double.isFinite(health) ? Math.max(0, health) : 0;
        energy = Double.isFinite(energy) ? Math.max(0, energy) : 0;
        world.remove.add(this);
        if (WorldConstants.WorldGen.spawnCorpse)
            world.add.add(new Corpse(this));
    }

    @Override
    public void creatureInteract(Creature c) {
        synchronized (this) {
            double damageDealt = Math.max(0, Math.min(health, c.getDamage() - armourAvailable));
            this.energy -= damageDealt / Math.max(1, armourAvailable);
            this.health -= damageDealt;
            genome.damageArmourIncrease();
            //if adding meat to stomach failed : turn damage-dealt into healing
            if (!c.addMeatMass(damageDealt / Stomach.meatMassToEnergy)) {
                if (c.health + damageDealt < c.maxHealth) {
                    c.health += damageDealt;
                } else {
                    c.energy += damageDealt;
                }
            }
        }
    }

    public Creature mate(Creature c) {
        seekingMate = false;
        return new Creature(this, c);
    }

    @Override
    public void damage(double damage) {
        if (damage - armourAvailable < 3) return;
        synchronized (this) {
            health -= damage - armourAvailable;
            genome.damageArmourIncrease();
        }
    }

    public boolean addPlantMass(double mass) {
        if (plantMass + mass + meatMass > stomachSize) return false;
        plantMass += mass;
        return true;
    }

    public boolean addMeatMass(double mass) {
        if (plantMass + mass + meatMass > stomachSize) return false;
        meatMass += mass;
        return true;
    }


    //accessor functions
    @Override
    public double getMass() {
        return size * Math.sqrt(size) * Movement.sizeMovementConstant * genome.armourMultiplier;
    }

    public Rectangle getEatingHitbox() {
        if (eating)
            return new Rectangle((int) Math.round(x + Math.cos(direction) * size * 2 / 3 - size / 3), (int) Math.round(y + Math.sin(direction) * size * 2 / 3 - size / 3), (int) (size * 2 / 3), (int) (size * 2 / 3));
        return null;
    }

    public Point[] getVisionRay() {
        double visionConeAngle = genome.visionConeAngle;
        return new Point[]{new Point((int) Math.round(x + Math.cos(direction - visionConeAngle * 0.5) * visionAvailable), (int) Math.round(y + Math.sin(direction - visionConeAngle * 0.5) * visionAvailable)),
                new Point((int) Math.round(x + Math.cos(direction + visionConeAngle * 0.5) * visionAvailable), (int) Math.round(y + Math.sin(direction + visionConeAngle * 0.5) * visionAvailable)),
                new Point((int) Math.round(x + Math.cos(direction) * visionAvailable), (int) Math.round(y + Math.sin(direction) * visionAvailable))};
    }

    @Override
    public double getEnergyIfConsumed() {
        return health + energy + size * genome.armourMultiplier + plantMass * Stomach.plantMassToEnergy + meatMass * Stomach.meatMassToEnergy;
    }

    public double getRotation() {
        return direction;
    }

    public double getDamage() {
        return strengthAvailable;
    }

    public NN getNN() {
        return brain;
    }

    public double getHealth() {
        return health;
    }

    public double getMaxEnergy() {
        return maxEnergy;
    }

    public double getEnergy() {
        return energy;
    }

    public CreatureGenome getGenome() {
        return genome;
    }

    public double getMaxHealth() {
        return maxHealth;
    }

    public double getArmour() {
        return armourAvailable;
    }

    public double getAngularSpeed() {
        return angularSpeed;
    }

    public double getMetabolism() {
        return deltaEnergy;
    }

    public double getForce() {
        return forceAvailable;
    }

    public boolean isSeekingMate() {
        return seekingMate;
    }

    public int getMaturity() {
        return maturity;
    }

    public double getPlantMass() {
        return plantMass;
    }

    public double getMeatMass() {
        return meatMass;
    }

    public double getStomachFluid() {
        return stomachFluid;
    }

    public double getStomachSize() {
        return stomachSize;
    }

    public boolean isStarving() {
        return starving;
    }

    public double[] getReport() {
        double[] report = new double[]{size, genome.minSize, genome.maxSize, genome.force, getVelocity(), energy, maxEnergy, genome.getReproductionCost(), genome.offspringInvestment, genome.incubationTime, health, maxHealth, maturity, genome.strength, genome.armour, genome.dietValue, genome.herbivoryAffinity, genome.carnivoryAffinity};
        for (int i = 0; i < report.length; i++)
            if (!Double.isFinite(report[i])) {
                System.out.println(WorldConstants.Settings.reportInfo[i] + " ERROR");
            }
        return report;
    }

    public ArrayList<Grid> getAllVisionGrids(){return allVisionGrids;}

    public void setNN(NN newNN) {
        brain = newNN;
    }

    @Override
    public void setSize(double newSize, Image image) {
        if (Math.round(newSize) != Math.round(size)) boundingBoxChange = true;
        super.setSize(Math.min(genome.maxSize, newSize), ImageConstants.getRotation(direction));
    }

    @Override
    public void reload(Image newImage) {
        super.reload(ImageConstants.getRotation(direction));
        this.brain.globalInnovations = World.globalInnovations;
        this.brain.globalNodes = World.globalNodes;
    }

    public void setEnergy(double newEnergy) {
        energy = Math.min(maxEnergy, newEnergy);
    }

    public void setHealth(double newHealth) {
        health = Math.min(maxHealth, newHealth);
    }
}