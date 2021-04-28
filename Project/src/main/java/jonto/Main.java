package jonto;

import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Objects;

public class Main {
    public static void main(String[] args) {
        String uri1 = "";
        String uri2 = "";
        String ref = "";
        String out;

        int ontopair = -1;
        boolean useOntopair = true;

        if (args.length == 2) {
            ontopair = Integer.parseInt(args[0]);
            out = args[1];
        } else if (args.length == 4) {
            uri1 = args[0];
            uri2 = args[1];
            ref = args[2];
            out = args[3];
            useOntopair = false;
        } else {
            System.out.println("Invalid arguments.");
            return;
        }

        if (useOntopair) {
            if (ontopair == 0) {
                // Default ontology pair.
                uri1 = "/datasets/anatomy_mouse.owl";
                uri2 = "/datasets/anatomy_human.owl";
                ref = "/datasets/anatomy_reference.rdf";
            } else if (ontopair == 1) {
                uri1 = "/datasets/FMA_small.owl";
                uri2 = "/datasets/NCI_small.owl";
                ref = "/datasets/FMA2NCI_reference.rdf";
            } else {
                System.out.println("Invalid ontology pair. Using default.");
                uri1 = "/datasets/anatomy_mouse.owl";
                uri2 = "/datasets/anatomy_human.owl";
                ref = "/datasets/anatomy_reference.rdf";
            }
        }

        try {
            long init = Calendar.getInstance().getTimeInMillis();

            // Absolute paths must be used.
            String pathonto1;
            String pathonto2;
            String pathref;
            if (useOntopair) {
                pathonto1 = "file:" + Paths.get(Objects.requireNonNull(Main.class.getResource(uri1)).toURI()).toFile().getAbsolutePath();
                pathonto2 = "file:" + Paths.get(Objects.requireNonNull(Main.class.getResource(uri2)).toURI()).toFile().getAbsolutePath();
                pathref = Paths.get(Objects.requireNonNull(Main.class.getResource(ref)).toURI()).toFile().getAbsolutePath();
            } else {
                pathonto1 = "file:" + uri1;
                pathonto2 = "file:" + uri2;
                pathref = ref;
            }

            new Jonto(pathonto1, pathonto2, pathref, out);

            long fin = Calendar.getInstance().getTimeInMillis();
            System.out.println("TOTAL TIME (s): " + ((double) fin - (double) init) / 1000.0 + ".");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
