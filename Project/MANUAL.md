# User Manual


## Introduction

This user manual provides a comprehensive set of instructions for building and deploying Jonto and LogMap for comparative testing. The structures of artefacts are introduced, and common pitfalls in running the tests are explained in detail.

### Jonto

The deliverable artefact of Jonto is a single Java archive `Jonto.jar`. It runs on either built-in or user-specified datasets, outputs an alignment in OWL format for all possible entities to a user specified directory, and evaluates the result with respect to the reference alignment. Please note that Jonto was built on Java SDK 16, and a lower version of Java may fail to run the program.

### LogMap

The deliverable artefact of LogMap is the full version of LogMap 4, which participated in the 2020 OAEI Campaign in the Anatomy and Largebio tracks. While it has modes for user interaction and so on, this archived version was configured to run fully automatically on commandline. It takes two ontologies and outputs alignments in different formats. It also evaluates its result with respect to a reference alignment.

The `LogMap` directory in the artefacts contains the following items:

1. `logmap-matcher-4.0.jar` the Java archive of LogMap built using Maven.

2. `java-dependencies` the dependencies required by the Java archive.

3. `parameters.txt` default parameters used by LogMap.

## Using the test script

### Running Jonto

Before running Jonto, it is important to note that the artefact cannot be put in a directory with spaces and special characters in its path. Also, please note that the 2-argument version of the command currently fails for tha Java archive due to a Maven issue.

Jonto can be configured to run matching tasks over either built-in ontologies or user-specified ones. There are two built-in ontology sets, being the Anatomy track and FMA_NCI_SMALL track as stated in the report. To run Jonto over these datasets, two commandline arguments need to be specified. The full command looks like:

`java --add-opens java.base/java.lang=ALL-UNNAMED -jar <path_to_jonto_archive> <dataset_id> <absolute_output_path>`

Detailed explanations are given below.

1. The `--add-opens java.base/java.lang=ALL-UNNAMED` VM options must be added. This is due to an exceptional attempt to access Java internal classes in the OWL API implementation.

2. Specify `<path_to_jonto_archive>` as the path to the `Jonto.jar` archive. The following 2 segments are commandline arguments for Jonto.

3. The first commandline argument `<dataset_id>` is the ID of the built-in dataset to be used. It can be either 0 or 1 at the moment, with 0 specifying the Anatomy dataset, and 1 specifying the FMA_NCI_SMALL track.

4. The second commandline argument `<absolute_output_path>` is the output path for the generated alignment. It should be absolute and must exist. For example:
   `/Users/jizhou/Downloads/out/`.

To run Jonto over external datasets, four commandline arguments need to be specified. The full command looks like:

`java --add-opens java.base/java.lang=ALL-UNNAMED -jar <path_to_jonto_archive> <path_to_owl_1> <path_to_owl_2> <path_to_reference_rdf_or_txt> <absolute_output_path>`

1. The first and second commandline arguments are the two OWL ontologies to be matched. They can be in either relative or absolute paths. It is important for Jonto that they are given in the correct order as specified in the reference alignment.

2. The third commandline argument is the reference alignment. It can be in either relative or absolute paths, and the RDF and TXT formats are supported.

3. The fourth commandline argument is the output path for the generated alignment. It should be absolute and must exist. For example:
   `/Users/jizhou/Downloads/out/`.

### Running LogMap

To run LogMap for evaluation, 6 commandline arguments need to be specified. The full command generally looks like:

`java --add-opens java.base/java.lang=ALL-UNNAMED -jar <path_to_logmap_archive> EVALUATION file:<absolute_path_to_owl_1> file:<absolute_path_to_owl_2> <absolute_path_to_reference_rdf> <absolute_output_path> <classify_boolean>`

Detailed explanations are given below.

1. The `--add-opens java.base/java.lang=ALL-UNNAMED` VM options must be added. Again, this is due to an exceptional attempt to access Java internal classes in the OWL API implementation.

2. Specify `<path_to_logmap_archive>` as the path to the `logmap-matcher-4.0.jar` archive. The following 6 segments are commandline arguments for the LogMap matcher.

3. The first commandline argument `EVALUATION` tells LogMap to perform evaluation with respect to a reference mapping file.

4. The second and third commandline arguments are the two OWL ontologies to be matched. LogMap requires them to be specified as a `file:` indicator followed by an absolute path. For example:

    `file:/Users/jizhou/Downloads/LargeBio_dataset_oaei/oaei_NCI_small_overlapping_fma.owl`

   Please note that non-ASCII characters as well as some reserved characters including spaces are not permitted in the OWL ontology path. If you encounter an `Illegal character in path at index #` error, try to avoid using special characters in the path.

5. The fourth commandline argument is the reference alignment RDF. It should be an absolute path to an RDF document. For example:

    `/Users/jizhou/Downloads/LargeBio_dataset_oaei/oaei_FMA2NCI_UMLS_mappings_with_flagged_repairs.rdf`

6. The fifth commandline argument is the output path. It should be absolute and must exist. For example:

    `/Users/jizhou/Downloads/out/`

7. The sixth commandline argument is a boolean telling LogMap whether to classify the input ontologies together with the mappings or not.
