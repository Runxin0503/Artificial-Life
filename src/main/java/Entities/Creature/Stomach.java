package Entities.Creature;

import Utils.Constants.CreatureConstants.Digestion;

class Stomach {

    /** TODO document */
    double stomachSize;

    /** TODO document */
    double plantMass;

    /** TODO document */
    double meatMass;

    /** TODO document */
    double stomachFluid;

    Stomach(double size) {
        reset(size);
    }

    void reset(double size) {
        stomachSize = Digestion.sizeToStomachSize(size);
        plantMass = 0;
        meatMass = 0;
        stomachFluid = 0;
    }

    /** TODO document */
    void updateSize(double size) {
        // TODO implement
    }

    /** TODO document */
    double digest(double deltaStomachFluid) {
        //TODO implement
        throw new UnsupportedOperationException("Not supported yet.");
    }

    boolean isStarving() {
        return (plantMass + meatMass) / stomachSize < 0.1;
    }
}
