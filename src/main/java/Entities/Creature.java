package Entities;


import Physics.Position;
import javafx.scene.shape.Rectangle;

/** A Creature Entity that harbors a brain, stomach, and vision system and is the main
 * living organism of this world.<br>
 * Loses energy passively and from exerting force to move around.<br>
 * Can interact with any other {@link Entity} class and gain energy.<br>
 * Able to reproduce by laying {@link Egg} that incubates into more Creatures.<br>
 * Energy --> Simply Energy<br>
 * Health --> Simply Health
 */
public class Creature extends Entity {


    public Creature(int id) {
        super(id);
    }

    void instantiate(Creature creature) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** Modifies {@code rect} to match the hit-box size and position of this Creature's mouth. */
    public void getEatingHitbox(Rectangle rect, Position pos) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getHealth() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getEnergy() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double getEnergyIfConsumed() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean tick() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void creatureInteract(Creature c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void reset() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
