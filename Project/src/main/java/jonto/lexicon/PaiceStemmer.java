package jonto.lexicon;

import java.io.*;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.Vector;

/* 	author:   	Christopher O'Neill
 date:		Sep 2000
 comments: 	The Paice/Husk Stemmer Translated from Pascal*/

/****************************************
 * Class: Paice * * Purpose: Stemming Algorithm *
 ****************************************/
public class PaiceStemmer {

    private final Vector ruleTable; // array of rules
    private final int[] ruleIndex; // index to above
    private boolean preStrip;

    /********************************
     * Method: Paice * * Purpose: init *
     ********************************/
    public PaiceStemmer(String rules, String pre) {
        ruleTable = new Vector();
        ruleIndex = new int[26];
        preStrip = pre.equals("/p");
        ReadRules(rules);
    }

    /************************************************************************
     * Method: ReadRules * Returns: void * Receives: * Purpose: read rules in
     * from stemRules and enters them * into ruleTable, ruleIndex is set up to
     * provide * faster access to relevant rules. *
     ************************************************************************/
    private void ReadRules(String stemRules) {
        int ruleCount = 0;
        int j;

        try {
            InputStreamReader isr = new InputStreamReader(Objects.requireNonNull(PaiceStemmer.class.getResourceAsStream(stemRules)));
            BufferedReader br = new BufferedReader(isr);
            String line;
            try {
                while ((line = br.readLine()) != null) {
                    ruleCount++;
                    j = 0;
                    StringBuilder rule;
                    rule = new StringBuilder();
                    while ((j < line.length()) && (line.charAt(j) != ' ')) {
                        rule.append(line.charAt(j));
                        j++;
                    }
                    ruleTable.addElement(rule.toString());
                }
            } catch (Exception e) {
                System.err.println("File Error Durring Reading Rules" + e);
                System.exit(0);
            }

            try {
                isr.close();
            } catch (Exception e) {
                System.err.println("Error Closing File During Reading Rules");
            }
        } catch (Exception e) {
            System.err.println("Input File" + stemRules + "not found");
            System.exit(1);
        }

        char ch = 'a';
        for (j = 0; j < 25; j++) {
            ruleIndex[j] = 0;
        }

        for (j = 0; j < (ruleCount - 1); j++) {
            while (((String) ruleTable.elementAt(j)).charAt(0) != ch) {
                ch++;
                ruleIndex[charCode(ch)] = j;
            }
        }
    }

    /****************************************************************
     * Method: FirstVowel * Returns: int * Recievs: String word, int last *
     * Purpose: checks lower-case word for position of * the first vowel *
     ****************************************************************/
    private int FirstVowel(String word, int last) {
        int i = 0;
        if ((i < last) && (!(vowel(word.charAt(i), 'a')))) {
            i++;
        }
        if (i != 0) {
            while ((i < last) && (!(vowel(word.charAt(i), word.charAt(i - 1))))) {
                i++;
            }
        }
        return Math.min(i, last);
    }

