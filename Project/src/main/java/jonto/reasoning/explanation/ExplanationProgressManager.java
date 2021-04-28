package jonto.reasoning.explanation;

import com.clarkparsia.owlapi.explanation.util.ExplanationProgressMonitor;
import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.HashSet;
import java.util.Set;

public class ExplanationProgressManager implements ExplanationProgressMonitor {
    private final Set<Set<OWLAxiom>> SetOfJustAxioms = new HashSet<Set<OWLAxiom>>();

    boolean cancelled;

    private int maxExplanations;
    private int numberExplanations = 0;

    private int maxExplanationSearch = 500;
    private int explanationSearch = 0;

    private final Set<OWLAxiom> axioms2consider;

    private long max_time = 60000;
    private long time_out;


    public ExplanationProgressManager(Set<OWLAxiom> axioms2consider, int maxExplanations) {

        this.axioms2consider = axioms2consider;
        this.maxExplanations = maxExplanations;
        cancelled = false;
    }


    public boolean isCancelled() {
        if (System.currentTimeMillis() > time_out) {
            cancelled = true;
        }

        return cancelled;

    }


    public void foundExplanation(Set<OWLAxiom> set) {
        handleFoundExplanation(set);
    }


    public void foundAllExplanations() {
        cancelled = true;
    }


    private void handleFoundExplanation(Set<OWLAxiom> explanation) {

        if (cancelled) {
            return;
        }

        explanationSearch++;


        if (!SetOfJustAxioms.contains(explanation)) {
            if (axioms2consider.size() > 0) {
                explanation.retainAll(axioms2consider);
            }


            if (explanation.size() > 0) {
                SetOfJustAxioms.add(explanation);

                numberExplanations++;
            }

        }

        if (numberExplanations >= maxExplanations || explanationSearch > maxExplanationSearch || System.currentTimeMillis() > time_out) {
            cancelled = true;
        }
    }


    public Set<Set<OWLAxiom>> getSetOfExplanantions() {
        return SetOfJustAxioms;
    }

    public void setClearMonitor() {
        SetOfJustAxioms.clear();
        cancelled = false;
        numberExplanations = 0;
        explanationSearch = 0;

        time_out = System.currentTimeMillis() + max_time;


    }


    public void setMaxExplanations(int maxExp) {
        maxExplanations = maxExp;
    }

    public void setMaxExplanationSearch(int maxSearch) {
        maxExplanationSearch = maxSearch;
    }


    public void setMaxTimeOut(long milisec) {
        max_time = milisec;
    }


    public int getNumberExplanations() {
        return numberExplanations;
    }

    public int getNumberExplanationSearch() {
        return explanationSearch;
    }


}
