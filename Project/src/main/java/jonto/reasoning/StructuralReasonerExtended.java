package jonto.reasoning;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNodeSet;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasoner;

import javax.annotation.Nonnull;

public class StructuralReasonerExtended extends StructuralReasoner {

    public StructuralReasonerExtended(OWLOntology rootOntology) {
        super(rootOntology, new SimpleConfiguration(), BufferingMode.NON_BUFFERING);
    }

    @Nonnull
    public NodeSet<OWLClass> getDisjointClasses(OWLClassExpression ce) {
        //super.ensurePrepared();
        OWLClassNodeSet nodeSet = new OWLClassNodeSet();
        if (!ce.isAnonymous()) {
            for (OWLOntology ontology : getRootOntology().getImportsClosure()) {
                for (OWLDisjointClassesAxiom ax : ontology.getDisjointClassesAxioms(ce.asOWLClass())) {
                    for (OWLClassExpression op : ax.getClassExpressions()) {
                        if (!op.isAnonymous() && !op.equals(ce)) {
                            nodeSet.addNode(getEquivalentClasses(op));
                        }
                    }
                }
            }
        }
        return nodeSet;
    }

    public boolean isSubClassOf(OWLClass cls1, OWLClass cls2) {
        return getSubClasses(cls2, false).getFlattened().contains(cls1);
    }

    public boolean areEquivalent(OWLClass cls1, OWLClass cls2) {
        return (getEquivalentClasses(cls1).getEntities().contains(cls2)) ||
                (getEquivalentClasses(cls2).getEntities().contains(cls1));
    }

    @Nonnull
    public NodeSet<OWLClass> getSubClasses(@Nonnull OWLClassExpression ce, boolean direct) throws InconsistentOntologyException, ClassExpressionNotInProfileException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
        OWLClassNodeSet ns = new OWLClassNodeSet();

        try {
            return super.getSubClasses(ce, direct);
        } catch (StackOverflowError e) {
            System.err.println("StackOverflowError in Structural reasoner: getSubClasses for Class " + ce);
            return ns;
        }
    }

    @Nonnull
    public NodeSet<OWLClass> getSuperClasses(@Nonnull OWLClassExpression ce, boolean direct) throws InconsistentOntologyException, ClassExpressionNotInProfileException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
        OWLClassNodeSet ns = new OWLClassNodeSet();

        try {
            return super.getSuperClasses(ce, direct);
        } catch (StackOverflowError e) {
            System.err.println("StackOverflowError in Structural reasoner: getSuperClasses for Class " + ce.toString());
            return ns;
        }
    }

    @Nonnull
    public String getReasonerName() {
        return "Extended Structural Reasoner";
    }
}
