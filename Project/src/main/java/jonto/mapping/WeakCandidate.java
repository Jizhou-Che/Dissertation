package jonto.mapping;

import jonto.indexing.IndexManager;
import jonto.indexing.OntologyProcessing;
import jonto.utilities.Pair;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class WeakCandidate extends Pair<Integer, Integer> implements Comparator<WeakCandidate> {

    public static final double MINSCORE = 0;
    public static final int WEIGHTED = 0;
    public static final int WEIGHTEDFILTER = 1;
    public static final int WEIGHTEDFILTERMATCH = 2;

    private OntologyProcessing m_o1 = null;
    private OntologyProcessing m_o2 = null;
    private IndexManager m_index = null;
    private double m_score = 0;
    private String rest1 = "", rest2 = "";

    public WeakCandidate(int id1, int id2, Set<String> label1, Set<String> label2,
                         OntologyProcessing o1, OntologyProcessing o2, int type) {
        super(id1, id2);
        m_o1 = o1;
        m_o2 = o2;
        getScore_weighted(id1, id2, label1, label2, type);
    }

    private void getScore_weighted(int id1, int id2, Set<String> lab1, Set<String> lab2, int type) {
        if (type == WEIGHTEDFILTER) {
            Set<String> super1 = m_o1.getSuperClass(id1, 10);
            Set<String> super2 = m_o2.getSuperClass(id2, 10);

            for (String str : super1) {
                for (String word : str.split("_")) {
                    if (!lab1.contains(word)) {
                        lab2.remove(word);
                    }
                }
            }

            for (String str : super2) {
                for (String word : str.split("_")) {
                    if (!lab2.contains(word)) {
                        lab1.remove(word);
                    }
                }
            }
        }

        m_score = getScore_weighted(lab1, lab2);
    }

    private double getScore_weighted(Set<String> lab1, Set<String> lab2) {
        double sum = 0, value = 0, temp;
        Set<String> combo = new HashSet<String>();

        for (String word : lab1) {
            temp = 1. / m_o1.getFrequency(word);
            sum += temp;
            if (lab2.contains(word)) {
                value += temp;
            }
            rest1 += word;
        }

        for (String word : lab2) {
            temp = 1. / m_o2.getFrequency(word);
            sum += temp;
            if (lab1.contains(word)) {
                value += temp;
            }
            rest2 += word;
        }


        return sum == 0 ? 1. : value / sum;
    }

    public double getScore(I_Sub isub) {
        return isub.score(rest1, rest2);
    }

    public WeakCandidate(Set<String> combo, int id1, int id2, String label1, String label2, IndexManager index, int f_combo) {
        super(id1, id2);
        m_index = index;
        getScore_frequency(combo, label1, label2, f_combo);
    }

    private void getScore_frequency(Set<String> combo, String label1, String label2, int f_combo) {
        Set<String> set1 = new HashSet<String>();
        Set<String> set2 = new HashSet<String>();

        for (String word : label1.split("_")) {
            if (!combo.contains(word)) {
                set1.add(word);
            }
        }

        for (String word : label2.split("_")) {
            if (!combo.contains(word)) {
                set2.add(word);
            }
        }

        Set<Integer> list1 = m_index.getCooccurrenceOfWords(set1);
        Set<Integer> list2 = m_index.getCooccurrenceOfWords(set2);

        int total = m_index.getIdentifier2ClassIndexMap().size();

        int fa = list1 == null ? total : list1.size();
        int fb = list2 == null ? total : list2.size();

        if ((f_combo - fa) * (f_combo - fb) < 0) {
            m_score = 0;
            return;
        }

        set1.addAll(set2);
        Set<Integer> list = m_index.intersectSet(list1, list2);
        int fab = list == null ? total : list.size();

        set1.clear();
        set2.clear();
        if (list1 != null) {
            list1.clear();
        }
        if (list2 != null) {
            list2.clear();
        }
        if (list != null) {
            list.clear();
        }

        m_score = 2. * fab / (fa + fb);
    }

    public double getScore() {
        return m_score;
    }

    @Override
    public int compare(WeakCandidate c1, WeakCandidate c2) {
        double x = c1.m_score - c2.m_score;
        if (x < -1e-6) {
            return 1;
        } else if (x > 1e-6) {
            return -1;
        } else {
            return 0;
        }
    }

    public int compareTo(WeakCandidate that) {
        return -compare(this, that);
    }

    public boolean equals(Object obj) {
        return compare(this, (WeakCandidate) obj) == 0;
    }

}
