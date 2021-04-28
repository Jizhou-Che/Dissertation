package jonto.io;

public class AlignmentWriter {
    private final OWLAlignmentFormat OWLAlignment;

    public AlignmentWriter(String out) throws Exception {
        OWLAlignment = new OWLAlignmentFormat("file:" + out + "alignment.owl");
    }

    public void addClassMapping(String iri_str1, String iri_str2, int direction, double conf) throws Exception {
        OWLAlignment.addClassMapping2Output(iri_str1, iri_str2, direction, conf);
    }

    public void addInstanceMapping(String iri_str1, String iri_str2, double conf) throws Exception {
        OWLAlignment.addInstanceMapping2Output(iri_str1, iri_str2, conf);
    }

    public void addObjPropMapping(String iri_str1, String iri_str2, int direction, double conf) throws Exception {
        OWLAlignment.addObjPropMapping2Output(iri_str1, iri_str2, direction, conf);
    }

    public void addDataPropMapping(String iri_str1, String iri_str2, int direction, double conf) throws Exception {
        OWLAlignment.addDataPropMapping2Output(iri_str1, iri_str2, direction, conf);
    }

    public void saveFiles() throws Exception {
        OWLAlignment.saveOutputFile();
    }
}
