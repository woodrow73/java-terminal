package com.bennavetta.jconsole;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

    /**
     * Finds the first index of the matching regex pattern in the given string.
     * @param regex The regex pattern to match.
     * @param text The string to search.
     * @return The index of the first match, or -1 if no match was found.
     */
    public static int indexOfRegex(String regex, String text) {
        Matcher m = Pattern.compile(regex).matcher(text);
        if(m.find())
            return m.start();
        else
            return -1;
    }
}
