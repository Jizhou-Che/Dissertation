package jonto.mapping.assessment;

import jonto.Parameters;
import jonto.indexing.IndexManager;
import jonto.mapping.MappingManager;

import java.util.HashSet;
import java.util.Set;

public class InstanceMatchingAssessment {
    protected IndexManager index;
    protected MappingManager mapping_manager;


    protected final int EMPTY_TYPES = 0;
    protected final int ONE_TYPE_EMPTY = 1;

    protected final int COMPATIBLE_TYPES = 2;
    protected final int INCOMPATIBLE_TYPES = 3;

    protected final int SAME_TYPES = 4;
    protected final int SUB_TYPES = 5;

    protected int compatibility = 0;

    public InstanceMatchingAssessment(IndexManager index, MappingManager mapping_manager) {

        this.index = index;
        this.mapping_manager = mapping_manager;

    }

    Set<String> categories1;
    Set<String> categories2;

    public boolean haveInstancesCompatibleCategories(int ident1, int ident2) {
        categories1 = index.getIndividualCategory4Identifier(ident1);
        categories2 = index.getIndividualCategory4Identifier(ident2);

        if (categories1.size() == 0 || categories2.size() == 0) {
            return true;
        }

        extendCategoriesWithMappings(categories1);
        extendCategoriesWithMappings(categories2);

        extendCategoriesWithMappings(categories1);
        extendCategoriesWithMappings(categories2);

        return areCompatibleCategories(categories1, categories2);
    }


    private final Set<String> additional_categories = new HashSet<String>();

    private void extendCategoriesWithMappings(Set<String> categories) {
        for (String cat : categories) {
            if (mapping_manager.hasCategoryMappings(cat)) {
                additional_categories.addAll(mapping_manager.getMappings4Category(cat));
            }
        }
        categories.addAll(additional_categories);
        additional_categories.clear();
    }


    protected int areInstancesCompatible(int ident1, int ident2) {
        Set<Integer> types1 = index.getIndividualClassTypes4Identifier(ident1);
        Set<Integer> types2 = index.getIndividualClassTypes4Identifier(ident2);

        Set<Integer> mapped_types1 = new HashSet<Integer>();

        if (types1.isEmpty() && types2.isEmpty()) {
            return EMPTY_TYPES;
        }
        if (types1.isEmpty() || types2.isEmpty()) {
            return ONE_TYPE_EMPTY;
        }

        for (int cls1 : types1) {
            if (mapping_manager.getMappings().containsKey(cls1)) {
                mapped_types1.addAll(mapping_manager.getMappings().get(cls1));
            }
        }

        if (areSameClassTypes(mapped_types1, types2)) {
            return SAME_TYPES;
        }

        for (int cls1 : types1) {
            for (int cls2 : types2) {
                if (mapping_manager.isMappingInConflictWithFixedMappings(cls1, cls2)) {
                    return INCOMPATIBLE_TYPES;
                }
            }
        }

        if (areAllSubTypes(types1, types2)) {
            return SUB_TYPES;
        }

        return COMPATIBLE_TYPES;
    }


    public double getConfidence4Compatibility(int ident1, int ident2) {
        compatibility = areInstancesCompatible(ident1, ident2);

        return switch (compatibility) {
            case EMPTY_TYPES -> Parameters.min_isub_instances + 0.15;
            case ONE_TYPE_EMPTY -> Parameters.min_isub_instances + 0.15;
            case SAME_TYPES -> Parameters.min_isub_instances;
            case SUB_TYPES -> Parameters.min_isub_instances + 0.05;
            case COMPATIBLE_TYPES -> Parameters.min_isub_instances + 0.10;
            case INCOMPATIBLE_TYPES -> 2.0;
            default -> 2.0;
        };
    }


    public double getCompatibilityFactor() {
        return switch (compatibility) {
            case EMPTY_TYPES -> 0.50;
            case ONE_TYPE_EMPTY -> 0.50;
            case SAME_TYPES -> 1.0;
            case SUB_TYPES -> 0.90;
            case COMPATIBLE_TYPES -> 0.70;
            case INCOMPATIBLE_TYPES -> 0.0;
            default -> 0.0;
        };
    }


    protected boolean areSameClassTypes(Set<Integer> types1, Set<Integer> types2) {
        if (types1.size() > 0 && types2.size() > 0) {
            return types1.equals(types2);
        }

        return false;
    }


    protected boolean areCompatibleCategories(Set<String> cat1, Set<String> cat2) {
        Set<String> intersection = new HashSet<String>(cat1);

        intersection.retainAll(cat2);

        return (intersection.size() > 0);
    }

    protected boolean areAllSubTypes(Set<Integer> types1, Set<Integer> types2) {

        for (int cls1 : types1) {
            for (int cls2 : types2) {
                if (!index.isSubClassOf(cls1, cls2) && !index.isSubClassOf(cls2, cls1)) {
                    return false;
                }
            }
        }

        return true;
    }
}
