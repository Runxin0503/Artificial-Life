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

    /** TODO document */
    double digest(double deltaStomachFluid) {
        //TODO implement
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** TODO document */
    void updateSize() {
        // TODO implement
    }
}
