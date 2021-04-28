package jonto.reasoning;

import jonto.utilities.OWLUtilities;
import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class HermiTReasoner extends DLReasoner {
    protected OWLOntology ontoBase;
    protected OWLOntologyManager ontoManager;
    protected OWLDataFactory datafactory;

    protected OWLReasoner reasoner;
    protected OWLReasonerFactory reasonerFactory;

    protected Set<OWLAxiom> closure;
    protected int closure_lang = DLReasoner.LSUB;

    protected String reasonerName = "HermiT";

    protected boolean isClassified = false;

    public HermiTReasoner(OWLOntologyManager ontoManager, OWLOntology onto) {
        this.ontoManager = ontoManager;
        this.ontoBase = onto;
        datafactory = ontoManager.getOWLDataFactory();

        closure = new HashSet<OWLAxiom>();

        setUpReasoner();
    }


    protected void setUpReasoner() {
        Configuration conf = new Configuration();
        conf.ignoreUnsupportedDatatypes = true;

        reasonerFactory = new ReasonerFactory();
        reasoner = reasonerFactory.createReasoner(ontoBase, conf);
    }

    public void clearStructures() {
        closure.clear();
        reasoner.dispose();
    }

    public void dispose() {
        reasoner.dispose();
    }

    public void interrupt() {
        reasoner.interrupt();
    }

    public boolean isOntologyClassified() {
        return isClassified;
    }


    public void classifyOntologyNoProperties() throws Exception {
        classifyOntology(false);
    }

    public void classifyOntology() throws Exception {
        classifyOntology(true);
    }


    public void classifyOntology(boolean classifyProperties) throws Exception {

        isClassified = false;

        try {
            Set<OWLOntology> importsClosure = new HashSet<OWLOntology>();
            importsClosure.add(ontoBase);
            DLExpressivityChecker checker = new DLExpressivityChecker(importsClosure);

            System.out.println("\nClassifying '" + checker.getDescriptionLogicName() + "' Ontology with " + reasonerName + "...");
            reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);

            if (classifyProperties) {
                reasoner.precomputeInferences(InferenceType.DATA_PROPERTY_HIERARCHY);
                reasoner.precomputeInferences(InferenceType.OBJECT_PROPERTY_HIERARCHY);
            }

            reasoner.precomputeInferences(InferenceType.CLASS_ASSERTIONS);

            isClassified = true;
        } catch (Exception e) {
            System.out.println("Error classifying ontology with " + reasonerName + "\n" + e.getMessage() + "\n" + e.getLocalizedMessage());
            throw new Exception();
        }
    }


    public OWLOntology getOntology() {
        return ontoBase;
    }

    public OWLReasoner getReasoner() {
        return reasoner;
    }

    public OWLReasonerFactory getReasonerFactory() {
        return reasonerFactory;
    }

    public boolean isConsistent() {
        return reasoner.isConsistent();
    }

    public boolean isEntailed(OWLAxiom ax) {
        return reasoner.isEntailed(ax);
    }

    public boolean isSubClassOf(OWLClass cls1, OWLClass cls2) {
        return reasoner.isEntailed(datafactory.getOWLSubClassOfAxiom(cls1, cls2));
    }

    public boolean areDisjointClasses(OWLClass cls1, OWLClass cls2) {
        return !reasoner.isSatisfiable(datafactory.getOWLObjectIntersectionOf(cls1, cls2));
    }

    public boolean areEquivalentClasses(OWLClass cls1, OWLClass cls2) {
        return reasoner.isEntailed(datafactory.getOWLEquivalentClassesAxiom(cls1, cls2));
    }

    public boolean isSatisfiable(OWLClassExpression cls) {
        return reasoner.isSatisfiable(cls);
    }

    public boolean hasUnsatisfiableClasses() {
        return getUnsatisfiableClasses().size() > 0;
    }


    public Set<OWLClass> getUnsatisfiableClasses() {
        try {
            Set<OWLClass> set;

            Node<OWLClass> node = reasoner.getUnsatisfiableClasses();

            set = node.getEntitiesMinusBottom();

            return set;
        } catch (Exception e) {
            System.err.println("Error when invoking the reasoner to get unsatisfiable classes.");
            return new HashSet<OWLClass>();
        }
    }


    public void setLanguage4Closure(int language) {
        closure_lang = language;
    }

    public void createClosure(int language) {
        setLanguage4Closure(language);

        createClosure();
    }

    public void createClosure() {
        if (closure_lang == DLReasoner.LSUB) {
            createClosureLSub();
        }
    }

    public Set<OWLAxiom> getClosure() {
        return closure;
    }

    private void createClosureLSub() {
        try {
            System.out.println("Extracting Lsub closure...");

            for (OWLClass cls : reasoner.getUnsatisfiableClasses()) {
                if (!cls.isOWLNothing()) {
                    OWLAxiom unsatAx = ontoManager.getOWLDataFactory().getOWLSubClassOfAxiom(cls, ontoManager.getOWLDataFactory().getOWLNothing());

                    closure.add(unsatAx);
                }
            }

            OWLOntologyManager classifiedOntoMan = OWLUtilities.createOWLOntologyManager();

            IRI iri;
            if (ontoBase.getOntologyID().getOntologyIRI().isPresent())
                iri = ontoBase.getOntologyID().getOntologyIRI().get();
            else
                iri = IRI.create("http://inferred-ontology.owl");


            OWLOntology inferredOnt = classifiedOntoMan.createOntology(iri);
            InferredOntologyGenerator ontGen = new InferredOntologyGenerator(
                    reasoner, new ArrayList<InferredAxiomGenerator<? extends OWLAxiom>>());


            ontGen.addGenerator(new InferredEquivalentClassAxiomGenerator());
            ontGen.addGenerator(new InferredSubClassAxiomGenerator());

            ontGen.fillOntology(classifiedOntoMan.getOWLDataFactory(), inferredOnt);

            for (OWLAxiom ax : inferredOnt.getAxioms()) {
                if (ax instanceof OWLSubClassOfAxiom) {
                    if (!((OWLSubClassOfAxiom) ax).getSuperClass().isOWLThing() && !ontoBase.containsAxiom(ax)) {
                        closure.add(ax);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
