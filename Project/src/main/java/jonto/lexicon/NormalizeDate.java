package jonto.lexicon;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class NormalizeDate {
    private static final SimpleDateFormat formatter_in = new SimpleDateFormat();
    private static final SimpleDateFormat formatter_out = new SimpleDateFormat("dd-MMM-yyyy");

    // Used date patterns.
    private static String date_format1 = "yyyy-MM-dd";
    private static String date_format2 = "yyyy-MMM-dd";
    private static String date_format3 = "dd-MM-yyyy";
    private static String date_format4 = "dd-MMM-yyyy";
    private static String date_format5 = "dd, MMM yyyy";
    private static String date_format6 = "dd MMM yyyy";
    private static String date_format7 = "yyyy MMM dd";
    private static String date_format8 = "yyyy/MM/dd";
    private static String date_format9 = "yyyy/MMM/dd";
    private static String date_format10 = "dd/MM/yyyy";
    private static String date_format11 = "dd/MMM/yyyy";

    private static final Set<String> date_formats = new HashSet<String>();

    static {
        date_formats.add(date_format1);
        date_formats.add(date_format2);
        date_formats.add(date_format3);
        date_formats.add(date_format4);
        date_formats.add(date_format5);
        date_formats.add(date_format6);
        date_formats.add(date_format7);
        date_formats.add(date_format8);
        date_formats.add(date_format9);
        date_formats.add(date_format10);
        date_formats.add(date_format11);
    }


    /**
     * Normalizes a give data (string format) to dd-MMM-yyyy.
     * If the give string is not a date or not in the accepted formats it returns the same string.
     */
    public static String normalize(String date_str) {
        String normalized_date;

        for (String pattern : date_formats) {
            if (!(normalized_date = applyPattern(date_str, pattern)).equals("")) {
                return normalized_date;
            }
        }

        return date_str;
    }

    private static String applyPattern(String date_str, String pattern) {
        try {
            formatter_in.applyPattern(pattern);

            Date date = formatter_in.parse(date_str);

            if (date.getYear() < -1000) {
                return "";
            }

            return formatter_out.format(date);

        } catch (Exception e) {
            return "";
        }

    }
}
