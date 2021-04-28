package jonto.indexing;

import com.google.common.collect.Multimap;
import jonto.Parameters;
import jonto.indexing.entities.ClassIndex;
import jonto.lexicon.LexicalUtilities;
import jonto.lexicon.NormalizeDate;
import jonto.lexicon.NormalizeNumbers;
import jonto.utilities.OWLUtilities;
import jonto.reasoning.HermiTReasoner;
import jonto.reasoning.ReasonerManager;
import jonto.reasoning.StructuralReasonerExtended;
import jonto.utilities.Utilities;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.search.EntitySearcher;

import java.util.*;

public class OntologyProcessing {
    private OWLOntology onto;

    private final IndexManager index;

    protected Map<Set<String>, Set<Integer>> invertedFileExact = new HashMap<Set<String>, Set<Integer>>();

    protected Map<Set<String>, Integer> invertedFileExactDataProp = new HashMap<Set<String>, Integer>();

    protected Map<Set<String>, Integer> invertedFileExactObjProp = new HashMap<Set<String>, Integer>();

    protected Map<Set<String>, Set<Integer>> invertedFileIndividuals = new HashMap<Set<String>, Set<Integer>>();

    protected Map<String, Set<Integer>> invertedFileWeakIndividuals = new HashMap<String, Set<Integer>>();

    protected Map<String, Set<Integer>> invertedFileRoleassertions = new HashMap<String, Set<Integer>>();

    protected Map<Set<String>, Set<Integer>> invertedFileWeakLabelsStemming = new HashMap<Set<String>, Set<Integer>>();

    protected Map<Set<String>, Set<Integer>> invertedFileWeakLabels = new HashMap<Set<String>, Set<Integer>>();

    protected Map<String, Integer> className2Identifier = new HashMap<String, Integer>();

    protected Map<String, Integer> dataPropName2Identifier = new HashMap<String, Integer>();

    protected Map<String, Integer> objectPropName2Identifier = new HashMap<String, Integer>();

    protected Map<String, Integer> individualName2Identifier = new HashMap<String, Integer>();

    private final Map<OWLClass, Integer> class2identifier = new HashMap<OWLClass, Integer>();
    private final Map<String, Integer> classIri2identifier = new HashMap<String, Integer>();

    private final Map<OWLNamedIndividual, Integer> inidividual2identifier = new HashMap<OWLNamedIndividual, Integer>();

    private final Map<Integer, OWLClass> identifier2class = new HashMap<Integer, OWLClass>();

    private final Set<Integer> dangerousClasses = new HashSet<Integer>();

    private final Set<String> alternative_labels_tmp = new HashSet<String>();

    private final Map<Integer, Set<List<String>>> identifier2stemmedlabels = new HashMap<Integer, Set<List<String>>>();

    public static String rdf_label_uri = "http://www.w3.org/2000/01/rdf-schema#label";
    public static String rdf_comment_uri = "http://www.w3.org/2000/01/rdf-schema#comment";
    public static String deprecated_uri = "http://www.w3.org/2002/07/owl#deprecated";

    private String iri_onto = "https://github.com/Jizhou-Che/Dissertation/Project/ontology.owl";

    private final int id_onto;

    int num_syn = 0;
    int toohigh_synset_cases = 0;

    private OWLReasoner reasoner;

    boolean use_all_labels_for_weak_mappings = false;


    private final LexicalUtilities lexUtil;

    private final PrecomputeIndexCombination precomputeIndexCombination = new PrecomputeIndexCombination();

    public ExtractAcceptedLabelsFromRoleAssertions roleAssertionLabelsExtractor = new ExtractAcceptedLabelsFromRoleAssertions();


    public OntologyProcessing(OWLOntology onto, IndexManager index, LexicalUtilities lexUtil) {
        this.onto = onto;
        this.index = index;
        this.lexUtil = lexUtil;

        precomputeIndexCombination.preComputeIdentifierCombination();

        if (onto.getOntologyID().getOntologyIRI().isPresent()) {
            iri_onto = onto.getOntologyID().getOntologyIRI().toString();
        }

        this.id_onto = this.index.addNewOntologyEntry(iri_onto);
    }

    public int getIdentifier4ConceptIRI(String iri) {
        return classIri2identifier.getOrDefault(iri, -1);
    }


    public void clearInvertedFilesExact() {
        invertedFileExact.clear();
    }

    public void clearInvertedFileStemming() {
        invertedFileWeakLabelsStemming.clear();
    }

    public void clearInvertedFileWeak() {
        invertedFileWeakLabels.clear();

    }

    public void clearStemmedLabels() {
        identifier2stemmedlabels.clear();
    }

    public void clearInvertedFiles4properties() {
        invertedFileExactDataProp.clear();
        invertedFileExactObjProp.clear();
    }

    public void clearInvertedFiles4Individuals() {
        invertedFileIndividuals.clear();
        invertedFileWeakIndividuals.clear();
        invertedFileRoleassertions.clear();
    }

    public void clearReasoner() {
        reasoner.dispose();
        reasoner = null;
    }


    public void processLexicon() {
        processLexicon(true);
    }

    public void processLexicon(boolean extractLabels) {
        for (OWLClass cls : onto.getClassesInSignature(Imports.INCLUDED)) {
            if (!cls.isTopEntity() && !cls.isBottomEntity()) {
                processLexiconClasses(cls, extractLabels);
            }
        }

        int max_redundancy_labels = 3;
        Set<Integer> redundant_ids = new HashSet<Integer>();
        for (Set<String> entry : invertedFileExact.keySet()) {
            if (invertedFileExact.get(entry).size() > max_redundancy_labels) {
                for (int ident : invertedFileExact.get(entry)) {
                    if (index.getAlternativeLabels4ConceptIndex(ident).size() > 1) {
                        redundant_ids.add(ident);
                    }
                }
                invertedFileExact.get(entry).removeAll(redundant_ids);
                redundant_ids.clear();
            }
        }

        processLexiconDataProperties(extractLabels);

        processLexiconObjectProperties(extractLabels);

        processNamedIndividuals(extractLabels);

        redundant_ids = new HashSet<Integer>();
        for (Set<String> entry : invertedFileIndividuals.keySet()) {
            if (invertedFileIndividuals.get(entry).size() > max_redundancy_labels) {
                for (int ident : invertedFileIndividuals.get(entry)) {
                    if (index.getAlternativeLabels4IndividualIndex(ident).size() > 1) {
                        redundant_ids.add(ident);
                    }
                }
                invertedFileIndividuals.get(entry).removeAll(redundant_ids);
                redundant_ids.clear();
            }
        }
    }


    private void processLexiconClasses(OWLClass cls, boolean extractLabels) {
        int ident;

        String ns_ent;
        String name;

        if (isDeprecatedClass(cls)) {
            return;
        }


        ns_ent = Utilities.getNameSpaceFromURI(cls.getIRI().toString());


        ident = index.addNewClassEntry();

        index.setOntologyId4Class(ident, id_onto);

        name = Utilities.getEntityLabelFromURI(cls.getIRI().toString());

        index.setClassName(ident, name);

        if (!ns_ent.equals("") && !ns_ent.equals(iri_onto)) {
            index.setClassNamespace(ident, ns_ent);
        }

        class2identifier.put(cls, ident);
        identifier2class.put(ident, cls);
        className2Identifier.put(name, ident);

        classIri2identifier.put(cls.getIRI().toString(), ident);

        if (extractLabels) {
            createEntryInLexicalInvertedFiles4ClassLabels(cls, ident);
        }
    }


