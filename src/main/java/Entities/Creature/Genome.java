package Entities.Creature;

import Utils.Constants.CreatureConstants.*;

import java.io.Serializable;

class Genome implements Serializable {
    /** Immutable value that only changes in the event of a {@linkplain #reset} call */
    int incubationTime;
    /** Immutable value that only changes in the event of a {@linkplain #reset} call */
    double growthWeight, growthBias;
    /** Immutable value that only changes in the event of a {@linkplain #reset} call */
    int visionDistance, visionRayCount;
    /** Immutable value that only changes in the event of a {@linkplain #reset} call */
    double visionConeAngle, visionValue;
    /** Immutable value that only changes in the event of a {@linkplain #reset} call */
    double carnivoryAffinity, herbivoryAffinity, dietValue;
    /** Immutable value that only changes in the event of a {@linkplain #reset} call */
    double boidSeparationWeight, boidAlignmentWeight, boidCohesionWeight;
    /** Immutable value that only changes in the event of a {@linkplain #reset} call */
    double offspringInvestment;
    /** Immutable value that only changes in the event of a {@linkplain #reset} call */
    double minSize, maxSize;
    /** Immutable value that only changes in the event of a {@linkplain #reset} call */
    double strength, force, armour, armourMultiplier;

    /** mutable value that changes whenever {@linkplain #updateCreatureSize} is called. */
    double visionAvailable, armourAvailable, forceAvailable, strengthAvailable;

    public Genome() {
        reset();
    }

    public void reset() {
        this.maxSize = Math.random() * (start.maxSize - (start.minSize / (1 - start.sizeDiff))) + (start.minSize / (1 - start.sizeDiff));
        this.minSize = Math.random() * (maxSize * (1 - start.sizeDiff) - start.minSize) + start.minSize;
        this.force = Math.random() * (Movement.sizeToMaxForce(maxSize) - start.minForce) + start.minForce;
        this.dietValue = Math.random() * (start.maxDietValue - start.minDietValue) + start.minDietValue;
        this.armour = start.armour;
        this.strength = start.strength;
        this.incubationTime = (int) Math.round(start.incubationTime * (1.5 - Math.random()));
        this.visionValue = Math.random() * (start.maxVisionValue - start.minVisionValue) + start.minVisionValue;
        this.offspringInvestment = Math.random() * (start.maxOffspringInvestment - start.minOffspringInvestment) + start.minOffspringInvestment;
        this.boidSeparationWeight = Math.random() * (start.maxSeparationWeight - start.minSeparationWeight) + start.minSeparationWeight;
        this.boidAlignmentWeight = Math.random() * (start.maxAlignmentWeight - start.minAlignmentWeight) + start.minAlignmentWeight;
        this.boidCohesionWeight = Math.random() * (start.maxCohesionWeight - start.minCohesionWeight) + start.minCohesionWeight;
        this.growthWeight = start.growthWeight * (Math.random() * 0.5 + 0.75);
        this.growthBias = start.growthBias * (Math.random() * 0.5 + 0.75);
        this.carnivoryAffinity = Energy.carnivoryAffinityFormula(dietValue);
        this.herbivoryAffinity = Energy.herbivoryAffinityFormula(dietValue);
        this.visionDistance = Vision.visionDistance(visionValue);
        this.visionConeAngle = Vision.visionConeAngle(visionValue);
        this.visionRayCount = Vision.visionRayCount(visionValue);
        this.armourMultiplier = Energy.armourGrowthMultiplier(armour);
    }

    public void reset(Genome dominant, Genome other) {
        this.maxSize = dominant.maxSize;
        this.force = Math.min(Movement.sizeToMaxForce(maxSize), dominant.force);
        this.minSize = dominant.minSize;
        this.strength = dominant.strength;
        this.incubationTime = dominant.incubationTime;
        this.growthWeight = dominant.growthWeight;
        this.growthBias = dominant.growthBias;
        this.visionValue = dominant.visionValue;
        this.dietValue = dominant.dietValue;
        this.armour = dominant.armour;
        this.offspringInvestment = dominant.offspringInvestment;
        this.boidSeparationWeight = dominant.boidSeparationWeight;
        this.boidAlignmentWeight = dominant.boidAlignmentWeight;
        this.boidCohesionWeight = dominant.boidCohesionWeight;
        mutateShift();
        mutateRandom(dominant, other);
        this.carnivoryAffinity = Energy.carnivoryAffinityFormula(dietValue);
        this.herbivoryAffinity = Energy.herbivoryAffinityFormula(dietValue);
        this.visionDistance = Vision.visionDistance(visionValue);
        this.visionConeAngle = Vision.visionConeAngle(visionValue);
        this.visionRayCount = Vision.visionRayCount(visionValue);
        this.armourMultiplier = Energy.armourGrowthMultiplier(armour);

    }

