package jonto.indexing;

import jonto.indexing.entities.ClassIndex;
import jonto.indexing.entities.DataPropertyIndex;
import jonto.indexing.entities.IndividualIndex;
import jonto.indexing.entities.ObjectPropertyIndex;
import jonto.indexing.labelling.Interval;
import jonto.reasoning.DLReasoner;
import jonto.utilities.OWLUtilities;
import org.semanticweb.owlapi.model.*;

import java.util.*;

public abstract class IndexManager {
    protected Map<String, Set<Integer>> singleWordInvertedIndex = new HashMap<String, Set<Integer>>();

    protected int calls_tax_question = 0;
    protected int calls_disj_question = 0;

    protected double time_tax_question = 0.0;
    protected double time_disj_question = 0.0;

    protected long init, fin;

    public void addWordOccurrence(String word, int ident) {
        Set<Integer> temp;

        if (singleWordInvertedIndex.containsKey(word)) {
            temp = singleWordInvertedIndex.get(word);
        } else {
            singleWordInvertedIndex.put(word, temp = new HashSet<Integer>());
        }

        temp.add(ident);
    }

    public Set<Integer> getCooccurrenceOfWords(Set<String> words) {
        Set<Integer> classList = null;
        Set<Integer> classList_temp = null;

        for (String word : words) {
            if (singleWordInvertedIndex.containsKey(word)) {
                if (classList == null) {
                    classList = new HashSet<Integer>(singleWordInvertedIndex.get(word));
                } else {
                    classList_temp = singleWordInvertedIndex.get(word);
                    classList = intersectSet(classList, classList_temp);
                }
            }
        }

        return classList;
    }

    public Set<Integer> intersectSet(Set<Integer> list1, Set<Integer> list2) {
        if (list1 == null) {
            return list2;
        }
        if (list2 == null) {
            return list1;
        }

        Set<Integer> list = new HashSet<Integer>();

        for (Integer i : list1) {
            if (list2.contains(i)) {
                list.add(i);
            }
        }

        return list;
    }

    protected Map<Integer, ClassIndex> identifier2ClassIndex = new HashMap<Integer, ClassIndex>();

    protected Map<Integer, DataPropertyIndex> identifier2DataPropIndex = new HashMap<Integer, DataPropertyIndex>();

    protected Map<Integer, ObjectPropertyIndex> identifier2ObjPropIndex = new HashMap<Integer, ObjectPropertyIndex>();

    protected Map<Integer, IndividualIndex> identifier2IndividualIndex = new HashMap<Integer, IndividualIndex>();

    protected Set<Integer> identifiersInModule = new HashSet<Integer>();

    protected Map<Integer, Integer> preOrderAnc2Identifier = new HashMap<Integer, Integer>();

    protected Map<Integer, Integer> preOrderDesc2Identifier = new HashMap<Integer, Integer>();

    protected Map<Interval, Set<Interval>> interval2disjointIntervals = new HashMap<Interval, Set<Interval>>();

    protected Set<Integer> unsatisfiableClassesILS = new HashSet<Integer>();

    protected Map<Integer, String> identifier2IRIOnto = new HashMap<Integer, String>();

    protected OWLDataFactory factory = OWLUtilities.createOWLDataFactory();

    protected Set<Integer> RootIdentifiers = new HashSet<Integer>();

    protected Set<Integer> representativeNodes = new HashSet<Integer>();

    protected Map<Set<Integer>, Integer> generalHornAxioms = new HashMap<Set<Integer>, Integer>();

    public Set<Integer> dangerousClasses = new HashSet<Integer>();

    private Set<Integer> allowedInstanceTypes = new HashSet<Integer>();

    private int class_indiv_ident = 0;

    private int dprop_ident = 0;

    private int oprop_ident = 0;

    private int onto_ident = 0;


    public void clearAlternativeLabels4Classes() {

        for (int ident : identifier2ClassIndex.keySet()) {

            if (identifier2ClassIndex.get(ident).hasAlternativeLabels()) {
                identifier2ClassIndex.get(ident).deleteAltLabels();
            }
        }
    }

