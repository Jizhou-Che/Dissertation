package jonto.utilities;

import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

public class OWLUtilities {
    public static synchronized OWLDataFactory createOWLDataFactory() {
        return new OWLDataFactoryImpl();
    }

    public static synchronized OWLOntologyManager createOWLOntologyManager() {
        return org.semanticweb.owlapi.apibinding.OWLManager.createOWLOntologyManager();
    }

    public static synchronized void saveOntology(OWLOntology ontology, String IRIstr) throws Exception {
        OWLOntologyManager moduleManager = OWLUtilities.createOWLOntologyManager();

        saveOntology(moduleManager, ontology, IRIstr);
    }

    public static synchronized void saveOntology(OWLOntologyManager moduleManager, OWLOntology ontology, String IRIstr) throws Exception {
        moduleManager.saveOntology(ontology, new RDFXMLDocumentFormat(), IRI.create(IRIstr));
    }
}
