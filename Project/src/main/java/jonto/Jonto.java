package jonto;

import jonto.indexing.IndexManager;
import jonto.indexing.OntologyProcessing;
import jonto.indexing.ReasonerBasedIndexManager;
import jonto.io.AlignmentWriter;
import jonto.io.RDFReader;
import jonto.io.TXTReader;
import jonto.lexicon.LexicalUtilities;
import jonto.mapping.CandidateMappingManager;
import jonto.mapping.MappingManager;
import jonto.mapping.objects.MappingObjectStr;
import jonto.reasoning.AnchorReasoning;
import jonto.io.OntologyLoader;
import jonto.utilities.Utilities;

import java.util.HashSet;
import java.util.Set;

public class Jonto {
    private IndexManager index;

    private final OntologyLoader onto_loader1;
    private final OntologyLoader onto_loader2;
    private OntologyProcessing onto_process1;
    private OntologyProcessing onto_process2;

    private final Set<MappingObjectStr> refMappings = new HashSet<MappingObjectStr>();

    private MappingManager mapping_extractor;

    private AnchorReasoning anchor_assessment;

    private final int reasoner_id = Parameters.reasoner;

    private final LexicalUtilities lexUtil = new LexicalUtilities();

    public Jonto(String uri1, String uri2, String ref, String out) throws Exception {
        // Lexical indexation and structural indexation.
        lexUtil.loadStopwords();
        onto_loader1 = new OntologyLoader(uri1);
        onto_loader2 = new OntologyLoader(uri2);
        IndexLexiconAndStructure();

        // Load reference mappings.
        if (!ref.equals("")) {
            loadRef(ref);
        }

        // Create anchors and perform interval labelling.
        createAndCleanAnchors();

        // Discover new candidates and index labelling.
        createCandidateMappings();

        // Create anchors for object properties and data properties.
        mapping_extractor.createObjectPropertyAnchors();
        mapping_extractor.createDataPropertyAnchors();
        onto_process1.clearInvertedFiles4properties();
        onto_process2.clearInvertedFiles4properties();

        // Evaluation.
        if (!ref.equals("")) {
            getPrecisionAndRecallMappings();
        }

        // Output alignment.
        Set<MappingObjectStr> alignments = new HashSet<MappingObjectStr>(mapping_extractor.getStringLogMapMappings());
        AlignmentWriter alignment_writer = new AlignmentWriter(out);
        for (MappingObjectStr alignment : alignments) {
            if (alignment.isClassMapping()) {
                alignment_writer.addClassMapping(alignment.getIRIStrEnt1(), alignment.getIRIStrEnt2(), alignment.getMappingDirection(), alignment.getConfidence());
            } else if (alignment.isInstanceMapping()) {
                alignment_writer.addInstanceMapping(alignment.getIRIStrEnt1(), alignment.getIRIStrEnt2(), alignment.getConfidence());
            } else if (alignment.isObjectPropertyMapping()) {
                alignment_writer.addObjPropMapping(alignment.getIRIStrEnt1(), alignment.getIRIStrEnt2(), alignment.getMappingDirection(), alignment.getConfidence());
            } else if (alignment.isDataPropertyMapping()) {
                alignment_writer.addDataPropMapping(alignment.getIRIStrEnt1(), alignment.getIRIStrEnt2(), alignment.getMappingDirection(), alignment.getConfidence());
            }
            alignment_writer.saveFiles();
        }
    }

    private void IndexLexiconAndStructure() {
        index = new ReasonerBasedIndexManager();

        // Process lexicons and inverted tables.
        onto_process1 = new OntologyProcessing(onto_loader1.getOWLOntology(), index, lexUtil);
        onto_process2 = new OntologyProcessing(onto_loader2.getOWLOntology(), index, lexUtil);

        onto_process1.processLexicon();
        onto_process2.processLexicon();

        // Initialise mapping extractor.
        mapping_extractor = new CandidateMappingManager(index, onto_process1, onto_process2);

        mapping_extractor.intersectInvertedTables();

        // Clear stemmed labels in ontologies.
        onto_process1.clearStemmedLabels();
        onto_process2.clearStemmedLabels();

        // Extract class structure.
        onto_process1.setClassStructure();
        onto_process2.setClassStructure();
    }