    protected HashMap<Integer, Set<Integer>> ident2DirectSubClasses_integration;

    protected Set<Integer> representativesFromMappings;


    public HashMap<Integer, Set<Integer>> getIdent2DirectSubClasses_Integration() {
        return ident2DirectSubClasses_integration;
    }

    public Set<Integer> getRepresentativesFromMappings() {
        return representativesFromMappings;
    }


    public Set<Integer> getDangerousClasses() {
        return dangerousClasses;
    }

    public void addDangerousClasses(int ide) {
        dangerousClasses.add(ide);
    }


    public Set<Integer> getAllowedInstanceTypes() {
        return allowedInstanceTypes;
    }

    public void setAllowedInstanceTypes(Set<Integer> allowedInstanceTypes) {
        this.allowedInstanceTypes = allowedInstanceTypes;
    }


    public abstract void setIntervalLabellingIndex(Map<Integer, Set<Integer>> exact_mappings);


    public void setJointReasoner(DLReasoner jointreasoner) {

    }


    public String getIRIStrOnto4Id(int id) {
        return identifier2IRIOnto.get(id);
    }


    public OWLDataFactory getFactory() {
        return factory;
    }


    public int addNewOntologyEntry(String iristr) {

        identifier2IRIOnto.put(onto_ident, iristr);

        onto_ident++;

        return (onto_ident - 1);

    }


    public int addNewClassEntry() {
        identifier2ClassIndex.put(class_indiv_ident, new ClassIndex(class_indiv_ident));
        class_indiv_ident++;
        return (class_indiv_ident - 1);
    }


    public void setOntologyId4Class(int ident, int id) {
        identifier2ClassIndex.get(ident).setOntologyId(id);
    }

    public void setClassName(int ident, String name) {
        identifier2ClassIndex.get(ident).setEntityName(name);
    }

    public void setClassNamespace(int ident, String ns_ent) {
        identifier2ClassIndex.get(ident).setNamespace(ns_ent);
    }

    public void setClassLabel(int ident, String label) {
        identifier2ClassIndex.get(ident).setLabel(label);
    }

    public void addAlternativeClassLabel(int ident, String altlabel) {
        identifier2ClassIndex.get(ident).addAlternativeLabel(altlabel);
    }

    public void addStemmedAltClassLabel(int ident, String label) {
        identifier2ClassIndex.get(ident).addStemmedAltLabel(label);
    }

    public void addRoot2Structure(int ide_root) {
        RootIdentifiers.add(ide_root);
    }

    public Set<Integer> getRootIdentifiers() {
        return RootIdentifiers;
    }

    public void addRepresentativeNode(int ide_rep) {
        representativeNodes.add(ide_rep);
    }

    public Set<Integer> getRepresentativeNodes() {
        return representativeNodes;
    }


    public void addGeneralHornAxiom2Structure(Set<Integer> body, int head) {
        if (!generalHornAxioms.containsKey(body)) {
            generalHornAxioms.put(new HashSet<Integer>(body), head);
        }
    }

    public Map<Set<Integer>, Integer> getGeneralHornAxiom() {
        return generalHornAxioms;
    }


    public int addNewIndividualEntry() {
        identifier2IndividualIndex.put(class_indiv_ident, new IndividualIndex(class_indiv_ident));
        class_indiv_ident++;
        return (class_indiv_ident - 1);
    }


    public void setOntologyId4Individual(int ident, int id) {
        identifier2IndividualIndex.get(ident).setOntologyId(id);
    }

    public void setIndividualName(int ident, String name) {
        identifier2IndividualIndex.get(ident).setEntityName(name);
    }

    public void setIndividualNamespace(int ident, String ns_ent) {
        identifier2IndividualIndex.get(ident).setNamespace(ns_ent);
    }

    public void setIndividualLabel(int ident, String label) {
        identifier2IndividualIndex.get(ident).setLabel(label);
    }

    public void addAlternativeIndividualLabel(int ident, String altlabel) {
        identifier2IndividualIndex.get(ident).addAlternativeLabel(altlabel);
    }