    private void processLexiconDataProperties(boolean extractLabels) {
        List<String> cleanWords;
        StringBuilder label;

        int ident;

        String ns_ent;
        String name;

        for (OWLDataProperty dProp : onto.getDataPropertiesInSignature(Imports.INCLUDED)) {
            ns_ent = Utilities.getNameSpaceFromURI(dProp.getIRI().toString());

            ident = index.addNewDataPropertyEntry();

            index.setOntologyId4DataProp(ident, id_onto);

            name = Utilities.getEntityLabelFromURI(dProp.getIRI().toString());

            index.setDataPropName(ident, name);

            dataPropName2Identifier.put(name, ident);

            if (!ns_ent.equals("") && !ns_ent.equals(iri_onto)) {
                index.setDataPropNamespace(ident, ns_ent);
            }

            cleanWords = processLabel(name);
            if (cleanWords.size() > 0) {
                if (extractLabels) {
                    invertedFileExactDataProp.put(new HashSet<String>(cleanWords), ident);
                }
            }

            label = new StringBuilder();
            for (String word : cleanWords) {
                label.append(word);
            }

            index.setDataPropLabel(ident, label.toString());
            index.addAlternativeDataPropertyLabel(ident, label.toString());
            cleanWords.clear();

            List<String> cleanWordsAlternative = createAlternativeLabel(name);
            if (cleanWordsAlternative.size() > 0) {
                label = new StringBuilder();
                for (String word : cleanWordsAlternative) {
                    label.append(word);
                }
                index.addAlternativeDataPropertyLabel(ident, label.toString());
            }
            cleanWordsAlternative.clear();

            for (OWLAnnotationAssertionAxiom dPropAnnAx : EntitySearcher.getAnnotationAssertionAxioms(dProp, onto)) {
                for (String label_value : alternative_labels_tmp) {
                    cleanWords = processLabel(label_value);

                    if (cleanWords.size() > 0) {
                        label = new StringBuilder();
                        for (String word : cleanWords) {
                            if (label.length() == 0) {
                                label = new StringBuilder(word);
                            } else {
                                label.append("_").append(word);
                            }
                        }
                        index.addAlternativeDataPropertyLabel(ident, label.toString());

                    }
                }
                alternative_labels_tmp.clear();
            }

            for (OWLClassExpression clsexp : EntitySearcher.getDomains(dProp, onto)) {
                if (!clsexp.isAnonymous()) {
                    if (class2identifier.containsKey(clsexp.asOWLClass())) {
                        index.addDomainClass4DataProperty(ident, class2identifier.get(clsexp.asOWLClass()));
                    }
                } else if (clsexp.getClassExpressionType() == ClassExpressionType.OBJECT_UNION_OF) {
                    for (OWLClassExpression clsexpunion : clsexp.asDisjunctSet()) {
                        if (!clsexpunion.isAnonymous()) {
                            if (class2identifier.containsKey(clsexpunion.asOWLClass())) {
                                index.addDomainClass4DataProperty(ident, class2identifier.get(clsexpunion.asOWLClass()));
                            }
                        }
                    }
                }
            }

            String range_type;

            // Process data ranges.
            for (OWLDataRange type : EntitySearcher.getRanges(dProp, onto)) {
                if (type.isDatatype()) {
                    try {
                        if (type.asOWLDatatype().isBuiltIn()) {
                            range_type = type.asOWLDatatype().getBuiltInDatatype().getShortForm();
                        } else {
                            range_type = Utilities.getEntityLabelFromURI(type.asOWLDatatype().getIRI().toString());
                        }
                    } catch (Exception e) {
                        range_type = Utilities.getEntityLabelFromURI(type.asOWLDatatype().getIRI().toString());
                    }
                    index.addRangeType4DataProperty(ident, range_type);
                }
            }
        }
    }

    private void processLexiconObjectProperties(boolean extractLabels) {
        List<String> cleanWords;
        StringBuilder label;

        int ident;

        String ns_ent;
        String name;

        for (OWLObjectProperty oProp : onto.getObjectPropertiesInSignature(Imports.INCLUDED)) {
            ns_ent = Utilities.getNameSpaceFromURI(oProp.getIRI().toString());

            ident = index.addNewObjectPropertyEntry();

            index.setOntologyId4ObjectProp(ident, id_onto);

            name = Utilities.getEntityLabelFromURI(oProp.getIRI().toString());

            index.setObjectPropName(ident, name);

            objectPropName2Identifier.put(name, ident);

            if (!ns_ent.equals("") && !ns_ent.equals(iri_onto)) {
                index.setObjectPropNamespace(ident, ns_ent);
            }

            cleanWords = processLabel(name);
            if (cleanWords.size() > 0) {
                if (extractLabels) {
                    invertedFileExactObjProp.put(new HashSet<String>(cleanWords), ident);
                }
            }

            label = new StringBuilder();
            for (String word : cleanWords) {
                label.append(word);
            }
            index.setObjectPropLabel(ident, label.toString());
            index.addAlternativeObjectPropertyLabel(ident, label.toString());
            cleanWords.clear();

            List<String> cleanWordsAlternative = createAlternativeLabel(name);
            if (cleanWordsAlternative.size() > 0) {
                label = new StringBuilder();
                for (String word : cleanWordsAlternative) {
                    label.append(word);
                }
                index.addAlternativeObjectPropertyLabel(ident, label.toString());
            }
            cleanWordsAlternative.clear();

            for (OWLAnnotationAssertionAxiom oPropAnnAx : EntitySearcher.getAnnotationAssertionAxioms(oProp, onto)) {
                for (String label_value : alternative_labels_tmp) {
                    cleanWords = processLabel(label_value);

                    if (cleanWords.size() > 0) {
                        label = new StringBuilder();
                        for (String word : cleanWords) {

                            if (label.length() == 0) {
                                label = new StringBuilder(word);
                            } else {
                                label.append("_").append(word);
                            }

                        }
                        index.addAlternativeObjectPropertyLabel(ident, label.toString());
                    }
                }
                alternative_labels_tmp.clear();
            }

            String inverse_name;
            List<String> cleanWordsInverse;
            for (OWLObjectPropertyExpression propexp : EntitySearcher.getInverses(oProp, onto)) {
                if (!propexp.isAnonymous()) {
                    inverse_name = Utilities.getEntityLabelFromURI(
                            propexp.asOWLObjectProperty().getIRI().toString());

                    cleanWordsInverse = processInverseLabel(inverse_name);

                    if (cleanWordsInverse.size() > 0) {
                        label = new StringBuilder();
                        for (String word : cleanWordsInverse) {
                            label.append(word);
                        }
                        index.addAlternativeObjectPropertyLabel(ident, label.toString());
                    }
                    cleanWordsInverse.clear();
                }
            }

            for (OWLClassExpression clsexp : EntitySearcher.getDomains(oProp, onto)) {
                if (!clsexp.isAnonymous()) {
                    if (class2identifier.containsKey(clsexp.asOWLClass())) {
                        index.addDomainClass4ObjectProperty(ident, class2identifier.get(clsexp.asOWLClass()));
                    }
                } else if (clsexp.getClassExpressionType() == ClassExpressionType.OBJECT_UNION_OF) {
                    for (OWLClassExpression clsexpunion : clsexp.asDisjunctSet()) {
                        if (!clsexpunion.isAnonymous()) {
                            if (class2identifier.containsKey(clsexpunion.asOWLClass())) {
                                index.addDomainClass4ObjectProperty(ident, class2identifier.get(clsexpunion.asOWLClass()));
                            }
                        }
                    }
                }

            }

            // Process ranges.
            for (OWLClassExpression clsexp : EntitySearcher.getRanges(oProp, onto)) {
                if (!clsexp.isAnonymous()) {
                    if (class2identifier.containsKey(clsexp.asOWLClass())) {
                        index.addRangeClass4ObjectProperty(ident, class2identifier.get(clsexp.asOWLClass()));
                    }
                } else if (clsexp.getClassExpressionType() == ClassExpressionType.OBJECT_UNION_OF) {
                    for (OWLClassExpression clsexpunion : clsexp.asDisjunctSet()) {
                        if (!clsexpunion.isAnonymous()) {
                            if (class2identifier.containsKey(clsexpunion.asOWLClass())) {
                                index.addRangeClass4ObjectProperty(ident, class2identifier.get(clsexpunion.asOWLClass()));
                            }
                        }
                    }
                }
            }
        }
    }


