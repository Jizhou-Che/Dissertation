package jonto.mapping;

import jonto.Parameters;
import jonto.indexing.IndexManager;
import jonto.indexing.OntologyProcessing;
import jonto.indexing.entities.ClassIndex;
import jonto.mapping.assessment.DataPropertyMappingAssessment;
import jonto.mapping.assessment.InstanceMatchingAssessment;
import jonto.mapping.assessment.ObjectPropertyMappingAssessment;
import jonto.mapping.objects.MappingObjectIdentifiers;
import jonto.mapping.objects.MappingObjectStr;

import java.util.*;

/**
 * This class extracts the anchors by intersecting the lexical inverted files
 * associated with the respective ontologies. It will also manage the discovery of
 * further mappings
 *
 * @author Ernesto Jimenez-Ruiz
 * Created: Dec 13, 2011
 */
public class CandidateMappingManager extends MappingManager {


    private OntologyProcessing onto_process1;
    private OntologyProcessing onto_process2;

    private Set<Set<String>> if_exact_intersection;
    private Set<Set<String>> if_stemming_intersection;
    private Set<Set<String>> if_weak_intersection;

    private Set<Set<String>> if_exact_intersection4data_prop;

    private Set<Set<String>> if_exact_intersection4obj_prop;

    private Set<Set<String>> if_exact_intersection4individuals;
    private Set<String> if_weak_intersection4individuals;
    private Set<String> if_roleassertions_intersection4individuals;

    private Set<MappingObjectIdentifiers> input_mappings = new HashSet<MappingObjectIdentifiers>();

    private int IF_VALIDATED = 0;
    private int IF_EXACT = 1;
    private int IF_STEMMING = 2;
    private int IF_WEAK = 3;


    public CandidateMappingManager(
            IndexManager index,
            OntologyProcessing onto_process1,
            OntologyProcessing onto_process2) {

        this.index = index;
        this.onto_process1 = onto_process1;
        this.onto_process2 = onto_process2;

    }


    public void clearIntersectedInvertedFilesExact() {
        if_exact_intersection.clear();

    }


    public void clearIntersectedInvertedFilesStemmingAndWeak() {
        if_stemming_intersection.clear();
        if_weak_intersection.clear();
    }


    public void clearIntersectedInvertedFiles4Properties() {
        if_exact_intersection4data_prop.clear();
        if_exact_intersection4obj_prop.clear();

    }


    /**
     * Intersects exact and stemming IFs, extract IF weak and intersects them
     */
    public void intersectInvertedTables() {

        //INTERSECTION

        //DATA PROP IF
        if_exact_intersection4data_prop = onto_process1.getInvertedFileExactMatching4DataProp().keySet();
        if_exact_intersection4data_prop.retainAll(onto_process2.getInvertedFileExactMatching4DataProp().keySet());
        onto_process2.getInvertedFileExactMatching4DataProp().keySet().retainAll(if_exact_intersection4data_prop);

        //OBJ PROP IF
        if_exact_intersection4obj_prop = onto_process1.getInvertedFileExactMatching4ObjProp().keySet();
        if_exact_intersection4obj_prop.retainAll(onto_process2.getInvertedFileExactMatching4ObjProp().keySet());
        onto_process2.getInvertedFileExactMatching4ObjProp().keySet().retainAll(if_exact_intersection4obj_prop);


        //INDIVIDUALS IF exact
        if_exact_intersection4individuals = onto_process1.getInvertedFileMatching4Individuals().keySet();
        if_exact_intersection4individuals.retainAll(onto_process2.getInvertedFileMatching4Individuals().keySet());
        onto_process2.getInvertedFileMatching4Individuals().keySet().retainAll(if_exact_intersection4individuals);


        //INDIVIDUALS IF weak
        if_weak_intersection4individuals = onto_process1.getInvertedFileWeakMatching4Individuals().keySet();
        if_weak_intersection4individuals.retainAll(onto_process2.getInvertedFileWeakMatching4Individuals().keySet());
        onto_process2.getInvertedFileWeakMatching4Individuals().keySet().retainAll(if_weak_intersection4individuals);


        //INDIVIDUALS IF role assertions
        if_roleassertions_intersection4individuals = onto_process1.getInvertedFileRoleAssertions().keySet();
        if_roleassertions_intersection4individuals.retainAll(onto_process2.getInvertedFileRoleAssertions().keySet());
        onto_process2.getInvertedFileRoleAssertions().keySet().retainAll(if_roleassertions_intersection4individuals);


        Map<Set<String>, Set<Integer>> if_difference1;
        Map<Set<String>, Set<Integer>> if_difference2;


        //INT EXACT
        //We perform intersection an we only keep in inverted file the intersected elements
        if_exact_intersection = onto_process1.getInvertedFileExactMatching().keySet();
        if_exact_intersection.retainAll(onto_process2.getInvertedFileExactMatching().keySet());
        onto_process2.getInvertedFileExactMatching().keySet().retainAll(if_exact_intersection);


        //WEAK STEMMING
        if_stemming_intersection = new HashSet<Set<String>>(onto_process1.getInvertedFileWeakLabelsStemming().keySet());
        if_stemming_intersection.retainAll(onto_process2.getInvertedFileWeakLabelsStemming().keySet());


        //NOT MATCHED IN IF STEMMING -> TO WEAK ENTRIES
        if_difference1 = new HashMap<Set<String>, Set<Integer>>(onto_process1.getInvertedFileWeakLabelsStemming());
        //Reduce IF
        onto_process1.getInvertedFileWeakLabelsStemming().keySet().retainAll(if_stemming_intersection);
        if_difference1.keySet().removeAll(if_stemming_intersection);
        onto_process1.addEntries2InvertedFileWeakLabels(if_difference1);
        if_difference1.clear();


        if_difference2 = new HashMap<Set<String>, Set<Integer>>(onto_process2.getInvertedFileWeakLabelsStemming());
        //Reduce IF
        onto_process2.getInvertedFileWeakLabelsStemming().keySet().retainAll(if_stemming_intersection);
        if_difference2.keySet().removeAll(if_stemming_intersection);
        onto_process2.addEntries2InvertedFileWeakLabels(if_difference2);
        if_difference2.clear();


        //Extract weak mappings
        onto_process1.setInvertedFileWeakLabels();
        onto_process2.setInvertedFileWeakLabels();



        //We perform intersection an we only keep in inverted file the intersected elements
        if_weak_intersection = onto_process1.getInvertedFileWeakLabels().keySet();
        if_weak_intersection.retainAll(onto_process2.getInvertedFileWeakLabels().keySet());
        onto_process2.getInvertedFileWeakLabels().keySet().retainAll(if_weak_intersection);

    }


