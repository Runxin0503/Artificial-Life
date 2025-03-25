package Specs.World;

import Specs.Reloadable;

import java.io.Serializable;

/** An instance of a simulated environment, where all entities are stored */
public interface AbstractWorld extends Serializable, Reloadable {
    /*
     * Must have
     * - An ExecutorService for multi-threading, maybe initialized within constructor
     * - An AbstractEntityFactory to ensure all currently used entities are serialized
     * - An AbstractDatabase that stores all globalInnovation and globalNodes
     * - An AbstractConstants that stores all Constants value
     * -
     */

    /**
     *
     */
    void tick();
}