    public int addNewDataPropertyEntry() {
        identifier2DataPropIndex.put(dprop_ident, new DataPropertyIndex(dprop_ident));
        dprop_ident++;
        return (dprop_ident - 1);
    }


    public void setOntologyId4DataProp(int ident, int id) {
        identifier2DataPropIndex.get(ident).setOntologyId(id);
    }

    public void setDataPropName(int ident, String name) {
        identifier2DataPropIndex.get(ident).setEntityName(name);
    }

    public void setDataPropNamespace(int ident, String ns_ent) {
        identifier2DataPropIndex.get(ident).setNamespace(ns_ent);
    }


    public void setDataPropLabel(int ident, String label) {
        identifier2DataPropIndex.get(ident).setLabel(label);
    }


    public void addDomainClass4DataProperty(int ident, int domain_ident) {
        identifier2DataPropIndex.get(ident).addDomainClassIndex(domain_ident);
    }

    public void addRangeType4DataProperty(int ident, String range) {
        identifier2DataPropIndex.get(ident).addRangeType(range);
    }


    public void addAlternativeDataPropertyLabel(int ident, String altlabel) {
        identifier2DataPropIndex.get(ident).addAlternativeLabel(altlabel);
    }


    public void addType4Individual(int ident, int type_class) {
        identifier2IndividualIndex.get(ident).addClassTypeIndex(type_class);
    }


    public void addReferencedIndividual4Individual(int ident, int ref_ident) {
        identifier2IndividualIndex.get(ident).addReferencedIndividuals(ref_ident);
    }


    public Set<Integer> getReferencedIndividuals4Individual(int ident) {
        return identifier2IndividualIndex.get(ident).getReferencedIndividuals();
    }


    public List<Integer> getCharactersitics4Individual(int ident) {
        return identifier2IndividualIndex.get(ident).getCharacteristics();
    }


    public int addNewObjectPropertyEntry() {
        identifier2ObjPropIndex.put(oprop_ident, new ObjectPropertyIndex(oprop_ident));
        oprop_ident++;
        return (oprop_ident - 1);
    }


    public void setOntologyId4ObjectProp(int ident, int id) {
        identifier2ObjPropIndex.get(ident).setOntologyId(id);
    }

    public void setObjectPropName(int ident, String name) {
        identifier2ObjPropIndex.get(ident).setEntityName(name);
    }

    public void setObjectPropNamespace(int ident, String ns_ent) {
        identifier2ObjPropIndex.get(ident).setNamespace(ns_ent);
    }

    public void setObjectPropLabel(int ident, String label) {
        identifier2ObjPropIndex.get(ident).setLabel(label);
    }

    public void addDomainClass4ObjectProperty(int ident, int domain_ident) {
        identifier2ObjPropIndex.get(ident).addDomainClassIndex(domain_ident);
    }

    public void addRangeClass4ObjectProperty(int ident, int range_ident) {
        identifier2ObjPropIndex.get(ident).addRangeClassIndex(range_ident);
    }

    public void addAlternativeObjectPropertyLabel(int ident, String altlabel) {
        identifier2ObjPropIndex.get(ident).addAlternativeLabel(altlabel);
    }


    public Map<Integer, ClassIndex> getIdentifier2ClassIndexMap() {
        return identifier2ClassIndex;
    }


    public ClassIndex getClassIndex(int ident) {
        return identifier2ClassIndex.get(ident);
    }


    public DataPropertyIndex getDataPropertyIndex(int ident) {
        return identifier2DataPropIndex.get(ident);
    }


    public ObjectPropertyIndex getObjectPropertyIndex(int ident) {
        return identifier2ObjPropIndex.get(ident);
    }

    public IndividualIndex getIndividualIndex(int ident) {
        return identifier2IndividualIndex.get(ident);
    }

    public int getSizeIndexClasses() {
        return identifier2ClassIndex.size();
    }


    public int getSizeDataProperties() {
        return identifier2DataPropIndex.size();
    }


    public int getSizeObjectProperties() {
        return identifier2ObjPropIndex.size();
    }


