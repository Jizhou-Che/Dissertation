package jonto.reasoning;

import org.semanticweb.owlapi.model.*;

import javax.annotation.Nonnull;

public abstract class ELAxiomVisitor implements OWLAxiomVisitor {
    // Unique axioms used in EL.
    public abstract void visit(@Nonnull OWLSubClassOfAxiom arg0);

    public abstract void visit(@Nonnull OWLEquivalentClassesAxiom arg0);


    public void visit(@Nonnull OWLAnnotationAssertionAxiom arg0) {

    }

    public void visit(@Nonnull OWLSubAnnotationPropertyOfAxiom arg0) {

    }

    public void visit(@Nonnull OWLAnnotationPropertyDomainAxiom arg0) {

    }

    public void visit(@Nonnull OWLAnnotationPropertyRangeAxiom arg0) {

    }

    public void visit(@Nonnull OWLDeclarationAxiom arg0) {

    }


    public void visit(@Nonnull OWLNegativeObjectPropertyAssertionAxiom arg0) {

    }

    public void visit(@Nonnull OWLAsymmetricObjectPropertyAxiom arg0) {

    }

    public void visit(@Nonnull OWLReflexiveObjectPropertyAxiom arg0) {

    }


    public void visit(@Nonnull OWLDisjointClassesAxiom arg0) {

    }

    public void visit(@Nonnull OWLDataPropertyDomainAxiom arg0) {

    }

    public void visit(@Nonnull OWLObjectPropertyDomainAxiom arg0) {

    }

    public void visit(@Nonnull OWLEquivalentObjectPropertiesAxiom arg0) {

    }

    public void visit(@Nonnull OWLNegativeDataPropertyAssertionAxiom arg0) {

    }


    public void visit(@Nonnull OWLDifferentIndividualsAxiom arg0) {

    }

    public void visit(@Nonnull OWLDisjointDataPropertiesAxiom arg0) {

    }

    public void visit(@Nonnull OWLDisjointObjectPropertiesAxiom arg0) {

    }

    public void visit(@Nonnull OWLObjectPropertyRangeAxiom arg0) {

    }

    public void visit(@Nonnull OWLObjectPropertyAssertionAxiom arg0) {

    }

    public void visit(@Nonnull OWLFunctionalObjectPropertyAxiom arg0) {

    }

    public void visit(@Nonnull OWLSubObjectPropertyOfAxiom arg0) {

    }

    public void visit(@Nonnull OWLDisjointUnionAxiom arg0) {

    }

    public void visit(@Nonnull OWLSymmetricObjectPropertyAxiom arg0) {

    }


    public void visit(@Nonnull OWLDataPropertyRangeAxiom arg0) {

    }

    public void visit(@Nonnull OWLFunctionalDataPropertyAxiom arg0) {

    }

    public void visit(@Nonnull OWLEquivalentDataPropertiesAxiom arg0) {

    }

    public void visit(@Nonnull OWLClassAssertionAxiom arg0) {

    }

    public void visit(@Nonnull OWLDataPropertyAssertionAxiom arg0) {

    }


    public void visit(@Nonnull OWLTransitiveObjectPropertyAxiom arg0) {

    }

    public void visit(@Nonnull OWLIrreflexiveObjectPropertyAxiom arg0) {

    }

    public void visit(@Nonnull OWLSubDataPropertyOfAxiom arg0) {

    }

    public void visit(@Nonnull OWLInverseFunctionalObjectPropertyAxiom arg0) {

    }


    public void visit(@Nonnull OWLSameIndividualAxiom arg0) {

    }


    public void visit(@Nonnull OWLSubPropertyChainOfAxiom arg0) {

    }


    public void visit(@Nonnull OWLInverseObjectPropertiesAxiom arg0) {

    }


    public void visit(@Nonnull OWLHasKeyAxiom arg0) {

    }


    public void visit(@Nonnull OWLDatatypeDefinitionAxiom arg0) {

    }


    public void visit(@Nonnull SWRLRule arg0) {

    }

}
