package com.bennavetta.jconsole.util;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class ColorUtil {

    public static final Color D_Black   = Color.getHSBColor( 0.000f, 0.000f, 0.000f );
    public static final Color D_Red     = Color.getHSBColor( 0.000f, 1.000f, 0.502f );
    public static final Color D_Blue    = Color.getHSBColor( 0.667f, 1.000f, 0.502f );
    public static final Color D_Magenta = Color.getHSBColor( 0.833f, 1.000f, 0.502f );
    public static final Color D_Green   = Color.getHSBColor( 0.333f, 1.000f, 0.502f );
    public static final Color D_Yellow  = Color.getHSBColor( 0.167f, 1.000f, 0.502f );
    public static final Color D_Cyan    = Color.getHSBColor( 0.500f, 1.000f, 0.502f );
    public static final Color D_White   = Color.getHSBColor( 0.000f, 0.000f, 0.753f );
    public static final Color B_Black   = Color.getHSBColor( 0.000f, 0.000f, 0.502f );
    public static final Color B_Red     = Color.getHSBColor( 0.000f, 1.000f, 1.000f );
    public static final Color B_Blue    = Color.getHSBColor( 0.667f, 1.000f, 1.000f );
    public static final Color B_Magenta = Color.getHSBColor( 0.833f, 1.000f, 1.000f );
    public static final Color B_Green   = Color.getHSBColor( 0.333f, 1.000f, 1.000f );
    public static final Color B_Yellow  = Color.getHSBColor( 0.167f, 1.000f, 1.000f );
    public static final Color B_Cyan    = Color.getHSBColor( 0.500f, 1.000f, 1.000f );
    public static final Color B_White   = Color.getHSBColor( 0.000f, 0.000f, 1.000f );
    private static final Color cReset    = Color.getHSBColor( 0.000f, 0.000f, 1.000f );

    public static Color[] rainbow = { Color.red, Color.orange, Color.yellow, Color.green, Color.blue, new Color(75, 0, 130),
            new Color(148, 0, 211) };

    public static Color[] almostRainbow = { Color.red, B_Red, Color.yellow, Color.green, Color.cyan,
            new Color(148, 0, 211), Color.magenta };

    public static Map<String, Color> ansiColorMap = new LinkedHashMap<>() {{
        put("\u001B[30m", D_Black);
        put("\u001B[31m", D_Red);
        put("\u001B[32m", D_Green);
        put("\u001B[33m", D_Yellow);
        put("\u001B[34m", D_Blue);
        put("\u001B[35m", D_Magenta);
        put("\u001B[36m", D_Cyan);
        put("\u001B[37m", D_White);
        put("\u001B[0;30m", D_Black);
        put("\u001B[0;31m", D_Red);
        put("\u001B[0;32m", D_Green);
        put("\u001B[0;33m", D_Yellow);
        put("\u001B[0;34m", D_Blue);
        put("\u001B[0;35m", D_Magenta);
        put("\u001B[0;36m", D_Cyan);
        put("\u001B[0;37m", D_White);
        put("\u001B[1;30m", B_Black);
        put("\u001B[1;31m", B_Red);
        put("\u001B[1;32m", B_Green);
        put("\u001B[1;33m", B_Yellow);
        put("\u001B[1;34m", B_Blue);
        put("\u001B[1;35m", B_Magenta);
        put("\u001B[1;36m", B_Cyan);
        put("\u001B[1;37m", B_White);
        put("\u001B[0m", cReset);
    }};

    public static Color getANSIColor(String ANSIColor) {
        if (ansiColorMap.containsKey(ANSIColor))
            return ansiColorMap.get(ANSIColor);
        else
            return B_White;
    }

    /**
     * Find the distance between 2 colors
     * @param c1 Color 1 in range 0-255
     * @param c2 Color 2 in range 0-255
     * @param useAlpha If true, use alpha channel as well as RGB channels
     * @return Distance between c1 and c2, where the maximum distance is 765
     */
    public static double colorDistance(int[] c1, int[] c2, boolean useAlpha) {
        if(!useAlpha) {
            int rmean = (c1[0] + c2[0]) / 2;
            int r = c1[0] - c2[0];
            int g = c1[1] - c2[1];
            int b = c1[2] - c2[2];

            return Math.sqrt((((512+rmean)*r*r)>>8) + 4*g*g + (((767-rmean)*b*b)>>8));
        }
        else {
            int rmean = (c1[0] + c2[0]) / 2;
            int r = c1[0] - c2[0];
            int g = c1[1] - c2[1];
            int b = c1[2] - c2[2];
            int a = Math.abs(c1[3] - c2[3]);

            int aDistance = (int)(a*1.3);

            return Math.sqrt((((512+rmean)*r*r)>>8) + 4*g*g + (((767-rmean)*b*b)>>8)) + aDistance;
        }
    }

    /**
     * Find the distance between 2 colors
     * @param c1 Color 1 in range 0-255
     * @param c2 Color 2 in range 0-255
     * @return Distance between c1 and c2, where the maximum distance is 765
     */
    public static double colorDistance(Color c1, Color c2) {
        return colorDistance(new int[]{ c1.getRed(), c1.getGreen(), c1.getBlue()},
                new int[]{ c2.getRed(), c2.getGreen(), c2.getBlue()}, false);
    }


    /**
     * Gets perceived brightness of a color
     * Formula from: https://stackoverflow.com/a/56678483/7254424
     * @param c color to get perceived brightness of
     * @return perceived brightness of color, 0 for black, 100 for white
     */
    public static double brightness(Color c) {
        double[] colorChannels = { c.getRed() / 255.0, c.getGreen() / 255.0, c.getBlue() / 255.0 };
        //linearize the color channels
        for(int i = 0; i < colorChannels.length; i++) {
            if ( colorChannels[i] <= 0.04045 ) {
                colorChannels[i] /= 12.92;
            } else {
                colorChannels[i] = Math.pow(((colorChannels[i] + 0.055)/1.055),2.4);
            }
        }

        double luminance = (0.2126 * colorChannels[0] + 0.7152 * colorChannels[1] + 0.0722 * colorChannels[2]);
        double perceivedLuminance = -1;
        // get a luminance value between 0.0 and 1.0,
        // calculate "perceptual lightness" from luminance
        if (luminance <= (216.0/24389)) {       // The CIE standard states 0.008856 but 216/24389 is the intent for 0.008856451679036
            perceivedLuminance = luminance * (24389.0/27);  // The CIE standard states 903.3, but 24389/27 is the intent, making 903.296296296296296
        } else {
            perceivedLuminance = Math.pow(luminance, (1.0/3)) * 116 - 16;
        }
        return perceivedLuminance;
    }

    /**
     * Generates a random color until it sufficiently contrasts
     * @param c color from which to generate a contrasting color
     * @return contrasting color
     */
    public static Color getContrastingColor(Color c) {
        Color rand;
        do {
            rand = getRandomColor();
        } while(colorDistance(c, rand) < 222);

        double cBrightness = brightness(c);
        while(Math.abs(cBrightness - brightness(rand)) < 40) {
            if(cBrightness > 50) {
                rand = new Color((int)(rand.getRed() * 0.8), (int)(rand.getGreen() * 0.8), (int)(rand.getBlue() * 0.8));
            } else {
                rand = new Color(Math.min((int)(rand.getRed() * 1.2), 255), Math.min((int)(rand.getGreen() * 1.2), 255),
                        Math.min((int)(rand.getBlue() * 1.2), 255));
            }
        }
        return rand;
    }

    /**
     * Generates a random color near the given color
     * @param c color from which to generate a close random color
     * @param multiplier how far away the random color can be from the given color - .1 is 10% of the given color
     * @return close random color
     */
    public static Color getCloseColor(Color c, double multiplier) {
        return new Color(
                (int)Math.min(c.getRed() * (Math.random() * multiplier + (1 - multiplier/2)), 255),
                (int)Math.min(c.getGreen() * (Math.random() * multiplier + (1 - multiplier/2)), 255),
                (int)Math.min(c.getBlue() * (Math.random() * multiplier + (1 - multiplier/2)), 255));
    }

    public static Color getRandomColor() {
        return new Color(
                (int)(Math.random() * 256),
                (int)(Math.random() * 256),
                (int)(Math.random() * 256)
        );
    }

    /**
     * Creates a castle assembled from ASCII; its flags dyed red with E-ANSI
     * @return your castle
     */
    public static String getCastle() {
        String castle = "               T~~\n" +
                "               |\n" +
                "              /\"\\\n" +
                "      T~~     |'| T~~\n" +
                "  T~~ |    T~ WWWW|\n" +
                "  |  /\"\\   |  |  |/\\T~~\n" +
                " /\"\\ WWW  /\"\\ |' |WW|\n" +
                "WWWWW/\\| /   \\|'/\\|/\"\\\n" +
                "|   /__\\/]WWW[\\/__\\WWWW\n" +
                "|\"  WWWW'|I_I|'WWWW'  |\n" +
                "|   |' |/  -  \\|' |'  |\n" +
                "|'  |  |LI=H=LI|' |   |\n" +
                "|   |' | |[_]| |  |'  |\n" +
                "|   |  |_|###|_|  |   |\n" +
                "'---'--'-/___\\-'--'---'";

        castle = castle.replace("~", "\u001B[31m~").replace("W", "\u001B[0;37mW").
                replace("_", "\u001B[1;31m_").replace("|", "\u001B[0;37m|").
                replace("\"", "\u001B[33m\"").replace("'", "\u001B[33m'").
                replace("T", "\u001B[37mT").
                replace("/", "\u001B[31m/").replace("I", "\u001B[0;37mI").
                replace("L", "\u001B[0;37mL").replace("=", "\u001B[0;35m=").
                replace("H", "\u001B[0;37mH").replace("-", "\u001B[0;37m-");

        String formatted = "";

        for(int i = 0; i < castle.length(); i++) {
            if(castle.charAt(i) == '\\' && castle.charAt(i+1) != 'n') {
                formatted += "\u001B[31m\\";
            }
            else if((castle.charAt(i) == '[' || castle.charAt(i) == ']') && !Character.isDigit(castle.charAt(i+1)))
                formatted += "\u001B[0;35m" + castle.charAt(i);
            else
                formatted += castle.charAt(i);
        }

        return formatted;
    }

}