    public int getSizeIndexIndividuals() {
        return identifier2IndividualIndex.size();
    }


    public Set<Integer> getDomainDataProp4Identifier(int index) {
        return identifier2DataPropIndex.get(index).getDomainClassIndexes();
    }

    public Set<Integer> getDomainObjProp4Identifier(int index) {
        return identifier2ObjPropIndex.get(index).getDomainClassIndexes();
    }

    public Set<String> getRangeDataProp4Identifier(int index) {
        return identifier2DataPropIndex.get(index).getRangeTypes();
    }

    public Set<Integer> getRangeObjProp4Identifier(int index) {
        return identifier2ObjPropIndex.get(index).getRangeClassIndexes();
    }


    public Set<Integer> getIndividualClassTypes4Identifier(int ident) {
        return identifier2IndividualIndex.get(ident).getClassTypes();
    }

    public Set<String> getIndividualCategory4Identifier(int ident) {
        return identifier2IndividualIndex.get(ident).getCategories();
    }


    public Set<Integer> getClassIdentifierSet() {
        return identifier2ClassIndex.keySet();
    }


    public Set<Integer> getDataPropIdentifierSet() {
        return identifier2DataPropIndex.keySet();
    }

    public Set<Integer> getObjectPropIdentifierSet() {
        return identifier2ObjPropIndex.keySet();
    }


    public Set<Integer> getIndividuaIdentifierSet() {
        return identifier2IndividualIndex.keySet();
    }


    public int getIdentifier4PreorderDesc(int preDesc) {
        return preOrderDesc2Identifier.getOrDefault(preDesc, -1);
    }

    public String getName4ConceptIndex(int index) {
        return identifier2ClassIndex.get(index).getEntityName();
    }

    public String getName4DataPropIndex(int index) {
        return identifier2DataPropIndex.get(index).getEntityName();
    }

    public String getName4ObjPropIndex(int index) {
        return identifier2ObjPropIndex.get(index).getEntityName();
    }

    public String getName4IndividualIndex(int index) {
        return identifier2IndividualIndex.get(index).getEntityName();
    }


    public String getIRIStr4ConceptIndex(int index) {

        int onto_id = identifier2ClassIndex.get(index).getOntologyId();

        return identifier2ClassIndex.get(index).getIRI(getIRIStrOnto4Id(onto_id));
    }


    public String getIRIStr4DataPropIndex(int index) {

        int onto_id = identifier2DataPropIndex.get(index).getOntologyId();

        return identifier2DataPropIndex.get(index).getIRI(getIRIStrOnto4Id(onto_id));
    }

    public String getIRIStr4ObjPropIndex(int index) {

        int onto_id = identifier2ObjPropIndex.get(index).getOntologyId();

        return identifier2ObjPropIndex.get(index).getIRI(getIRIStrOnto4Id(onto_id));
    }


    public String getIRIStr4IndividualIndex(int index) {

        int onto_id = identifier2IndividualIndex.get(index).getOntologyId();

        return identifier2IndividualIndex.get(index).getIRI(getIRIStrOnto4Id(onto_id));
    }


    public IRI getIRI4ConceptIndex(int index) {
        return IRI.create(getIRIStr4ConceptIndex(index));
    }

    public IRI getIRI4DataProptIndex(int index) {
        return IRI.create(getIRIStr4DataPropIndex(index));
    }

    public IRI getIRI4ObjPropIndex(int index) {
        return IRI.create(getIRIStr4ObjPropIndex(index));
    }

    public IRI getIRI4IndividualIndex(int index) {
        return IRI.create(getIRIStr4IndividualIndex(index));
    }


    public String getLabel4ConceptIndex(int index) {
        return identifier2ClassIndex.get(index).getLabel();
    }

    public String getLabel4DataPropIndex(int index) {
        return identifier2DataPropIndex.get(index).getLabel();
    }

    public String getLabel4ObjPropIndex(int index) {
        return identifier2ObjPropIndex.get(index).getLabel();
    }


    public String getLabel4IndividualIndex(int index) {
        return identifier2IndividualIndex.get(index).getLabel();
    }


