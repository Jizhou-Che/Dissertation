package jonto.indexing;

import jonto.indexing.labelling.Interval;
import jonto.indexing.labelling.IntervalLabelledHierarchy;
import jonto.indexing.labelling.PreIntervalLabelledHierarchy;

import java.util.*;
import java.util.Map.Entry;

public class JointIndexManager extends IndexManager {
    public Set<Integer> getScope4Identifier_Big(int ide) {
        return getScope4Identifier(ide, 10, 10, 1000);
    }

    public Set<Integer> getScope4Identifier_Condifence(int ide) {
        return getScope4Identifier(ide, 3, 10, 50);
    }

    public Set<Integer> getScope4Identifier_Expansion(int ide) {
        return getScope4Identifier(ide, 2, 2, 50);
    }

    public Set<Integer> getScope4Identifier(int ide, int sub_levels, int super_levels, int max_size_subclasses) {
        Set<Integer> scope = new HashSet<Integer>();

        allSuperClasses.clear();
        allSubClasses.clear();

        if (sub_levels > 0) {
            getSubclasses4Identifiers(getDirectSubClasses4Identifier(ide, false), sub_levels, max_size_subclasses);
            scope.addAll(allSubClasses);
        }

        if (super_levels > 0) {
            getSuperclasses4Identifiers(getDirectSuperClasses4Identifier(ide, false), super_levels);
            scope.addAll(allSuperClasses);
        }

        return scope;

    }


    Set<Integer> allSuperClasses = new HashSet<Integer>();
    Set<Integer> allSubClasses = new HashSet<Integer>();

    private void getSubclasses4Identifiers(Set<Integer> classes, int level, int max_size_subclasses) {
        allSubClasses.addAll(classes);

        if (level < 1 || classes.size() < 1 || allSubClasses.size() > max_size_subclasses) {
            return;
        }

        Set<Integer> subClasses = new HashSet<Integer>();

        for (int ide : classes) {
            subClasses.addAll(getDirectSubClasses4Identifier(ide, false));
        }


        getSubclasses4Identifiers(subClasses, level - 1, max_size_subclasses);


    }


    private void getSuperclasses4Identifiers(Set<Integer> classes, int level) {

        allSuperClasses.addAll(classes);

        if (level < 1 || classes.size() < 1) {
            return;
        }

        Set<Integer> superClasses = new HashSet<Integer>();

        for (int ide : classes) {
            superClasses.addAll(getDirectSuperClasses4Identifier(ide, false));
        }

        getSuperclasses4Identifiers(superClasses, level - 1);
    }


    private void duplicateDirectSubClasses() {

        ident2DirectSubClasses_integration = new HashMap<Integer, Set<Integer>>();

        for (int parent : getDirectSubClasses(false).keySet()) {

            if (!ident2DirectSubClasses_integration.containsKey(parent)) {
                ident2DirectSubClasses_integration.put(parent, new HashSet<Integer>());
            }

            for (int kid : getDirectSubClasses(false).get(parent)) {
                ident2DirectSubClasses_integration.get(parent).add(kid);
            }

        }

    }

    private void setAdaptedMap4DirectSubclasses(Map<Integer, Set<Integer>> exact_mappings) {
        duplicateDirectSubClasses();

        representativesFromMappings = new HashSet<Integer>();

        for (int ide_rep : exact_mappings.keySet()) {
            for (int ide_equiv : exact_mappings.get(ide_rep)) {

                if (ide_rep > ide_equiv) {
                    break;
                }

                representativesFromMappings.add(ide_rep);

                for (int ide_parent : getDirectSuperClasses4Identifier(ide_equiv, false)) {
                    if (ident2DirectSubClasses_integration.containsKey(ide_parent)) {//just in case
                        ident2DirectSubClasses_integration.get(ide_parent).add(ide_rep);
                        ident2DirectSubClasses_integration.get(ide_parent).remove(ide_equiv);
                    }
                }

                if (ident2DirectSubClasses_integration.containsKey(ide_equiv)) {

                    if (!ident2DirectSubClasses_integration.containsKey(ide_rep)) {
                        ident2DirectSubClasses_integration.put(ide_rep, new HashSet<Integer>());
                    }

                    ident2DirectSubClasses_integration.get(ide_rep).addAll(ident2DirectSubClasses_integration.get(ide_equiv));

                    ident2DirectSubClasses_integration.get(ide_equiv).clear();
                    ident2DirectSubClasses_integration.remove(ide_equiv);
                }
            }
        }
    }

