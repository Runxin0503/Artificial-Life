package Entities.Creature;

import Utils.Constants.*;
import Utils.Constants.CreatureConstants.*;

class Stomach {

    /** TODO document */
    private double stomachSize;

    /** TODO document */
    private double plantMass;

    /** TODO document */
    private double meatMass;

    /** TODO document */
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

    /** TODO document */
    void updateSize(double size) {
        // TODO implement
    }

    /** TODO document */
    double digest(double deltaStomachFluid) {
        //TODO implement
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
