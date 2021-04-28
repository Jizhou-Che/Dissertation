package jonto.reasoning;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.util.Set;

public abstract class DLReasoner {
    public static final int LSUB = 0;
    public static final int LBASIC = 1;
    public static final int LACTIVE = 2;

    public static final int SAT = 0;
    public static final int UNSAT = 1;
    public static final int UNKNOWN = 2;

    public abstract boolean isOntologyClassified();

    public abstract void classifyOntology() throws Exception;

    public abstract void classifyOntology(boolean class_properties) throws Exception;

    public abstract OWLOntology getOntology();

    public abstract OWLReasoner getReasoner();

    public abstract OWLReasonerFactory getReasonerFactory();

    public abstract boolean isSatisfiable(OWLClassExpression cls);

    public abstract boolean isEntailed(OWLAxiom ax);

    public abstract boolean isSubClassOf(OWLClass cls1, OWLClass cls2);

    public abstract boolean areDisjointClasses(OWLClass cls1, OWLClass cls2);

    public abstract boolean isConsistent();

    public abstract boolean areEquivalentClasses(OWLClass cls1, OWLClass cls2);

    public abstract void setLanguage4Closure(int language);

    public abstract void createClosure();

    public abstract void createClosure(int language);

    public abstract Set<OWLAxiom> getClosure();

    public abstract Set<OWLClass> getUnsatisfiableClasses();

    public abstract boolean hasUnsatisfiableClasses();

    public abstract void clearStructures();
}