    /**
     * This method extract all (weak) mappings. This set will only be used for scope purposes
     */
    public void extractAllWeakMappings() {
        for (Set<String> set_str : if_exact_intersection) {
            for (int ide1 : onto_process1.getInvertedFileExactMatching().get(set_str)) {
                for (int ide2 : onto_process2.getInvertedFileExactMatching().get(set_str)) {
                    addEquivMapping2ListOfWeakAnchors(ide1, ide2);
                }
            }
        }

        for (Set<String> set_str : if_weak_intersection) {
            for (int ide1 : onto_process1.getInvertedFileWeakLabels().get(set_str)) {
                for (int ide2 : onto_process2.getInvertedFileWeakLabels().get(set_str)) {
                    addEquivMapping2ListOfWeakAnchors(ide1, ide2);
                }
            }
        }

        //Count all weak anchors
        int weak_anchors = 0;
        for (int ide1 : allWeakMappings1N.keySet()) {
            weak_anchors += allWeakMappings1N.get(ide1).size();
        }


    }


    public void extractCandidatesSubsetFromWeakMappings() {

        extractSubsetFromWeakMapping();
        extractSubsetFromWeakMappingFrequency();

        //Clear structures
        //Clear weak
        onto_process1.clearInvertedFileWeak();
        onto_process2.clearInvertedFileWeak();
        if_weak_intersection.clear();


    }


    public void createAnchors() {
        createAnchors(false);
    }


    @Override
    public void createAnchors(boolean are_input_mapping_validated) {

        try {

            //From input mappings (e.g. composed mappings given as input)
            createCandidatesFromInputMappings(are_input_mapping_validated);

            //Inverted file intersection
            createCandidatesFromExactIF();


            //Dataprop and ObjectProp. Currently is at the end of the process


            //Clear
            //Clear Exact IF
            onto_process1.clearInvertedFilesExact();
            onto_process2.clearInvertedFilesExact();

            clearIntersectedInvertedFilesExact();
            //clearIntersectedInvertedFiles4Properties();


        } catch (Exception e) {
            System.err.println("Error extracting anchors: " + e.getMessage());
            e.printStackTrace();
        }

    }


    /**
     * We will increase the number of anchors and create the set of mappings 2 ask
     */
    // TODO: 4/23/21 Analyse necessity.
    public void createCandidates() {
        try {
            //Inverted file intersection
            assessAnchors2Review(); //Mappings from IF exact to assess

            //Stemmming extraction
            createCandidates4StemmingLikeAnchors();
            //clear
            onto_process1.clearInvertedFileStemming();
            onto_process2.clearInvertedFileStemming();
            if_stemming_intersection.clear();


            //Deprecated EXPANSION from LogMap 1
            //createAnchorsExpansion(); //Not good results (does not improves what extracted by weak mappings)

            //Extracts/assess weak mappings
            createCandidates4WeakAnchors();

            //We remove the set of week candidate mappings: already in correspondent sets
            //clearWeakCandidateAnchors();
            weakCandidateMappings1N.clear();
        } catch (Exception e) {
            System.err.println("Error extracting anchors: " + e.getMessage());
            e.printStackTrace();
        }

    }


    public void processInputMappings(Set<MappingObjectStr> mappings) {

        for (MappingObjectStr mapping : mappings) {

            if (mapping.isClassMapping()) {

                int ide1 = onto_process1.getIdentifier4ConceptIRI(mapping.getIRIStrEnt1());
                int ide2 = onto_process2.getIdentifier4ConceptIRI(mapping.getIRIStrEnt2());

                if (ide1 >= 0 && ide2 >= 0) {
                    input_mappings.add(
                            new MappingObjectIdentifiers(
                                    onto_process1.getIdentifier4ConceptIRI(mapping.getIRIStrEnt1()),
                                    onto_process2.getIdentifier4ConceptIRI(mapping.getIRIStrEnt2()))
                    );
                } else {
                    System.err.println("Mapping with no direct correspondence to ids: " + mapping);
                }
            }
        }
    }


    /**
     * Create candidates from input mappings (e.g. )
     */
    private void createCandidatesFromInputMappings(boolean are_input_mapping_validated) throws Exception {

        //int candidates=0;

        for (MappingObjectIdentifiers mapping : input_mappings) {

            if (onto_process1.getDangerousClasses().contains(mapping.getIdentifierOnto1()) ||
                    onto_process2.getDangerousClasses().contains(mapping.getIdentifierOnto2())) {
                continue;
            }

            if (are_input_mapping_validated) {
                evaluateCandidateMapping(mapping.getIdentifierOnto1(), mapping.getIdentifierOnto2(), IF_VALIDATED);
            } else {
                evaluateCandidateMapping(mapping.getIdentifierOnto1(), mapping.getIdentifierOnto2(), IF_EXACT);
            }

        }


    }


    /**
     * Processes the intersected 'exact' inverted files and extract anchors.
     * When ambiguity filter by isub and scope
     */
    private void createCandidatesFromExactIF() throws Exception {

        int candidates = 0;


        /*We need to remove ambiguity*/
        for (Set<String> set_str : if_exact_intersection) {

            if (set_str.isEmpty()) {
                continue;
            }


            for (int ide1 : onto_process1.getInvertedFileExactMatching().get(set_str)) {

                if (onto_process1.getDangerousClasses().contains(ide1)) {
                    continue;
                }

                for (int ide2 : onto_process2.getInvertedFileExactMatching().get(set_str)) {

                    if (onto_process2.getDangerousClasses().contains(ide2)) {
                        continue;
                    }

                    //Decides if mappings should be considered
                    //Checks ambiguity and scope
                    evaluateCandidateMapping(ide1, ide2, IF_EXACT);
                    candidates++;

                }
            }
        }

    }


    /**
     * Evaluates mapping confidence and decides if mappings should be included or must be treated later (e.g.
     * ambiguity cases and mappings without scope)
     *
     * @param ide1
     * @param ide2
     * @param origin
     */
    private void evaluateCandidateMapping(int ide1, int ide2, int origin) {


        if (origin == IF_VALIDATED) {
            evaluateCandidateMappingIFValidated(ide1, ide2);
        } else if (origin == IF_EXACT) {
            evaluateCandidateMappingIFExact(ide1, ide2);
        } else if (origin == IF_STEMMING) {
            evaluateCandidateMappingIFStemming(ide1, ide2);
        } else if (origin == IF_WEAK) {
            evaluateCandidateMappingIFWeak(ide1, ide2);
        }

    }


    /**
     * Evaluation of mappings coming from a validated input
     *
     * @param ide1
     * @param ide2
     */
    private void evaluateCandidateMappingIFValidated(int ide1, int ide2) {


        //Already considered
        if (isMappingAlreadyConsidered(ide1, ide2)) {
            return;
        }

        //We extract and store
        extractISUB4Mapping(ide1, ide2);
        extractISUBAverage4Mapping(ide1, ide2);

        extractScopeAll4Mapping(ide1, ide2);

        getConfidence4Mapping(ide1, ide2); //isub+scope


        //We do not need to filter by isub in exact cases
        //With scope, no ambiguous or best confidence
        addSubMapping2ListOfAnchors(ide1, ide2);
        addSubMapping2ListOfAnchors(ide2, ide1);


    }