    public Set<String> getAlternativeLabels4ConceptIndex(int index) {

        if (identifier2ClassIndex.get(index).hasAlternativeLabels()) {
            return identifier2ClassIndex.get(index).getAlternativeLabels();
        }

        HashSet<String> set = new HashSet<String>();
        set.add(getLabel4ConceptIndex(index));
        return set;

    }

    public Set<String> getAlternativeLabels4IndividualIndex(int index) {

        if (identifier2IndividualIndex.get(index).hasAlternativeLabels()) {
            return identifier2IndividualIndex.get(index).getAlternativeLabels();
        }

        HashSet<String> set = new HashSet<String>();
        set.add(getLabel4IndividualIndex(index));
        return set;

    }

    public Set<String> getAlternativeLabels4ObjectPropertyIndex(int index) {

        if (identifier2ObjPropIndex.get(index).hasAlternativeLabels()) {
            return identifier2ObjPropIndex.get(index).getAlternativeLabels();
        }

        HashSet<String> set = new HashSet<String>();
        set.add(getLabel4ObjPropIndex(index));
        return set;

    }


    public Set<String> getAlternativeLabels4DataPropertyIndex(int index) {

        if (identifier2DataPropIndex.get(index).hasAlternativeLabels()) {
            return identifier2DataPropIndex.get(index).getAlternativeLabels();
        }

        HashSet<String> set = new HashSet<String>();
        set.add(getLabel4DataPropIndex(index));
        return set;

    }


    public boolean hasObjectPropertyAlternativeLabels(int index) {

        return identifier2ObjPropIndex.get(index).hasAlternativeLabels();

    }


    public boolean hasDataPropertyAlternativeLabels(int index) {

        return identifier2DataPropIndex.get(index).hasAlternativeLabels();

    }


    public boolean hasIndividualAlternativeLabels(int index) {

        return identifier2IndividualIndex.get(index).hasAlternativeLabels();

    }


    public OWLClass getOWLClass4ConceptIndex(int index) {
        return factory.getOWLClass(getIRI4ConceptIndex(index));
    }


    public OWLDataProperty getOWLDataProperty4PropertyIndex(int index) {
        return factory.getOWLDataProperty(getIRI4DataProptIndex(index));
    }

    public OWLObjectProperty getOWLObjectProperty4PropertyIndex(int index) {
        return factory.getOWLObjectProperty(getIRI4ObjPropIndex(index));
    }

    public OWLNamedIndividual getOWLNamedIndividual4IndividualIndex(int index) {
        return factory.getOWLNamedIndividual(getIRI4IndividualIndex(index));
    }


    public int getPreOrderNumber(int conceptIdentifier) {
        return identifier2ClassIndex.get(conceptIdentifier).getNode().getDescOrder();
    }


    public int getPreOrderNumberReversed(int conceptIdentifier) {
        return identifier2ClassIndex.get(conceptIdentifier).getNode().getAscOrder();
    }


    public int getTopologicalOrder(int conceptIdentifier) {
        return identifier2ClassIndex.get(conceptIdentifier).getHierarchyLevel();
    }


    public Set<Interval> getIntervalsDescendants(int conceptIdentifier) {
        return identifier2ClassIndex.get(conceptIdentifier).getNode().getDescIntervals();
    }

    public Set<Interval> getIntervalsAncestors(int conceptIdentifier) {
        return identifier2ClassIndex.get(conceptIdentifier).getNode().getAscIntervals();
    }


    public Set<Integer> getDirectDisjointClasses4Identifier(int conceptIdentifier) {

        if (identifier2ClassIndex.get(conceptIdentifier).hasDirectDisjointClasses()) {
            return identifier2ClassIndex.get(conceptIdentifier).getDisjointClasses();
        } else {
            return Collections.emptySet();
        }
    }


    public Set<Integer> getDirectSuperClasses4Identifier(int conceptIdentifier, boolean module) {

        if (module) {
            return getDirectSuperClassesModule4Identifier(conceptIdentifier);
        } else {
            return getDirectSuperClassesOnto4Identifier(conceptIdentifier);
        }
    }