    private void loadRef(String ref) throws Exception {
        // Check reference format.
        int i = ref.lastIndexOf('.');
        String refType = "txt";
        if (i > 0) {
            refType = ref.substring(i + 1).toLowerCase();
        }

        if (refType.equals("rdf")) {
            RDFReader reader = new RDFReader(ref);

            for (MappingObjectStr mapping : reader.getMappingObjects()) {
                String entity1 = mapping.getIRIStrEnt1();
                String entity2 = mapping.getIRIStrEnt2();

                int index1 = onto_process1.getId4Class(Utilities.getEntityLabelFromURI(entity1));
                int index2 = onto_process2.getId4Class(Utilities.getEntityLabelFromURI(entity2));

                // TODO: 4/23/21 Consider the other mapping direction?
                if (index1 > 0 && index2 > 0 && mapping.getMappingDirection() == MappingObjectStr.EQ) {
                    mapping_extractor.addMapping2GoldStandardAnchors(index1, index2);
                    refMappings.add(new MappingObjectStr(entity1, entity2));
                    mapping_extractor.getStringGoldStandardAnchors().add(new MappingObjectStr(entity1, entity2));
                }
            }
        } else if (refType.equals("txt")) {
            TXTReader reader = new TXTReader(ref);

            String line = reader.readLine();
            while (line != null) {
                if (!line.contains("|")) {
                    line = reader.readLine();
                    continue;
                }

                String[] elements = line.split("\\|");

                int index1 = onto_process1.getId4Class(Utilities.getEntityLabelFromURI(elements[0]));
                int index2 = onto_process2.getId4Class(Utilities.getEntityLabelFromURI(elements[1]));

                if (index1 > 0 && index2 > 0) {
                    mapping_extractor.addMapping2GoldStandardAnchors(index1, index2);
                }

                refMappings.add(new MappingObjectStr(elements[0], elements[1]));
                mapping_extractor.getStringGoldStandardAnchors().add(new MappingObjectStr(elements[0], elements[1]));

                line = reader.readLine();
            }

            reader.closeBuffer();
        }
    }

    private void createAndCleanAnchors() throws Exception {
        mapping_extractor.createAnchors();

        // Anchor reasoning and repair.
        anchor_assessment = new AnchorReasoning(reasoner_id, index, mapping_extractor, onto_loader1.getOWLOntology(), onto_loader2.getOWLOntology(), true);
        anchor_assessment.classifyAndRepairUnsatisfiability();

        // Fix exact anchors after repair.
        mapping_extractor.setExactAsFixed(true);

        // Set reasoner for index.
        index.setJointReasoner(anchor_assessment.getReasoner());
    }

    private void createCandidateMappings() throws Exception {
        mapping_extractor.createCandidates();

        index.clearAlternativeLabels4Classes();

        // Reset candidate anchors.
        anchor_assessment.clearStructures();
        anchor_assessment = new AnchorReasoning(reasoner_id, index, mapping_extractor, onto_loader1.getOWLOntology(), onto_loader2.getOWLOntology(), false);

        anchor_assessment.classifyAndRepairUnsatisfiability();

        // Set reasoner for index.
        index.setJointReasoner(anchor_assessment.getReasoner());
    }

    private void getPrecisionAndRecallMappings() {
        double precision;
        double recall;
        double fmeasure;

        mapping_extractor.setStringAnchors();

        // Intersect discovered mappings with reference.
        Set<MappingObjectStr> intersection = new HashSet<MappingObjectStr>(mapping_extractor.getStringLogMapMappings());
        intersection.retainAll(refMappings);

        // Calculate evaluation results.
        precision = ((double) intersection.size()) / ((double) mapping_extractor.getStringLogMapMappings().size());
        recall = ((double) intersection.size()) / ((double) refMappings.size());
        fmeasure = (2 * recall * precision) / (precision + recall);

        // Print evaluation results.
        System.out.println("Number of mappings: " + mapping_extractor.getStringLogMapMappings().size() + ".");
        System.out.println("\tPrecision: " + precision + ".");
        System.out.println("\tRecall: " + recall + ".");
        System.out.println("\tF measure: " + fmeasure + ".");

        Set<MappingObjectStr> difference1;
        difference1 = new HashSet<MappingObjectStr>(refMappings);
        difference1.removeAll(mapping_extractor.getStringLogMapMappings());
        System.out.println("Reference - Candidate: " + difference1.size() + ".");

        Set<MappingObjectStr> difference2;
        difference2 = new HashSet<MappingObjectStr>(mapping_extractor.getStringLogMapMappings());
        difference2.removeAll(refMappings);
        System.out.println("Candidate - Reference: " + difference2.size());
    }
}
