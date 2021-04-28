package jonto.utilities;

import java.util.Calendar;

public class Utilities {
    // Mapping directions.
    public static final int L2R = 0;
    public static final int R2L = -1;
    public static final int EQ = -2;
    public static final int NoMap = -3;
    public static final int Flagged = -4;

    public static double getRoundValue(double value, int decimals) {
        double aux = Math.pow(10.0, (double) decimals);
        return (double) Math.round(value * aux) / aux;
    }

    public static String getEntityLabelFromURI(String uriStr) {
        if (uriStr.contains("#")) {
            if (uriStr.split("#").length > 1) {
                int index = uriStr.indexOf("#"); //First occurrence
                return uriStr.substring(index + 1);
            } else {
                System.out.println("Empty label: " + uriStr);
                return "empty" + Calendar.getInstance().getTimeInMillis();
            }
        }
        int index = uriStr.lastIndexOf("/");
        if (index >= 0) {
            return uriStr.substring(index + 1);
        }
        System.out.println("Complete URI: " + uriStr);

        return uriStr;
    }

    public static String getNameSpaceFromURI(String uriStr) {
        if (uriStr.startsWith("http")) {
            if (uriStr.contains("#")) {
                return uriStr.split("#")[0];
            } else {
                int index = uriStr.lastIndexOf("/");
                if (index >= 0) {
                    return uriStr.substring(0, index + 1);
                }
            }
            return uriStr;
        } else {
            return "";
        }
    }

    public static String capitalPrepositions(String str) {
        // Most propositions are not in capitals within the label.

        str = str.replaceAll("of(?=\\p{Upper})", "Of");
        str = str.replaceAll("with(?=\\p{Upper})", "With");
        str = str.replaceAll("for(?=\\p{Upper})", "For");
        str = str.replaceAll("and(?=\\p{Upper})", "And");
        str = str.replaceAll("by(?=\\p{Upper})", "By");
        str = str.replaceAll("to(?=\\p{Upper})", "To");
        str = str.replaceAll("on(?=\\p{Upper})", "On");
        str = str.replaceAll("in(?=\\p{Upper})", "In");

        return str;
    }

    public static String[] splitStringByCapitalLetter(String str) {
        // Capitalise prepositions.
        str = capitalPrepositions(str);

        // Either there is anything that is not an uppercase character followed by an uppercase character, or there is a lowercase character followed by a digit.
        String pattern = "(?<=[^\\p{Upper}])(?=\\p{Upper})" + "|(?<=[\\p{Lower}])(?=\\d)";

        return str.split(pattern);
    }
}
