package jonto.reasoning;

import jonto.indexing.IndexManager;
import jonto.mapping.MappingManager;
import jonto.reasoning.explanation.BlackBoxExplanationExtractor;
import jonto.reasoning.explanation.PlanExtractor;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;

import javax.annotation.Nonnull;
import java.util.*;

public class AnchorReasoning {
    private final IndexManager index;

    private final MappingManager mapping_extractor;

    private final OWLOntology onto1;
    private final OWLOntology onto2;

    private DLReasoner reasoner_access;

    private BlackBoxExplanationExtractor explanations_onto;

    private final int reasoner_id;

    private final List<Set<OWLAxiom>> justifications_unsat = new ArrayList<Set<OWLAxiom>>();

    private List<Set<OWLAxiom>> repair_plans;

    private final Map<OWLClass, Integer> owlclass2identifier = new HashMap<OWLClass, Integer>();
    private final Map<OWLDataProperty, Integer> owldprop2identifier = new HashMap<OWLDataProperty, Integer>();
    private final Map<OWLObjectProperty, Integer> owloprop2identifier = new HashMap<OWLObjectProperty, Integer>();

    private final MappingAxiomVisitor mappingVisitor = new MappingAxiomVisitor();

    private final Set<OWLAxiom> mappingAxioms = new HashSet<OWLAxiom>();
    private final Set<OWLAxiom> mappingAxioms2repair = new HashSet<OWLAxiom>();


    private final boolean review_anchors;

    private StructuralReasonerExtended strctReasoner;

    public AnchorReasoning(
            int ReasonerID,
            IndexManager index,
            MappingManager mapping_extractor,
            OWLOntology onto1,
            OWLOntology onto2,
            boolean review_anchors) throws Exception {
        this.reasoner_id = ReasonerID;
        this.index = index;
        this.mapping_extractor = mapping_extractor;
        this.onto1 = onto1;
        this.onto2 = onto2;
        this.review_anchors = review_anchors;

        setUpReasoner(false);
    }


    public void classifyAndRepairUnsatisfiability() throws Exception {
        System.out.println("Classifying...");

        getOWLAxioms4Mappings();

        setUpReasoner(true);
        setUpBlackBosExplanationManager();

        strctReasoner = new StructuralReasonerExtended(reasoner_access.getOntology());

        while (reasoner_access.hasUnsatisfiableClasses()) {
            if (!repairUnsatisfiability()) {
                break;
            }

            applyBestRepairPlan();

            clearStructures();

            getOWLAxioms4Mappings();

            setUpReasoner(true);
            setUpBlackBosExplanationManager();
        }

        System.out.println("Unsatisfiable classes after cleaning: " + reasoner_access.getUnsatisfiableClasses().size());
    }


    public DLReasoner getReasoner() {
        return reasoner_access;
    }