    /**
     * Evaluation of mappings coming from exact IF
     *
     * @param ide1
     * @param ide2
     */
    private void evaluateCandidateMappingIFExact(int ide1, int ide2) {


        double scoreScope;
        double scoreISUB;
        double scoreISUB_avg;

        double conf;


        //Already considered
        if (isMappingAlreadyConsidered(ide1, ide2)) {
            return;
        }


        //We extract and store
        scoreISUB = extractISUB4Mapping(ide1, ide2);
        scoreISUB_avg = extractISUBAverage4Mapping(ide1, ide2);

        scoreScope = extractScopeAll4Mapping(ide1, ide2);

        conf = getConfidence4Mapping(ide1, ide2); //isub+scope


        if (scoreISUB < Parameters.good_isub_anchors || scoreISUB_avg < 0.35 || scoreScope <= Parameters.bad_score_scope) {
            addSubMapping2Mappings2Review(ide1, ide2);//only one side, at this stage we consider in this set equivalent mappings
            return;
        }


        int ide1a;
        int ide2a;

        double conf1;
        double conf2;


        boolean ambiguous = false;


        //SCOPE and ISUB>=0.95
        //Ambiguity: cases with scope that are already in exact mappings
        //At this stage we will only allow mappings 1:1
        if (isEntityAlreadyMapped(ide1) && isEntityAlreadyMapped(ide2)) {
            //compare cases (3 cases)
            ///ide1-ide2a
            //ide1a-ide2
            //ide1=ide2

            ide2a = getTargetEntity4Index(ide1);
            ide1a = getTargetEntity4Index(ide2);
            conf1 = getConfidence4Mapping(ide1, ide2a);
            conf2 = getConfidence4Mapping(ide1a, ide2);

            //substitute only if better than both two
            if (conf > conf1 && conf > conf2) {

                moveMapping2ReviewList(ide1, ide2a);
                moveMapping2ReviewList(ide1a, ide2);

                //we add mapping to list in the end
            } else { //Ambiguous
                ambiguous = true;
            }

        } else if (isEntityAlreadyMapped(ide1)) {

            //If better confidence then substitute (add loser to ambiguity)
            ide2a = getTargetEntity4Index(ide1);
            conf1 = getConfidence4Mapping(ide1, ide2a);

            if (conf > conf1) {
                moveMapping2ReviewList(ide1, ide2a);
            } else { //Ambiguous
                ambiguous = true;
            }


        } else if (isEntityAlreadyMapped(ide2)) {

            //If better confidence then substitute (add loser to ambiguity)
            ide1a = getTargetEntity4Index(ide2);
            conf1 = getConfidence4Mapping(ide1a, ide2);

            if (conf > conf1) {
                moveMapping2ReviewList(ide1a, ide2);
            } else { //Ambiguous
                ambiguous = true;
            }
        }


        if (ambiguous) {
            addSubMapping2Mappings2Review(ide1, ide2);
            return;
        }


        //We do not need to filter by isub in exact cases
        //With scope, no ambiguous or best confidence
        addSubMapping2ListOfAnchors(ide1, ide2);
        addSubMapping2ListOfAnchors(ide2, ide1);

    }


    /**
     * Mappings coming from exact IF. We evaluate wrt fixed mappings
     */
    private void assessAnchors2Review() {


        //Mappings to review and
		/*for (int ide1 : weakenedDandG_Mappings1N.keySet()){
			
			for (int ide2 : weakenedDandG_Mappings1N.get(ide1)){
				
				if (!isMappingInferred(ide1, ide2)){
					
					//Add in exact
					addSubMapping2ListOfAnchors(ide1, ide2);
					
				}
			}
		}
		
		//We remove those mappings that are already inferred
		weakenedDandG_Mappings1N.clear();*/


        //Deleted with D&G
        //We try to recover some of them (we give the another chance)
        //Preliminary tests: it seem there is not impact on that
        //Note that we may check them again as a weak mapping
		/*for (int ide1 : getDircardedAnchors().keySet()){
			for (int ide2 : getDircardedAnchors().get(ide1)){
				
				if (isId1SmallerThanId2(ide1, ide2)){
					addSubMapping2Mappings2Review(ide1, ide2);
				}
				else{
					addSubMapping2Mappings2Review(ide2, ide1);
				}		
			}
		}*/


        for (int ide1 : mappings2Review.keySet()) {
            for (int ide2 : mappings2Review.get(ide1)) {

                if (!isId1SmallerThanId2(ide1, ide2)) {
                    continue; //JUST IN CASE
                }

                evaluateAnchor2Review(ide1, ide2);

            }
        }

        mappings2Review.clear();


    }


    private void evaluateAnchor2Review(int ide1, int ide2) {


        //if (isMappingInAnchors(ide1, ide2))
        //	return;

        if (isMappingInConflictWithFixedMappings(ide1, ide2)) {
            addSubMapping2ConflictiveAnchors(ide1, ide2);
            addSubMapping2ConflictiveAnchors(ide2, ide1);
            return;
        }

        if (isMappingDangerousEquiv(ide1, ide2)) {
            addEquivMapping2HardDiscardedAnchors(ide1, ide2);
            isHardMappingInGS(ide1, ide2, 1);// tmp method
            return;
        }

        //Already extracted
        //double scoreISUB = extractISUB4Mapping(ide1, ide2);
        //double scoreISUB_avg = extractISUBAverage4Mapping(ide1, ide2);

        //Scope with only anchors: for future check
        extractScopeAnchors4Mapping(ide1, ide2);

        double scoreScope = extractScopeAll4Mapping(ide1, ide2);


        //Discard if no scope and ambiguity
        if (scoreScope <= Parameters.bad_score_scope && (isEntityAlreadyMapped(ide1) || isEntityAlreadyMapped(ide2))) {// && scoreISUB_avg<0.9

            addEquivMapping2HardDiscardedAnchors(ide1, ide2); //No scope and ambiguous
            isHardMappingInGS(ide1, ide2, 2);//TODO tmp method
            return;
        }

        //Minimum ISUB... as before....
        //Only ambiguity -> ask
        //No scope ->ask


        //Always ask otherwise
        addMapping2ListOfAnchors2AskLogMap(ide1, ide2);


    }


