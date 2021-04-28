package jonto.reasoning.explanation;

import jonto.indexing.PrecomputeIndexCombination;
import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.*;

public class PlanExtractor {

    private final Map<Integer, OWLAxiom> ident2axiom;

    private final Map<OWLAxiom, Set<Integer>> axioms2justifications;

    protected TreeSet<OWLAxiom> listOrderedAxioms;

    private final PrecomputeIndexCombination precomputeIndexCombination = new PrecomputeIndexCombination();

    private final List<Set<Integer>> setOfRepairPlans_int;
    private final List<Set<OWLAxiom>> setOfRepairPlans_ax;

    private final int num_justifications;

    public PlanExtractor(List<Set<OWLAxiom>> allJustifications) {
        ident2axiom = new HashMap<Integer, OWLAxiom>();
        axioms2justifications = new HashMap<OWLAxiom, Set<Integer>>();
        setOfRepairPlans_int = new ArrayList<Set<Integer>>();
        setOfRepairPlans_ax = new ArrayList<Set<OWLAxiom>>();

        precomputeIndexCombination.clearCombinations();
        precomputeIndexCombination.preComputeIdentifierCombination();

        num_justifications = allJustifications.size();

        int ident = 0;

        for (int index_just = 0; index_just < allJustifications.size(); index_just++) {

            for (OWLAxiom ax : allJustifications.get(index_just)) {

                if (!axioms2justifications.containsKey(ax)) {
                    axioms2justifications.put(ax, new HashSet<Integer>());
                }
                axioms2justifications.get(ax).add(index_just);
            }
        }

        orderAxiomsByImact();

        for (OWLAxiom listOrderedAxiom : listOrderedAxioms) {
            ident2axiom.put(ident, listOrderedAxiom);
            ident++;
        }
    }

    public void extractPlans() {
        setOfRepairPlans_int.clear();
        setOfRepairPlans_ax.clear();

        Set<Set<Integer>> candidate_plans;

        int maxSize4Plans;

        int combineOver;

        if (axioms2justifications.size() > 20) {
            combineOver = getNumberOfAxiomsWithHighImpact();
        } else {
            combineOver = axioms2justifications.size();
        }

        if (combineOver > 150) {
            maxSize4Plans = 1;
        } else if (combineOver > 50) {
            maxSize4Plans = 2;
        } else if (combineOver > 20) {
            maxSize4Plans = 3;
        } else if (combineOver > 15) {
            maxSize4Plans = 4;
        } else {
            maxSize4Plans = combineOver;
        }

        for (int size_plan = 1; size_plan <= maxSize4Plans; size_plan++) {
            candidate_plans = precomputeIndexCombination.getIdentifierCombination(combineOver, size_plan);

            System.out.println("Candidates size: " + candidate_plans.size() + ".");

            for (Set<Integer> candidate_plan : candidate_plans) {
                if (isaGoodPlan(candidate_plan)) {
                    setOfRepairPlans_int.add(new HashSet<Integer>(candidate_plan));
                }
            }

            if (!setOfRepairPlans_int.isEmpty()) {
                break;
            }
        }

        if (setOfRepairPlans_int.isEmpty()) {
            setOfRepairPlans_ax.add(new HashSet<OWLAxiom>(createIncrementalPlan()));
        } else {
            for (int i = 0; i < setOfRepairPlans_int.size(); i++) {
                setOfRepairPlans_ax.add(new HashSet<OWLAxiom>(getAxioms4PlanIde(i)));
            }
        }
    }


    private int getNumberOfAxiomsWithHighImpact() {
        int count = 0;

        for (OWLAxiom ax : axioms2justifications.keySet()) {
            if (getImpact(ax) > 1) {
                count++;
            }
        }

        return count;
    }


    private final Set<Integer> justification_ids = new HashSet<Integer>();

    private boolean isaGoodPlan(Set<Integer> candidate_plan) {

        justification_ids.clear();

        for (Set<Integer> repair : setOfRepairPlans_int) {
            if (candidate_plan.containsAll(repair)) {
                return false;
            }
        }

        for (int ide_ax : candidate_plan) {

            justification_ids.addAll(axioms2justifications.get(ident2axiom.get(ide_ax)));

            if (justification_ids.size() == num_justifications) {
                return true;
            }

        }

        return false;


    }


    private boolean isaGoodPlanAx(Set<OWLAxiom> candidate_plan) {
        justification_ids.clear();

        for (OWLAxiom ax : candidate_plan) {
            justification_ids.addAll(axioms2justifications.get(ax));

            if (justification_ids.size() == num_justifications) {
                return true;
            }

        }

        return false;


    }


    private List<Set<Integer>> getAllPlansInt() {
        return setOfRepairPlans_int;
    }


    public List<Set<OWLAxiom>> getAllPlansAx() {
        return setOfRepairPlans_ax;
    }

    private Set<OWLAxiom> createIncrementalPlan() {
        Iterator<OWLAxiom> it = listOrderedAxioms.iterator();

        Set<OWLAxiom> axioms_plan = new HashSet<OWLAxiom>();

        while (it.hasNext()) {

            axioms_plan.add(it.next());

            if (isaGoodPlanAx(axioms_plan)) {
                break;
            }

        }

        return axioms_plan;


    }

    private void orderAxiomsByImact() {
        listOrderedAxioms = new TreeSet<OWLAxiom>(new RepairMappingComparator());

        listOrderedAxioms.addAll(axioms2justifications.keySet());
    }


    private final Set<OWLAxiom> set_axioms = new HashSet<OWLAxiom>();

    private Set<OWLAxiom> getAxioms4PlanIde(int idePlan) {
        set_axioms.clear();

        for (int ide : setOfRepairPlans_int.get(idePlan)) {
            set_axioms.add(ident2axiom.get(ide));
        }

        return set_axioms;
    }


    private Set<OWLAxiom> getAxioms4PlanInt(Set<Integer> plan_int) {
        set_axioms.clear();

        for (int ide : plan_int) {
            set_axioms.add(ident2axiom.get(ide));
        }

        return set_axioms;
    }


    public int getImpact(OWLAxiom ax) {

        return axioms2justifications.get(ax).size();

    }


    private class RepairMappingComparator implements Comparator<OWLAxiom> {
        public int compare(OWLAxiom m1, OWLAxiom m2) {
            if (getImpact(m1) < getImpact(m2)) {
                return 1;
            } else {
                return -1;
            }
        }
    }
}