    private void processNamedIndividuals(boolean extractLabels) {
        Set<String> cleanWords = new HashSet<String>();
        List<String> cleanWordsList;

        int ident;

        String ns_ent;
        String name;


        Set<String> altLabels = new HashSet<String>();


        String longestALabel;


        int num_dummy_indiv = 0;
        Set<OWLNamedIndividual> dummyIndividualsSet = new HashSet<OWLNamedIndividual>();


        for (OWLNamedIndividual indiv : onto.getIndividualsInSignature(Imports.INCLUDED)) {

            ns_ent = Utilities.getNameSpaceFromURI(indiv.getIRI().toString());

            ident = index.addNewIndividualEntry();

            index.setOntologyId4Individual(ident, id_onto);

            inidividual2identifier.put(indiv, ident);


            name = Utilities.getEntityLabelFromURI(indiv.getIRI().toString());
            index.setIndividualName(ident, name);

            index.setIndividualLabel(ident, name);

            individualName2Identifier.put(name, ident);

            if (!ns_ent.equals("") && !ns_ent.equals(iri_onto)) {
                index.setIndividualNamespace(ident, ns_ent);
            }

            if (roleAssertionLabelsExtractor.isDummyIndividual(indiv)) {
                num_dummy_indiv++;
                dummyIndividualsSet.add(indiv);
                continue;
            }

            altLabels.addAll(roleAssertionLabelsExtractor.extractLexiconFromRoleAssertions(indiv));

            altLabels.addAll(extractAnnotations4Infividual(indiv));

            longestALabel = "";

            for (String alabel : altLabels) {
                if (!isLabelAnIdentifier(alabel)) {
                    if (alabel.length() > longestALabel.length()) {
                        longestALabel = alabel;
                    }

                    index.addAlternativeIndividualLabel(ident, alabel.toLowerCase());

                    cleanWordsList = processLabel(alabel, true);

                    changeOrderAltLabelWords(ident, cleanWordsList);

                    cleanWords.addAll(cleanWordsList);

                    String stemmedWord;
                    for (String word : cleanWords) {
                        if (extractLabels) {
                            if (!invertedFileWeakIndividuals.containsKey(word)) {
                                invertedFileWeakIndividuals.put(word, new HashSet<Integer>());
                            }

                            invertedFileWeakIndividuals.get(word).add(ident);

                            stemmedWord = lexUtil.getStemming4Word(word);
                            if (stemmedWord.length() > 2) {
                                if (!invertedFileWeakIndividuals.containsKey(stemmedWord)) {
                                    invertedFileWeakIndividuals.put(stemmedWord, new HashSet<Integer>());
                                }
                                invertedFileWeakIndividuals.get(stemmedWord).add(ident);
                            }
                        }
                    }

                    if (cleanWords.size() > 0) {

                        if (extractLabels) {
                            if (!invertedFileIndividuals.containsKey(cleanWords)) {
                                invertedFileIndividuals.put(new HashSet<String>(cleanWords), new HashSet<Integer>());
                            }

                            invertedFileIndividuals.get(cleanWords).add(ident);
                        }

                        cleanWords.clear();
                        cleanWordsList.clear();
                    }
                }
            }

            if (!longestALabel.equals("")) {
                index.setIndividualLabel(ident, longestALabel);
            }

            altLabels.clear();


        }


        if (extractLabels) {
            for (OWLNamedIndividual indiv : onto.getIndividualsInSignature(Imports.INCLUDED)) {
                if (dummyIndividualsSet.contains(indiv)) {
                    continue;
                }

                for (String str_label : roleAssertionLabelsExtractor.extractExtendedLexiconFromRoleAssertions(indiv)) {


                    if (!invertedFileRoleassertions.containsKey(str_label)) {
                        invertedFileRoleassertions.put(str_label, new HashSet<Integer>());
                    }

                    invertedFileRoleassertions.get(str_label).add(inidividual2identifier.get(indiv));

                }

                Multimap<OWLObjectPropertyExpression, OWLIndividual> objProp2values = EntitySearcher.getObjectPropertyValues(indiv, onto);

                for (OWLObjectPropertyExpression objprop : objProp2values.keySet()) {

                    for (OWLIndividual indiv_deep2 : objProp2values.get(objprop)) {

                        if (indiv_deep2.isAnonymous()) {
                            continue;
                        }

                        index.addReferencedIndividual4Individual(
                                inidividual2identifier.get(indiv), inidividual2identifier.get(indiv_deep2));
                    }
                }
            }


        }

        inidividual2identifier.clear();
        dummyIndividualsSet.clear();
    }

    private void changeOrderAltLabelWords(int ident, List<String> cleanWordsList) {
        StringBuilder original;
        StringBuilder changed;

        if (cleanWordsList.size() > 0) {
            original = new StringBuilder(cleanWordsList.get(0));
            for (int i = 1; i < cleanWordsList.size(); i++) {
                original.append(" ").append(cleanWordsList.get(i));
            }

            changed = new StringBuilder(cleanWordsList.get(cleanWordsList.size() - 1));
            for (int i = cleanWordsList.size() - 2; i > -1; i--) {
                changed.append(" ").append(cleanWordsList.get(i));
            }

            index.addAlternativeIndividualLabel(ident, original.toString());
            index.addAlternativeIndividualLabel(ident, changed.toString());
        }
    }


    Set<String> lexiconValues4individual = new HashSet<String>();


    private Set<String> extractAnnotations4Infividual(OWLNamedIndividual indiv) {
        lexiconValues4individual.clear();

        for (OWLAnnotationAssertionAxiom indivAnnAx : EntitySearcher.getAnnotationAssertionAxioms(indiv, onto)) {
            for (String label_value : alternative_labels_tmp) {

                if (label_value.length() > 2) {
                    lexiconValues4individual.add(label_value);
                }
            }
            alternative_labels_tmp.clear();
        }

        return lexiconValues4individual;
    }


    private List<String> processLabel(String label) {
        return processLabel(label, false);
    }