    /**
     * Created candidates for anchors using a stemmed lexicon.
     */
    private void createCandidates4StemmingLikeAnchors() throws Exception {

        int candidates = 0;
        int ambiguity, size1, size2;


        for (Set<String> set_str : if_stemming_intersection) {


            if (set_str.isEmpty()) {
                continue;
            }

            size1 = onto_process1.getInvertedFileWeakLabelsStemming().get(set_str).size();
            size2 = onto_process2.getInvertedFileWeakLabelsStemming().get(set_str).size();


            //Only Low amabiguity
            ambiguity = size1 * size2;
            if (ambiguity >= 7 || size1 >= 4 || size2 >= 4) {//Max cases 3*2  (for FMA-NCI-SNOMED)
                //if (ambiguity>=13 && size1>=5 && size2>=5){//Max cases 3*2 (Mosue?)

                if (ambiguity > 20) {
                    System.out.println("High ambiguity stemming: " + ambiguity);
                }

                continue;
            }

            for (int ide1 : onto_process1.getInvertedFileWeakLabelsStemming().get(set_str)) {

                if (onto_process1.getDangerousClasses().contains(ide1)) {
                    continue;
                }

                for (int ide2 : onto_process2.getInvertedFileWeakLabelsStemming().get(set_str)) {

                    if (onto_process2.getDangerousClasses().contains(ide2)) {
                        continue;
                    }

                    //Decides if mappings should be considered
                    //Checks ambiguity and scope
                    evaluateCandidateMapping(ide1, ide2, IF_STEMMING);
                    candidates++;
                }
            }

        }
    }


    /**
     * Evaluation of mappings coming from stemming IF
     *
     * @param ide1
     * @param ide2
     */
    private void evaluateCandidateMappingIFStemming(int ide1, int ide2) {

        double scoreScope;
        double scoreISUB;
        double confidence;

        //Already in list or discarded
        if (isMappingAlreadyConsidered(ide1, ide2)) {
            return;
        }


        if (isMappingInConflictWithFixedMappings(ide1, ide2)) {
            addSubMapping2ConflictiveAnchors(ide1, ide2);
            addSubMapping2ConflictiveAnchors(ide2, ide1);
            return;
        }


        if (isMappingDangerousEquiv(ide1, ide2)) {
            addEquivMapping2HardDiscardedAnchors(ide1, ide2);
            isHardMappingInGS(ide1, ide2, 1);// tmp method
            return;
        }

        //Ambiguity->discard! with current anchors
        if (isEntityAlreadyMapped(ide1) || isEntityAlreadyMapped(ide2)) {
            addEquivMapping2HardDiscardedAnchors(ide1, ide2);
            isHardMappingInGS(ide1, ide2, 2);// tmp method
            return;
        }

        //Scope with only anchors: for future check
        extractScopeAnchors4Mapping(ide1, ide2);

        scoreScope = extractScopeAll4Mapping(ide1, ide2);
        //We extract and store
        scoreISUB = extractISUB4Mapping(ide1, ide2);
        //if (scoreISUB<0.75)
        //			return; //do not consider...

        confidence = getConfidence4Mapping(ide1, ide2);

        if (scoreScope <= Parameters.bad_score_scope || scoreISUB < Parameters.good_isub_candidates) {

            //For mouse anatomy
            if (scoreISUB > 0.70 && confidence > 0.45) {
                addEquivMapping2DiscardedAnchors(ide1, ide2);
            } else {
                addEquivMapping2HardDiscardedAnchors(ide1, ide2);
                isHardMappingInGS(ide1, ide2, 3);
            }
            return;
        }


        addMapping2ListOfAnchors2AskLogMap(ide1, ide2);


    }


    /**
     * Evaluation of mappings coming from expansion
     *
     * @param ide1
     * @param ide2
     * @deprecated Not used
     */
    private boolean evaluateCandidateMappingExpansion(int ide1, int ide2) {

        double scoreISUB;


        //Already in list or discarded
        if (isMappingAlreadyConsidered(ide1, ide2)) {
            return false;
        }


        scoreISUB = extractISUB4Mapping(ide1, ide2);
        if (scoreISUB < 0.95)//do not consider
        {
            return false;
        }


        //Cases Isub > 0.95

        if (isMappingInConflictWithFixedMappings(ide1, ide2)) {
            addSubMapping2ConflictiveAnchors(ide1, ide2);
            addSubMapping2ConflictiveAnchors(ide2, ide1);
            return false;
        }

        if (isMappingDangerousEquiv(ide1, ide2)) {
            addEquivMapping2HardDiscardedAnchors(ide1, ide2);
            return false;
        }

        //Ambiguity->discard!
        if (isEntityAlreadyMapped(ide1) || isEntityAlreadyMapped(ide2) || scoreISUB < 0.98) {
            //addEquivMapping2HardDiscardedAnchors(ide1, ide2); //to avoid many discards
            return false;
        }

        //>0.98
        addMapping2ListOfAnchors2AskLogMap(ide1, ide2);
        return true;


    }


    /**
     * Creation of candidate mappings from weak anchors. called from main LogMap method
     */
    private void createCandidates4WeakAnchors() {


        //Now the set weakCandidateMappings1N is pre-extracted
        //extractSubsetFromWeakMapping();
        //extractSubsetFromWeakMappingFrequency();

        int candidates = 0;

        //for (int ide1 : weakMappings1N.keySet()){
        for (int ide1 : weakCandidateMappings1N.keySet()) {

            if (onto_process1.getDangerousClasses().contains(ide1)) {
                continue;
            }

            //for (int ide2 : weakMappings1N.get(ide1)){
            for (int ide2 : weakCandidateMappings1N.get(ide1)) {

                if (onto_process2.getDangerousClasses().contains(ide2)) {
                    continue;
                }

                if (!isId1SmallerThanId2(ide1, ide2)) {
                    continue;
                }


                evaluateCandidateMappingIFWeak(ide1, ide2);


                candidates++;

            }

        }
    }


