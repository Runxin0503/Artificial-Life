package Utils;

/** Stores a pair of Factory items. Should never be de-referenced once its created. */
public record Pair<T,R>(T first, R second) {
}
