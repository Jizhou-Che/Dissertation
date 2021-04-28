package jonto.lexicon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NormalizeNumbers {
    private final static String[] CARDINALS_STR_ARRAY = {
            "zero",
            "one",
            "two",
            "three",
            "four",
            "five",
            "six",
            "seven",
            "eight",
            "nine",
            "ten"
    };

    private final static String[] CARDINALS_ARRAY = {
            "0",
            "1",
            "2",
            "3",
            "4",
            "5",
            "6",
            "7",
            "8",
            "9",
            "10"
    };

    private final static String[] ORDINALS_STR_ARRAY = {
            "zeroth",
            "first",
            "second",
            "third",
            "fourth",
            "fifth",
            "sixth",
            "seventh",
            "eighth",
            "ninth",
            "tenth"
    };

    private final static String[] ORDINALS_ARRAY = {
            "0th",
            "1st",
            "2nd",
            "4rd",
            "4th",
            "5th",
            "6th",
            "7th",
            "8th",
            "9th",
            "10th"
    };

    private final static String[] ROMANS_ARRAY = {
            "0", //Workaround: No roman representation, but need to fill index 0
            "i",
            "ii",
            "iii",
            "iv",
            "v",
            "vi",
            "vii",
            "viii",
            "ix",
            "x"
    };

    private static final List<String> CARDINALS_STR = new ArrayList<String>();
    private static final List<String> CARDINALS = new ArrayList<String>();
    private static final List<String> ORDINALS_STR = new ArrayList<String>();
    private static final List<String> ORDINALS = new ArrayList<String>();
    private static final List<String> ROMANS = new ArrayList<String>();

    static {
        Collections.addAll(ROMANS, ROMANS_ARRAY);

        Collections.addAll(CARDINALS_STR, CARDINALS_STR_ARRAY);

        Collections.addAll(CARDINALS, CARDINALS_ARRAY);

        Collections.addAll(ORDINALS_STR, ORDINALS_STR_ARRAY);

        Collections.addAll(ORDINALS, ORDINALS_ARRAY);
    }

    public static String getRomanNormalization(String word) {
        int index;

        index = CARDINALS_STR.indexOf(word);
        if (index > -1)
            return ROMANS.get(index);

        index = CARDINALS.indexOf(word);
        if (index > -1)
            return ROMANS.get(index);

        index = ORDINALS_STR.indexOf(word);
        if (index > -1)
            return ROMANS.get(index);

        index = ORDINALS.indexOf(word);
        if (index > -1)
            return ROMANS.get(index);

        return "";
    }

    public static List<String> getRomanNumbers10() {
        return ROMANS;
    }
}
