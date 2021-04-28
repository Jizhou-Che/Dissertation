package jonto.mapping.assessment;

import jonto.indexing.IndexManager;
import jonto.mapping.MappingManager;

import java.util.HashSet;
import java.util.Set;

public class DataPropertyMappingAssessment extends PropertyMappingAssessment<String> {
    public DataPropertyMappingAssessment(IndexManager index, MappingManager mapping_manager) {
        this.index = index;
        this.mapping_manager = mapping_manager;
    }

    protected int arePropertiesCompatible(int ident1, int ident2) {
        Set<Integer> domain1 = index.getDomainDataProp4Identifier(ident1);
        Set<Integer> mapped_domain1 = new HashSet<Integer>();
        Set<Integer> domain2 = index.getDomainDataProp4Identifier(ident2);

        Set<String> range1 = index.getRangeDataProp4Identifier(ident1);
        Set<String> range2 = index.getRangeDataProp4Identifier(ident2);

        boolean same_domain;
        boolean same_range;

        if (domain1.isEmpty() && domain2.isEmpty() && range1.isEmpty() && range2.isEmpty()) {
            return EMPTY_RANGE_OR_DOMAIN;
        }

        if ((domain1.isEmpty() && !domain2.isEmpty()) || (range1.isEmpty() && !range2.isEmpty()) || (!domain1.isEmpty() && domain2.isEmpty()) || (!range1.isEmpty() && range2.isEmpty())) {
            return INCOMPATIBLE_RANGE_OR_DOMAIN;
        }


        for (int ide1 : domain1) {
            if (index.getDangerousClasses().contains(ide1)) {
                return INCOMPATIBLE_RANGE_OR_DOMAIN;
            }
        }

        for (int ide2 : domain2) {
            if (index.getDangerousClasses().contains(ide2)) {
                return INCOMPATIBLE_RANGE_OR_DOMAIN;
            }
        }

        if (!domain1.isEmpty() && !domain2.isEmpty()) {
            for (int cls1 : domain1) {
                if (mapping_manager.getMappings().containsKey(cls1)) {
                    mapped_domain1.addAll(mapping_manager.getMappings().get(cls1));
                }
            }
        }

        same_domain = haveSameDomain(mapped_domain1, domain2);
        same_range = haveSameRange(range1, range2);

        if (same_domain && same_range) {
            return SAME_RANGE_AND_DOMAIN;
        } else if (same_range) {
            for (int cls1 : domain1) {
                for (int cls2 : domain2) {
                    if (mapping_manager.isMappingInConflictWithFixedMappings(cls1, cls2)) {
                        return INCOMPATIBLE_RANGE_OR_DOMAIN;
                    }
                }
            }

            return COMPATIBLE_RANGE_DOMAIN;
        }

        return INCOMPATIBLE_RANGE_OR_DOMAIN;
    }

    protected int arePropertiesCompatibleLight(int ident1, int ident2) {
        Set<Integer> domain1 = index.getDomainDataProp4Identifier(ident1);
        Set<Integer> mapped_domain1 = new HashSet<Integer>();
        Set<Integer> domain2 = index.getDomainDataProp4Identifier(ident2);

        Set<String> range1 = index.getRangeDataProp4Identifier(ident1);
        Set<String> range2 = index.getRangeDataProp4Identifier(ident2);

        boolean same_domain;
        boolean same_range;

        if (domain1.isEmpty() && domain2.isEmpty() && range1.isEmpty() && range2.isEmpty()) {
            return EMPTY_RANGE_OR_DOMAIN;
        }

        for (int ide1 : domain1) {
            if (index.getDangerousClasses().contains(ide1)) {
                return INCOMPATIBLE_RANGE_OR_DOMAIN;
            }
        }

        for (int ide2 : domain2) {
            if (index.getDangerousClasses().contains(ide2)) {
                return INCOMPATIBLE_RANGE_OR_DOMAIN;
            }
        }

        if (!domain1.isEmpty() && !domain2.isEmpty()) {
            for (int cls1 : domain1) {
                if (mapping_manager.getMappings().containsKey(cls1)) {
                    mapped_domain1.addAll(mapping_manager.getMappings().get(cls1));
                }
            }
        }


        same_domain = haveSameDomain(mapped_domain1, domain2);
        same_range = haveSameRange(range1, range2);

        if (range1.size() == 1 && range1.contains("Literal")) {
            same_range = true;
        } else if (range2.size() == 1 && range2.contains("Literal")) {
            same_range = true;
        }

        if (same_domain && same_range) {
            return SAME_RANGE_AND_DOMAIN;
        } else if (same_range) {
            for (int cls1 : domain1) {
                for (int cls2 : domain2) {
                    if (mapping_manager.isMappingInConflictWithFixedMappings(cls1, cls2)) {
                        return INCOMPATIBLE_RANGE_OR_DOMAIN;
                    }
                }
            }
            return COMPATIBLE_RANGE_DOMAIN;
        }

        return INCOMPATIBLE_RANGE_OR_DOMAIN;
    }
}
