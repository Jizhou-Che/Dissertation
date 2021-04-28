package jonto.lexicon;

import jonto.io.TXTReader;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LexicalUtilities {
    private final Set<String> stopwordsSet = new HashSet<String>();

    private final Map<String, String> word2stemming = new HashMap<String, String>();

    public void loadStopwords() throws Exception {
        TXTReader reader = new TXTReader(LexicalUtilities.class.getResourceAsStream("/lexicon/stopwords.txt"));

        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.startsWith("#")) {
                stopwordsSet.add(line);
            }
        }
        reader.closeBuffer();

    }

    public Set<String> getStopwordsSet() {
        return stopwordsSet;
    }

    public String getStemming4Word(String str) {
        if (word2stemming.containsKey(str)) {
            return word2stemming.get(str);
        }

        String stemmed_word = Stemming.getStemmer(true).stripAffixes(str);

        word2stemming.put(str, stemmed_word);

        return stemmed_word;
    }

    public String getRomanNormalization4Number(String word) {
        return NormalizeNumbers.getRomanNormalization(word);
    }
}
