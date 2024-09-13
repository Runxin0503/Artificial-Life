package Entity.Immovable;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

import Constants.Constants.*;
import Entity.Entity;
import Entity.Movable.Creature;
import Entity.Movable.Movable;

public class Bush extends Immovable implements Serializable {
    private final ArrayList<Point> berries=new ArrayList<Point>();
    private int maxBerries;
//    private double energy,roots,leaves;
    private final ArrayList<Creature> queuedBerryEating;
    public Bush(Point coord){
        this(coord.x,coord.y);
    }
    public Bush(int x,int y){
        super(x,y);
        setSize((int)(Math.random()*(BushConstants.initialMaxSize-BushConstants.initialMinSize))+BushConstants.initialMinSize,ImageConstants.bush);
        this.maxBerries = (int)(size /BushConstants.initialMaxSize*BushConstants.maxBerries);
        int currentBerries = (int)(Math.random()*maxBerries);
        for(int i=0;i<currentBerries;i++)berries.add(new Point((int)(Math.random()*(getBoundingBox().width - ImageConstants.berriesWidth)),(int)(Math.random()*(getBoundingBox().height - ImageConstants.berriesHeight))));
        this.queuedBerryEating = new ArrayList<Creature>();
    }

    @Override
    public void creatureInteract(Creature c){
        if(!berries.isEmpty()&&c!=null){
            queuedBerryEating.add(c);
        }
    }

    private void clearQueue(){
        queuedBerryEating.sort((o1, o2) -> (int) (100 * (o1.getSize() - o2.getSize())));
        while(!berries.isEmpty()&&!queuedBerryEating.isEmpty()){
            Creature c = queuedBerryEating.remove(queuedBerryEating.size()-1);
            if(c.addPlantMass(BushConstants.energy/CreatureConstants.Stomach.plantMassToEnergy)) berries.remove(berries.size()-1);
        }
        queuedBerryEating.clear();
    }

    public void tick(){
        if(berries.size()<maxBerries&&Math.random()<BushConstants.berriesGrowProbability*maxBerries/WorldConstants.Settings.ticksToSecond){//&&energy>=BushConstants.energy
//            energy-=BushConstants.energy;
            growBerry();
        }
        clearQueue();
//        energy+=roots/(neighbors+1)+leaves*getArea();
    }
    public void growBerry(){// 1 * x * 20 * 4 < 1.2       80 * x = 1.2
        berries.add(new Point((int)(Math.random()*getBoundingBox().width),(int)(Math.random()*getBoundingBox().height)));
    }

    @Override
    public double getEnergyIfConsumed() {
        return berries.size()*BushConstants.energy;
    }
    public ArrayList<Point> getBerries(){
        return berries;
    }
    public int getMaxBerries(){
        return maxBerries;
    }

    @Override
    public void setSize(double newSize,Image image) {
        size = newSize;
        int width=(int)Math.round(size*BushConstants.sizeToWidth),height=(int)Math.round(size*BushConstants.sizeToHeight);
        getBoundingBox().setBounds((int)Math.round(x-width/2.0),(int)Math.round(y-height/2.0),width,height);
        setImage(ImageConstants.bush.getScaledInstance(width,height,ImageConstants.ResizeConstant));
        this.maxBerries = (int)(size / BushConstants.initialMaxSize*BushConstants.maxBerries);
    }

    @Override
    public void reload(Image newImage){
        super.reload(null);
        setImage(ImageConstants.bush.getScaledInstance(getBoundingBox().width,getBoundingBox().height,ImageConstants.ResizeConstant));
    }

    @Override
    public Vector2D collision(Movable m) {
        return new Vector2D(m.getVelocityVector().x*-Physics.bushFriction,m.getVelocityVector().y*-Physics.bushFriction);
    }

    @Override
    public void damage(double damage){

    }

    @Override
    public double getMass(){
        return size * size * CreatureConstants.Movement.sizeMovementConstant;
    }
}