    private void getOWLAxioms4Mappings() {
        mappingAxioms.clear();
        mappingAxioms2repair.clear();

        if (review_anchors) {
            for (int ide1 : mapping_extractor.getMappings().keySet()) {
                for (int ide2 : mapping_extractor.getMappings().get(ide1)) {
                    if (ide1 < ide2) {
                        mappingAxioms.add(
                                index.getFactory().getOWLEquivalentClassesAxiom(
                                        index.getOWLClass4ConceptIndex(ide1),
                                        index.getOWLClass4ConceptIndex(ide2)
                                )
                        );

                        mappingAxioms2repair.add(
                                index.getFactory().getOWLEquivalentClassesAxiom(
                                        index.getOWLClass4ConceptIndex(ide1),
                                        index.getOWLClass4ConceptIndex(ide2)
                                )
                        );

                        owlclass2identifier.put(index.getOWLClass4ConceptIndex(ide1), ide1);
                        owlclass2identifier.put(index.getOWLClass4ConceptIndex(ide2), ide2);
                    }
                }
            }
        } else {
            for (int ide1 : mapping_extractor.getFixedMappings().keySet()) {
                for (int ide2 : mapping_extractor.getFixedMappings().get(ide1)) {
                    if (ide1 < ide2) {
                        mappingAxioms.add(
                                index.getFactory().getOWLEquivalentClassesAxiom(
                                        index.getOWLClass4ConceptIndex(ide1),
                                        index.getOWLClass4ConceptIndex(ide2)
                                )
                        );

                        owlclass2identifier.put(index.getOWLClass4ConceptIndex(ide1), ide1);
                        owlclass2identifier.put(index.getOWLClass4ConceptIndex(ide2), ide2);
                    }
                }
            }

            for (int ide1 : mapping_extractor.getMappings2Review().keySet()) {
                for (int ide2 : mapping_extractor.getMappings2Review().get(ide1)) {
                    if (ide1 < ide2) {
                        mappingAxioms.add(
                                index.getFactory().getOWLEquivalentClassesAxiom(
                                        index.getOWLClass4ConceptIndex(ide1),
                                        index.getOWLClass4ConceptIndex(ide2)
                                )
                        );

                        mappingAxioms2repair.add(
                                index.getFactory().getOWLEquivalentClassesAxiom(
                                        index.getOWLClass4ConceptIndex(ide1),
                                        index.getOWLClass4ConceptIndex(ide2)
                                )
                        );

                        owlclass2identifier.put(index.getOWLClass4ConceptIndex(ide1), ide1);
                        owlclass2identifier.put(index.getOWLClass4ConceptIndex(ide2), ide2);
                    }
                }
            }


        }


        int ide2;
        for (int ide1 : mapping_extractor.getDataPropertyAnchors().keySet()) {
            ide2 = mapping_extractor.getDataPropertyAnchors().get(ide1);

            mappingAxioms.add(
                    index.getFactory().getOWLEquivalentDataPropertiesAxiom(
                            index.getOWLDataProperty4PropertyIndex(ide1),
                            index.getOWLDataProperty4PropertyIndex(ide2)
                    )
            );

            mappingAxioms2repair.add(
                    index.getFactory().getOWLEquivalentDataPropertiesAxiom(
                            index.getOWLDataProperty4PropertyIndex(ide1),
                            index.getOWLDataProperty4PropertyIndex(ide2)
                    )
            );


            owldprop2identifier.put(index.getOWLDataProperty4PropertyIndex(ide1), ide1);
            owldprop2identifier.put(index.getOWLDataProperty4PropertyIndex(ide2), ide2);
        }

        for (int ide1 : mapping_extractor.getObjectPropertyAnchors().keySet()) {
            ide2 = mapping_extractor.getObjectPropertyAnchors().get(ide1);

            mappingAxioms.add(
                    index.getFactory().getOWLEquivalentObjectPropertiesAxiom(
                            index.getOWLObjectProperty4PropertyIndex(ide1),
                            index.getOWLObjectProperty4PropertyIndex(ide2)
                    )
            );

            mappingAxioms2repair.add(
                    index.getFactory().getOWLEquivalentObjectPropertiesAxiom(
                            index.getOWLObjectProperty4PropertyIndex(ide1),
                            index.getOWLObjectProperty4PropertyIndex(ide2)
                    )
            );

            owloprop2identifier.put(index.getOWLObjectProperty4PropertyIndex(ide1), ide1);
            owloprop2identifier.put(index.getOWLObjectProperty4PropertyIndex(ide2), ide2);
        }
    }


    public void clearStructures() {
        reasoner_access.clearStructures();

        owlclass2identifier.clear();
        owldprop2identifier.clear();
        owloprop2identifier.clear();

        mappingAxioms.clear();
        mappingAxioms2repair.clear();

        explanations_onto = null;

        justifications_unsat.clear();

    }


    private void setUpReasoner(boolean classify) throws Exception {
        // TODO: 4/14/21 Consider setting up the reasoner with not only TBox axioms.
        reasoner_access = ReasonerManager.getMergedOntologyReasoner(
                reasoner_id,
                onto1.getTBoxAxioms(Imports.INCLUDED),
                onto2.getTBoxAxioms(Imports.INCLUDED),
                mappingAxioms);

        if (classify) {
            reasoner_access.classifyOntology();
        }
    }


    private void setUpBlackBosExplanationManager() {
        int max_explanations = 1;
        explanations_onto = new BlackBoxExplanationExtractor(
                reasoner_access.getOntology(),
                reasoner_access.getReasonerFactory(),
                reasoner_access.getReasoner(),
                mappingAxioms2repair,
                max_explanations
        );
    }


    private boolean repairUnsatisfiability() {
        int max_unsat4repair = 100;
        int num = 0;

        explanations_onto.setMaxExplanations(1);

        Set<OWLClass> topUnsat = new HashSet<OWLClass>();

        if (reasoner_access.getUnsatisfiableClasses().size() < 5000) {

            Set<OWLClass> excluded = new HashSet<OWLClass>();

            List<OWLClass> initialUnsat = new ArrayList<OWLClass>(reasoner_access.getUnsatisfiableClasses());

            boolean isTop;

            for (int i = 0; i < initialUnsat.size(); i++) {

                if (excluded.contains(initialUnsat.get(i))) {
                    continue;
                }

                isTop = true;

                for (int j = 0; j < initialUnsat.size(); j++) {
                    if (i == j) {
                        continue;
                    }

                    if (strctReasoner.areEquivalent(initialUnsat.get(i), initialUnsat.get(j))) {
                        excluded.add(initialUnsat.get(j));
                    } else if (strctReasoner.isSubClassOf(initialUnsat.get(j), initialUnsat.get(i))) {
                        excluded.add(initialUnsat.get(j));
                    } else if (strctReasoner.isSubClassOf(initialUnsat.get(i), initialUnsat.get(j))) {
                        isTop = false;
                        break;
                    }

                }

                if (isTop) {
                    topUnsat.add(initialUnsat.get(i));
                    if (topUnsat.size() >= max_unsat4repair) {
                        break;
                    }
                }

            }

            initialUnsat.clear();
            excluded.clear();
        }


        if (topUnsat.isEmpty()) {
            topUnsat = reasoner_access.getUnsatisfiableClasses();
        }


        for (OWLClass cls : topUnsat) {
            explanations_onto.handleExplanations(index.getFactory().getOWLSubClassOfAxiom(cls, index.getFactory().getOWLNothing()));

            justifications_unsat.addAll(explanations_onto.getExplanations());

            num++;
            if (num > max_unsat4repair) {
                break;
            }

        }

        topUnsat.clear();

        PlanExtractor planExtractor = new PlanExtractor(justifications_unsat);
        planExtractor.extractPlans();
        repair_plans = planExtractor.getAllPlansAx();

        if (repair_plans.size() == 0) {
            System.out.println("No repairs were found!");
            return false;
        }

        return true;
    }

