package Specs;

/**
 * Serializable classes can implement this class to call reload() in order to 'fill in' any missing instance variables that weren't serialized
 */
@FunctionalInterface
public interface Reloadable {

    /** Reloads and instantiate any transient values / maintain any class invariant */
    void reload();
}