    /************************************************************************
     * Method: stripSuffixes * Returns: String * Recievs: String word * Purpose:
     * strips suffix off word and returns stem using * paice stemming algorithm
     * *
     ************************************************************************/
    private String stripSuffixes(String word) {
        // integer variables 1 is positive, 0 undecided, -1 negative equiverlent
        // of pun vars positive undecided negative
        int ruleok;
        int Continue = 0;
        // integer varables
        int pll = 0; // position of last letter
        int xl; // counter for nuber of chars to be replaced and length of
        // stemmed word if rule was aplied
        int pfv; // poition of first vowel
        int prt; // pointer into rule table
        int ir; // index of rule
        int iw; // index of word
        // char variables
        char ll; // last letter
        // String variables eqiverlent of tenchar variables
        String rule; // varlable holding the current rule
        StringBuilder stem = new StringBuilder(); // string holding the word as it is being stemmed this
        // is returned as a stemmed word.
        // boolean varable
        boolean intact = true; // intact if the word has not yet been stemmed to
        // determin a requirement of some stemming rules

        // set stem = to word
        stem = new StringBuilder(Clean(word.toLowerCase()));

        // move through the word to find the position of the last letter before
        // a non letter char
        while ((pll + 1 < stem.length())
                && ((stem.charAt(pll + 1) >= 'a') && (stem.charAt(pll + 1) <= 'z'))) {
            pll++;
        }

        if (pll < 1) {
            Continue = -1;
        }
        // find the position of the first vowel
        pfv = FirstVowel(stem.toString(), pll);

        // repeat until continue == negative ie. -1
        while (Continue != -1) {
            Continue = 0; // SEEK RULE FOR A NEW FINAL LETTER
            ll = stem.charAt(pll); // last letter

            // Check to see if there are any possible rules for stemming
            if ((ll >= 'a') && (ll <= 'z')) {
                prt = ruleIndex[charCode(ll)]; // pointer into rule-table
            } else {
                prt = -1;// 0 is a vaild rule
            }
            if (prt == -1) {
                Continue = -1; // no rule available
            }

            if (Continue == 0)
            // THERE IS A POSSIBLE RULE (OR RULES) : SEE IF ONE WORKS
            {
                rule = (String) ruleTable.elementAt(prt); // Take first rule
                while (Continue == 0) {
                    ruleok = 0;
                    if (rule.charAt(0) != ll) {
                        // rule-letter changes
                        Continue = -1;
                        ruleok = -1;
                    }
                    ir = 1; // index of rule: 2nd character
                    iw = pll - 1; // index of word: next-last letter

                    // repeat untill the rule is not undecided find a rule that
                    // is acceptable
                    while (ruleok == 0) {
                        if ((rule.charAt(ir) >= '0')
                                && (rule.charAt(ir) <= '9')) // rule fully
                        // matched
                        {
                            ruleok = 1;
                        } else if (rule.charAt(ir) == '*') {
                            // match only if word intact
                            if (intact) {
                                ir = ir + 1; // move forwards along rule
                                ruleok = 1;
                            } else {
                                ruleok = -1;
                            }
                        } else if (rule.charAt(ir) != stem.charAt(iw)) {
                            // mismatch of letters
                            ruleok = -1;
                        } else if (iw <= pfv) {
                            // insufficient stem remains
                            ruleok = -1;
                        } else {
                            // move on to compare next pair of letters
                            ir = ir + 1; // move forwards along rule
                            iw = iw - 1; // move backwards along word
                        }
                    }

                    // if the rule that has just been checked is valid
                    if (ruleok == 1) {
                        // CHECK ACCEPTABILITY CONDITION FOR PROPOSED RULE
                        xl = 0; // count any replacement letters
                        while (!((rule.charAt(ir + xl + 1) >= '.') && (rule
                                .charAt(ir + xl + 1) <= '>'))) {
                            xl++;
                        }
                        xl = pll + xl + 48 - ((int) (rule.charAt(ir)));
                        // position of last letter if rule used
                        if (pfv == 0) {
                            // if word starts with vowel...
                            if (xl < 1) {
                                // ...minimal stem is 2 letters
                                ruleok = -1;
                            }
                        }
                        // if word start swith consonant...
                        else if ((xl < 2) | (xl < pfv)) {
                            ruleok = -1;
                            // ...minimal stem is 3 letters...
                            // ...including one or more vowel
                        }
                    }
                    // if using the rule passes the assertion tests
                    if (ruleok == 1) {
                        // APPLY THE MATCHING RULE
                        intact = false;
                        // move end of word marker to position...
                        // ... given by the numeral.
                        pll = pll + 48 - ((int) (rule.charAt(ir)));
                        ir++;
                        stem = new StringBuilder(stem.substring(0, (pll + 1)));

                        // append any letters following numeral to the word
                        while ((ir < rule.length())
                                && (('a' <= rule.charAt(ir)) && (rule
                                .charAt(ir) <= 'z'))) {
                            stem.append(rule.charAt(ir));
                            ir++;
                            pll++;
                        }

                        // if rule ends with '.' then terminate
                        if ((rule.charAt(ir)) == '.') {
                            Continue = -1;
                        } else {
                            // if rule ends with '>' then Continue
                            Continue = 1;
                        }
                    } else {
                        // if rule did not match then look for another
                        prt = prt + 1; // move to next rule in RULETABLE
                        rule = (String) ruleTable.elementAt(prt);
                        if (rule.charAt(0) != ll) {
                            // rule-letter changes
                            Continue = -1;
                        }
                    }
                }
            }
        }
        return stem.toString();
    }

    /****************************************************************
     * Method: vowel * Returns: boolean * Recievs: char ch, char prev * Purpose:
     * determin whether ch is a vowel or not * uses prev determination when ch
     * == y *
     ****************************************************************/
    private boolean vowel(char ch, char prev) {
        switch (ch) {
            case 'a':
            case 'e':
            case 'i':
            case 'o':
            case 'u':
                return true;
            case 'y': {
                return switch (prev) {
                    case 'a', 'e', 'i', 'o', 'u' -> false;
                    default -> true;
                };
            }
            default:
                return false;
        }
    }

    /****************************************************************
     * Method: charCode * Returns: int * Recievs: char ch * Purpose: returns the
     * relavent array index for * specified char 'a' to 'z' *
     ****************************************************************/
    private int charCode(char ch) {
        return ((int) ch) - 97;
    }

    /********************************************************
     * Method: stripPrefixes * Returns: String * Recievs: String str * Purpose:
     * removes prefixes so that suffix * removal can comence *
     ********************************************************/
    private String stripPrefixes(String str) {
        String[] prefixes = {"kilo", "micro", "milli", "intra", "ultra", "mega", "nano", "pico", "pseudo"};

        for (String prefix : prefixes) {
            if ((str.startsWith(prefix))
                    && (str.length() > prefix.length())) {
                str = str.substring(prefix.length());
                return str;
            }
        }
        return str;
    }

    /********************************************************
     * Method: Clean * Returns: String * Recievs: String str * Purpose: remove
     * all non letter or digit * characters from srt and return *
     ********************************************************/
    private String Clean(String str) {
        int last = str.length();
        StringBuilder temp = new StringBuilder();
        for (int i = 0; i < last; i++) {
            if ((str.charAt(i) >= 'a') & (str.charAt(i) <= 'z')) {
                temp.append(str.charAt(i));
            }
        }
        return temp.toString();
    }

    /****************************************************************
     * Method: stripAffixes * Returns: String * Recievs: String str * Purpose:
     * prepares string and calls stripPrefixes * and stripSuffixes *
     ****************************************************************/
    public String stripAffixes(String str) {
        // str = str.toLowerCase(); //change all letters in the input to
        // lowercase
        // str = Clean(str); // remove all chars from string that are not a
        // letter or a digit (why digit?)
        if ((str.length() > 3) && (preStrip)) // if str's length is greater than
        // 2 then remove prefixes
        {
            str = stripPrefixes(str);
        }
        if (str.length() > 3) // if str is not null remove suffix
        {
            str = stripSuffixes(str);
        }
        return str;
    } // stripAffixes

}
