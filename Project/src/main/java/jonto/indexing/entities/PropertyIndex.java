package jonto.indexing.entities;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class PropertyIndex extends EntityIndex {
    public static int DATAPROPERTY = 0;
    public static int OBJECTPROPERTY = 1;

    protected int type_property;

    protected Set<Integer> domain = new HashSet<Integer>();

    private Set<String> alternativeLabels;

    public PropertyIndex(int i, int atype) {
        index = i;
        type_property = atype;
    }

    public int getPropertyType() {
        return type_property;
    }

    public boolean isObjectProperty() {
        return (type_property == OBJECTPROPERTY);
    }

    public boolean isDataProperty() {
        return (type_property == DATAPROPERTY);
    }

    public Set<Integer> getDomainClassIndexes() {
        return domain;
    }

    public void addDomainClassIndex(int icls) {
        domain.add(icls);
    }

    public void addAlternativeLabel(String altLabel) {
        if (alternativeLabels == null) {
            alternativeLabels = new HashSet<String>();
        }

        alternativeLabels.add(altLabel);
    }

    public void setAlternativeLabels(Set<String> altLabels) {
        alternativeLabels = new HashSet<String>(altLabels);
    }

    public void setEmptyAlternativeLabels() {
        alternativeLabels = new HashSet<String>();
    }

    public Set<String> getAlternativeLabels() {
        return alternativeLabels == null ? Collections.emptySet() : alternativeLabels;

    }

    public boolean hasAlternativeLabels() {
        return alternativeLabels != null;
    }
}
