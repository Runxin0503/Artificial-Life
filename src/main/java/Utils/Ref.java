package Utils;

import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * A thread-safe mutable reference wrapper.
 * <p>
 * This class allows encapsulating a reference to an object,
 * which can be modified or cleared after initialization.
 * It is useful for simulating pass-by-reference semantics
 * or holding optional state in Java.
 *
 * @param <T> the type of the referenced value
 */
public class Ref<T> {
    private T value;
    private ArrayList<Consumer<T>> onUpdates;

    /**
     * Constructs a new {@code Ref} with the given initial value.
     *
     * @param value the initial value to reference
     */
    public Ref(T value) {
        this.value = value;
        this.onUpdates = new ArrayList<>();
    }

    /**
     * Returns the current value of this reference.
     *
     * @return the referenced value, or {@code null} if cleared
     */
    public synchronized T get() {
        return value;
    }

    /**
     * Sets or replaces the referenced value.
     *
     * @param value the new value to reference
     */
    public synchronized void set(T value) {
        this.value = value;
        onUpdates.forEach(c -> c.accept(value));
    }

    /**
     * Clears the referenced value by setting it to {@code null}.
     */
    public synchronized void clear() {
        this.set(null);
    }

    /**
     * Returns whether this referenced value is null or not.
     */
    public synchronized boolean isEmpty() {
        return value == null;
    }

    /**
     * Registers a callback to be run whenever this Ref's value is updated via {@link #set(Object)}.
     *
     * @param onUpdate the consumer to be triggered on value updates
     */
    public synchronized void onUpdate(Consumer<T> onUpdate) {
        this.onUpdates.add(onUpdate);
    }
}
