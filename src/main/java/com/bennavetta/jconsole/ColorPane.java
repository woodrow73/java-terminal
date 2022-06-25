package com.bennavetta.jconsole;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class ColorPane extends JTextPane {

    private Console console;

    public List<Color> colors = new ArrayList<>();
    public List<String> text = new ArrayList<>();

    static Color colorCurrent;
    String remaining = "";

    public ColorPane(Console console, Color foreground) {
        this.console = console;
        this.colorCurrent = foreground;
    }

    public void append(Color c, String s, MutableAttributeSet attrs) {
        StyleConstants.setForeground(attrs, c);
        int len = getDocument().getLength(); // same value as getText().length();
        setCaretPosition(len);  // place caret at the end (with no selection)
        setCharacterAttributes(attrs, false);
        replaceSelection(s); // there is no selection, so inserts at caret
    }

    public void appendANSI(String s, MutableAttributeSet attrs) { // convert ANSI color codes first
        colorCurrent = console.resetColorAfterEachMsg ? console.FOREGROUND : colorCurrent;
        int aPos = 0;   // current char position in addString
        int aIndex = 0; // index of next Escape sequence
        int mIndex = 0; // index of "m" terminating Escape sequence
        String tmpString = "";
        boolean stillSearching = true; // true until no more Escape sequences
        String addString = remaining + s;
        remaining = "";

        if (addString.length() > 0) {
            aIndex = addString.indexOf("\u001B"); // find first escape
            if (aIndex == -1) { // no escape/color change in this string, so just send it with current color
                append(colorCurrent, addString, attrs);
                return;
            }
            // otherwise There is an escape character in the string, so we must process it

            if (aIndex > 0) { // Escape is not first char, so send text up to first escape
                tmpString = addString.substring(0,aIndex);
                append(colorCurrent, tmpString, attrs);
                aPos = aIndex;
            }
            // aPos is now at the beginning of the first escape sequence

            stillSearching = true;
            while (stillSearching) {
                mIndex = addString.indexOf("m",aPos); // find the end of the escape sequence
                if (mIndex < 0) { // the buffer ends halfway through the ansi string!
                    remaining = addString.substring(aPos,addString.length());
                    stillSearching = false;
                    continue;
                }
                else {
                    tmpString = addString.substring(aPos,mIndex+1);
                    colorCurrent = ColorUtil.getANSIColor(tmpString);
                }
                aPos = mIndex + 1;
                // now we have the color, send text that is in that color (up to next escape)

                aIndex = addString.indexOf("\u001B", aPos);

                if (aIndex == -1) { // if that was the last sequence of the input, send remaining text
                    tmpString = addString.substring(aPos,addString.length());
                    append(colorCurrent, tmpString, attrs);
                    stillSearching = false;
                    continue; // jump out of loop early, as the whole string has been sent now
                }

                // there is another escape sequence, so send part of the string and prepare for the next
                tmpString = addString.substring(aPos,aIndex);
                aPos = aIndex;
                append(colorCurrent, tmpString, attrs);

            } // while there's text in the input buffer
        }
    }

}