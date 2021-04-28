package jonto.io;

import jonto.mapping.objects.MappingObjectStr;
import jonto.utilities.Utilities;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class RDFReader {
    public static final String CELL = "Cell";
    public static final String ENTITY1 = "entity1";
    public static final String ENTITY2 = "entity2";
    public static final String RELATION = "relation";
    public static final String MEASURE = "measure";

    protected Set<MappingObjectStr> mappings = new HashSet<MappingObjectStr>();

    // Used in old reference standards.
    private static final String ALIGNMENTENTITY1 = "alignmententity1";
    private static final String ALIGNMENTENTITY2 = "alignmententity2";
    private static final String ALIGNMENTRELATION = "alignmentrelation";
    private static final String ALIGNMENTMEASURE = "alignmentmeasure";


    public RDFReader(URL url_rdf_alignment_file) throws Exception {
        this(url_rdf_alignment_file.openStream());

    }

    public RDFReader(String rdf_alignment_file) throws Exception {
        this(new FileInputStream(new File(rdf_alignment_file)));
    }


    public RDFReader(InputStream is) throws Exception {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader reader = factory.createXMLStreamReader(is);

        mappings.clear();

        String iri_str1 = "";
        String iri_str2 = "";
        String relation = "";
        double confidence = 0.0;
        int dir_relation;

        while (reader.hasNext()) {
            if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                if (reader.hasName()) {
                    switch (reader.getLocalName()) {
                        case RDFReader.CELL:
                            iri_str1 = "";
                            iri_str2 = "";
                            relation = "";
                            confidence = 0.0;
                            break;
                        case RDFReader.ENTITY1:
                        case RDFReader.ALIGNMENTENTITY1:
                            if (reader.getAttributeCount() > 0) {
                                iri_str1 = reader.getAttributeValue(0);
                            }
                            break;
                        case RDFReader.ENTITY2:
                        case RDFReader.ALIGNMENTENTITY2:
                            if (reader.getAttributeCount() > 0) {
                                iri_str2 = reader.getAttributeValue(0);
                            }
                            break;
                        case RDFReader.RELATION:
                        case RDFReader.ALIGNMENTRELATION:
                            relation = reader.getElementText();
                            break;
                        case RDFReader.MEASURE:
                        case RDFReader.ALIGNMENTMEASURE:
                            confidence = Double.parseDouble(reader.getElementText());
                            break;
                    }
                }
            } else if (reader.getEventType() == XMLStreamConstants.END_ELEMENT) {
                if (reader.hasName()) {
                    if (reader.getLocalName().equals(RDFReader.CELL)) {
                        dir_relation = switch (relation) {
                            case ">" -> Utilities.R2L;
                            case "<" -> Utilities.L2R;
                            case "?" -> Utilities.Flagged;
                            default -> Utilities.EQ;
                        };
                        mappings.add(new MappingObjectStr(iri_str1, iri_str2, confidence, dir_relation));
                    }
                }
            }
            reader.next();
        }
    }

    public Set<MappingObjectStr> getMappingObjects() {
        return mappings;
    }

    public int getMappingObjectsSize() {
        return mappings.size();
    }

}
