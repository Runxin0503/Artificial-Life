package Entity.Movable;

import Constants.Constants.*;
import World.World;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class Corpse extends Movable implements Serializable {
    private double energy;
    private final double initialEnergy;
    private final ArrayList<Creature> queuedQuestionableMunching;

    public Corpse(Creature c){
        super(c.getX(),c.getY(),c.getSize(),ImageConstants.corpse);
        this.initialEnergy = c.getEnergyIfConsumed();
        this.energy = initialEnergy;
        this.speed = c.speed;
        this.queuedQuestionableMunching = new ArrayList<Creature>();
    }

    public void tick(World world){
        this.energy -= initialEnergy * CorpseConstants.corpseDecayRate;
        if(energy <= initialEnergy * CorpseConstants.corpseRottenPercentage || size < CorpseConstants.minCorpseSize){
            death(world);
            return;
        }
        clearQueue();
    }
    public void death(World world){
        world.remove.add(this);
    }
    private void clearQueue(){
        queuedQuestionableMunching.sort((o1, o2) -> (int)(100*(o1.getDamage()-o2.getDamage())));
        while(energy>0&&!queuedQuestionableMunching.isEmpty()){
            Creature c = queuedQuestionableMunching.remove(queuedQuestionableMunching.size()-1);
            double damageDealt = Math.min(energy,c.getDamage());
            if(c.addMeatMass(damageDealt/CreatureConstants.Stomach.meatMassToEnergy)) setEnergy(energy-damageDealt);
        }
        queuedQuestionableMunching.clear();
    }

    @Override
    public void creatureInteract(Creature c) {
        queuedQuestionableMunching.add(c);
    }

    @Override
    public void damage(double damage){}

    @Override
    public double getMass(){return size*size*CorpseConstants.sizeMovementConstant;}

    @Override
    public double getEnergyIfConsumed() {
        if(Double.isFinite(energy))
            return energy;
        return 0;
    }

    public double getEnergy(){
        return energy;
    }
    public void setEnergy(double newEnergy){
        if(newEnergy>initialEnergy)energy=initialEnergy;
        else energy=newEnergy;
    }
    public double getInitialEnergy() {
        return initialEnergy;
    }
}