    private List<String> processLabel(String label, boolean filterStopwords) {

        String label_value;
        List<String> cleanWords = new ArrayList<String>();
        String[] words;

        label_value = label.replace(",", "").replace("-", "");

        if (label_value.indexOf("_") > 0) {
            words = label_value.split("_");
        } else if (label_value.indexOf(" ") > 0) {
            words = label_value.split(" ");
        } else {
            words = Utilities.splitStringByCapitalLetter(label_value);
        }

        for (int i = 0; i < words.length; i++) {
            words[i] = words[i].toLowerCase();

            if (words[i].length() > 0 && (!filterStopwords || !lexUtil.getStopwordsSet().contains(words[i]))) {
                cleanWords.add(words[i]);
            }
        }


        return cleanWords;

    }

    private List<String> processInverseLabel(String label) {
        List<String> words = processLabel(label);

        int lastIndex = words.size() - 1;

        String firstWord = words.get(0).toLowerCase();
        String lastWord = words.get(lastIndex).toLowerCase();

        if (firstWord.equals("has")) {
            words.remove(0);
            words.add("of");
        } else if (lastWord.equals("of")) {
            words.remove(lastIndex);
            words.add(0, "has");
        } else if (lastWord.equals("by")) {
            words.remove(lastIndex);
            if (firstWord.equals("is")) {
                words.remove(0);
            }
        } else {
            words.clear();
        }

        return words;

    }

    private List<String> createAlternativeLabel(String label) {

        List<String> words = processLabel(label);

        int lastIndex = words.size() - 1;

        String firstWord = words.get(0).toLowerCase();
        String lastWord = words.get(lastIndex).toLowerCase();

        if (firstWord.equals("has")) {
            words.remove(0);
        } else if (lastWord.equals("by")) {
            words.remove(lastIndex);
            if (firstWord.equals("is")) {
                words.remove(0);
            }
            words.add(0, "has");
        } else {
            words.clear();
        }

        return words;


    }

    private void createEntryInLexicalInvertedFiles4ClassLabels(OWLClass cls, int ident) {
        Set<String> cleanWords = extractCleanLabel4OWLCls(cls, ident);
        Set<String> stemmed_words = new HashSet<String>();

        String[] words;

        StringBuilder cleanAltLabel;

        StringBuilder cleanReverseAltLabel;

        String stemmedWord;

        if (cleanWords.size() > 0) {
            if (!invertedFileExact.containsKey(cleanWords)) {
                invertedFileExact.put(new HashSet<String>(cleanWords), new HashSet<Integer>());
            }

            invertedFileExact.get(cleanWords).add(ident);
        }

        for (String str : cleanWords) {
            stemmedWord = lexUtil.getStemming4Word(str);
            if (!stemmedWord.isEmpty()) {
                stemmed_words.add(stemmedWord);
            }
        }

        if (!invertedFileWeakLabelsStemming.containsKey(stemmed_words)) {
            invertedFileWeakLabelsStemming.put(new HashSet<String>(stemmed_words), new HashSet<Integer>());
        }

        invertedFileWeakLabelsStemming.get(stemmed_words).add(ident);

        identifier2stemmedlabels.put(ident, new HashSet<List<String>>());
        identifier2stemmedlabels.get(ident).add(new ArrayList<String>(stemmed_words));

        stemmed_words.clear();
        cleanWords.clear();

        StringBuilder stemmedAltLabel;

        for (String altlabel_value : extractAlternateLabels4OWLCls(cls, ident)) {
            cleanAltLabel = new StringBuilder();
            cleanReverseAltLabel = new StringBuilder();
            stemmedAltLabel = new StringBuilder();

            if (altlabel_value.length() > 2) {

                words = altlabel_value.split("_");

                for (String word : words) {

                    if (!lexUtil.getStopwordsSet().contains(word) && word.length() > 0) {

                        cleanWords.add(word);

                        if (cleanAltLabel.length() == 0) {
                            cleanAltLabel = new StringBuilder(word);
                            cleanReverseAltLabel = new StringBuilder(word);
                        } else {
                            cleanAltLabel.append("_").append(word);
                            cleanReverseAltLabel.insert(0, word + "_");
                        }

                        stemmedWord = lexUtil.getStemming4Word(word);
                        if (stemmedWord.isEmpty()) {
                            continue;
                        }

                        stemmedAltLabel.append("_").append(stemmedWord);

                        index.addWordOccurrence(stemmedWord, ident);

                    }
                }

                if (cleanWords.size() > 0) {
                    if (!invertedFileExact.containsKey(cleanWords)) {
                        invertedFileExact.put(new HashSet<String>(cleanWords), new HashSet<Integer>());
                    }

                    invertedFileExact.get(cleanWords).add(ident);

                    index.addAlternativeClassLabel(ident, cleanAltLabel.toString());
                    if (stemmedAltLabel.length() > 0) {
                        index.addStemmedAltClassLabel(ident, stemmedAltLabel.substring(1));
                    }
                }

                for (String str : cleanWords) {
                    stemmedWord = lexUtil.getStemming4Word(str);
                    if (!stemmedWord.isEmpty()) {
                        stemmed_words.add(stemmedWord);
                    }
                }

                if (!invertedFileWeakLabelsStemming.containsKey(stemmed_words)) {
                    invertedFileWeakLabelsStemming.put(new HashSet<String>(stemmed_words), new HashSet<Integer>());
                }

                invertedFileWeakLabelsStemming.get(stemmed_words).add(ident);

                if (use_all_labels_for_weak_mappings) {
                    identifier2stemmedlabels.get(ident).add(new ArrayList<String>(stemmed_words));
                }

                stemmed_words.clear();
                cleanWords.clear();
            }

        }


    }


    private boolean isDeprecatedClass(OWLClass cls) {
        for (OWLAnnotationAssertionAxiom annAx : EntitySearcher.getAnnotationAssertionAxioms(cls, onto)) {
            if (annAx.getAnnotation().getProperty().getIRI().toString().equals(deprecated_uri)) {
                String ann_label = ((OWLLiteral) annAx.getAnnotation().getValue()).getLiteral().toLowerCase();
                if (ann_label.equals("true")) {
                    return true;
                }
            }
        }
        return false;
    }


    private boolean isLabelAnIdentifier(String label_value) {
        return label_value.matches(".+[0-9][0-9][0-9]+")
                || label_value.matches("[0-9][0-9][0-9][0-9][0-9]+-[0-9]+")
                || label_value.matches("[0-9]+(\\.[0-9]+)+")
                || label_value.matches(".+[0-9]+.+[0-9]+.+[0-9]+.+")
                || label_value.matches("[a-zA-Z][0-9]+");

    }