    public Set<Integer> getDirectSubClasses4Identifier(int conceptIdentifier, boolean module) {
        if (module) {
            return getDirectSubClassesModule4Identifier(conceptIdentifier);
        } else {
            return getDirectSubClassesOnto4Identifier(conceptIdentifier);
        }
    }


    private Set<Integer> getDirectSubClassesOnto4Identifier(int conceptIdentifier) {


        if (identifier2ClassIndex.get(conceptIdentifier).hasDirectSubClasses()) {
            return identifier2ClassIndex.get(conceptIdentifier).getDirectSubclasses();
        } else {
            return Collections.emptySet();
        }
    }


    private Set<Integer> getDirectSuperClassesOnto4Identifier(int conceptIdentifier) {

        if (identifier2ClassIndex.get(conceptIdentifier).hasDirectSuperClasses()) {
            return identifier2ClassIndex.get(conceptIdentifier).getDirectSuperclasses();
        } else {
            return Collections.emptySet();
        }
    }


    private Set<Integer> getDirectSubClassesModule4Identifier(int conceptIdentifier) {

        if (identifiersInModule.contains(conceptIdentifier)) {
            return getDirectSubClassesOnto4Identifier(conceptIdentifier);
        } else {
            return Collections.emptySet();
        }
    }


    private Set<Integer> getDirectSuperClassesModule4Identifier(int conceptIdentifier) {

        if (identifiersInModule.contains(conceptIdentifier)) {
            return getDirectSuperClassesOnto4Identifier(conceptIdentifier);
        } else {
            return Collections.emptySet();
        }

    }


    public Set<Integer> getRoots4Identifier(int conceptIdentifier) {
        if (identifier2ClassIndex.get(conceptIdentifier).hasRoots()) {
            return identifier2ClassIndex.get(conceptIdentifier).getRoots();
        } else {
            return Collections.emptySet();
        }

    }


    private final Map<Integer, Set<Integer>> ident2equivalents = new HashMap<Integer, Set<Integer>>();

    public Map<Integer, Set<Integer>> getEquivalentClasses() {

        if (ident2equivalents.size() > 0) {
            return ident2equivalents;
        }

        for (int ident : identifier2ClassIndex.keySet()) {
            if (identifier2ClassIndex.get(ident).hasEquivalentClasses()) {
                ident2equivalents.put(ident, identifier2ClassIndex.get(ident).getEquivalentClasses());
            }
        }

        return ident2equivalents;
    }


    private final Map<Integer, Set<Integer>> ident2subclasses_module = new HashMap<Integer, Set<Integer>>();

    private Map<Integer, Set<Integer>> getDirectSubClassesModule() {

        if (ident2subclasses_module.size() > 0) {
            return ident2subclasses_module;
        }

        for (int ident : identifiersInModule) {
            if (identifier2ClassIndex.get(ident).hasDirectSubClasses()) {
                ident2subclasses_module.put(ident, identifier2ClassIndex.get(ident).getDirectSubclasses());
            }
        }

        return ident2subclasses_module;
    }


    private final Map<Integer, Set<Integer>> ident2superclasses_module = new HashMap<Integer, Set<Integer>>();

    private Map<Integer, Set<Integer>> getDirectSuperClassesModule() {
        if (ident2superclasses_module.size() > 0) {
            return ident2superclasses_module;
        }

        for (int ident : identifiersInModule) {
            if (identifier2ClassIndex.get(ident).hasDirectSuperClasses()) {
                ident2superclasses_module.put(ident, identifier2ClassIndex.get(ident).getDirectSuperclasses());
            }
        }

        return ident2superclasses_module;
    }


    private final Map<Integer, Set<Integer>> ident2subclasses = new HashMap<Integer, Set<Integer>>();

    private Map<Integer, Set<Integer>> getDirectSubClassesOnto() {

        if (ident2subclasses.size() > 0) {
            return ident2subclasses;
        }

        for (int ident : identifier2ClassIndex.keySet()) {
            if (identifier2ClassIndex.get(ident).hasDirectSubClasses()) {
                ident2subclasses.put(ident, identifier2ClassIndex.get(ident).getDirectSubclasses());
            }
        }


        return ident2subclasses;
    }


