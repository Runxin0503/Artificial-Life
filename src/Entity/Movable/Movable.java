package Entity.Movable;

import Constants.Constants.*;
import Entity.Entity;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;

public abstract class Movable extends Entity implements Serializable {
    Vector2D speed;
    public Movable(double x,double y){
        super(x,y);
        this.speed=new Vector2D();
    }
    public Movable(double x, double y, double size, Image image){
        super(x,y,size,image);
    }

    public void updatePos(){
        setCoord(x + speed.x / WorldConstants.Settings.ticksToSecond,y + speed.y / WorldConstants.Settings.ticksToSecond);
    }
    public void friction(){
        speed.multiply(1-Physics.frictionPerpendicular/size/ WorldConstants.Settings.ticksToSecond)
                .max(WorldConstants.Settings.maxSpeed);
        if(speed.x<0.1)speed.x=0;
        if(speed.y<0.1)speed.y=0;
    }
    public double getVelocity(){return speed.length();}
    public Vector2D getVelocityVector(){return speed;}
    public Vector2D collision(Movable m){
        if(m.equals(this))return null;
        double minSpeedX=0,minSpeedY=0;
        Rectangle eBoundingBox = m.getBoundingBox();
        Rectangle boundingBox = getBoundingBox();
        if(Math.min(Math.abs(eBoundingBox.getMinX()-boundingBox.getMaxX()),Math.abs(eBoundingBox.getMaxX()-boundingBox.getMinX()))<Math.min(Math.abs(eBoundingBox.getMinY()-boundingBox.getMaxY()),Math.abs(eBoundingBox.getMaxY()-boundingBox.getMinY()))) {
            if(Math.abs(eBoundingBox.getMinX()-boundingBox.getMaxX())<Math.abs(eBoundingBox.getMaxX()-boundingBox.getMinX()))
                minSpeedX = eBoundingBox.getMinX() - boundingBox.getMaxX();
            else
                minSpeedX = eBoundingBox.getMaxX() - boundingBox.getMinX();
        }else{
            if(Math.abs(eBoundingBox.getMinY()-boundingBox.getMaxY())<Math.abs(eBoundingBox.getMaxY()-boundingBox.getMinY()))
                minSpeedY = eBoundingBox.getMinY() - boundingBox.getMaxY();
            else
                minSpeedY = eBoundingBox.getMaxY() - boundingBox.getMinY();
        }
        Vector2D[] velocities = Physics.elasticCollision(getMass(),m.getMass(),speed,m.speed);
        Vector2D speed = velocities[0].multiply(Physics.impulseVelocityLoss).minVector(minSpeedX,minSpeedY);
        Vector2D otherSpeed = velocities[1].multiply(Physics.impulseVelocityLoss).minVector(-minSpeedX,-minSpeedY);

        double mass = m.getMass() + getMass();
        double damage = (getMass() * this.speed.length() + m.getMass() * m.speed.length()) * CreatureConstants.Movement.momentumToDamage;
        m.damage(damage * getMass()/mass);
        damage(damage * m.getMass()/mass);

        this.speed = speed;
        return otherSpeed;
    }
    public void physics(ArrayList<ArrayList<ArrayList<Entity>>> nearbyEntities) {
        Vector2D deltaSpeed = new Vector2D();
        for(ArrayList<ArrayList<Entity>> ess : nearbyEntities) for(ArrayList<Entity> es : ess) for(Entity e : es) if(!e.equals(this)&&getBoundingBox().intersects(e.getBoundingBox())){
            Vector2D impulseVector = e.collision(this);
            if(impulseVector!=null) deltaSpeed.add(impulseVector);
        }
        speed.add(deltaSpeed);
    }
    @Override
    public abstract void damage(double damage);

    @Override
    public abstract double getMass();

    @Override
    public abstract double getEnergyIfConsumed();

    @Override
    public abstract void creatureInteract(Creature c);
}