    private Set<String> extractCleanLabel4OWLCls(OWLClass cls, int ident) {

        StringBuilder label_value;

        Set<String> cleanWords = new HashSet<String>();

        String[] words;

        String ann_label;

        label_value = new StringBuilder(index.getIdentifier2ClassIndexMap().get(ident).getEntityName());

        if (isLabelAnIdentifier(label_value.toString())) {
            for (OWLAnnotationAssertionAxiom annAx : EntitySearcher.getAnnotationAssertionAxioms(cls, onto)) {
                if (annAx.getAnnotation().getProperty().getIRI().toString().equals(rdf_label_uri)) {
                    ann_label = ((OWLLiteral) annAx.getAnnotation().getValue()).getLiteral();

                    if (!ann_label.equals("") && !ann_label.equals("null")) {
                        label_value = new StringBuilder(ann_label);
                    }
                    break;

                }

            }

        }

        label_value = new StringBuilder(label_value.toString().replace(",", ""));


        if (label_value.toString().startsWith("_")) {
            label_value = new StringBuilder(label_value.substring(1));
        }
        if (label_value.toString().endsWith("_")) {
            label_value = new StringBuilder(label_value.substring(0, label_value.length() - 1));
        }


        if (label_value.indexOf("_") > 0) {
            words = label_value.toString().split("_");
        } else if (label_value.indexOf(" ") > 0) {
            words = label_value.toString().split(" ");
        } else {
            words = Utilities.splitStringByCapitalLetter(label_value.toString());
        }

        label_value = new StringBuilder();
        for (int i = 0; i < words.length; i++) {

            words[i] = words[i].toLowerCase();

            if (words[i].length() > 0) {
                label_value.append(words[i]).append("_");

                if (!lexUtil.getStopwordsSet().contains(words[i])) {
                    cleanWords.add(words[i]);
                }
            }
        }

        if (label_value.length() > 0) {
            label_value = new StringBuilder(label_value.substring(0, label_value.length() - 1));

            index.setClassLabel(ident, label_value.toString());
        } else {
            index.setClassLabel(ident, cls.getIRI().toString());
        }

        return cleanWords;
    }


    private final Set<String> labels_set = new HashSet<String>();

    private void considerLabel(String label_value) {

        labels_set.addAll(extendAlternativeLabel(label_value));
    }


    private Set<String> extractAlternateLabels4OWLCls(OWLClass cls, int ident) {


        String label_value;

        labels_set.clear();

        label_value = index.getIdentifier2ClassIndexMap().get(ident).getEntityName().toLowerCase();

        if (!isLabelAnIdentifier(label_value)) {
            considerLabel(label_value);
        }

        for (OWLAnnotationAssertionAxiom clsAnnAx : EntitySearcher.getAnnotationAssertionAxioms(cls, onto)) {
            for (String l_value : alternative_labels_tmp) {
                if (l_value.length() > 2) {
                    considerLabel(l_value);
                }
            }
            alternative_labels_tmp.clear();

        }

        num_syn = num_syn + labels_set.size();

        return labels_set;


    }

    private Set<String> extendAlternativeLabel(String label_value) {
        Set<String> set_syn = new HashSet<String>();

        List<Set<String>> wordi2syn = new ArrayList<Set<String>>();

        String[] words;
        int shift = 1;

        String roman;

        label_value = label_value.replaceAll("/", "_");

        if (label_value.indexOf(" ") > 0) {
            words = label_value.split(" ");
        } else if (label_value.indexOf("_") > 0) {
            words = label_value.split("_");
        } else {
            words = Utilities.splitStringByCapitalLetter(label_value);
        }

        for (String word : words) {
            set_syn.add(word.replace(",", ""));

            roman = lexUtil.getRomanNormalization4Number(word);
            if (!roman.equals("")) {
                set_syn.add(roman);
            }


            wordi2syn.add(new HashSet<String>(set_syn));
            set_syn.clear();
        }


        long comb = 1;

        for (Set<String> set : wordi2syn) {
            comb = comb * set.size();
        }

        StringBuilder label;
        if (comb > 50 || comb < 0) {
            toohigh_synset_cases++;

            label = new StringBuilder();
            for (int i = 0; i < words.length - shift; i++) {
                label.append(words[i]).append("_");
            }

            label.append(words[words.length - shift]);
            set_syn.add(label.toString());
            return set_syn;
        } else {

            if (wordi2syn.size() == 1) {
                return wordi2syn.get(0);
            }

            return combineWordSynonyms(wordi2syn, wordi2syn.get(0), 1);

        }

    }


    private Set<String> combineWordSynonyms(List<Set<String>> wordi2syn, Set<String> currentSet, int index) {

        Set<String> newSet = new HashSet<String>();

        for (String clabel : currentSet) {

            for (String syn : wordi2syn.get(index)) {

                newSet.add(clabel + "_" + syn);

            }
        }

        if (wordi2syn.size() <= index + 1) {
            return newSet;
        } else {
            return combineWordSynonyms(wordi2syn, newSet, index + 1);
        }


    }

    public void addEntries2InvertedFileWeakLabels(Map<Set<String>, Set<Integer>> entries) {
        invertedFileWeakLabels.putAll(entries);
    }


    public void setInvertedFileWeakLabels() {
        int max_size_labels = 8;
        int max_size_list_words_missing = 3;

        for (int ident : identifier2stemmedlabels.keySet()) {
            for (List<String> list_words : identifier2stemmedlabels.get(ident)) {
                if (list_words.size() > max_size_labels) {
                    continue;
                }

                if (list_words.size() > 1) {
                    createWeakLabels4Identifier(list_words, ident, 1);

                    if (list_words.size() > 3) {
                        createWeakLabels4Identifier(list_words, ident, 2);

                        if (list_words.size() > 5) {
                            createWeakLabels4Identifier(list_words, ident, 3);

                        }
                    }
                }
            }
        }
    }

    public void setFullInvertedFileWeakLabels() {
        List<String> list_words = new ArrayList<String>();
        Set<Integer> identifiers;

        int max_size_labels = 8;
        int max_size_list_words_missing = 3;


        for (Set<String> stemmed_set : invertedFileWeakLabelsStemming.keySet()) {

            if (stemmed_set.size() > max_size_labels) {
                continue;
            }

            list_words.addAll(stemmed_set);

            identifiers = invertedFileWeakLabelsStemming.get(stemmed_set);

            if (list_words.size() > 1) {

                createWeakLabels4Identifier(list_words, identifiers, 1);

                if (list_words.size() > 3) {

                    createWeakLabels4Identifier(list_words, identifiers, 2);

                    if (list_words.size() > 5) {

                        createWeakLabels4Identifier(list_words, identifiers, 3);

                    }
                }
            }
            list_words.clear();
        }
    }


    private void createWeakLabels4Identifier(List<String> cleanWords, int ident, int missing_words) {
        Set<String> combo = new HashSet<String>();

        Set<Set<Integer>> combination_set = precomputeIndexCombination.getIdentifierCombination(cleanWords.size(), missing_words);

        for (Set<Integer> toExclude : combination_set) {

            for (int pos = 0; pos < cleanWords.size(); pos++) {
                if (!toExclude.contains(pos)) {
                    combo.add(cleanWords.get(pos));
                }

            }

            if (!invertedFileWeakLabels.containsKey(combo)) {
                invertedFileWeakLabels.put(new HashSet<String>(combo), new HashSet<Integer>());
            }

            invertedFileWeakLabels.get(combo).add(ident);

            combo.clear();

        }
    }

    private void createWeakLabels4Identifier(List<String> cleanWords, Set<Integer> identifiers, int missing_words) {

        Set<String> combo = new HashSet<String>();

        Set<Set<Integer>> combination_set = precomputeIndexCombination.getIdentifierCombination(cleanWords.size(), missing_words);

        for (Set<Integer> toExclude : combination_set) {

            for (int pos = 0; pos < cleanWords.size(); pos++) {
                if (!toExclude.contains(pos)) {
                    combo.add(cleanWords.get(pos));
                }

            }

            if (!invertedFileWeakLabels.containsKey(combo)) {
                invertedFileWeakLabels.put(new HashSet<String>(combo), new HashSet<Integer>());
            }

            invertedFileWeakLabels.get(combo).addAll(identifiers);

            combo.clear();

        }


    }