    private void mutateShift() {
        if (Math.random() < Reproduce.geneMutationShiftProbability)
            force = Math.max(Math.min(force + Reproduce.geneMutationShiftStrength * ((Math.random() - 0.5) * Movement.sizeToMaxForce(maxSize)), Movement.maxForce), 0);
        if (Math.random() < Reproduce.geneMutationShiftProbability / 2)
            maxSize = Math.max(Math.min(maxSize + Reproduce.geneMutationShiftStrength * ((Math.random() - 0.5) * (Reproduce.maxSize - Reproduce.minSize)), Reproduce.maxSize), (Reproduce.minSize / (1 - Reproduce.sizeDiff)));
        if (Math.random() < Reproduce.geneMutationShiftProbability / 2)
            minSize = Math.max(Math.min(minSize + Reproduce.geneMutationShiftStrength * ((Math.random() - 0.5) * (Reproduce.maxSize - Reproduce.minSize)), maxSize * (1 - Reproduce.sizeDiff)), Reproduce.minSize);
        if (Math.random() < Reproduce.geneMutationShiftProbability)
            strength = Math.max(Math.min(strength + Reproduce.geneMutationShiftStrength * ((Math.random() - 0.5) * (Combat.sizeToMaxStrength(maxSize) - Combat.minStrength)), Combat.sizeToMaxStrength(maxSize)), Combat.minStrength);
        if (Math.random() < Reproduce.geneMutationShiftProbability)
            armour = Math.max(Math.min(armour + Reproduce.geneMutationShiftStrength * ((Math.random() - 0.5) * (Combat.sizeToMaxArmour(maxSize) - Combat.minArmour)), Combat.sizeToMaxArmour(maxSize)), Combat.minArmour);
        if (Math.random() < Reproduce.geneMutationShiftProbability)
            incubationTime = Math.min(Reproduce.maxIncubationTime, Math.max(Reproduce.minIncubationTime, (int) Math.round(incubationTime + Reproduce.geneMutationShiftStrength * ((Math.random() - 0.5) * (Reproduce.maxIncubationTime - Reproduce.minIncubationTime)))));
        if (Math.random() < Reproduce.geneMutationShiftProbability / 4)
            growthWeight = Math.min(Math.max(growthWeight + Reproduce.geneMutationShiftStrength * ((Math.random() - 0.5) * (Energy.maxGrowthWeight - Energy.minGrowthWeight)), Energy.minGrowthWeight), Energy.maxGrowthWeight);
        if (Math.random() < Reproduce.geneMutationShiftProbability / 4)
            growthBias = Math.min(Math.max(growthBias + Reproduce.geneMutationShiftStrength * ((Math.random() - 0.5) * (Energy.maxGrowthBias - Energy.minGrowthBias)), Energy.minGrowthBias), Energy.maxGrowthBias);
        if (Math.random() < Reproduce.geneMutationShiftProbability)
            visionValue = Math.min(Math.max(visionValue + Reproduce.geneMutationShiftStrength * ((Math.random() - 0.5) * (Vision.maxVisionValue - Vision.minVisionValue)), Vision.minVisionValue), Vision.maxVisionValue);
        if (Math.random() < Reproduce.geneMutationShiftProbability)
            dietValue = Math.min(Math.max(dietValue + Reproduce.geneMutationShiftStrength * ((Math.random() - 0.5)), Energy.minDietValue), Energy.maxDietValue);
        if (Math.random() < Reproduce.geneMutationShiftProbability)
            offspringInvestment = Math.min(Math.max(offspringInvestment + Reproduce.geneMutationShiftStrength * ((Math.random() - 0.5) * (Reproduce.maxOffspringInvestment - Reproduce.minOffspringInvestment)), Reproduce.minOffspringInvestment), Reproduce.maxOffspringInvestment);
        if (Math.random() < Reproduce.geneMutationShiftProbability)
            boidSeparationWeight = Math.min(Math.max(boidSeparationWeight + Reproduce.geneMutationShiftStrength * ((Math.random() - 0.5) * (Boids.maxSeparationWeight - Boids.minSeparationWeight)), Boids.minSeparationWeight), Boids.maxSeparationWeight);
        if (Math.random() < Reproduce.geneMutationShiftProbability)
            boidAlignmentWeight = Math.min(Math.max(boidAlignmentWeight + Reproduce.geneMutationShiftStrength * ((Math.random() - 0.5) * (Boids.maxAlignmentWeight - Boids.minAlignmentWeight)), Boids.minAlignmentWeight), Boids.maxAlignmentWeight);
        if (Math.random() < Reproduce.geneMutationShiftProbability)
            boidCohesionWeight = Math.min(Math.max(boidCohesionWeight + Reproduce.geneMutationShiftStrength * ((Math.random() - 0.5) * (Boids.maxCohesionWeight - Boids.minCohesionWeight)), Boids.minCohesionWeight), Boids.maxCohesionWeight);
    }

