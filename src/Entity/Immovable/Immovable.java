package Entity.Immovable;

import Constants.Constants;
import Entity.*;
import Entity.Movable.Creature;
import Entity.Movable.Movable;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.Serializable;

public abstract class Immovable extends Entity implements Serializable {
    public Immovable(double x,double y){
        super(x,y);
    }
    public Immovable(double x, double y, double size, Image image){
        super(x,y,size,image);
    }
}