    public int getId4Class(String name) {
        return className2Identifier.getOrDefault(name, -1);
    }

    public int getId4ObjectProperty(String name) {
        return objectPropName2Identifier.getOrDefault(name, -1);
    }

    public int getId4DataProperty(String name) {
        return dataPropName2Identifier.getOrDefault(name, -1);
    }

    public int getId4Instance(String name) {
        return individualName2Identifier.getOrDefault(name, -1);
    }


    public Map<Set<String>, Set<Integer>> getInvertedFileExactMatching() {
        return invertedFileExact;
    }

    public Map<Set<String>, Integer> getInvertedFileExactMatching4DataProp() {
        return invertedFileExactDataProp;
    }

    public Map<Set<String>, Integer> getInvertedFileExactMatching4ObjProp() {
        return invertedFileExactObjProp;
    }


    public Map<Set<String>, Set<Integer>> getInvertedFileMatching4Individuals() {
        return invertedFileIndividuals;
    }

    public Map<String, Set<Integer>> getInvertedFileRoleAssertions() {
        return invertedFileRoleassertions;
    }

    public Map<String, Set<Integer>> getInvertedFileWeakMatching4Individuals() {
        return invertedFileWeakIndividuals;
    }


    public Map<Set<String>, Set<Integer>> getInvertedFileWeakLabelsStemming() {
        return invertedFileWeakLabelsStemming;
    }

    public Map<Set<String>, Set<Integer>> getInvertedFileWeakLabels() {
        return invertedFileWeakLabels;
    }

    // Extract class structures.
    public void setClassStructure() {
        setUpReasoner(Parameters.reasoner);

        extractDangerousClasses();

        extractStringTaxonomiesAndDisjointness();

        extractGeneralHornAxioms();

        extractInferredTypes4Individuals();
    }

    private void setUpReasoner(int reasoner_id) {
        if (reasoner_id == ReasonerManager.HERMIT) {
            HermiTReasoner hermit;

            // TODO: 4/17/21 IMPORTANT: fix datatype processing with HermiT.
            try {
                if (onto.getDatatypesInSignature(Imports.INCLUDED).size() == 0) {
                    hermit = new HermiTReasoner(OWLUtilities.createOWLOntologyManager(), onto);
                    hermit.classifyOntology();

                    if (hermit.isOntologyClassified()) {
                        reasoner = hermit.getReasoner();
                    } else {
                        throw new Exception("Ontology not classified with HermiT.");
                    }
                } else {
                    throw new Exception("Ontology with datatypes, too slow to process with HermiT.");
                }
            } catch (Exception e) {
                System.out.println("Error setting up HermiT reasoner.");
                System.out.println("Using structural reasoner instead.");
                reasoner = new StructuralReasonerExtended(onto);
            }
        } else {
            System.out.println("Reasoner undefined. Using HermiT instead.");
            setUpReasoner(ReasonerManager.HERMIT);
        }
    }


    private void extractDangerousClasses() {
        for (OWLClass cls : reasoner.getTopClassNode().getEntitiesMinusTop()) {
            dangerousClasses.add(class2identifier.get(cls));
            index.addDangerousClasses(class2identifier.get(cls));
        }
    }

    public Set<Integer> getDangerousClasses() {
        return dangerousClasses;
    }

    private void extractStringTaxonomiesAndDisjointness() {
        int bignode = 0;

        int equiv = 0;
        int disj = 0;

        Map<Node<OWLClass>, Integer> node2identifier = new HashMap<Node<OWLClass>, Integer>();

        int identRepresentative;
        int ident1;
        int ident2;

        OWLClass clsRepresentative;


        NodeSet<OWLClass> topClasses = reasoner.getSubClasses(reasoner.getTopClassNode().getRepresentativeElement(), true);

        for (OWLClass cls : topClasses.getFlattened()) {
            if (class2identifier.containsKey(cls)) {
                index.addRoot2Structure(class2identifier.get(cls));
            }
        }

        NodeSet<OWLClass> nodeSet = reasoner.getSubClasses(reasoner.getTopClassNode().getRepresentativeElement(), false);

        for (Node<OWLClass> node : nodeSet) {
            if (node.isBottomNode() || node.isTopNode()) {
                continue;
            }

            clsRepresentative = node.getRepresentativeElement();

            if (!class2identifier.containsKey(clsRepresentative)) {
                continue;
            }

            identRepresentative = class2identifier.get(clsRepresentative);

            node2identifier.put(node, identRepresentative);

            if (node.getEntities().size() > 1) {
                bignode++;
                index.addRepresentativeNode(identRepresentative);
            }
        }

        for (Node<OWLClass> node : nodeSet) {
            if (node.isBottomNode() || node.isTopNode()) {
                continue;
            }

            if (!node2identifier.containsKey(node)) {
                continue;
            }


            identRepresentative = node2identifier.get(node);

            clsRepresentative = node.getRepresentativeElement();


            if (!reasoner.getSubClasses(clsRepresentative, true).isEmpty()) {
                index.getClassIndex(identRepresentative).setEmptyDirectSubClasses();

                for (Node<OWLClass> nodeSub : reasoner.getSubClasses(clsRepresentative, true).getNodes()) {
                    if (nodeSub.isBottomNode() || nodeSub.isTopNode()) {
                        continue;
                    }

                    if (!node2identifier.containsKey(nodeSub)) {
                        continue;
                    }

                    index.getClassIndex(identRepresentative).addDirectSubClass(node2identifier.get(nodeSub));
                }
            }

            if (!reasoner.getSuperClasses(clsRepresentative, true).isEmpty()) {
                index.getClassIndex(identRepresentative).setEmptyDirectSuperClasses();

                for (Node<OWLClass> nodeSup : reasoner.getSuperClasses(clsRepresentative, true).getNodes()) {
                    if (nodeSup.isTopNode() || nodeSup.isBottomNode()) {
                        continue;
                    }

                    if (!node2identifier.containsKey(nodeSup)) {
                        continue;
                    }

                    index.getClassIndex(identRepresentative).addDirectSuperClass(node2identifier.get(nodeSup));
                }
            }

            for (OWLClass nodeClass : node.getEntities()) {
                if (nodeClass.isTopEntity() || nodeClass.isBottomEntity()) {
                    continue;
                }

                if (!reasoner.getDisjointClasses(nodeClass).isEmpty()) {
                    ident1 = class2identifier.get(nodeClass);

                    index.getClassIndex(ident1).setEmptyDisjointClasses();
                    disj++;

                    for (Node<OWLClass> nodeDisj : reasoner.getDisjointClasses(nodeClass).getNodes()) {
                        for (OWLClass disjcls : nodeDisj.getEntities()) {
                            if (disjcls.isTopEntity() || disjcls.isBottomEntity()) {
                                continue;
                            }

                            if (!class2identifier.containsKey(disjcls)) {
                                continue;
                            }

                            if (ident1 != class2identifier.get(disjcls)) {
                                index.getClassIndex(ident1).addDisjointClass(class2identifier.get(disjcls));
                                index.getClassIndex(class2identifier.get(disjcls)).addDisjointClass(ident1);
                            }
                        }
                    }
                }
            }

            if (node.getEntities().size() > 1) {
                OWLClass[] nodeClasses = new OWLClass[node.getEntities().size()];
                nodeClasses = node.getEntities().toArray(nodeClasses);

                for (int i = 0; i < nodeClasses.length; i++) {
                    ident1 = class2identifier.get(nodeClasses[i]);
                    index.getClassIndex(ident1).setEmptyEquivalentClasses();
                    equiv++;

                    for (int j = 0; j < nodeClasses.length; j++) {
                        if (i == j) {
                            continue;
                        }

                        ident2 = class2identifier.get(nodeClasses[j]);

                        index.getClassIndex(ident1).addEquivalentClass(ident2);

                        // Propagation of disjointness.
                        if (index.getClassIndex(ident1).hasDirectDisjointClasses()) {

                            if (!index.getClassIndex(ident2).hasDirectDisjointClasses()) {
                                index.getClassIndex(ident2).setEmptyDisjointClasses();
                            }
                            index.getClassIndex(ident2).addAllDisjointClasses(index.getClassIndex(ident1).getDisjointClasses());
                        }
                    }
                }
            }
        }

        node2identifier.clear();
    }


