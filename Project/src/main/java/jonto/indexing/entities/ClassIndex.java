package jonto.indexing.entities;

import jonto.indexing.labelling.Node;
import jonto.indexing.labelling.PreNode;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ClassIndex extends EntityIndex {
    private Set<String> alternativeLabels;

    private Set<Integer> disjointClasses;
    private Set<Integer> equivalentClasses;
    private Set<Integer> directSubclasses;
    private Set<Integer> directSuperclasses;
    private int hierarchyLevel = -1;

    private Set<Integer> scope4Score;
    private Set<Integer> scope4Exploration;

    private Set<Integer> roots;

    public ClassIndex(int i) {
        index = i;
        node = new PreNode(i);
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
        return Objects.requireNonNullElse(alternativeLabels, Collections.emptySet());
    }

    public boolean hasAlternativeLabels() {
        return alternativeLabels != null;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public Node getNode() {
        return node;
    }


    public boolean hasDirectDisjointClasses() {
        return disjointClasses != null;
    }

    public void setDisjointClasses(Set<Integer> disjointClasses) {
        this.disjointClasses = disjointClasses;
    }

    public void setEmptyDisjointClasses() {
        if (disjointClasses == null) {
            this.disjointClasses = new HashSet<Integer>();
        } else {
            this.disjointClasses.clear();
        }
    }

    public void addDisjointClass(int disjident) {
        if (disjointClasses == null) {
            this.disjointClasses = new HashSet<Integer>();
        }

        this.disjointClasses.add(disjident);
    }

    public void addAllDisjointClasses(Set<Integer> disjclasses) {
        if (disjointClasses == null) {
            this.disjointClasses = new HashSet<Integer>();
        }

        this.disjointClasses.addAll(disjclasses);
    }

    public Set<Integer> getDisjointClasses() {
        return disjointClasses;
    }

    public boolean hasEquivalentClasses() {
        return equivalentClasses != null;
    }

    public void setEquivalentClasses(Set<Integer> equivalentClasses) {
        this.equivalentClasses = equivalentClasses;
    }

    public void setEmptyEquivalentClasses() {
        if (equivalentClasses == null) {
            this.equivalentClasses = new HashSet<Integer>();
        } else {
            this.equivalentClasses.clear();
        }
    }

    public void addEquivalentClass(int disjident) {
        if (equivalentClasses == null) {
            this.equivalentClasses = new HashSet<Integer>();
        }

        this.equivalentClasses.add(disjident);
    }

    public Set<Integer> getEquivalentClasses() {
        return equivalentClasses;
    }


    public boolean hasDirectSubClasses() {
        return directSubclasses != null;
    }

    public void setEmptyDirectSubClasses() {
        if (directSubclasses == null) {
            this.directSubclasses = new HashSet<Integer>();
        } else {
            this.directSubclasses.clear();
        }
    }

    public void addDirectSubClass(int disjident) {
        if (directSubclasses == null) {
            this.directSubclasses = new HashSet<Integer>();
        }

        this.directSubclasses.add(disjident);

    }

    public void setDirectSubclasses(Set<Integer> directSubclasses) {
        this.directSubclasses = directSubclasses;
    }


    public Set<Integer> getDirectSubclasses() {
        return directSubclasses;
    }


    public boolean hasDirectSuperClasses() {
        return directSuperclasses != null;
    }

    public void setEmptyDirectSuperClasses() {
        if (directSuperclasses == null) {
            this.directSuperclasses = new HashSet<Integer>();
        } else {
            this.directSuperclasses.clear();
        }
    }


    public void addDirectSuperClass(int disjident) {
        if (directSuperclasses == null) {
            this.directSuperclasses = new HashSet<Integer>();
        }

        this.directSuperclasses.add(disjident);

    }


    public void setDirectSuperclasses(Set<Integer> directSuperclasses) {
        this.directSuperclasses = directSuperclasses;
    }


    public Set<Integer> getDirectSuperclasses() {
        return directSuperclasses;
    }


    public boolean hasScope4Scores() {
        return scope4Score != null;
    }


    public void setScope4Scores(Set<Integer> scope) {
        this.scope4Score = scope;
    }


    public Set<Integer> getScope4Scores() {
        return scope4Score;
    }


    public boolean hasScope4Exploration() {
        return scope4Exploration != null;
    }

    public void setScope4Exploration(Set<Integer> scope) {
        this.scope4Exploration = scope;
    }


    public Set<Integer> getScope4Exploration() {
        return scope4Exploration;
    }


    public boolean hasRoots() {
        return roots != null;
    }


    public void setRoots(Set<Integer> roots) {
        this.roots = roots;
    }


    public Set<Integer> getRoots() {
        return roots;
    }


    public void setHierarchyLevel(int hierarchyLevel) {
        this.hierarchyLevel = hierarchyLevel;
    }


    public int getHierarchyLevel() {
        return hierarchyLevel;
    }


    private Set<String> stemmedAltLabels;

    public String findSimilarStemmedAltLable(ClassIndex that) {
        if (stemmedAltLabels == null || that.stemmedAltLabels == null) {
            return null;
        }

        int combo = -1, left = -1, temp_c, temp_l;
        String ret = null;
        String[] words2;

        for (String lab1 : stemmedAltLabels) {
            for (String lab2 : that.stemmedAltLabels) {
                temp_l = -1;
                if (((temp_c = getCommonWordsNumber(lab1, (words2 = lab2.split("_")))) > combo) ||
                        temp_c == combo && (temp_l = lab1.split("_").length + words2.length) < left) {
                    combo = temp_c;
                    left = temp_l == -1 ? lab1.split("_").length + words2.length : temp_l;
                    ret = lab1 + " " + lab2;
                }
            }
        }

        words2 = null;
        return ret;
    }

    private int getCommonWordsNumber(String str, String[] words) {
        int ret = 0;
        for (String word : words) {
            if (!word.isEmpty() && str.contains(word))
            {
                ++ret;
            }
        }
        return ret;
    }

    public String findStemmedAltLabel(Set<String> words) {
        if (stemmedAltLabels == null) {
            System.out.println("The class named " + name4Entitity + " has no stemmed alt labels.");
            return null;
        }

        String label = "";
        int maxScore = 0, score;
        for (String l : stemmedAltLabels) {
            if (maxScore < (score = getCommonWordsNumber(l, words))) {
                maxScore = score;
                label = l;
            } else if (maxScore == score && l.length() < label.length()) {
                label = l;
            }
        }

        return label;
    }

    private int getCommonWordsNumber(String str, Set<String> words) {
        int ret = 0;
        for (String word : words) {
            if (str.contains(word)) {
                ++ret;
            }
        }
        return ret;
    }

    public void addStemmedAltLabel(String label) {
        if (stemmedAltLabels == null) {
            stemmedAltLabels = new HashSet<String>();
        }
        stemmedAltLabels.add(label);
    }

    public Set<String> getStemmedAltLabels() {
        return stemmedAltLabels;
    }


    public boolean hasStemmedAlternativeLabels() {
        return stemmedAltLabels != null;
    }


    public void deleteAltStemmedLabels() {
        stemmedAltLabels.clear();
    }

    public void deleteAltLabels() {
        alternativeLabels.clear();
    }

}
