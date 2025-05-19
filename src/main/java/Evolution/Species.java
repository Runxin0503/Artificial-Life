package Evolution;

import Entities.Creature.Creature;

import java.util.ArrayList;

/**
 * Used in a part of the NEAT algorithm to determine if a genome is successful or not based on its species
 * <br>Randomly selects a representative from its population every generation to use as comparison during
 * species identification
 */
class Species {
    /** The representative of this species. Used during species identification and randomly chosen every new generation */
    private Creature representative;

    /** Arraylist containing all members of this species */
    private final ArrayList<Creature> population = new ArrayList<Creature>();

    private final Constants Constants;

    public Species(Creature representative, Constants Constants) {
        this.representative = representative;
        this.population.add(representative);
        this.Constants = Constants;
    }

    /**
     * Attempts to add {@code newAgent} to this species if it's genome is similar enough
     * @return true if successful, false otherwise
     */
    public boolean add(Creature newAgent) {
        if (representative.compare(newAgent) < Constants.compatibilityThreshold) {
            population.add(newAgent);
            return true;
        }
        return false;
    }

    /** Removes {@code agent} from the current population.
     * <br>Selects a new representative if {@code agent} == {@code representative}.  */
    public boolean remove(Creature agent) {
        boolean contains = population.remove(agent);
        if (representative == agent)
            representative = population.isEmpty() ? null : population.get((int)(Math.random() * population.size()));
        return contains;
    }

    /**
     * Returns if this species has no current members
     */
    public boolean isEmpty() {
        return population.isEmpty();
    }
}
