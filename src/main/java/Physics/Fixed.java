package Physics;

import Utils.Constants;
import Utils.Rectangle;
import Utils.Vector2D;

import java.awt.*;

public class Fixed extends Position {

    public Fixed(int id, double widthToHeight, Image image) {
        super(id, widthToHeight, image);
    }

    @Override
    boolean isBoundingBoxChange() {
        return false;
    }

    @Override
    double getMass() {
        return sizeToMass * boundingBox.width * boundingBox.height;
    }

    @Override
    public double getDamage() {
        return damage;
    }

    public void setSize(double newSize) {
        throw new UnsupportedOperationException("Fixed Position is immutable");
    }

    @Override
    Vector2D collision(Dynamic movable) {
        double minSpeedX = 0, minSpeedY = 0;
        Rectangle eBoundingBox = movable.boundingBox;
        Rectangle boundingBox = this.boundingBox;
        if (Math.min(Math.abs(eBoundingBox.getMinX() - boundingBox.getMaxX()), Math.abs(eBoundingBox.getMaxX() - boundingBox.getMinX())) < Math.min(Math.abs(eBoundingBox.getMinY() - boundingBox.getMaxY()), Math.abs(eBoundingBox.getMaxY() - boundingBox.getMinY()))) {
            if (Math.abs(eBoundingBox.getMinX() - boundingBox.getMaxX()) < Math.abs(eBoundingBox.getMaxX() - boundingBox.getMinX()))
                minSpeedX = eBoundingBox.getMinX() - boundingBox.getMaxX();
            else
                minSpeedX = eBoundingBox.getMaxX() - boundingBox.getMinX();
        } else {
            if (Math.abs(eBoundingBox.getMinY() - boundingBox.getMaxY()) < Math.abs(eBoundingBox.getMaxY() - boundingBox.getMinY()))
                minSpeedY = eBoundingBox.getMinY() - boundingBox.getMaxY();
            else
                minSpeedY = eBoundingBox.getMaxY() - boundingBox.getMinY();
        }
        //No need for elastic collision calculation, invert velocity directly
        Vector2D otherSpeed = movable.getVelocity().multiplied(-Constants.Physics.impulseVelocityLoss).minVectored(-minSpeedX, -minSpeedY);

        double mass = movable.getMass() + getMass();
        double damage = movable.getMass() * movable.getVelocity().length() * Constants.CreatureConstants.Movement.momentumToDamage;
        movable.damage += damage * getMass() / mass;
        this.damage += damage * movable.getMass() / mass;

        return otherSpeed;
    }

    protected void reset() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