    private void evaluateCandidateMappingIFWeak(int ide1, int ide2) {


        double scoreScopeAnc;
        double scoreScope;
        double scoreISUB;
        double confidence;

        //Only if the mapping is already in list of anchors or in list to ask logmap
        if (isMappingAlreadyInList(ide1, ide2) || isMappingInAnchors2AskLogMap(ide1, ide2) || isMappingInConflictiveSet(ide1, ide2)) {
            return;
        }

        //Already in list or discarded (but not weak yujiao, we give another chance)
        //Probably not necessary anymore
        if (isMappingAlreadyConsidered(ide1, ide2) && !hasWeakMappingSim(ide1, ide2)) {
            return;
        }


        if (isMappingInConflictWithFixedMappings(ide1, ide2)) {
            addSubMapping2ConflictiveAnchors(ide1, ide2);
            addSubMapping2ConflictiveAnchors(ide2, ide1);
            return;
        }


        //Do not consider?
        if (isMappingDangerousEquiv(ide1, ide2)) {
            addEquivMapping2HardDiscardedAnchors(ide1, ide2);
            isHardMappingInGS(ide1, ide2, 1);// tmp method
            return;
        }

        //Ambiguity->discard! with anchors then no consider
        if (isEntityAlreadyMapped(ide1) || isEntityAlreadyMapped(ide2)) { //no anchor
            addEquivMapping2HardDiscardedAnchors(ide1, ide2);
            isHardMappingInGS(ide1, ide2, 2);// tmp method
            return;
        }


        scoreScope = extractScopeAll4Mapping(ide1, ide2);
        //We extract and store
        scoreISUB = extractISUB4Mapping(ide1, ide2);

        confidence = getConfidence4Mapping(ide1, ide2);

        //Scope with only anchors: for future check
        scoreScopeAnc = extractScopeAnchors4Mapping(ide1, ide2);


        if (!hasWeakMappingSim(ide1, ide2)) {

            if (scoreISUB < Parameters.good_isub_candidates && scoreScope <= Parameters.bad_score_scope) {
                //Must be hard discarded always
                addEquivMapping2HardDiscardedAnchors(ide1, ide2);
                //isHardMappingInGS(ide1, ide2, 3);//TODO tmp method
                return;
            }


            if (scoreISUB < Parameters.good_isub_candidates) {
                //if (scoreISUB<0.70 && scoreScope<=0.01)
                //	return;

                //For mouse anatomy
                if (scoreISUB > 0.83 && confidence > 0.45) {
                    addEquivMapping2DiscardedAnchors(ide1, ide2);
                } else {
                    //Must be hard discarded always
                    addEquivMapping2HardDiscardedAnchors(ide1, ide2);
                    //isHardMappingInGS(ide1, ide2, 3);// tmp method
                }

                return;
            }

            addMapping2ListOfAnchors2AskLogMap(ide1, ide2);
        } else {


            if (scoreISUB >= Parameters.good_isub_candidates && scoreScope > Parameters.bad_score_scope) {

                addMapping2ListOfAnchors2AskLogMap(ide1, ide2);
                return;
            }

            if (getSimWeak4Mapping2(ide1, ide2) >= Parameters.good_sim_coocurrence && scoreScope > Parameters.bad_score_scope) {
                addMapping2ListOfAnchors2AskLogMap(ide1, ide2);

                return;
            }


            if (scoreISUB < 0.65) {
                //Always
                addEquivMapping2HardDiscardedAnchors(ide1, ide2);

                return;
            }


            if (scoreISUB > 0.89 || (scoreISUB > 0.80 && confidence > Parameters.good_confidence && scoreScope > Parameters.bad_score_scope) || (confidence > 0.80)) {
                addEquivMapping2DiscardedAnchors(ide1, ide2);


            } else {

                addEquivMapping2HardDiscardedAnchors(ide1, ide2);
                isHardMappingInGS(ide1, ide2, 3);
            }
        }
    }


    private void extractSubsetFromWeakMapping() {
        int size1;
        int size2;
        int ambiguity;


        //Look in allweakmappings

        for (Set<String> set_str : if_weak_intersection) {


            if (set_str.isEmpty()) {
                continue;
            }


            size1 = onto_process1.getInvertedFileWeakLabels().get(set_str).size();
            size2 = onto_process2.getInvertedFileWeakLabels().get(set_str).size();

            //Only Low amabiguity
            ambiguity = size1 * size2;
            //if (ambiguity>=7 || size1>=4 || size2>=4){//Max cases 3*2  (for FMA-NCI-SNOMED)
            if (ambiguity >= 13 && size1 >= 5 && size2 >= 5) {//Max cases 3*2 (Mosue?)
                continue;
            }


            for (int ide1 : onto_process1.getInvertedFileWeakLabels().get(set_str)) {

                if (onto_process1.getDangerousClasses().contains(ide1)) {
                    continue;
                }


                for (int ide2 : onto_process2.getInvertedFileWeakLabels().get(set_str)) {

                    if (onto_process2.getDangerousClasses().contains(ide2)) {
                        continue;
                    }


                    addEquivMapping2ListOfWeakCandidateAnchors(ide1, ide2);

                }
            }
        }
        //count candidateweak anchors
        int weak_anchors = 0;
        for (int ide1 : weakCandidateMappings1N.keySet()) {
            weak_anchors += weakCandidateMappings1N.get(ide1).size();
        }
    }


    /**
     * frequency formula
     */
    private void extractSubsetFromWeakMappingFrequency() {

        int size1, size2;
        String label1, label2;
        Set<Integer> set1, set2;
        Set<String> words1 = new HashSet<String>(), words2 = new HashSet<String>();
//		ArrayList<WeakCandidate> newCandidates = new ArrayList<WeakCandidate>();
        WeakCandidate[] picked = new WeakCandidate[3];
        WeakCandidate cand;
        ClassIndex class1 = null, class2 = null;
//		boolean mark = false;
        double score;
        int f_key;
//		Set<Integer> list_key;
        double iSub, sim;

        for (Set<String> set_str : if_weak_intersection) {
//			set_str = new HashSet<String>();
//			set_str.add("scalp");
//			mark = set_str.toString().contains("tricep");

            set1 = onto_process1.getInvertedFileWeakLabels().get(set_str);
            size1 = set1.size();
            set2 = onto_process2.getInvertedFileWeakLabels().get(set_str);
            size2 = set2.size();

            //For scalability??
            if (size1 > 5 & size2 > 5 && size1 * size2 > 100) //too ambiguous
            //if (size1 > 6 & size2 > 6 && size1 * size2 > 100) //too ambiguous
            {
                continue;
            }

            try {
                f_key = index.getCooccurrenceOfWords(set_str).size();
            } catch (NullPointerException e) {
                System.out.println("NullPointerException   " + set_str.toString());
                continue;
            }

            for (int ide1 : set1) {
//				label1 = (class1 = index.getClassIndex(ide1)).getEntityName();
                label1 = (class1 = index.getClassIndex(ide1)).findStemmedAltLabel(set_str);
                if (label1 == null) {
                    continue;
                }
                //TODO Yujiao - why is there a class named "F" that doesn't have alternativeLabels & other attributes?

//				if (class1.getEntityName().equals("Pseudolobe"))
//					mark = true;

                Collections.addAll(words1, label1.split("_"));

                for (int ide2 : set2) {
//					label2 = (class2 = index.getClassIndex(ide2)).getEntityName();
                    label2 = (class2 = index.getClassIndex(ide2)).findStemmedAltLabel(set_str);
                    if (label2 == null) {
                        continue;
                    }

                    if (!words2.isEmpty()) {
                        words2.clear();
                    }

                    Collections.addAll(words2, label2.split("_"));

                    //ensure that common(label1, label2) == set_str
                    if (!hasCommonWords(words2, words1, set_str)) {
                        continue;
                    }

                    cand = new WeakCandidate(set_str, ide1, ide2, label1, label2, index, f_key);
//					newCandidates.add(cand);
                    insertCandidate(picked, cand);

                }

                for (int i = 0, id1, id2; i < picked.length; ++i) {
                    if (picked[i] == null) {
                        break;
                    }
                    addEquivMapping2ListOfWeakCandidateAnchors((id1 = picked[i].getKey()), (id2 = picked[i].getValue()));

                    sim = picked[i].getScore();

                    addSimWeak2Structure(id1, id2, sim);
                    addSimWeak2Structure(id2, id1, sim);
                    picked[i] = null;
                }

                words1.clear();
            }
			
/*
			if (newCandidates.size() == 0)
				continue;

			Collections.sort(newCandidates, newCandidates.get(0));
			
			for (int i = 0; i < newCandidates.size(); ++i)
			{	
				if (i > 2)// && Lib.dcmp(1. - score) > 0)
					break;
				score = newCandidates.get(i).getScore();
				if (Lib.dcmp(score - WeakCandidate.MINSCORE) <= 0)
					break;
				addEquivMapping2ListOfWeakCandidateAnchors(newCandidates.get(i).getKey(), newCandidates.get(i).getValue());
			}
*/

//			newCandidates.clear();
        }

    }


