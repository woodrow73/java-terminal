package com.bennavetta.jconsole.util;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

    /**
     * Finds the first occurrence of the given regex in the given string
     * @param text The string to search in
     * @param regex The regex to match
     * @return  Optional.empty() if no match was found. Otherwise, it'll return the index of the first character
     *          inside the first matching regex in the string.
     */
    public static Optional<Integer> indexOf(String text, String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(text);

        Optional<Integer> firstIndex = Optional.empty();
        if(matcher.find()) {
            firstIndex = Optional.of(matcher.start());
        }
        return firstIndex;
    }

    /**
     * Check if the beginning of a string matches a given regex
     * @param text The string to test
     * @param regex The regex to match
     * @return  True if the string starts with the given regex
     */
    public static boolean startsWith(String text, String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(text);
        return matcher.find() && matcher.start() == 0;
    }

    /**
     * Check if a string contains at least one match of a given regex
     * @param text The string to test
     * @param regex The regex to match
     * @return  True if the string contains at least one match of the regex
     */
    public static boolean contains(String text, String regex) {
        return Pattern.compile(regex).matcher(text).find();
    }


}
