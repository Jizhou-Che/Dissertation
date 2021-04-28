package jonto.mapping.objects;

public class MappingObjectStr {
    String Iri_ent1_str;
    String Iri_ent2_str;
    double confidence;

    double lexical_conf;
    double scope_conf;

    int dir_mappings;
    int typeMappings;


    // Direction.
    public static final int SUB = 0;
    public static final int SUP = -1;
    public static final int EQ = -2;
    public static final int Flagged = -4;


    // Entities.
    public static final int CLASSES = 0;
    public static final int DATAPROPERTIES = 1;
    public static final int OBJECTPROPERTIES = 2;
    public static final int INSTANCES = 3;
    public static final int UNKNOWN = 4;


    public MappingObjectStr(String iri_ent1, String iri_ent2) {

        Iri_ent1_str = iri_ent1;
        Iri_ent2_str = iri_ent2;
        confidence = -1;
        dir_mappings = MappingObjectStr.EQ;
        typeMappings = MappingObjectStr.UNKNOWN;
    }

    public MappingObjectStr(String iri_ent1, String iri_ent2, double conf) {

        Iri_ent1_str = iri_ent1;
        Iri_ent2_str = iri_ent2;
        confidence = conf;
        dir_mappings = MappingObjectStr.EQ;
        typeMappings = MappingObjectStr.UNKNOWN;

    }

    public MappingObjectStr(String iri_ent1, String iri_ent2, double conf, int dir_mapping) {

        Iri_ent1_str = iri_ent1;
        Iri_ent2_str = iri_ent2;
        confidence = conf;
        dir_mappings = dir_mapping;
        typeMappings = MappingObjectStr.UNKNOWN;
    }

    public MappingObjectStr(String iri_ent1, String iri_ent2, double conf, int dir_mapping, int typeMapping) {

        Iri_ent1_str = iri_ent1;
        Iri_ent2_str = iri_ent2;
        confidence = conf;
        dir_mappings = dir_mapping;
        typeMappings = typeMapping;
    }


    public int getMappingDirection() {
        return dir_mappings;
    }

    public String getIRIStrEnt1() {
        return Iri_ent1_str;

    }

    public String getIRIStrEnt2() {
        return Iri_ent2_str;

    }

    public double getConfidence() {
        return confidence;

    }

    public boolean isClassMapping() {
        return getTypeOfMapping() == CLASSES;
    }

    public boolean isInstanceMapping() {
        return getTypeOfMapping() == INSTANCES;
    }

    public boolean isDataPropertyMapping() {
        return getTypeOfMapping() == DATAPROPERTIES;
    }

    public boolean isObjectPropertyMapping() {
        return getTypeOfMapping() == OBJECTPROPERTIES;
    }

    public int getTypeOfMapping() {
        return typeMappings;

    }

    public void setTypeOfMapping(int type) {
        typeMappings = type;

    }


    public void setDirMapping(int dir) {
        dir_mappings = dir;
    }

    public void setIRIStrEnt1(String iri1) {
        Iri_ent1_str = iri1;

    }

    public void setIRIStrEnt2(String iri2) {
        Iri_ent2_str = iri2;

    }


    public void setConfidenceMapping(double conf) {
        confidence = conf;

    }

    public void setLexicalConfidenceMapping(double conf) {
        lexical_conf = conf;

    }

    public void setScopeConfidenceMapping(double conf) {
        scope_conf = conf;

    }

    public double getLexicalConfidenceMapping() {
        return lexical_conf;
    }

    public double getStructuralConfidenceMapping() {
        return scope_conf;
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (!(o instanceof MappingObjectStr i)) {
            return false;
        }

        return equals(i);
    }

    public boolean equals(MappingObjectStr m) {
        return Iri_ent1_str.equals(m.getIRIStrEnt1()) && Iri_ent2_str.equals(m.getIRIStrEnt2());
    }

    public String toString() {
        if (dir_mappings == EQ) {
            return "<" + Iri_ent1_str + "==" + Iri_ent2_str + ">";
        } else if (dir_mappings == SUB) {
            return "<" + Iri_ent1_str + "<" + Iri_ent2_str + ">";
        } else {
            return "<" + Iri_ent1_str + ">" + Iri_ent2_str + ">";
        }
    }

    public int hashCode() {
        int code = 10;
        code = 40 * code + Iri_ent1_str.hashCode();
        code = 50 * code + Iri_ent2_str.hashCode();
        return code;
    }
}