    private boolean hasCommonWords(Set<String> a, Set<String> b, Set<String> c) {
        for (String word : a) {
            if (b.contains(word) && !c.contains(word)) {
                return false;
            }
        }
        return true;
    }

    private void insertCandidate(WeakCandidate[] picked, WeakCandidate cand) {
        if (cand.getScore() - WeakCandidate.MINSCORE <= 1e-6) {
            return;
        }

        for (int i = 0; i < picked.length; ++i) {
            if (picked[i] == null || cand.compareTo(picked[i]) > 0) {
                if (picked.length - 1 - i >= 0) {
                    System.arraycopy(picked, i, picked, i + 1, picked.length - 1 - i);
                }
                picked[i] = cand;
                break;
            }
        }
    }


    /**
     * We add them iff they are not already inferred and are not in conflict
     * Last Step of LogMap
     *
     * @deprecated
     */
    public void assesWeakenedMappingsDandG2(boolean removeAfterwards, boolean add2Anchors) {
        for (int ide1 : weakenedDandG_Mappings1N.keySet()) {

            for (int ide2 : weakenedDandG_Mappings1N.get(ide1)) {

                if (isMappingInConflictWithFixedMappings(ide1, ide2)) {
                    addSubMapping2ConflictiveAnchors(ide1, ide2);
                } else if (!isMappingInferred(ide1, ide2)) { //If inferred do not add
                    //Add in exact
                    //
                    if (add2Anchors) //Final addition
                    {
                        addSubMapping2ListOfAnchors(ide1, ide2);
                    } else {
                        addSubMapping2Mappings2Review(ide1, ide2); //check with new ones
                    }
                }
            }
        }

        if (removeAfterwards) {
            weakenedDandG_Mappings1N.clear();
        }
    }


    /**
     * Instance ANCHORS
     */
    public void createInstanceAnchors() {

        InstanceMatchingAssessment instanceMappingAssessment = new InstanceMatchingAssessment(index, this);

        double required_confidence;
        double compatibility_factor;
        double confidence;
        //0: ok, 1: disc1, 2: disc2, 3: incompatible
        int type_output;
        //int num=0;

        int num_incompatible_instances = 0;

        //EXACT IF

        boolean ambiguity;
        for (Set<String> if_entry : if_exact_intersection4individuals) {

            //Only those individuals uniquely identified or almost!!
            ambiguity = onto_process1.getInvertedFileMatching4Individuals().get(if_entry).size() > 1 ||
                    onto_process2.getInvertedFileMatching4Individuals().get(if_entry).size() > 1;

            for (int ident1 : onto_process1.getInvertedFileMatching4Individuals().get(if_entry)) {

                for (int ident2 : onto_process2.getInvertedFileMatching4Individuals().get(if_entry)) {

                    //required confidence and comp factor are the same...
                    required_confidence = instanceMappingAssessment.getConfidence4Compatibility(ident1, ident2);
                    compatibility_factor = instanceMappingAssessment.getCompatibilityFactor();


                    //TODO Categories, only for ambiguouss mappings
                    if (!instanceMappingAssessment.haveInstancesCompatibleCategories(ident1, ident2) && ambiguity) {
                        required_confidence = 3.0;
                    }


                    if (required_confidence > 1.0) {
                        num_incompatible_instances++;

                        type_output = 3;//incomp

                    } else {

                        //we extract isub
                        confidence = extractISUB4InstanceMapping(ident1, ident2);


                        if (confidence >= required_confidence) {

                            addInstanceMapping(ident1, ident2, ambiguity);
                            //addSubInstanceMapping(ident2, ident1); we only add one side since we do not split instance mappings

                            type_output = 0;


                        } else {
                            type_output = 1;
                        }
                    }

                    // For output instance mapping files.
                    addOutputType4Indivual(ident1, ident2, type_output);

                    //EXTRACT ISB IF not yet
                    extractISUB4InstanceMapping(ident1, ident2);

                    //add comp factor
                    addCompFactor4Indivual(ident1, ident2, compatibility_factor);

                    //Extract scope
                    extractScope4InstanceMapping(ident1, ident2);


                }

            }

        }//for if exact

        if_exact_intersection4individuals.clear();
        num_incompatible_instances = 0;

        for (String if_entry : if_weak_intersection4individuals) {

            //Only those individuals uniquely identified or almost!!
            ambiguity = onto_process1.getInvertedFileWeakMatching4Individuals().get(if_entry).size() > 1 ||
                    onto_process2.getInvertedFileWeakMatching4Individuals().get(if_entry).size() > 1;


            for (int ident1 : onto_process1.getInvertedFileWeakMatching4Individuals().get(if_entry)) {

                if (isIndividualAlreadyMapped(ident1)) {
                    continue;
                }

                for (int ident2 : onto_process2.getInvertedFileWeakMatching4Individuals().get(if_entry)) {

                    if (isIndividualAlreadyMapped(ident2)) {
                        continue;
                    }

                    //New candidate mapping

                    required_confidence = instanceMappingAssessment.getConfidence4Compatibility(ident1, ident2);
                    compatibility_factor = instanceMappingAssessment.getCompatibilityFactor();

                    if (!instanceMappingAssessment.haveInstancesCompatibleCategories(ident1, ident2) & ambiguity) {
                        required_confidence = 3.0;
                    }


                    if (required_confidence > 1.0) {
                        num_incompatible_instances++;

                        type_output = 3;

                    } else {
                        //we extract isub
                        confidence = extractISUB4InstanceMapping(ident1, ident2);

                        if (confidence >= required_confidence) {

                            addInstanceMapping(ident1, ident2, ambiguity);
                            //addSubInstanceMapping(ident2, ident1); we only add one side

                            type_output = 0;

                        } else {
                            if (confidence > 0.65) {
                                type_output = 1;
                            } else {
                                type_output = 2;
                            }
                        }
                    }

                    // For output instance mapping files.
                    addOutputType4Indivual(ident1, ident2, type_output);

                    //EXTRACT ISB IF not yet
                    extractISUB4InstanceMapping(ident1, ident2);

                    //add comp factor
                    addCompFactor4Indivual(ident1, ident2, compatibility_factor);

                    //Extract scope
                    extractScope4InstanceMapping(ident1, ident2);


                }

            }

        }

        if_weak_intersection4individuals.clear();


        //max ambiguity for these cases
        int max_amb = 2;

        for (String if_entry : if_roleassertions_intersection4individuals) {


            //Only those individuals uniquely identified or almost!!
            if (onto_process1.getInvertedFileRoleAssertions().get(if_entry).size() > max_amb ||
                    onto_process2.getInvertedFileRoleAssertions().get(if_entry).size() > max_amb) {
                continue;
            }


            for (int ident1 : onto_process1.getInvertedFileRoleAssertions().get(if_entry)) {

                if (isIndividualAlreadyMapped(ident1)) {
                    continue;
                }

                for (int ident2 : onto_process2.getInvertedFileRoleAssertions().get(if_entry)) {

                    if (isIndividualAlreadyMapped(ident2)) {
                        continue;
                    }

                    required_confidence = instanceMappingAssessment.getConfidence4Compatibility(ident1, ident2);
                    compatibility_factor = instanceMappingAssessment.getCompatibilityFactor();

                    //TODO Categories
                    if (!instanceMappingAssessment.haveInstancesCompatibleCategories(ident1, ident2)) {
                        required_confidence = 3.0;
                    }


                    if (required_confidence > 1.0) {
                        num_incompatible_instances++;
                        type_output = 3;

                    } else {
                        //we extract isub
                        confidence = extractISUB4InstanceMapping(ident1, ident2);

                        //we do not discard by confidence since if is probably low
                        addInstanceMapping(ident1, ident2, true); //check always

                        type_output = 0;
                    }


                    // For output instance mapping files.

                    addOutputType4Indivual(ident1, ident2, type_output);

                    //EXTRACT ISB IF not yet
                    extractISUB4InstanceMapping(ident1, ident2);

                    //add comp factor
                    addCompFactor4Indivual(ident1, ident2, compatibility_factor);

                    //Extract scope
                    extractScope4InstanceMapping(ident1, ident2);


                }

            }

        }

        if_roleassertions_intersection4individuals.clear();

        //-----------------------------------------------------------------
        //REVISE instance mappings using referenced individuals
        //------------------------------------------------------------------
        double required_sim;
        for (int index1 : instanceMappings1N_ambiguity.keySet()) {
            for (int index2 : instanceMappings1N_ambiguity.get(index1)) {


                double sim = getSimilarityReferredInstances(index1, index2);

                required_sim = 0.20;
                if (getISUB4InstanceMapping(index1, index2) > 0.9) {
                    required_sim = 0.10;
                }


                if (sim > required_sim) {
                    addInstanceMapping(index1, index2);
                }

                //We give another chance to the individuals using their characteristics (e.g. publication count, numb of brothers, num directed films etc.)
                //Charactersitics are a list of values
                if (haveIndividualsSameCharacteristics(index1, index2)) {
                    addInstanceMapping(index1, index2);
                }
            }

        }


    }

