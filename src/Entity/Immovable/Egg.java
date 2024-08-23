package Entity.Immovable;

import java.io.Serializable;

import Constants.Constants.*;
import Entity.Movable.Creature;
import Entity.Movable.Movable;
import World.World;

public class Egg extends Immovable implements Serializable {
    private final Creature c;
    private int timeCount;
    private final double invIncubationTime;
    private boolean isEaten = false;

    public Egg(Creature c,double x,double y){
        super(x,y,c.getGenome().minSize,ImageConstants.egg);
        this.c = c;
        this.invIncubationTime =1.0/c.getGenome().incubationTime;
    }
    public Egg(Creature c){
        this(c,c.getCoord().x,c.getCoord().y);
    }
    public void tick(World world){
        if(isEaten) world.remove.add(this);
        timeCount++;
        if(timeCount*invIncubationTime>1)hatch(world);
    }

    private void hatch(World world){
        world.remove.add(this);
        if(Math.random()< CreatureConstants.Reproduce.rottenEggPerct)return;
        Creature birth = c;
        for(int i=0;i<1;i++){
            world.add.add(birth);
            if(Math.random()< CreatureConstants.Reproduce.multipleEmbryoPerct){
                i--;
                birth = birth.mate(birth);
            }
        }
    }


    @Override
    public double getEnergyIfConsumed() {
        return c.getEnergyIfConsumed()*timeCount*invIncubationTime;
    }
    @Override
    public void creatureInteract(Creature c) {
        //if it hasn't been eaten already and the creature has space for the egg's meat-mass : delete itself
        if(!isEaten && !c.addMeatMass(getEnergyIfConsumed() / CreatureConstants.Stomach.meatMassToEnergy))
            isEaten = true;
    }

    @Override
    public Vector2D collision(Movable m) {
        damage(m.getVelocity()*m.getMass()*CreatureConstants.Movement.momentumToDamage);
        if(!isEaten) {
            isEaten = true;
            return new Vector2D(m.getVelocityVector().x * -0.5, m.getVelocityVector().x * -0.5);
        }
        return null;
    }

    @Override
    public void damage(double damage){
        if(c.getHealth()<damage)isEaten = true;
        else c.setHealth(c.getHealth()-damage);
    }

    @Override
    public double getMass(){
        return c.getMass() * timeCount*invIncubationTime;
    }

    public int getTimeLeft(){
        return (int)Math.round(1/invIncubationTime-timeCount);
    }

    public void setInvIncubationTime(int i) {
        this.timeCount = (int)Math.round(1/invIncubationTime)-i;
    }
}
