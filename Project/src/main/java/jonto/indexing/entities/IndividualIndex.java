package jonto.indexing.entities;

import java.util.*;

public class IndividualIndex extends EntityIndex {

    protected Set<Integer> class_types = new HashSet<Integer>();

    protected Set<String> categories = new HashSet<String>();

    private Set<String> alternativeLabels;

    private boolean showInOutput = true;

    protected List<Integer> characteristics = new ArrayList<Integer>();


    private Set<Integer> referencedIndividuals = new HashSet<Integer>();


    public IndividualIndex(int i) {
        index = i;
    }


    public Set<Integer> getClassTypes() {
        return class_types;
    }

    public void addClassTypeIndex(int icls) {
        class_types.add(icls);
    }


    public Set<String> getCategories() {
        return categories;
    }

    public void addCategory(String cat) {
        categories.add(cat);
    }

    public boolean showInOutput() {
        return showInOutput;
    }

    public void setShowInOutput(boolean showInOutput) {
        this.showInOutput = showInOutput;
    }


    public boolean hasDirectClassTypes() {
        return !class_types.isEmpty();
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

    public List<Integer> getCharacteristics() {
        return characteristics;
    }

    public Set<String> getAlternativeLabels() {
        return Objects.requireNonNullElse(alternativeLabels, Collections.emptySet());

    }

    public boolean hasAlternativeLabels() {
        return alternativeLabels != null;
    }

    public Set<Integer> getReferencedIndividuals() {
        return referencedIndividuals;
    }

    public void setReferencedIndividuals(Set<Integer> referencedIndividuals) {
        this.referencedIndividuals = referencedIndividuals;
    }

    public void addReferencedIndividuals(int index) {
        this.referencedIndividuals.add(index);
    }

    public void addCharacteristic(int value) {
        this.characteristics.add(value);
    }
}
