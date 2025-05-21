package Entities.Creature;

import Utils.Constants.*;
import Utils.Constants.CreatureConstants.*;

class Stomach {


    private double stomachSize;
    private double plantMass;
    private double meatMass;
    private double stomachFluid;

    Stomach(double size) {
        reset(size);
    }

    void reset(double size) {
        stomachSize = Digestion.sizeToStomachSize(size);
        plantMass = 0;
        meatMass = 0;
        stomachFluid = 0;
    }

    double digest(double deltaStomachFluid) {
        //TODO implement
        throw new UnsupportedOperationException("Not supported yet.");
//        stomachFluid = Math.max(0, Math.min(stomachSize * Digestion.stomachSizeToMaxStomachFluid, stomachFluid + deltaStomachFluid));
//        double massDigested = stomachFluid;
//        if (massDigested == 0) return 0;
//
//        double nutrientsGained = 0;
//        boolean rand = Math.random() >= 0.5;
//
//        if (plantMass > 0 && rand) {
//            double min = Math.min(plantMass, massDigested * Digestion.plantDigestionRate);
//            plantMass -= min;
//            massDigested -= min / Digestion.plantDigestionRate;
//            nutrientsGained += min * Digestion.plantMassToEnergy * genome.herbivoryAffinity;
//        }
//        if (massDigested <= 0) return nutrientsGained;
//        if (meatMass > 0) {
//            double min = Math.min(meatMass, massDigested * Digestion.meatDigestionRate);
//            meatMass -= min;
//            massDigested -= min / Digestion.meatDigestionRate;
//            nutrientsGained += min * Digestion.meatMassToEnergy * genome.carnivoryAffinity;
//        }
//        if (massDigested <= 0) return nutrientsGained;
//        if (plantMass > 0 && !rand) {
//            double min = Math.min(plantMass, massDigested * Digestion.plantDigestionRate);
//            plantMass -= min;
//            massDigested -= min / Digestion.plantDigestionRate;
//            nutrientsGained += min * Digestion.plantMassToEnergy * genome.herbivoryAffinity;
//        }
//        return nutrientsGained;
    }
}