    public void setIntervalLabellingIndex(Map<Integer, Set<Integer>> exact_mappings) {
        setAdaptedMap4DirectSubclasses(exact_mappings);

        IntervalLabelledHierarchy interval_schema = new PreIntervalLabelledHierarchy(dealWithCyclesIntervalLabellingSchema(true));

        for (int ident : interval_schema.getClassesToNodesMap().keySet()) {
            identifier2ClassIndex.get(ident).setNode(interval_schema.getClassesToNodesMap().get(ident));
        }

        for (int iRep : getRepresentativeNodes()) {
            if (identifier2ClassIndex.get(iRep).hasEquivalentClasses()) {
                for (int iEquiv : identifier2ClassIndex.get(iRep).getEquivalentClasses()) {
                    identifier2ClassIndex.get(iEquiv).setNode(identifier2ClassIndex.get(iRep).getNode());
                }
            }
        }

        for (int iRep : getRepresentativesFromMappings()) {
            if (exact_mappings.containsKey(iRep)) {
                for (int iEquiv : exact_mappings.get(iRep)) {
                    identifier2ClassIndex.get(iEquiv).setNode(identifier2ClassIndex.get(iRep).getNode());
                }
            }
        }

        createDisjointIntervalsStructure();

        checkBasicSatisfiability();

    }

    private HashMap<Integer, Set<Integer>> dealWithCyclesIntervalLabellingSchema(boolean fixCycles) {
        HashMap<Integer, Set<Integer>> ontoHierarchy = null;
        Set<Set<Integer>> nonTrivialSCCs = new HashSet<>();

        if (fixCycles) {
            ontoHierarchy = getIdent2DirectSubClasses_Integration();
            Map<Integer, Set<Integer>> sccs = new LightTarjan().executeTarjan(ontoHierarchy);

            for (Set<Integer> scc : sccs.values()) {
                if (scc.size() > 1) {
                    nonTrivialSCCs.add(scc);

                    Integer represId = scc.iterator().next();
                    Set<Integer> sccMinusRepr = new HashSet<>(scc);
                    sccMinusRepr.remove(represId);

                    for (Integer id : sccMinusRepr) {
                        ontoHierarchy.get(id).removeAll(scc);
                        ontoHierarchy.get(represId).addAll(ontoHierarchy.get(id));
                    }

                    ontoHierarchy.get(represId).removeAll(sccMinusRepr);

                    for (Entry<Integer, Set<Integer>> e : ontoHierarchy.entrySet()) {
                        if (!scc.contains(e.getKey())) {
                            int pre = e.getValue().size();
                            e.getValue().removeAll(sccMinusRepr);

                            if (e.getValue().size() != pre) {
                                e.getValue().add(represId);
                            }
                        }
                    }
                }
            }
            if (!nonTrivialSCCs.isEmpty()) {
                sccs = new LightTarjan().executeTarjan(ontoHierarchy);
                int unsolved = 0;
                for (Set<Integer> scc : sccs.values()) {
                    if (scc.size() > 1) {
                        ++unsolved;
                    }
                }
                if (unsolved > 0) {
                    throw new Error(unsolved + " unsolved SCCs");
                }
            }

            for (Set<Integer> scc : nonTrivialSCCs) {
                for (Integer i : scc) {
                    Set<Integer> sccMinusSelf = new HashSet<>(scc);
                    sccMinusSelf.remove(i);
                    if (identifier2ClassIndex.get(i).getEquivalentClasses() == null) {
                        identifier2ClassIndex.get(i).setEquivalentClasses(new HashSet<>(sccMinusSelf));
                    } else {
                        identifier2ClassIndex.get(i).getEquivalentClasses().addAll(sccMinusSelf);
                    }
                }
            }
        }


        if (ontoHierarchy == null) {
            return getIdent2DirectSubClasses_Integration();
        } else {
            return ontoHierarchy;
        }


    }