    private void applyBestRepairPlan() {
        Set<OWLAxiom> best_repair = null;

        double min_conf = 10000;
        double conf;

        mappingVisitor.setDeleteAxiom(false);

        for (Set<OWLAxiom> repair : repair_plans) {
            conf = getConfidence4Plan(repair);
            if (min_conf > conf) {
                min_conf = conf;
                best_repair = repair;
            }
        }

        mappingVisitor.setDeleteAxiom(true);

        for (OWLAxiom ax : best_repair) {
            ax.accept(mappingVisitor);
        }
    }


    private double getConfidence4Plan(Set<OWLAxiom> repair) {
        double conf = 0.0;

        for (OWLAxiom ax : repair) {
            ax.accept(mappingVisitor);
            conf += mappingVisitor.conf;
        }

        return conf;
    }

    public class MappingAxiomVisitor extends ELAxiomVisitor {
        public int ide1;
        public int ide2;
        public int type;
        public double conf;
        private boolean deleteAxiom = false;

        public void setDeleteAxiom(boolean delete_axiom) {
            deleteAxiom = delete_axiom;
        }

        public void visit(@Nonnull OWLSubClassOfAxiom ax) {
            ide1 = owlclass2identifier.get(ax.getSubClass().asOWLClass());
            ide2 = owlclass2identifier.get(ax.getSuperClass().asOWLClass());
            type = 0;
            conf = mapping_extractor.getConfidence4Mapping(ide1, ide2);

            deleteSubMapping();


        }

        public void visit(@Nonnull OWLEquivalentClassesAxiom ax) {
            ide1 = owlclass2identifier.get(ax.getClassExpressionsAsList().get(0).asOWLClass());
            ide2 = owlclass2identifier.get(ax.getClassExpressionsAsList().get(1).asOWLClass());
            type = 1;
            conf = mapping_extractor.getConfidence4Mapping(ide1, ide2);

            deleteEquivMapping();

        }


        public void visit(@Nonnull OWLEquivalentObjectPropertiesAxiom ax) {

            Iterator<OWLObjectPropertyExpression> it = ax.getProperties().iterator();

            ide1 = owloprop2identifier.get(it.next().asOWLObjectProperty());
            ide2 = owloprop2identifier.get(it.next().asOWLObjectProperty());
            type = 2;
            conf = mapping_extractor.getConfidence4ObjectPropertyAnchor(ide1, ide2);

            if (deleteAxiom) {
                mapping_extractor.getObjectPropertyAnchors().remove(ide1);
            }


        }

        public void visit(@Nonnull OWLEquivalentDataPropertiesAxiom ax) {

            Iterator<OWLDataPropertyExpression> it = ax.getProperties().iterator();
            ide1 = owldprop2identifier.get(it.next().asOWLDataProperty());
            ide2 = owloprop2identifier.get(it.next().asOWLDataProperty());
            type = 3;
            conf = mapping_extractor.getConfidence4DataPropertyAnchor(ide1, ide2);

            if (deleteAxiom) {
                mapping_extractor.getDataPropertyAnchors().remove(ide1);
            }

        }


        private void deleteSubMapping() {
            if (deleteAxiom) {
                if (review_anchors) {
                    mapping_extractor.removeSubMappingFromStructure(ide1, ide2);
                } else {
                    mapping_extractor.removeSubMappingFromMappings2Review(ide1, ide2);
                }

                mapping_extractor.addSubMapping2ConflictiveAnchors(ide1, ide2);
            }
        }

        private void deleteEquivMapping() {
            if (deleteAxiom) {
                if (review_anchors) {
                    mapping_extractor.removeSubMappingFromStructure(ide1, ide2);
                    mapping_extractor.removeSubMappingFromStructure(ide2, ide1);
                } else {
                    mapping_extractor.removeSubMappingFromMappings2Review(ide1, ide2);
                    mapping_extractor.removeSubMappingFromMappings2Review(ide2, ide1);
                }

                mapping_extractor.addSubMapping2ConflictiveAnchors(ide1, ide2);
                mapping_extractor.addSubMapping2ConflictiveAnchors(ide2, ide1);
            }
        }
    }
}