    private final Map<Integer, Set<Integer>> ident2superclasses = new HashMap<Integer, Set<Integer>>();

    private Map<Integer, Set<Integer>> getDirectSuperClassesOnto() {
        if (ident2superclasses.size() > 0) {
            return ident2superclasses;
        }

        for (int ident : identifier2ClassIndex.keySet()) {
            if (identifier2ClassIndex.get(ident).hasDirectSuperClasses()) {
                ident2superclasses.put(ident, identifier2ClassIndex.get(ident).getDirectSuperclasses());
            }
        }


        return ident2superclasses;
    }


    private final Map<Integer, Set<Integer>> individual2classTypes = new HashMap<Integer, Set<Integer>>();

    public Map<Integer, Set<Integer>> getDirectIndividualClassTypes() {

        if (individual2classTypes.size() > 0) {
            return individual2classTypes;
        }

        for (int ident : identifier2IndividualIndex.keySet()) {
            if (identifier2IndividualIndex.get(ident).hasDirectClassTypes()) {
                individual2classTypes.put(ident, identifier2IndividualIndex.get(ident).getClassTypes());
            }
        }


        return individual2classTypes;
    }


    public Map<Integer, Set<Integer>> getDirectSubClasses(boolean module) {
        if (module) {
            return getDirectSubClassesModule();
        } else {
            return getDirectSubClassesOnto();
        }
    }


    public Map<Integer, Set<Integer>> getDirectSuperClasses(boolean module) {
        if (module) {
            return getDirectSuperClassesModule();
        } else {
            return getDirectSuperClassesOnto();
        }
    }


    private final Map<Integer, Set<Integer>> ident2disjointclasses = new HashMap<Integer, Set<Integer>>();

    public Map<Integer, Set<Integer>> getDirectDisjointClasses() {
        if (ident2disjointclasses.size() > 0) {
            return ident2disjointclasses;
        }

        for (int ident : identifier2ClassIndex.keySet()) {
            if (identifier2ClassIndex.get(ident).hasDirectDisjointClasses()) {
                ident2disjointclasses.put(ident, identifier2ClassIndex.get(ident).getDisjointClasses());
            }
        }


        return ident2disjointclasses;
    }

    public boolean isSubClassOf(int cIdent1, int cIdent2) {
        calls_tax_question++;
        init = Calendar.getInstance().getTimeInMillis();

        int preorder1 = getPreOrderNumber(cIdent1);

        for (Interval i2 : getIntervalsDescendants(cIdent2)) {
            if (i2.containsIndex(preorder1)) {

                fin = Calendar.getInstance().getTimeInMillis();
                time_tax_question += (float) ((double) fin - (double) init) / 1000.0;

                return true;
            }
        }

        fin = Calendar.getInstance().getTimeInMillis();
        time_tax_question += (float) ((double) fin - (double) init) / 1000.0;

        return false;

    }

    private boolean isSubClassOfInverseTax(int cIdent1, int cIdent2) {
        calls_tax_question++;
        init = Calendar.getInstance().getTimeInMillis();

        int preorder1 = getPreOrderNumberReversed(cIdent1);

        for (Interval i2 : getIntervalsAncestors(cIdent2)) {
            if (i2.containsIndex(preorder1)) {

                fin = Calendar.getInstance().getTimeInMillis();
                time_tax_question += (float) ((double) fin - (double) init) / 1000.0;

                return true;
            }
        }

        fin = Calendar.getInstance().getTimeInMillis();
        time_tax_question += (float) ((double) fin - (double) init) / 1000.0;

        return false;

    }


    public boolean isSuperClassOf(int cIdent1, int cIdent2) {
        return isSubClassOfInverseTax(cIdent1, cIdent2);
    }


    public boolean areEquivalentClasses(int cIdent1, int cIdent2) {
        return getPreOrderNumber(cIdent1) == getPreOrderNumber(cIdent2);
    }

