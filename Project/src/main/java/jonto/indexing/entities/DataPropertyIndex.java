package jonto.indexing.entities;

import java.util.HashSet;
import java.util.Set;

public class DataPropertyIndex extends PropertyIndex {
    Set<String> range = new HashSet<String>();

    public DataPropertyIndex(int index) {
        super(index, PropertyIndex.DATAPROPERTY);
    }

    public Set<String> getRangeTypes() {
        return range;
    }

    public void addRangeType(String dtype) {
        range.add(dtype);
    }
}
