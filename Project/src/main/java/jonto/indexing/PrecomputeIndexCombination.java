package jonto.indexing;

import java.util.*;


public class PrecomputeIndexCombination {
    private final List<Map<Integer, Set<Set<Integer>>>> precomputedCombinations = new ArrayList<Map<Integer, Set<Set<Integer>>>>();

    private final Set<Set<Integer>> identifierCombination = new HashSet<Set<Integer>>();
    private final Set<Integer> combination = new HashSet<Integer>();

    private int size_combination = 3;

    private int size_input = 10;


    public void clearCombinations() {
        precomputedCombinations.clear();
    }


    public Set<Set<Integer>> getIdentifierCombination(int size_object, int size_combo) {
        if (precomputedCombinations.size() <= size_combo - 1) {
            extractIdentifierCombination(size_object, size_combo);

            precomputedCombinations.add(size_combo - 1, new HashMap<Integer, Set<Set<Integer>>>());
            precomputedCombinations.get(size_combo - 1).put(size_object, new HashSet<Set<Integer>>(identifierCombination));

        } else if (!precomputedCombinations.get(size_combo - 1).containsKey(size_object)) {
            extractIdentifierCombination(size_object, size_combo);

            precomputedCombinations.get(size_combo - 1).put(size_object, new HashSet<Set<Integer>>(identifierCombination));
        }

        return precomputedCombinations.get(size_combo - 1).get(size_object);
    }


    public void setDefaultSizes(int sizeinput, int sizecombination) {
        size_input = sizeinput;
        size_combination = sizecombination;
    }


    public void preComputeIdentifierCombination() {
        for (int j = 1; j <= size_combination; j++) {
            precomputedCombinations.add(j - 1, new HashMap<Integer, Set<Set<Integer>>>());

            for (int i = 2; i <= size_input; i++) {
                extractIdentifierCombination(i, j);

                precomputedCombinations.get(j - 1).put(i, new HashSet<Set<Integer>>(identifierCombination));


            }

        }
    }

    private void extractIdentifierCombination(int numElements, int sizeCombinations) {
        identifierCombination.clear();
        combination.clear();
        if (numElements == sizeCombinations) {
            for (int i = 0; i < numElements; i++) {
                combination.add(i);
            }
            identifierCombination.add(new HashSet<Integer>(combination));
        } else {
            extractIdentifierCombination(numElements, sizeCombinations, 0, 0);
        }
    }


    public Set<Integer> getMaxCombinationSet(int size) {
        combination.clear();
        for (int i = 0; i < size; i++) {
            combination.add(i);
        }

        return combination;

    }

    private void extractIdentifierCombination(int numElements, int sizeCombinations, int current_number, int level) {
        for (int i = current_number; i < numElements; i++) {
            if (level == sizeCombinations - 1) {
                combination.add(i);
                identifierCombination.add(new HashSet<Integer>(combination));
                combination.remove(i);
            } else {
                combination.add(i);
                extractIdentifierCombination(numElements, sizeCombinations, i + 1, level + 1);
                combination.remove(i);
            }
        }
    }
}
