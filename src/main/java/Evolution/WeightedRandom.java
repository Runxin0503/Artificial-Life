package Evolution;

import java.util.List;


/** Any Object that extends this class can be used in {@link #getRandom}
 *  to pick a random Object with weighted probability. */
public interface WeightedRandom {

    /** Returns the score or {@code weight} of the specified class.
     * Higher the weight, higher the probability of being selected in {@link #getRandom}. */
    double getScore();

    /** Takes a list of objects that extends this class and randomly selects and returns
     * one such object with weighted probability. */
    static <T extends WeightedRandom> T getRandom(List<T> list) {
        if (list.isEmpty()) throw new IllegalArgumentException("List is empty");
        double totalValue = 0;
        for (T weightedRandom : list) totalValue += weightedRandom.getScore();
        if (totalValue == 0) return list.get((int) (Math.random() * list.size()));

        double randomValue = totalValue * Math.random();
        for (T weightedRandom : list) if ((randomValue -= weightedRandom.getScore()) < 0) return weightedRandom;

        throw new RuntimeException("Unexpected occurrence");
    }
}
