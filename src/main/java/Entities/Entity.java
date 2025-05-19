package Entities;

import Entities.Creature.Creature;
import Physics.Position;

/**
 * Contains all required methods of Entity class, used for subtyping and inheritance <br>
 * Stores a Position Object and a Health & Energy value
 */
public abstract class Entity extends Entities.EntityFactory.EntityFactoryObject {

    /** The Health / Energy of this Entity, has different meaning in different
     * classes but is a must-have for every Entity. */
    double health,energy;

    /** The size of this Entity */
    int size;

    public Entity(int id) {
        super(id);
    }

    /** Returns the Health of this Entity */
    public abstract int getHealth();

    /** Returns the Energy of this Entity */
    public abstract int getEnergy();

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

    public interface ReadOnlyEntity {}
}
