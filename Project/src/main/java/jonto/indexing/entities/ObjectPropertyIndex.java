package jonto.indexing.entities;

import java.util.HashSet;
import java.util.Set;

public class ObjectPropertyIndex extends PropertyIndex {
    Set<Integer> range = new HashSet<Integer>();

    public ObjectPropertyIndex(int index) {
        super(index, PropertyIndex.OBJECTPROPERTY);
    }

    public Set<Integer> getRangeClassIndexes() {
        return range;
    }

    public void addRangeClassIndex(int icls) {
        range.add(icls);
    }
}