    Set<Integer> ausxSetOfClasses = new HashSet<Integer>();

    private void extractGeneralHornAxioms() {
        int idecls;

        for (OWLClass cls : class2identifier.keySet()) {
            idecls = class2identifier.get(cls);

            for (OWLEquivalentClassesAxiom ax : onto.getEquivalentClassesAxioms(cls)) {
                for (OWLClassExpression exp_equiv : ax.getClassExpressions()) {
                    addOWLClassExpresion2GeneralHornAxiom(idecls, exp_equiv);
                }

            }

            for (OWLSubClassOfAxiom ax : onto.getSubClassAxiomsForSuperClass(cls)) {
                addOWLClassExpresion2GeneralHornAxiom(idecls, ax.getSubClass());
            }
        }
    }

    private void extractInferredTypes4Individuals() {

        for (int identIndiv : index.getIndividuaIdentifierSet()) {

            for (Node<OWLClass> node_cls : reasoner.getTypes(
                    index.getOWLNamedIndividual4IndividualIndex(identIndiv), true)) {

                for (OWLClass cls : node_cls.getEntitiesMinusTop()) {
                    index.addType4Individual(identIndiv, class2identifier.get(cls));
                }
            }
        }
    }

    private void addOWLClassExpresion2GeneralHornAxiom(int idecls, OWLClassExpression exp) {
        ausxSetOfClasses.clear();

        if (exp instanceof OWLObjectIntersectionOf) {
            for (OWLClassExpression exp_intersect : ((OWLObjectIntersectionOf) exp).getOperands()) {
                if (!exp_intersect.isAnonymous()) {
                    ausxSetOfClasses.add(class2identifier.get(exp_intersect.asOWLClass()));
                } else {
                    ausxSetOfClasses.clear();
                    return;
                }

            }

        } else {
            return;
        }

        if (ausxSetOfClasses.size() > 1) {
            index.addGeneralHornAxiom2Structure(ausxSetOfClasses, idecls);
        }
    }


    private final Map<String, Set<Integer>> singleWordInvertedIndex = new HashMap<String, Set<Integer>>();

    public Integer getFrequency(String word) {
        if (singleWordInvertedIndex.containsKey(word)) {
            return singleWordInvertedIndex.get(word).size();
        }
        return 0;
    }

    private final Map<Set<String>, Set<Integer>> filteredInvertedIndex = new HashMap<Set<String>, Set<Integer>>();

    public Map<Set<String>, Set<Integer>> getFilteredInvertedIndex() {
        return filteredInvertedIndex;
    }

    public Set<String> getSuperClass(int id, int TOTAL) {
        Set<String> ret = new HashSet<String>();
        Set<Integer> superclasses;
        Set<Integer> visited = new HashSet<Integer>();
        ClassIndex cls;

        Queue<ClassIndex> q = new LinkedList<ClassIndex>();
        q.add(index.getClassIndex(id));
        visited.add(id);

        while (!q.isEmpty() && ret.size() < TOTAL) {
            superclasses = q.remove().getDirectSuperclasses();

            for (int i : superclasses) {
                if (!visited.contains(i)) {
                    q.add(cls = index.getClassIndex(i));
                    visited.add(i);
                    addStemmedAltLabels(ret, cls);
                }
            }
        }

        return ret;
    }

    private void addStemmedAltLabels(Set<String> labels, ClassIndex cls) {
        Set<String> newLabs = cls.getStemmedAltLabels();

        if (newLabs == null) {
            return;
        }
        labels.addAll(newLabs);
    }


    private class ExtractAcceptedLabelsFromRoleAssertions {
        Set<String> lexiconValues4individual = new HashSet<String>();
        String label_value;

        int max_size_name_label = 0;
        int min_size_name_label = 5000;

        protected boolean isDummyIndividual(OWLNamedIndividual indiv) {
            OWLObjectPropertyAssertionAxiom opaa;
            String prop_uri;

            for (OWLAxiom refAx : onto.getReferencingAxioms(indiv, Imports.INCLUDED)) {
                if (refAx instanceof OWLObjectPropertyAssertionAxiom) {
                    opaa = (OWLObjectPropertyAssertionAxiom) refAx;

                    if (opaa.getObject().isAnonymous()) {
                        continue;
                    }

                    if (!indiv.equals(opaa.getObject().asOWLNamedIndividual())) {
                        continue;
                    }

                    if (!opaa.getProperty().isAnonymous()) {
                        prop_uri = opaa.getProperty().asOWLObjectProperty().getIRI().toString();
                    }
                }
            }

            return false;
        }

        protected Set<String> extractLexiconFromRoleAssertions(OWLNamedIndividual indiv) {
            lexiconValues4individual.clear();

            // Consider rdfs:comments annotations.
            for (OWLAnnotationAssertionAxiom indivAnnAx : EntitySearcher.getAnnotationAssertionAxioms(indiv, onto)) {
                String uri_ann = indivAnnAx.getAnnotation().getProperty().getIRI().toString();

                if (uri_ann.equals(rdf_comment_uri)) {
                    try {
                        label_value = processLabel(((OWLLiteral) indivAnnAx.getAnnotation().getValue()).getLiteral().toLowerCase());
                    } catch (Exception e) {
                        label_value = "";
                    }

                    if (label_value.length() > max_size_name_label) {
                        max_size_name_label = label_value.length();
                    }

                    if (label_value.length() > 0 && label_value.length() < min_size_name_label) {
                        min_size_name_label = label_value.length();
                    }

                    if (label_value.length() > 2) {
                        lexiconValues4individual.add(label_value);
                    }
                }
            }

            return lexiconValues4individual;
        }