    private void createDisjointIntervalsStructure() {
        List<Interval> list_intervals = new ArrayList<Interval>();
        Interval[] array_intervals;
        Interval current_interval;

        interval2disjointIntervals.clear();

        for (int icls : identifier2ClassIndex.keySet()) {
            if (identifier2ClassIndex.get(icls).hasDirectDisjointClasses()) {
                for (int disjcls : identifier2ClassIndex.get(icls).getDisjointClasses()) {
                    for (Interval disjcls_interval : identifier2ClassIndex.get(disjcls).getNode().getDescIntervals()) {
                        if (disjcls_interval.getLeftBound() >= 0 && disjcls_interval.getRightBound() >= 0) {
                            list_intervals.add(disjcls_interval);
                        }
                    }
                }

                if (list_intervals.size() >= 3) {
                    array_intervals = new Interval[list_intervals.size()];
                    array_intervals = list_intervals.toArray(array_intervals);

                    _Quicksort(array_intervals, 0, array_intervals.length - 1);

                    list_intervals.clear();

                    current_interval = array_intervals[0];

                    for (int i = 1; i < array_intervals.length; i++) {

                        if (current_interval.isAdjacentTo(array_intervals[i])) {
                            current_interval = current_interval.getUnionWith(array_intervals[i]);
                        } else {
                            list_intervals.add(current_interval);
                            current_interval = array_intervals[i];
                        }
                    }
                    list_intervals.add(current_interval);

                    for (Interval cls_interval : identifier2ClassIndex.get(icls).getNode().getDescIntervals()) {
                        List<Interval> tmpList = new LinkedList<>(list_intervals);

                        if (interval2disjointIntervals.containsKey(cls_interval)) {
                            tmpList.addAll(interval2disjointIntervals.get(cls_interval));
                            tmpList = mergeIntervals(tmpList);
                        }
                        interval2disjointIntervals.put(cls_interval, new HashSet<Interval>(tmpList));
                    }
                } else if (list_intervals.size() == 2) {
                    List<Interval> originalList = list_intervals.get(0).getUnionWithList(list_intervals.get(1));
                    for (Interval cls_interval : identifier2ClassIndex.get(icls).getNode().getDescIntervals()) {
                        List<Interval> tmpList = new LinkedList<>(originalList);
                        if (interval2disjointIntervals.containsKey(cls_interval)) {
                            tmpList.addAll(interval2disjointIntervals.get(cls_interval));
                            tmpList = mergeIntervals(tmpList);
                        }
                        interval2disjointIntervals.put(cls_interval, new HashSet<Interval>(tmpList));
                    }
                } else if (list_intervals.size() == 1) { //Only one
                    for (Interval cls_interval : identifier2ClassIndex.get(icls).getNode().getDescIntervals()) {
                        List<Interval> originalList = new LinkedList<>(list_intervals);

                        if (interval2disjointIntervals.containsKey(cls_interval)) {
                            originalList.addAll(interval2disjointIntervals.get(cls_interval));
                            originalList = mergeIntervals(originalList);
                        }
                        interval2disjointIntervals.put(cls_interval, new HashSet<Interval>(originalList));
                    }
                }

                list_intervals.clear();
            }

        }
    }

    public List<Interval> mergeIntervals(List<Interval> list_intervals) {
        if (list_intervals.size() >= 3) {
            Interval[] array_intervals = new Interval[list_intervals.size()];
            array_intervals = list_intervals.toArray(array_intervals);

            _Quicksort(array_intervals, 0, array_intervals.length - 1);

            list_intervals.clear();

            Interval current_interval = array_intervals[0];

            for (int i = 1; i < array_intervals.length; i++) {

                if (current_interval.isAdjacentTo(array_intervals[i])) {
                    current_interval = current_interval.getUnionWith(array_intervals[i]);
                } else {
                    list_intervals.add(current_interval);
                    current_interval = array_intervals[i];
                }
            }
            list_intervals.add(current_interval);
        } else if (list_intervals.size() == 2) {
            list_intervals =
                    list_intervals.get(0).getUnionWithList(
                            list_intervals.get(1));
        }

        return list_intervals;
    }

    private void checkBasicSatisfiability() {
        unsatisfiableClassesILS.clear();

        for (Interval interv1 : interval2disjointIntervals.keySet()) {

            for (Interval disj_interv : interval2disjointIntervals.get(interv1)) {

                if (interv1.hasNonEmptyIntersectionWith(disj_interv)) {
                    for (int pre = interv1.getIntersectionWith(disj_interv).getLeftBound();
                         pre <= interv1.getIntersectionWith(disj_interv).getRightBound();
                         pre++) {

                        if (getIdentifier4PreorderDesc(pre) > 0) {
                            unsatisfiableClassesILS.add(getIdentifier4PreorderDesc(pre));
                        }
                    }
                }
            }
        }
    }

    private void _Quicksort(Interval[] matrix, int a, int b) {
        Interval buf;
        int from = a;
        int to = b;
        Interval pivot = matrix[(from + to) / 2];
        do {
            while (from <= b && matrix[from].hasLowerLeftBoundThan(pivot)) {
                from++;
            }
            while (to >= a && matrix[to].hasGreaterLeftBoundThan(pivot)) {
                to--;
            }
            if (from <= to) {
                buf = matrix[from];
                matrix[from] = matrix[to];
                matrix[to] = buf;
                from++;
                to--;
            }
        } while (from <= to);

        if (a < to) {
            _Quicksort(matrix, a, to);
        }
        if (from < b) {
            _Quicksort(matrix, from, b);
        }
    }
}