    /**
     * Returns Jaccard-based similarity
     */
    private double getSimilarityReferredInstances(int index1, int index2) {

        //For mapped instances we use the target individuals
        Set<Integer> referred1_2 = new HashSet<Integer>();
        Set<Integer> referred1 = index.getReferencedIndividuals4Individual(index1);
        for (int ref_indiv1 : referred1) {
            if (instanceMappings1N.containsKey(ref_indiv1)) {
                referred1_2.addAll(instanceMappings1N.get(ref_indiv1));
            }
            if (instanceMappings1N_ambiguity.containsKey(ref_indiv1)) {
                referred1_2.addAll(instanceMappings1N_ambiguity.get(ref_indiv1));
            }
            if (instanceMappings1N_not_allowed_output.containsKey(ref_indiv1)) {
                referred1_2.addAll(instanceMappings1N_not_allowed_output.get(ref_indiv1));
            }

        }



        Set<Integer> referred2 = index.getReferencedIndividuals4Individual(index2);

        int size_union = referred2.size() + referred1_2.size();




        //intersection
        referred1_2.retainAll(referred2);

        //Jaccard index
        return (double) ((double) referred1_2.size() / (double) size_union);

    }


    private boolean haveIndividualsSameCharacteristics(int index1, int index2) {

        List<Integer> charact1 = index.getCharactersitics4Individual(index1);
        List<Integer> charact2 = index.getCharactersitics4Individual(index2);

        if (charact1.size() != charact2.size()) {
            return false;
        }

        for (int i = 0; i < charact1.size(); i++) {
            if (!charact1.get(i).equals(charact2.get(i))) {
                return false;
            }
        }


        return true;
    }


    /**
     * DATA PROPERTY ANCHORS
     */
    public void createDataPropertyAnchors() {

        // Intersects IF and also uses ISUB


        //if_exact_intersection4data_prop.clear();


        //Entries are only 1 to 1
        for (Set<String> if_entry : if_exact_intersection4data_prop) {

            dataPropertyMappings.put(
                    onto_process1.getInvertedFileExactMatching4DataProp().get(if_entry),
                    onto_process2.getInvertedFileExactMatching4DataProp().get(if_entry)
            );

            dataPropertyMappings2confidence.put(
                    onto_process1.getInvertedFileExactMatching4DataProp().get(if_entry),
                    1.0
            );
        }

        if_exact_intersection4data_prop.clear();


        //ISUB
        double maxconf = -10;
        double score;
        int identmax = -1;

        //use isub to extract references
        for (int ident1 : index.getDataPropIdentifierSet()) {
            maxconf = -10;
            identmax = -1;

            //if (index.getDataPropertyIndex(ident1).getOntologyId()>)
            //	continue;

            if (dataPropertyMappings.containsKey(ident1)) {
                continue;
            }

            for (int ident2 : index.getDataPropIdentifierSet()) {

                if (dataPropertyMappings.containsValue(ident2)) {
                    continue;
                }

                if (ident1 >= ident2) {
                    continue;
                }

                //They are from the same ontology
                if (index.getDataPropertyIndex(ident1).getOntologyId() == index.getDataPropertyIndex(ident2).getOntologyId()) {
                    continue;
                }

                score = getIsubScore4DataPropertyLabels(ident1, ident2);

                if (score > maxconf) {
                    maxconf = score;
                    identmax = ident2;
                }
            }

            if (maxconf > 0.75) { //Input parameter
                dataPropertyMappings.put(ident1, identmax);
                dataPropertyMappings2confidence.put(ident1, maxconf);
            }
        }

        //COMPATIBILITY
        //Analize compatibility of domains and ranges
        //Filter: eg writtenBy (cmt confof)
        evaluateCompatibilityDataPropertyMappings(); //and delete conflictive mappings


    }


