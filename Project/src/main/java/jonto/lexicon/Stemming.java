package jonto.lexicon;

public class Stemming {
    private static PaiceStemmer paice;

    public static final String STEMRULES_FILE = "/lexicon/stemrules.txt";

    public static PaiceStemmer getStemmer(Boolean stripPreffixes) {
        if (paice == null) {
            if (stripPreffixes) {
                paice = new PaiceStemmer(STEMRULES_FILE, "/p");
            } else {
                paice = new PaiceStemmer(STEMRULES_FILE, "");
            }
        }
        return paice;
    }
}
