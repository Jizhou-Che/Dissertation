package jonto.reasoning.explanation;

import com.clarkparsia.owlapi.explanation.BlackBoxExplanation;
import com.clarkparsia.owlapi.explanation.ExplanationGenerator;
import com.clarkparsia.owlapi.explanation.HSTExplanationGenerator;
import com.clarkparsia.owlapi.explanation.SatisfiabilityConverter;
import jonto.utilities.OWLUtilities;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.util.Set;


public class BlackBoxExplanationExtractor {
    BlackBoxExplanation bbexp;

    final ExplanationGenerator debugger;

    OWLDataFactory dataFactory;

    ExplanationProgressManager monitor;


    public BlackBoxExplanationExtractor(
            OWLOntology ontology,
            OWLReasonerFactory reasonerFactory,
            OWLReasoner reasoner,
            Set<OWLAxiom> axioms2consider,
            int maxExplanations) {

        dataFactory = OWLUtilities.createOWLDataFactory();

        bbexp = new BlackBoxExplanation(ontology, reasonerFactory, reasoner);

        HSTExplanationGenerator hstGen = new HSTExplanationGenerator(bbexp);

        monitor = new ExplanationProgressManager(axioms2consider, maxExplanations);
        hstGen.setProgressMonitor(monitor);

        debugger = hstGen;
    }


    public void handleExplanations(OWLAxiom ax) {
        monitor.setClearMonitor();

        SatisfiabilityConverter satCon = new SatisfiabilityConverter(dataFactory);
        final OWLClassExpression desc = satCon.convert(ax);
        debugger.getExplanations(desc);

    }

    public Set<Set<OWLAxiom>> getExplanations() {
        return monitor.getSetOfExplanantions();
    }


    public int getNumberExplanations() {
        return monitor.getNumberExplanations();
    }

    public int getNumberExplanationSearch() {
        return monitor.getNumberExplanationSearch();
    }


    public void setMaxExplanations(int maxExp) {
        monitor.setMaxExplanations(maxExp);
    }

    public void setMaxExplanationSearch(int maxSearch) {
        monitor.setMaxExplanationSearch(maxSearch);
    }

    public void setTimeOut(long miliseconds) {
        monitor.setMaxTimeOut(miliseconds);
    }

    public boolean isTimedOut() {
        return monitor.isCancelled();
    }

}