    public void evaluateCompatibilityDataPropertyMappings() {

        //COMPATIBILITY
        //Analize compatibility of domains and ranges
        //Filter: eg writtenBy (cmt confof)
        DataPropertyMappingAssessment propertyMappingAssessment = new DataPropertyMappingAssessment(index, this);
        Set<Integer> todelete = new HashSet<Integer>();
        double required_confidence;
        double confidence_mapping;
        for (int ident1 : dataPropertyMappings.keySet()) {

            //required_confidence = propertyMappingAssessment.arePropertiesCompatible(ident1, dataPropertyMappings.get(ident1));
            required_confidence = propertyMappingAssessment.getConfidence4Compatibility(ident1, dataPropertyMappings.get(ident1));


            confidence_mapping = getConfidence4DataPropertyAnchor(ident1, dataPropertyMappings.get(ident1));

            if (confidence_mapping < required_confidence) {
                todelete.add(ident1);

            }
        }//end for


        deleteDataPropertyAnchors(todelete);

        todelete.clear();

    }


    /**
     * OBJECT PROPERTY ANCHORS
     */
    public void createObjectPropertyAnchors() {

        for (Set<String> set_str : if_exact_intersection4obj_prop) {

            objPropertyMappings.put(
                    onto_process1.getInvertedFileExactMatching4ObjProp().get(set_str),
                    onto_process2.getInvertedFileExactMatching4ObjProp().get(set_str)
            );

            objPropertyMappings2confidence.put(
                    onto_process1.getInvertedFileExactMatching4ObjProp().get(set_str),
                    1.0);
        }


        if_exact_intersection4obj_prop.clear();


        ///ISUB
        //use isub to extract references
        double maxconf = -10;
        double score;
        int identmax = -1;

        //use isub to extract references
        for (int ident1 : index.getObjectPropIdentifierSet()) {
            maxconf = -10;
            identmax = -1;

            if (objPropertyMappings.containsKey(ident1)) {
                continue;
            }

            for (int ident2 : index.getObjectPropIdentifierSet()) {

                if (objPropertyMappings.containsValue(ident2)) {
                    continue;
                }


                if (ident1 >= ident2) {
                    continue;
                }

                //They are from the same ontology
                if (index.getObjectPropertyIndex(ident1).getOntologyId() == index.getObjectPropertyIndex(ident2).getOntologyId()) {
                    continue;
                }


                score = getIsubScore4ObjectPropertyLabels(ident1, ident2);

                if (score > maxconf) {
                    maxconf = score;
                    identmax = ident2;
                }
            }

            if (maxconf > 0.75) {
                objPropertyMappings.put(ident1, identmax);
                objPropertyMappings2confidence.put(ident1, maxconf);
            }
        }


        //COMPATIBILITY
        //Analize compatibility of domains and ranges
        //Filter: eg writtenBy (cmt confof)
        evaluateCompatibilityObjectPropertyMappings();


    }


    public void evaluateCompatibilityObjectPropertyMappings() {

        //COMPATIBILITY
        //Analize compatibility of domains and ranges
        //Filter: eg writtenBy (cmt confof)


        ObjectPropertyMappingAssessment propertyMappingAssessment = new ObjectPropertyMappingAssessment(index, this);

        Set<Integer> todelete = new HashSet<Integer>();
        double required_confidence;
        double confidence_mapping;
        for (int ident1 : objPropertyMappings.keySet()) {


            //required_confidence = propertyMappingAssessment.arePropertiesCompatible(ident1, objPropertyMappings.get(ident1));
            required_confidence = propertyMappingAssessment.getConfidence4Compatibility(ident1, objPropertyMappings.get(ident1));

            confidence_mapping = getConfidence4ObjectPropertyAnchor(ident1, objPropertyMappings.get(ident1));
            if (confidence_mapping < required_confidence) {
                todelete.add(ident1);
                //deleteObjectPropertyAnchor(ident1);

            }
        }//end for

        deleteObjectPropertyAnchors(todelete);

        todelete.clear();

    }



    private void deleteDataPropertyAnchors(Set<Integer> todelete) {
        dataPropertyMappings.keySet().removeAll(todelete);
        dataPropertyMappings2confidence.keySet().removeAll(todelete);

    }

    private void deleteObjectPropertyAnchors(Set<Integer> todelete) {
        objPropertyMappings.keySet().removeAll(todelete);
        objPropertyMappings2confidence.keySet().removeAll(todelete);

    }


    private Map<Integer, Set<Integer>> visitedScopeMappings = new HashMap<Integer, Set<Integer>>();

    private boolean isaVisitedScopeMapping(int index1, int index2) {

        if (visitedScopeMappings.containsKey(index1)) {
            if (visitedScopeMappings.get(index1).contains(index2)) {
                return true;
            }
        }
        return false;

    }

    private void addVisistedScopeMapping(int index1, int index2) {
        if (!visitedScopeMappings.containsKey(index1)) {
            visitedScopeMappings.put(index1, new HashSet<Integer>());
        }
        visitedScopeMappings.get(index1).add(index2);
    }


    /**
     * We anaylize the scope of current mappings
     *
     * @deprecated
     */
    protected void createAnchorsExpansion() {

        //We transform structure anchorMappings1N to list of objects

        LinkedList<MappingObjectIdentifiers> list_mappings = new LinkedList<MappingObjectIdentifiers>();

        for (int ide1 : logmapMappings1N.keySet()) {
            for (int ide2 : logmapMappings1N.get(ide1)) {

                //we consider the equivalence only
                if (isId1SmallerThanId2(ide1, ide2)) {

                    list_mappings.add(new MappingObjectIdentifiers(ide1, ide2));

                }
            }
        }
		
		
		/*for (int ide1 : candidateMappings2ask1N.keySet()){
			for (int ide2 : candidateMappings2ask1N.get(ide1)){
				
				//we consider the equivalence only
				if (isId1SmallerThanId2(ide1, ide2)){
				
					list_mappings.add(new MappingObjectIdentifiers(ide1, ide2));
					
				}
			}	
		}*/

        Set<Integer> module1;
        Set<Integer> module2;
        MappingObjectIdentifiers head_mapping;

        int newIn = 0;
        int newCons = 0;

        while (!list_mappings.isEmpty()) {

            head_mapping = list_mappings.poll(); //retrieves and removes elements

            module1 = index.getScope4Identifier_Expansion(head_mapping.getIdentifierOnto1());
            module2 = index.getScope4Identifier_Expansion(head_mapping.getIdentifierOnto2());

            for (int ide1 : module1) {
                for (int ide2 : module2) {

                    if (!isId1SmallerThanId2(ide1, ide2)) {
                        continue;
                    }

                    //Ignore
                    if (isaVisitedScopeMapping(ide1, ide2)) {
                        continue;
                    }

                    addVisistedScopeMapping(ide1, ide2);
                    newCons++;


                    //if true the add to expansion list
                    if (evaluateCandidateMappingExpansion(ide1, ide2)) {
                        list_mappings.add(new MappingObjectIdentifiers(ide1, ide2));
                        newIn++;
                    }


                }
            }

        }


        list_mappings.clear();


    }//en anchor expansion
}
