package Entity.Immovable;

import Entity.Entity;

import java.awt.*;
import java.io.Serializable;

public abstract class Immovable extends Entity implements Serializable {
    public Immovable(double x, double y) {
        super(x, y);
    }

    public Immovable(double x, double y, double size, Image image) {
        super(x, y, size, image);
    }
}
