package Entities;

import Entities.Creature.Creature;
import Physics.Position;

/**
 * Contains all required methods of Entity class, used for subtyping and inheritance. <br>
 */
public abstract class Entity extends Entities.EntityFactory.EntityFactoryObject {

    public Entity(int id) {
        super(id);
    }

    /** Returns the max energy a creature can theoretically extract from this Entity through digestion before this Entity dies */
    public abstract double getEnergyIfConsumed();

    /**
     * Allow the Entity to perform all actions in a tick<br>
     * Returns true if this Entity has to be removed.
     */
    public abstract boolean tick();

    /** Dictates what happens when a Creature's mouth hitbox intersects with this object */
    public abstract void creatureInteract(Creature c);

    /** Gets a Read-only copy of this Current Entity, the Read-only copy should contain
     * all the information about this Entity including its positional data. */
    public abstract ReadOnlyEntity getReadOnlyCopy(Position pos);

    /** The Read-Only class that all entity's read-only copy should inherit from
     * to allow for casting and storing in {@linkplain Physics.GridWorld.ReadOnlyWorld}. */
    public interface ReadOnlyEntity {
        int x();

        int y();

        int width();

        int height();

        int getX();

        int getY();

        int ID();
    }
}
