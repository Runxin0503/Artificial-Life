package Specs.Entity;

import Entity.Movable.Creature;
import Specs.Physics.AbstractPosition;
import Specs.Reloadable;

import java.io.Serializable;

/**
 * Contains all required methods of Entity class, used for subtyping and inheritance <br>
 * Stores a Position Object and a Health & Energy value
 */
public interface AbstractEntity extends Serializable, Reloadable, AbstractEntityFactory.AbstractFactoryObject {

    /*
     * Must have
     * - A Position Object
     * - An energy and health value
     * - All functions an entity must have
     */

    /** Returns the Health of this Entity */
    int getHealth();

    /** Returns the Energy of this Entity */
    int getEnergy();

    /** Returns the Position Object of this Entity */
    AbstractPosition getPosition();

    /** Returns the max energy a creature can theoretically extract from this Entity through digestion before this Entity dies */
    double getEnergyIfConsumed();

    /**
     * Allow the Entity to perform all actions in a tick<br>
     * Returns true if this Entity has to be removed
     */
    boolean tick();

    /** Dictates what happens when a Creature's mouth hitbox intersects with this object */
    void creatureInteract(Creature c);
}
