package Entity;

import Constants.Constants.ImageConstants;
import Constants.Constants.Vector2D;
import Constants.Constants.WorldConstants;
import Entity.Movable.Creature;
import Entity.Movable.Movable;
import World.GridList;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class Entity implements Serializable {
    protected final Rectangle boundingBox = new Rectangle();
    private transient Image image;
    private final Rectangle prevBoundingBox = new Rectangle();
    protected transient boolean boundingBoxChange = true;
    private transient List<GridList.Grid> occupiedGrids = new ArrayList<>();
    protected double size = 0, x, y;

    public Entity(double x, double y) {
        setCoord(x, y);
    }

    public Entity(double x, double y, double size, Image image) {
        this(x, y);
        setSize(size, image);
    }

    public int getX() {
        return (int) Math.round(x);
    }

    public int getY() {
        return (int) Math.round(y);
    }

    public List<GridList.Grid> getOccupiedGrids() {
        return occupiedGrids;
    }

    public Rectangle getPrevBoundingBox() {
        return prevBoundingBox;
    }

    public boolean isBoundingBoxChange() {
        if (boundingBoxChange) {
            boundingBoxChange = false;
            return true;
        }
        return false;
    }

    public void setCoord(double newX, double newY) {
        this.x = newX;
        this.y = newY;
        if (x < 0) x += WorldConstants.xBound;
        if (y < 0) y += WorldConstants.yBound;
        if (x > WorldConstants.xBound) x -= WorldConstants.xBound;
        if (y > WorldConstants.yBound) y -= WorldConstants.yBound;
        this.boundingBox.setLocation((int) x - boundingBox.width / 2, (int) y - boundingBox.height / 2);
    }

    public void setSize(double newSize, Image newImage) {
        int tempSizeRound = (int) Math.round(newSize);
        if ((int) Math.round(size) != tempSizeRound) {
            boundingBox.setRect((int) x - tempSizeRound / 2, (int) y - tempSizeRound / 2, tempSizeRound, tempSizeRound);
            image = newImage.getScaledInstance(boundingBox.width, boundingBox.height, ImageConstants.ResizeConstant);
        }
        this.size = newSize;
    }

    public void reload(Image newImage) {
        if (newImage != null)
            image = newImage.getScaledInstance(boundingBox.width, boundingBox.height, ImageConstants.ResizeConstant);
        this.occupiedGrids = new ArrayList<>();
        this.boundingBoxChange = true;
    }

    public Rectangle getBoundingBox() {
        return boundingBox;
    }

    public Image getImage() {
        return image;
    }

    public double getSize() {
        return size;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public abstract Vector2D collision(Movable m);

    public abstract void damage(double damage);

    public abstract double getMass();

    public abstract double getEnergyIfConsumed();

    public abstract void creatureInteract(Creature c);
}