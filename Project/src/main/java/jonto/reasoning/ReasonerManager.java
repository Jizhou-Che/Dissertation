package jonto.reasoning;

import jonto.utilities.OWLUtilities;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.util.HashSet;
import java.util.Set;


public class ReasonerManager {
    public static int HERMIT = 0;

    public static DLReasoner getReasoner(int reasoner_id, OWLOntologyManager ontoManager, OWLOntology onto) throws Exception {
        if (reasoner_id == HERMIT) {
            return new HermiTReasoner(ontoManager, onto);
        } else {
            // Use HermiT as default.
            return new HermiTReasoner(ontoManager, onto);
        }
    }

    public static DLReasoner getMergedOntologyReasoner(int reasoner_id, OWLOntology onto1, OWLOntology onto2, OWLOntology m) throws Exception {
        return getMergedOntologyReasoner(reasoner_id, onto1.getAxioms(), onto2.getAxioms(), m.getAxioms());
    }

    public static DLReasoner getMergedOntologyReasoner(int reasoner_id, Set<OWLAxiom> onto1_ax, Set<OWLAxiom> onto2_ax, Set<OWLAxiom> m_ax) throws Exception {
        Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
        axioms.addAll(onto1_ax);
        axioms.addAll(onto2_ax);
        axioms.addAll(m_ax);

        OWLOntologyManager managerMerged = OWLUtilities.createOWLOntologyManager();
        OWLOntology mergedOntology = managerMerged.createOntology(axioms, IRI.create("http://integration.owl"));

        if (reasoner_id == HERMIT) {
            return new HermiTReasoner(managerMerged, mergedOntology);
        } else {
            // Use HermiT as default.
            return new HermiTReasoner(managerMerged, mergedOntology);
        }
    }
}