        protected Set<String> extractExtendedLexiconFromRoleAssertions(OWLNamedIndividual indiv) {
            lexiconValues4individual.clear();

            String label_name;

            Multimap<OWLDataPropertyExpression, OWLLiteral> dataProp2values = EntitySearcher.getDataPropertyValues(indiv, onto);

            for (OWLDataPropertyExpression dataprop : dataProp2values.keySet()) {
                if (dataprop.isAnonymous()) {
                    continue;
                }

                label_name = Utilities.getEntityLabelFromURI(dataprop.asOWLDataProperty().getIRI().toString());

                for (OWLLiteral literal : dataProp2values.get(dataprop)) {
                    lexiconValues4individual.add(label_name + "_" + NormalizeDate.normalize(literal.getLiteral()));
                }
            }

            Multimap<OWLObjectPropertyExpression, OWLIndividual> objProp2values = EntitySearcher.getObjectPropertyValues(indiv, onto);


            int ident;

            for (OWLObjectPropertyExpression objprop : objProp2values.keySet()) {
                if (objprop.isAnonymous()) {
                    continue;
                }

                label_name = Utilities.getEntityLabelFromURI(objprop.asOWLObjectProperty().getIRI().toString());

                for (OWLIndividual indiv_deep2 : objProp2values.get(objprop)) {

                    if (indiv_deep2.isAnonymous()) {
                        continue;
                    }


                    if (!inidividual2identifier.containsKey(indiv_deep2.asOWLNamedIndividual())) {
                        continue;
                    }

                    ident = inidividual2identifier.get(indiv_deep2.asOWLNamedIndividual());

                    if (index.hasIndividualAlternativeLabels(ident)) {
                        lexiconValues4individual.add(label_name + "_" + index.getLabel4IndividualIndex(ident));
                    } else {
                        dataProp2values = EntitySearcher.getDataPropertyValues(indiv_deep2, onto);

                        for (OWLDataPropertyExpression dataprop : dataProp2values.keySet()) {
                            if (dataprop.isAnonymous()) {
                                continue;
                            }

                            label_name = Utilities.getEntityLabelFromURI(dataprop.asOWLDataProperty().getIRI().toString());

                            for (OWLLiteral literal : dataProp2values.get(dataprop)) {
                                lexiconValues4individual.add(label_name + "_" + NormalizeDate.normalize(literal.getLiteral().toString()));

                            }

                        }

                        Multimap<OWLObjectPropertyExpression, OWLIndividual> objProp2values_deep2 = EntitySearcher.getObjectPropertyValues(indiv_deep2, onto);

                        for (OWLObjectPropertyExpression objectprop2 : objProp2values_deep2.keySet()) {
                            if (objectprop2.isAnonymous()) {
                                continue;
                            }

                            label_name = Utilities.getEntityLabelFromURI(objectprop2.asOWLObjectProperty().getIRI().toString());

                            for (OWLIndividual indiv_deep3 : objProp2values_deep2.get(objectprop2)) {
                                if (indiv_deep3.isAnonymous()) {
                                    continue;
                                }

                                int ident2 = inidividual2identifier.get(indiv_deep3.asOWLNamedIndividual());

                                if (index.hasIndividualAlternativeLabels(ident2)) {
                                    lexiconValues4individual.add(label_name + "_" + index.getLabel4IndividualIndex(ident2));
                                }
                            }
                        }
                    }
                }
            }

            return lexiconValues4individual;
        }

        public String processLabel(String value) {
            String processedLabel;

            String reg_ex_split = "[&\\,;(/\\[]|(\\s)is(\\s)|(\\s)are(\\s)|(\\s)was(\\s)|(\\s)were(\\s)|(\\s)est(\\s)|(\\s)fut(\\s)|(\\s)un(\\s)|(\\s)a(\\s)|(\\s)an(\\s)";

            processedLabel = value.replaceAll(" obe ", "");
            processedLabel = processedLabel.replaceAll(" obe", "");
            processedLabel = processedLabel.replaceAll("obe ", "");
            processedLabel = processedLabel.replaceAll(" frs ", "");
            processedLabel = processedLabel.replaceAll(" frs", "");
            processedLabel = processedLabel.replaceAll("frs ", "");

            int manageable_length = 65;

            // Remove white spaces.
            processedLabel = processedLabel.replaceAll(String.valueOf((char) 160), " ");

            if (processedLabel.split(reg_ex_split).length == 0) {
                return "";
            }

            if (processedLabel.length() <= manageable_length && !processedLabel.contains("<p>") && !processedLabel.contains("</p>")) {
                processedLabel = processedLabel.split(reg_ex_split)[0];
                processedLabel = processedLabel.split(reg_ex_split)[0];
                processedLabel = processedLabel.trim();
                return processedLabel;
            } else {
                if (processedLabel.startsWith("<p>")) {

                    processedLabel = processedLabel.split("<p>")[1];

                    processedLabel = processedLabel.split(reg_ex_split)[0];
                    processedLabel = processedLabel.split(reg_ex_split)[0];

                    processedLabel = processedLabel.trim();

                    if (processedLabel.length() <= manageable_length) {

                        if (isGoodLabel(processedLabel)) {
                            return processedLabel;
                        }
                    }

                    return "";

                } else {
                    processedLabel = processedLabel.split(reg_ex_split)[0];
                    processedLabel = processedLabel.split(reg_ex_split)[0];

                    processedLabel = processedLabel.trim();

                    if (processedLabel.length() <= manageable_length) {
                        if (isGoodLabel(processedLabel)) {
                            return processedLabel;
                        }

                    } else {
                        processedLabel = processedLabel.substring(0, manageable_length);

                        if (isGoodLabel(processedLabel)) {
                            return processedLabel;
                        }
                    }

                    return "";
                }
            }
        }


        private boolean isGoodLabel(String label) {
            String consonant_regex = "[b-df-hj-np-tv-xz]";
            String more3_consonants_regex = consonant_regex + consonant_regex + consonant_regex + consonant_regex + "+";
            String more5_consonants_regex = consonant_regex + consonant_regex + consonant_regex + consonant_regex + consonant_regex + consonant_regex + "+";
            String vowel_regex = "[aeiou]";
            String more3_vowels_regex = vowel_regex + vowel_regex + vowel_regex + vowel_regex + "+";
            String same_character_3_times = ".*(.)\\1\\1.*";
            String space_character_3_times = ".*(\\s)\\1\\1.*";

            String[] words;

            if (label.length() < 3) {
                return false;
            }

            if (label.contains("!") || label.contains("?")) {
                return false;
            }


            if (label.matches(space_character_3_times)) {
                return false;
            }

            words = label.split(" ");

            boolean has_min_size_word = false;

            for (String word : words) {
                word = word.toLowerCase();

                if (NormalizeNumbers.getRomanNumbers10().contains(word)) {
                    continue;
                }

                if (word.length() > 1 && NormalizeNumbers.getRomanNumbers10().contains(word.substring(0, word.length() - 1))) {
                    continue;
                }

                if (word.equals("st") || word.equals("dr")) {
                    continue;
                }

                if (word.length() < 2) {
                    if (word.equals("a")) {
                        continue;
                    }
                    return false;

                } else {
                    has_min_size_word = true;
                }


                if (word.matches(same_character_3_times)) {
                    return false;
                }

                if (!word.startsWith("mc") && word.matches(more3_consonants_regex + ".*")) {
                    return false;
                }
                if (word.matches(".*" + more5_consonants_regex + ".*")) {
                    return false;
                }
                if (word.matches(".*" + more3_vowels_regex + ".*")) {
                    return false;
                }

                if (word.matches(consonant_regex + "+") || word.matches(vowel_regex + "+")) {
                    return false;
                }
            }

            return has_min_size_word;
        }
    }
}
