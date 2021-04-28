package jonto.indexing;

import jonto.utilities.OWLUtilities;
import jonto.reasoning.DLReasoner;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;

import java.util.Calendar;

public class ReasonerBasedIndexManager extends JointIndexManager {
    private DLReasoner jointreasoner;
    private final OWLDataFactory dataFactory;

    public ReasonerBasedIndexManager() {
        dataFactory = OWLUtilities.createOWLDataFactory();
    }


    public void setJointReasoner(DLReasoner jointreasoner) {
        this.jointreasoner = jointreasoner;
    }


    public boolean isSubClassOf(int cIdent1, int cIdent2) {
        calls_tax_question++;

        boolean answer;

        answer = isSubClassOf(
                getOWLClass4ConceptIndex(cIdent1),
                getOWLClass4ConceptIndex(cIdent2)
        );

        return answer;
    }

    public boolean isSubClassOf(OWLClass cls1, OWLClass cls2) {
        return jointreasoner.isEntailed(dataFactory.getOWLSubClassOfAxiom(cls1, cls2));
    }


    public boolean isSuperClassOf(int cIdent1, int cIdent2) {
        return isSubClassOf(cIdent2, cIdent1);
    }

    public boolean areEquivalentClasses(int cIdent1, int cIdent2) {
        return areEquivalent(getOWLClass4ConceptIndex(cIdent1), getOWLClass4ConceptIndex(cIdent2));
    }

    public boolean areEquivalent(OWLClass cls1, OWLClass cls2) {
        return jointreasoner.isEntailed(dataFactory.getOWLEquivalentClassesAxiom(cls1, cls2));
    }

    public boolean areDisjoint(int cIdent1, int cIdent2) {
        calls_disj_question++;
        init = Calendar.getInstance().getTimeInMillis();

        // Disjointness is unsatisfiable intersection.
        OWLClass cls1 = getOWLClass4ConceptIndex(cIdent1);
        OWLClass cls2 = getOWLClass4ConceptIndex(cIdent2);

        return !jointreasoner.isSatisfiable(dataFactory.getOWLObjectIntersectionOf(cls1, cls2));
    }
}
