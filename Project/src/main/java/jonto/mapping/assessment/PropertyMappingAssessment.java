package jonto.mapping.assessment;

import jonto.indexing.IndexManager;
import jonto.mapping.MappingManager;

import java.util.Set;

public abstract class PropertyMappingAssessment<T> {
    protected IndexManager index;
    protected MappingManager mapping_manager;

    protected final int EMPTY_RANGE_OR_DOMAIN = 0;
    protected final int SAME_RANGE_AND_DOMAIN = 1;
    protected final int ONLY_SAME_RANGE_OR_DOMAIN = 2;
    protected final int COMPATIBLE_RANGE_DOMAIN = 3;
    protected final int INCOMPATIBLE_RANGE_OR_DOMAIN = 4;
    protected final int PROBABLY_INCOMPATIBLE_RANGE_OR_DOMAIN = 5;

    protected abstract int arePropertiesCompatible(int ident1, int ident2);

    protected abstract int arePropertiesCompatibleLight(int ident1, int ident2);


    public double getConfidence4Compatibility(int ident1, int ident2) {
        int compatibility = arePropertiesCompatibleLight(ident1, ident2);

        return switch (compatibility) {
            case EMPTY_RANGE_OR_DOMAIN -> 0.90;
            case SAME_RANGE_AND_DOMAIN -> 0.75;
            case ONLY_SAME_RANGE_OR_DOMAIN -> 0.85;
            case COMPATIBLE_RANGE_DOMAIN -> 0.90;
            case PROBABLY_INCOMPATIBLE_RANGE_OR_DOMAIN -> 1.5;
            case INCOMPATIBLE_RANGE_OR_DOMAIN -> 2.0;
            default -> 2.0;
        };
    }


    protected boolean haveSameRange(Set<T> range1, Set<T> range2) {
        if (range1.size() > 0 && range2.size() > 0) {
            return range1.equals(range2);
        }

        return false;
    }


    protected boolean haveSameDomain(Set<Integer> dom1, Set<Integer> dom2) {

        if (dom1.size() > 0 || dom2.size() > 0) {
            return dom1.equals(dom2);
        }

        return false;
    }
}