    public boolean areSiblings(int cIdent1, int cIdent2) {
        for (int parent1 : getDirectSuperClassesOnto4Identifier(cIdent1)) {
            if (getDirectSuperClassesOnto4Identifier(cIdent2).contains(parent1)) {
                return true;
            }
        }

        return false;

    }

    public boolean areDisjoint(int cIdent1, int cIdent2) {
        calls_disj_question++;
        init = Calendar.getInstance().getTimeInMillis();

        int preorder1 = getPreOrderNumber(cIdent1);
        int preorder2 = getPreOrderNumber(cIdent2);


        for (Interval disj_int1 : interval2disjointIntervals.keySet()) {

            if (disj_int1.containsIndex(preorder1)) {

                for (Interval disj_int2 : interval2disjointIntervals.get(disj_int1)) {

                    if (disj_int2.containsIndex(preorder2)) {

                        fin = Calendar.getInstance().getTimeInMillis();
                        time_disj_question += (float) ((double) fin - (double) init) / 1000.0;

                        return true;
                    }
                }
            }
        }

        fin = Calendar.getInstance().getTimeInMillis();
        time_disj_question += (float) ((double) fin - (double) init) / 1000.0;

        return false;
    }

    public boolean isDisjointWithDescendants(int cIdent1, int cIdent2) {
        calls_disj_question++;
        init = Calendar.getInstance().getTimeInMillis();


        int preorder1 = getPreOrderNumber(cIdent1);
        Set<Interval> descendants = getIntervalsDescendants(cIdent2);


        for (Interval disj_int1 : interval2disjointIntervals.keySet()) {
            for (Interval desc : descendants) {
                if (disj_int1.hasNonEmptyIntersectionWith(desc)) {
                    for (Interval disj_int2 : interval2disjointIntervals.get(disj_int1)) {
                        if (disj_int2.containsIndex(preorder1)) {
                            fin = Calendar.getInstance().getTimeInMillis();
                            time_disj_question += (float) ((double) fin - (double) init) / 1000.0;

                            return true;
                        }
                    }
                }
            }
        }

        for (Interval disj_int1 : interval2disjointIntervals.keySet()) {
            if (disj_int1.containsIndex(preorder1)) {
                for (Interval disj_int2 : interval2disjointIntervals.get(disj_int1)) {
                    for (Interval desc : descendants) {
                        if (disj_int2.hasNonEmptyIntersectionWith(desc)) {
                            fin = Calendar.getInstance().getTimeInMillis();
                            time_disj_question += (float) ((double) fin - (double) init) / 1000.0;

                            return true;
                        }
                    }
                }
            }
        }

        fin = Calendar.getInstance().getTimeInMillis();
        time_disj_question += (float) ((double) fin - (double) init) / 1000.0;

        return false;
    }

    public boolean arePartiallyDisjoint(int cIdent1, int cIdent2) {

        calls_disj_question++;
        init = Calendar.getInstance().getTimeInMillis();


        Set<Interval> descendants1 = getIntervalsDescendants(cIdent1);
        Set<Interval> descendants2 = getIntervalsDescendants(cIdent2);


        for (Interval disj_int1 : interval2disjointIntervals.keySet()) {
            for (Interval desc1 : descendants1) {
                if (disj_int1.hasNonEmptyIntersectionWith(desc1)) {
                    for (Interval disj_int2 : interval2disjointIntervals.get(disj_int1)) {
                        for (Interval desc2 : descendants2) {
                            if (disj_int2.hasNonEmptyIntersectionWith(desc2)) {
                                return true;
                            }
                        }
                    }
                    break;
                }
            }
        }

        fin = Calendar.getInstance().getTimeInMillis();
        time_disj_question += (float) ((double) fin - (double) init) / 1000.0;

        return false;
    }


    public abstract Set<Integer> getScope4Identifier_Big(int ide);

    public abstract Set<Integer> getScope4Identifier_Condifence(int ide);

    public abstract Set<Integer> getScope4Identifier_Expansion(int ide);
}
