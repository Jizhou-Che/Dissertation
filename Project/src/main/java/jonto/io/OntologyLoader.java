package jonto.io;

import jonto.utilities.OWLUtilities;
import jonto.utilities.Utilities;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;

import java.util.HashSet;
import java.util.Set;

public class OntologyLoader {
    protected OWLDataFactory dataFactory;
    protected OWLOntologyManager ontoManager;
    protected OWLOntology onto;

    protected String iri_onto_str;

    protected int size_signature;
    protected int size_classes;

    protected Set<OWLAxiom> axiomSet = new HashSet<OWLAxiom>();

    private final boolean keepLogicalAxiomsOnly;


    public OntologyLoader(String phy_iri_onto, boolean keepLogicalAxiomsOnly) throws OWLOntologyCreationException {
        ontoManager = OWLUtilities.createOWLOntologyManager();

        dataFactory = ontoManager.getOWLDataFactory();
        this.keepLogicalAxiomsOnly = keepLogicalAxiomsOnly;
        loadOWLOntology(phy_iri_onto);
    }

    public OntologyLoader(String phy_iri_onto) throws OWLOntologyCreationException {
        this(phy_iri_onto, false);
    }


    private String getURIFromClasses(OWLOntology ontology) {
        for (OWLClass cls : ontology.getClassesInSignature(Imports.INCLUDED)) {
            return Utilities.getNameSpaceFromURI(cls.getIRI().toString());
        }

        // Default IRI.
        return "";
    }

    public void loadOWLOntology(String phy_iri_onto) throws OWLOntologyCreationException {
        try {
            onto = ontoManager.loadOntology(IRI.create(phy_iri_onto));

            loadOntologyInformation();
        } catch (Exception e) {
            System.err.println("Error loading OWL ontology: " + e.getMessage());
            e.printStackTrace();
            throw new OWLOntologyCreationException();
        }
    }

    protected void loadOntologyInformation() throws OWLOntologyCreationException {
        onto.getOntologyID().getOntologyIRI();
        if (onto.getOntologyID().getOntologyIRI().isPresent()) {
            iri_onto_str = onto.getOntologyID().getOntologyIRI().get().toString();
        } else {
            iri_onto_str = getURIFromClasses(onto);
        }

        size_signature = onto.getSignature(Imports.INCLUDED).size();
        size_classes = onto.getClassesInSignature(Imports.INCLUDED).size();

        if (keepLogicalAxiomsOnly) {
            Set<OWLAxiom> filteredAxioms = new HashSet<OWLAxiom>();
            filteredAxioms.addAll(onto.getTBoxAxioms(Imports.INCLUDED));
            filteredAxioms.addAll(onto.getRBoxAxioms(Imports.INCLUDED));
            filteredAxioms.addAll(onto.getABoxAxioms(Imports.INCLUDED));
            ontoManager.removeOntology(onto);
            onto = ontoManager.createOntology(filteredAxioms, IRI.create(iri_onto_str));
        }
    }

    public void createAxiomSet() {
        // Add All axioms, including annotations and imports closure.
        axiomSet.addAll(onto.getAxioms());
        axiomSet.addAll(onto.getTBoxAxioms(Imports.INCLUDED));
        axiomSet.addAll(onto.getABoxAxioms(Imports.INCLUDED));
        axiomSet.addAll(onto.getRBoxAxioms(Imports.INCLUDED));
    }

    public void clearAxiomSet() {
        axiomSet.clear();
    }

    public Set<OWLAxiom> getAxiomSet() {
        return axiomSet;
    }

    public void clearOntology() {
        ontoManager.removeOntology(onto);
        onto = null;
        ontoManager = null;
    }

    public OWLOntology getOWLOntology() {
        return onto;
    }

    public Set<OWLClass> getClassesInSignature() {
        return onto.getClassesInSignature(Imports.INCLUDED);//With imports!!
    }

    public void saveOntology(String phy_iri_onto) throws Exception {
        ontoManager.saveOntology(onto, new RDFXMLDocumentFormat(), IRI.create(phy_iri_onto));
    }
}