    private void mutateRandom(Genome first, Genome second) {
        if (Math.random() < Reproduce.geneMutationRandomProbability)
            force = Math.min(Movement.sizeToMaxForce(maxSize), Math.random() * Math.abs(first.force - second.force) + Math.min(first.force, second.force));
        if (Math.random() < Reproduce.geneMutationRandomProbability / 2)
            maxSize = Math.max(Reproduce.minSize / (1 - Reproduce.sizeDiff), Math.random() * Math.abs(first.maxSize - second.maxSize) + Math.min(first.maxSize, second.maxSize));
        if (Math.random() < Reproduce.geneMutationRandomProbability / 2)
            minSize = Math.min(maxSize * (1 - Reproduce.sizeDiff), Math.random() * Math.abs(first.minSize - second.minSize) + Math.min(first.minSize, second.minSize));
        if (Math.random() < Reproduce.geneMutationRandomProbability)
            strength = Math.min(Combat.sizeToMaxStrength(maxSize), Math.random() * Math.abs(first.strength - second.strength) + Math.min(first.strength, second.strength));
        if (Math.random() < Reproduce.geneMutationRandomProbability)
            armour = Math.min(Combat.sizeToMaxArmour(maxSize), Math.random() * Math.abs(first.armour - second.armour) + Math.min(first.armour, second.armour));
        if (Math.random() < Reproduce.geneMutationRandomProbability)
            incubationTime = (int) (Math.random() * Math.abs(first.incubationTime - second.incubationTime) + Math.min(first.incubationTime, second.incubationTime));
        if (Math.random() < Reproduce.geneMutationRandomProbability / 4)
            growthWeight = Math.random() * Math.abs(first.growthWeight - second.growthWeight) + Math.min(first.growthWeight, second.growthWeight);
        if (Math.random() < Reproduce.geneMutationRandomProbability / 4)
            growthBias = Math.random() * Math.abs(first.growthBias - second.growthBias) + Math.min(first.growthBias, second.growthBias);
        if (Math.random() < Reproduce.geneMutationRandomProbability)
            visionValue = Math.random() * Math.abs(first.visionValue - second.visionValue) + Math.min(first.visionValue, second.visionValue);
        if (Math.random() < Reproduce.geneMutationRandomProbability)
            dietValue = Math.random() * Math.abs(first.dietValue - second.dietValue) + Math.min(first.dietValue, second.dietValue);
        if (Math.random() < Reproduce.geneMutationRandomProbability)
            offspringInvestment = Math.random() * Math.abs(first.offspringInvestment - second.offspringInvestment) + Math.min(first.offspringInvestment, second.offspringInvestment);
        if (Math.random() < Reproduce.geneMutationRandomProbability)
            boidSeparationWeight = Math.random() * Math.abs(first.boidSeparationWeight - second.boidSeparationWeight) + Math.min(first.offspringInvestment, second.offspringInvestment);
        if (Math.random() < Reproduce.geneMutationRandomProbability)
            boidAlignmentWeight = Math.random() * Math.abs(first.boidAlignmentWeight - second.boidAlignmentWeight) + Math.min(first.offspringInvestment, second.offspringInvestment);
        if (Math.random() < Reproduce.geneMutationRandomProbability)
            boidCohesionWeight = Math.random() * Math.abs(first.boidCohesionWeight - second.boidCohesionWeight) + Math.min(first.offspringInvestment, second.offspringInvestment);
    }

    public double updateCreatureSize(int maturity) {
        // update mutable fields like armourAvailable, forceAvailable, strengthAvailable, visionAvailable, etc.
        double size = Energy.maturingSizeFormula(maturity, minSize, maxSize, growthWeight, growthBias);
        forceAvailable = force / Movement.sizeToMaxForce(maxSize) * Movement.sizeToMaxForce(size);
        armourAvailable = armour / Combat.sizeToMaxArmour(maxSize) * Combat.sizeToMaxArmour(size);
        strengthAvailable = strength / Combat.sizeToMaxStrength(maxSize) * Combat.sizeToMaxStrength(size);
        visionAvailable = Math.round(Math.min(Vision.maxVisionDistance, visionDistance * Reproduce.scalesWithMaturity(minSize, maxSize, size) + size));
        return size;
    }

    public void biteStrengthIncrease() {
        this.strength = Math.min(Combat.sizeToMaxStrength(maxSize), strength + Combat.biteStrengthIncrease);
    }

    public void damageArmourIncrease() {
        this.armour = Math.min(Combat.sizeToMaxArmour(maxSize), armour + Combat.damageArmourIncrease);
    }

    public double getReproductionCost() {
        return Combat.sizeToMaxHealth(minSize) * offspringInvestment * 2 + Energy.sizeToMaxEnergyFormula(minSize) * offspringInvestment * 2 + Energy.armourGrowthMultiplier(armour) * (minSize - Reproduce.babySize);
    }
}
