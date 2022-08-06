package com.bennavetta.jconsole.console;

import com.bennavetta.jconsole.commands.InputProcessor;
import com.bennavetta.jconsole.util.ColorUtil;
import com.bennavetta.jconsole.util.StringUtil;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Uses the Builder Design Pattern with the inner class TuiBuilder.<br><br>
 * <p>
 * Given a JFrame, it'll construct a T-UI (text user interface) inside, handling both input and output -
 * multiple Tui objects can be created for a JFrame, though only one can be selected at a time.<br>
 * To select a Tui object, either call the applySettingsToFrame() method directly, or indirectly through
 * any of the <b>print</b> or <b>nextFoo</b> or <b>queueBackground</b> methods.
 * </p>
 * @author woodrow73
 */
public class Tui {

    /** Thread safe map of ConsoleHandler.uniqueID to a list of all the user's input for a console on that JFrame. */
    public static Map<Integer, CopyOnWriteArrayList<String>> allUserInputLogs = Collections.synchronizedMap(new LinkedHashMap());

    /** A log of the user's input for the ConsoleHandler connected to the current Tui object.
     *  Holds an object reference to a <i>value</i> in allUserInputLogs. */
    private final List<String> userInputLog;

    /** When switching between Tui objects, how long the fade between background colors should take in milliseconds. */
    @Setter @Getter
    private static int backgroundColorTransitionMS = 1219;

    /** Processes all user input. */
    @Getter
    private final InputProcessor processor;

    /** A map to store commands and triggers. Multiple strings can be used for the same command, but multiple
     *  commands may not be referenced by the same string. */
    @Getter
    private final Map<String, InputProcessor> commandMap;

    /** How to process unrecognized commands. */
    @Getter
    private final InputProcessor processUnrecognizedCommand;

    /** Mutable. The background color of the console. (default Color.black) */
    @Getter
    private Color backgroundColor;

    /** The text color used with the print methods (doesn't change the color of previously printed text).<br>If this
     *  value is never set, it will be set to Color.green */
    @Getter
    private volatile Color defaultForegroundColor;

    /** The font of the console. (default new Font(Font.MONOSPACED, Font.BOLD, 14)) */
    @Getter
    private Font font;

    /** The prompt to display before each command. */
    @Getter
    private final String prompt;

    /** (default true) Whether the text color in the console should be reset to the defaultForegroundColor after each message. */
    private final AtomicBoolean resetColorAfterEachMsg;

    /** The delay after each character is printed in milliseconds. */
    @Getter @Setter
    private int charPrintDelayMS;

    /** The JFrame that this Tui is for. */
    @Getter
    private final JFrame frame;

    /** Settings for the JFrame passed into Tui.TuiBuilder to look like a Windows 10 CMD<br>
     *  Can choose to apply them in Tui.TuiBuilder's constructor. */
    public static class FrameSettings {
        /** The default frame size - same as the Windows 10 CMD window */
        public static final Dimension DEFAULT_SIZE = new Dimension(670, 435);

        /** The default frame location */
        public static final Point DEFAULT_LOCATION = new Point(30, 30);

        /** The default frame close operation */
        public static final int DEFAULT_CLOSE_OPERATION = JFrame.EXIT_ON_CLOSE;
    }

    private static final Color DEFAULT_BACKGROUND_COLOR = Color.black, DEFAULT_FOREGROUND_COLOR = Color.green;
    private static final Font DEFAULT_FONT = new Font(Font.MONOSPACED, Font.BOLD, 14);
    private static final String DEFAULT_PROMPT = "";
    private static final boolean DEFAULT_RESET_COLOR_AFTER_EACH_MSG = true;

    private final ConsoleHandler consoleHandler;

    /** A series of background colors to cycle through when this Tui is selected. */
    protected Color[] backgroundColorQueue = new Color[]{};

    /** The duration for each background color transition in the backgroundColorQueue. */
    protected int backgroundColorQueueDuration = getBackgroundColorTransitionMS();

    /** Whether the backgroundColorQueue should be deleted after it's been used. */
    protected boolean deleteBackgroundColorQueueAfterUse = false;

    /** A regex to get '0xRRGGBB' hex strings that are not immediately followed by another hex string */
    private String hexRegex = "0x[0-9A-Fa-f]{6}";

    private Tui(Tui.TuiBuilder builder) {
        this.processor = builder.processor.isPresent() ? builder.processor.get() : InputProcessor.NO_OP;
        this.commandMap = builder.commandMap.isPresent() ? builder.commandMap.get() : Map.of();
        this.processUnrecognizedCommand = builder.processUnrecognizedCommand.isPresent() ?
                builder.processUnrecognizedCommand.get() : InputProcessor.NO_OP;
        this.backgroundColor = builder.backgroundColor.isPresent() ? builder.backgroundColor.get() : DEFAULT_BACKGROUND_COLOR;
        this.defaultForegroundColor = builder.defaultForegroundColor.isPresent() ? builder.defaultForegroundColor.get() :
                DEFAULT_FOREGROUND_COLOR;
        this.font = builder.font.isPresent() ? builder.font.get() : DEFAULT_FONT;
        this.prompt = builder.prompt.isPresent() ? builder.prompt.get() : DEFAULT_PROMPT;
        this.resetColorAfterEachMsg = builder.resetColorAfterEachMsg.isPresent() ?
                new AtomicBoolean(builder.resetColorAfterEachMsg.get()) : new AtomicBoolean(DEFAULT_RESET_COLOR_AFTER_EACH_MSG);
        this.charPrintDelayMS = builder.charPrintDelayMS.isPresent() ? builder.charPrintDelayMS.get() : 0;

        this.frame = builder.frame;
        if(builder.setFrameLikeWindows10CMD) {
            frame.setSize(FrameSettings.DEFAULT_SIZE);
            frame.setLocation(FrameSettings.DEFAULT_LOCATION);
            frame.setDefaultCloseOperation(FrameSettings.DEFAULT_CLOSE_OPERATION);
        }

        // There is only one ConsoleHandler per JFrame, but there can be multiple Tui objects per ConsoleHandler/JFrame
        // Create a new ConsoleHandler instance if one doesn't already exist for this JFrame
        if(!ConsoleHandler.instances.containsKey(frame))
            consoleHandler = new ConsoleHandler(this);
        else
            consoleHandler = ConsoleHandler.instances.get(frame);

        userInputLog = allUserInputLogs.get(consoleHandler.getUniqueID());
    }

    /**
     * @param color The color to set the default foreground color to. If this Tui object is currently selected for the console,
     *              its foreground color will also be set.
     */
    public void setForegroundColor(Color color) {
        defaultForegroundColor = color;
        if(this.equals(consoleHandler.currentSettings)) { // if this Tui is the current Tui for the ConsoleHandler
            try {
                SwingUtilities.invokeAndWait(() -> consoleHandler.getConsole().setConsoleForeground(color));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Gets the current foreground color of the console. Not to be confused with the defaultForegroundColor of this
     * Tui object.
     * @return
     */
    public Color getForegroundColor() {
        return consoleHandler.getConsole().getTextPane().getForeground();
    }

    /**
     * Sets the background color.<br>
     * If this Tui is currently selected, the background color will immediately transition for a duration of
     * Tui.backgroundColorTransitionMS milliseconds, stopping any ongoing color transitions.<br><br>
     * To create a queue of color transitions, use the queueBackground() method.
     * @param color The color to set the background to (if this Tui object is currently selected).
     */
    public void setBackgroundColor(Color color) {
        backgroundColor = color;

        // if this Tui is the currently selected, update the background color of the console
        if(this.equals(consoleHandler.currentSettings))
            consoleHandler.setBackgroundTransitions(backgroundColorTransitionMS, color);
    }

    /**
     * Sets the background color.<br>
     * If this Tui is currently selected, the background color will immediately transition for the given duration,
     * stopping any ongoing color transitions.<br><br>
     * To create a queue of color transitions, use the queueBackground() method.
     * @param color The color to set the background to (if this Tui object is currently selected).
     * @param durationMS The duration of the color transition in milliseconds.
     */
    public void setBackgroundColor(Color color, int durationMS) {
        backgroundColor = color;

        // if this Tui is the currently selected, update the background color of the console
        if(this.equals(consoleHandler.currentSettings))
            consoleHandler.setBackgroundTransitions(durationMS, color);
    }

    /**
     * Creates a queue of background colors that'll either display immediately if this Tui object is selected,
     * or when this Tui object is next selected.<br><br>
     * The transition time between each of the colors will be Tui.backgroundColorTransitionMS milliseconds.
     * @param colors The colors to add to the queue of background color transitions.
     * @param deleteBackgroundColorQueueAfterUse Whether the queue of background colors should be deleted after it's been used.
     *                                           If false, the queue of colors will display every time this Tui object is selected.
     */
    public void queueBackground(Color[] colors, boolean deleteBackgroundColorQueueAfterUse) {
        backgroundColorQueue = colors;
        this.deleteBackgroundColorQueueAfterUse = deleteBackgroundColorQueueAfterUse;
        this.backgroundColorQueueDuration = getBackgroundColorTransitionMS();

        if(this.equals(consoleHandler.currentSettings)) {
            consoleHandler.setBackgroundTransitions(backgroundColorTransitionMS, colors);
            if(deleteBackgroundColorQueueAfterUse)
                backgroundColorQueue = new Color[]{};
        }
    }

    /**
     * Creates a queue of background colors that'll either display immediately if this Tui object is selected,
     * or when this Tui object is next selected.
     * @param colors The colors to add to the queue of background color transitions.
     * @param deleteBackgroundColorQueueAfterUse Whether the queue of background colors should be deleted after it's been used.
     *                                           If false, the queue of colors will display every time this Tui object is selected.
     * @param durationMS The duration of each color transition in milliseconds.
     */
    public void queueBackground(Color[] colors, boolean deleteBackgroundColorQueueAfterUse, int durationMS) {
        backgroundColorQueue = colors;
        this.deleteBackgroundColorQueueAfterUse = deleteBackgroundColorQueueAfterUse;
        this.backgroundColorQueueDuration = durationMS;

        if(this.equals(consoleHandler.currentSettings)) {
            consoleHandler.setBackgroundTransitions(durationMS, colors);
            if(deleteBackgroundColorQueueAfterUse)
                backgroundColorQueue = new Color[]{};
        }
    }

    /** Applies the settings in this Tui to the associated TuiFrame.
     *  This method gets called when any of the <b>print</b> methods or <b>nextFoo</b> methods are used. */
    public void applySettingsToFrame() {
        try {
            SwingUtilities.invokeAndWait(() -> consoleHandler.setConsoleSettings(this, true) );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setFont(Font font) {
        this.font = font;
        applySettingsToFrame();
    }

    public void setResetColorAfterEachMsg(boolean resetColorAfterEachMsg) {
        this.resetColorAfterEachMsg.set(resetColorAfterEachMsg);
    }

    public boolean getResetColorAfterEachMsg() {
        return resetColorAfterEachMsg.get();
    }

    /** Prints text to the console.
     *
     * @param text What to print; with color support for both ASCII sequences (listed in ColorUtil.getSupportedAnsiColors()),
     *             and hex codes in the format '0xRRGGBB'. Text after either sequence will be colorized.<br><br>
     *             The method ColorUtil.hex() can be used to convert a Color object to a hex code.<br><br>
     *             If resetColorAfterEachMessage is false, then the last detected ANSI or hex code will become the default foreground color.<br>
     *             The reset ANSI code \u001b[0m (saved in ColorUtil.resetANSI) will reset the color to the defaultForegroundColor.
     */
    public void print(String text) {
        print(text, Optional.empty(), Optional.empty(), false);
    }

    /** Prints text to the console then pauses the thread.
     *
     * @param text What to print; with color support for both ASCII sequences (listed in ColorUtil.getSupportedAnsiColors()),
     *             and hex codes in the format '0xRRGGBB'. Text after either sequence will be colorized.<br><br>
     *             The method ColorUtil.hex() can be used to convert a Color object to a hex code.<br><br>
     *             If resetColorAfterEachMessage is false, then the last detected ANSI or hex code will become the default foreground color.<br>
     *             The reset ANSI code \u001b[0m (saved in ColorUtil.resetANSI) will reset the color to the defaultForegroundColor.
     * @param pauseTime After printing the text, the time to pause the thread in milliseconds.
     */
    public void print(String text, int pauseTime) {
        print(text, Optional.empty(), Optional.of(pauseTime), false);
    }

    /** Prints text to the console with a newline at the end.
     *
     * @param text What to print; with color support for both ASCII sequences (listed in ColorUtil.getSupportedAnsiColors()),
     *             and hex codes in the format '0xRRGGBB'. Text after either sequence will be colorized.<br><br>
     *             The method ColorUtil.hex() can be used to convert a Color object to a hex code.<br><br>
     *             If resetColorAfterEachMessage is false, then the last detected ANSI or hex code will become the default foreground color.<br>
     *             The reset ANSI code \u001b[0m (saved in ColorUtil.resetANSI) will reset the color to the defaultForegroundColor.
     */
    public void println(String text) {
        print(text, Optional.empty(), Optional.empty(), true);
    }

    /** Prints text to the console with a newline at the end.
     *
     * @param text What to print; with color support for both ASCII sequences (listed in ColorUtil.getSupportedAnsiColors()),
     *             and hex codes in the format '0xRRGGBB'. Text after either sequence will be colorized.<br><br>
     *             The method ColorUtil.hex() can be used to convert a Color object to a hex code.<br><br>
     *             If resetColorAfterEachMessage is false, then the last detected ANSI or hex code will become the default foreground color.<br>
     *             The reset ANSI code \u001b[0m (saved in ColorUtil.resetANSI) will reset the color to the defaultForegroundColor.
     * @param pauseTime After printing the text, the time to pause the thread in milliseconds.
     */
    public void println(String text, int pauseTime) {
        print(text, Optional.empty(), Optional.of(pauseTime), true);
    }

    /** Prints text to the console in color.
     *
     * @param text What to print; with color support for both ASCII sequences (listed in ColorUtil.getSupportedAnsiColors()),
     *             and hex codes in the format '0xRRGGBB'. Text after either sequence will be colorized.<br><br>
     *             The method ColorUtil.hex() can be used to convert a Color object to a hex code.<br><br>
     *             If resetColorAfterEachMessage is false, then the last detected ANSI or hex code will become the default foreground color.<br>
     *             The reset ANSI code \u001b[0m (saved in ColorUtil.resetANSI) will reset the color to the defaultForegroundColor.
     * @param color What color to make the text - if resetColorAfterEachMessage is false, and no color hex codes or ANSI are
     *              in the text, this color will become the default foreground color.
     */
    public void print(String text, Color color) {
        print(text, Optional.of(color), Optional.empty(), false);
    }

    /** Prints text to the console in color.
     *
     * @param text What to print; with color support for both ASCII sequences (listed in ColorUtil.getSupportedAnsiColors()),
     *             and hex codes in the format '0xRRGGBB'. Text after either sequence will be colorized.<br><br>
     *             The method ColorUtil.hex() can be used to convert a Color object to a hex code.<br><br>
     *             If resetColorAfterEachMessage is false, then the last detected ANSI or hex code will become the default foreground color.<br>
     *             The reset ANSI code \u001b[0m (saved in ColorUtil.resetANSI) will reset the color to the defaultForegroundColor.
     * @param color What color to make the text - if resetColorAfterEachMessage is false, and no color hex codes or ANSI are
     *              in the text, this color will become the default foreground color.
     * @param pauseTime After printing the text, the time to pause the thread in milliseconds.
     */
    public void print(String text, Color color, int pauseTime) {
        print(text, Optional.of(color), Optional.of(pauseTime), false);
    }

    /** Prints text to the console in color with a newline at the end.
     *
     * @param text What to print; with color support for both ASCII sequences (listed in ColorUtil.getSupportedAnsiColors()),
     *             and hex codes in the format '0xRRGGBB'. Text after either sequence will be colorized.<br><br>
     *             The method ColorUtil.hex() can be used to convert a Color object to a hex code.<br><br>
     *             If resetColorAfterEachMessage is false, then the last detected ANSI or hex code will become the default foreground color.<br>
     *             The reset ANSI code \u001b[0m (saved in ColorUtil.resetANSI) will reset the color to the defaultForegroundColor.
     * @param color What color to make the text - if resetColorAfterEachMessage is false, and no color hex codes or ANSI are
     *              in the text, this color will become the default foreground color.
     */
    public void println(String text, Color color) {
        print(text, Optional.of(color), Optional.empty(), true);
    }

    /** Prints text to the console in color with a newline at the end.
     *
     * @param text What to print; with color support for both ASCII sequences (listed in ColorUtil.getSupportedAnsiColors()),
     *             and hex codes in the format '0xRRGGBB'. Text after either sequence will be colorized.<br><br>
     *             The method ColorUtil.hex() can be used to convert a Color object to a hex code.<br><br>
     *             If resetColorAfterEachMessage is false, then the last detected ANSI or hex code will become the default foreground color.<br>
     *             The reset ANSI code \u001b[0m (saved in ColorUtil.resetANSI) will reset the color to the defaultForegroundColor.
     * @param color What color to make the text - if resetColorAfterEachMessage is false, and no color hex codes or ANSI are
     *              in the text, this color will become the default foreground color.
     * @param pauseTime After printing the text, the time to pause the thread in milliseconds.
     */
    public void println(String text, Color color, int pauseTime) {
        print(text, Optional.of(color), Optional.of(pauseTime), true);
    }

    /**
     * First converts any Color objects into String hex codes, then formats the text with
     * String.format(String format, Object... args), then prints it to the console.
     *
     * @param format The text to format with String.format(String format, Object... args), then print to the console.<br><br>
     *               There's color support for both ASCII sequences (listed in ColorUtil.getSupportedAnsiColors()),
     *               and hex codes in the format '0xRRGGBB'. Text after either sequence will be colorized.<br><br>
     *               The method ColorUtil.hex() can be used to convert colors to a hex code.<br><br>
     *               If resetColorAfterEachMessage is false, then the last detected ANSI or hex code will become the default foreground color.<br>
     *               The reset ANSI code \u001b[0m (saved in ColorUtil.resetANSI) will reset the color to the defaultForegroundColor.<br>
     * @param args Any Color objects will be converted into String hex codes before being passed into
     *             String.format(String format, Object... args).<br><br>
     *             From String.format's Javadoc:<br><br>
     *             Arguments referenced by the format specifiers in the format string. If there are more arguments than
     *             format specifiers, the extra arguments are ignored. The number of arguments is variable and may be zero.
     *             The maximum number of arguments is limited by the maximum dimension of a Java array as defined by The
     *             Java™ Virtual Machine Specification. The behaviour on a null argument depends on the
     *             <a href="https://docs.oracle.com/javase/7/docs/api/java/util/Formatter.html#syntax">conversion</a>.
     */
    public void printf(String format, Object... args) {
        // Convert Color objects to hex codes
        for(int i = 0; i < args.length; i++) {
            if(args[i] instanceof Color) {
                args[i] = ColorUtil.hex((Color) args[i]);
            }
        }

        print(String.format(format, args), Optional.empty(), Optional.empty(), false);
    }

    /** Private print method that all other print methods point to. Prints text to the console.
     *
     * @param text What to print; with color support for both ASCII sequences (listed in ColorUtil.getSupportedAnsiColors()),
     *             and hex codes in the format '0xRRGGBB'. Text after either sequence will be colorized.<br><br>
     *             The method ColorUtil.hex() can be used to convert a Color object to a hex code.<br><br>
     *             If resetColorAfterEachMessage is false, then the last detected ANSI or hex code will become the default foreground color.<br>
     *             The reset ANSI code \u001b[0m (saved in ColorUtil.resetANSI) will reset the color to the defaultForegroundColor.
     * @param color What color to make the text - if resetColorAfterEachMessage is false, and no color hex codes or ANSI are
     *              in the text, this color will become the default foreground color.
     * @param pauseTime After printing the text, the time to pause the thread in milliseconds.
     * @param newLine Whether or not to print a newline at the end.
     */
    private void print(String text, Optional<Color> color, Optional<Integer> pauseTime, boolean newLine) {
        if(!consoleHandler.getCurrentSettings().equals(this))
            applySettingsToFrame();

        if(color.isPresent()) { // set the console's color
            try {
                SwingUtilities.invokeAndWait(() -> consoleHandler.getConsole().setConsoleForeground(color.get()));
                defaultForegroundColor = resetColorAfterEachMsg.get() ? defaultForegroundColor : color.get(); // update the default color
            } catch (Exception e) { e.printStackTrace(); }
        }
        text = ColorUtil.replaceAllAnsiWithHex(text, defaultForegroundColor);

        // print the text, setting the color after each detected hex code
        for(int i = 0; i < text.length(); i++) {
            // text that hasn't been printed yet
            String remaining = text.substring(i);

            // if the remaining text starts with a hex, set the color, move i past the hex, and continue
            if(StringUtil.startsWith(remaining, hexRegex)) {
                try {
                    SwingUtilities.invokeAndWait(() -> consoleHandler.getConsole()
                            .setConsoleForeground(Color.decode("#" + remaining.substring(2, 8))));
                } catch (Exception e) { e.printStackTrace(); }
                i += 7;
                continue;
            }

            // if the remaining text contains a hex code, print only the text before it
            boolean hasHex = StringUtil.contains(remaining, hexRegex);
            int singleColorSubstringEndIndex = hasHex ? StringUtil.indexOf(remaining, hexRegex).get() : remaining.length();

            // string to print
            String singleColorSubstring = remaining.substring(0, singleColorSubstringEndIndex);

            // if there's a char print delay, print the chars one at a time
            if(charPrintDelayMS > 0) {
                for(char c : singleColorSubstring.toCharArray()) {
                    try {
                        SwingUtilities.invokeAndWait(() -> consoleHandler.getConsole().print(String.valueOf(c)) );
                        Thread.sleep(charPrintDelayMS);
                    } catch (Exception e) { e.printStackTrace(); }
                }
            }
            else { // print the whole singleColorSubstring
                SwingUtilities.invokeLater(() -> consoleHandler.getConsole().print(singleColorSubstring) );
            }
            // move i to point after the printed text (accounting for i++ in the for loop header)
            i += singleColorSubstring.length() - 1;
        }

        if(newLine)
            println();

        if(resetColorAfterEachMsg.get()) {
            try {
                SwingUtilities.invokeAndWait(() -> consoleHandler.getConsole().setConsoleForeground(defaultForegroundColor));
            } catch (Exception e) { e.printStackTrace(); }
        }

        if(pauseTime.isPresent()) {
            try {
                Thread.sleep(pauseTime.get());
            } catch (InterruptedException e) { e.printStackTrace(); }
        }
    }

    /** Prints a newline to the console. */
    public void println() {
        if(!consoleHandler.getCurrentSettings().equals(this))
            applySettingsToFrame();

        SwingUtilities.invokeLater(() -> consoleHandler.getConsole().println());
    }

    /**
     * Waits for the user to enter a line of text, then returns it.
     * @return The user's next input.
     */
    public String nextLine() {
        if(!consoleHandler.getCurrentSettings().equals(this))
            applySettingsToFrame();

        final int initialInputLogSize = userInputLog.size();

        // TODO make an implementation using wait() and notify()
        while(true) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // check if the user has entered a line of text inside this.frame TuiFrame
            if (initialInputLogSize < userInputLog.size())
                return userInputLog.get(userInputLog.size() - 1);
        }
    }

    /**
     * Waits for the user to enter a line of text, then returns it.
     * @param color What color to make the user's input - if resetColorAfterEachMessage is false, this color will become
     *              the default foreground color.
     * @return The user's next input.
     */
    public String nextLine(Color color) {
        if(!consoleHandler.getCurrentSettings().equals(this))
            applySettingsToFrame();

        try { // set the color
            SwingUtilities.invokeAndWait(() -> consoleHandler.getConsole().setConsoleForeground(color));
            defaultForegroundColor = resetColorAfterEachMsg.get() ? defaultForegroundColor : color;
        } catch (Exception e) {
            e.printStackTrace();
        }

        String input = nextLine();
        setForegroundColor(defaultForegroundColor);
        return input;
    }

    /**
     * Waits for the user to enter a line of text, and attempts to parse it as an integer.
     * @return The int parsed from the user's input.
     * @throws InputMismatchException If the user's input is not parsable as an int.
     */
    public int nextInt() throws InputMismatchException {
        if(!consoleHandler.getCurrentSettings().equals(this))
            applySettingsToFrame();

        final int initialInputLogSize = userInputLog.size();

        // TODO make an implementation using wait() and notify()
        while(true) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // check if the user has entered a line of text inside this.frame TuiFrame
            if (initialInputLogSize < userInputLog.size()) {
                String input = userInputLog.get(userInputLog.size() - 1);
                try {
                    return Integer.parseInt(input);
                } catch(NumberFormatException e) {
                    throw new InputMismatchException(
                            String.format("The user's input '%s' is not parsable as an int.%n%s", input, e.getMessage()));
                }
            }
        }
    }

    /**
     * Waits for the user to enter a line of text, and attempts to parse it as an integer.
     * @param color What color to make the user's input - if resetColorAfterEachMessage is false, this color will become
     *              the default foreground color.
     * @return The int parsed from the user's input.
     * @throws InputMismatchException If the user's input is not parsable as an int.
     */
    public int nextInt(Color color) throws InputMismatchException {
        if(!consoleHandler.getCurrentSettings().equals(this))
            applySettingsToFrame();

        try { // set the color
            SwingUtilities.invokeAndWait(() -> consoleHandler.getConsole().setConsoleForeground(color));
            defaultForegroundColor = resetColorAfterEachMsg.get() ? defaultForegroundColor : color;
        } catch (Exception e) {
            e.printStackTrace();
        }

        int input = nextInt();
        setForegroundColor(defaultForegroundColor);
        return input;
    }

    /**
     * Uses the Builder Design Pattern, creating an immutable Tui object when the build() method is called<br>
     * <p>
     * A preset of settings for a TuiFrame - multiple presets can be created for a TuiFrame.
     *
     * </p>
     */

    /** Uses the Builder Design Pattern, creating an immutable Tui object when the build() method is called. */
    public static class TuiBuilder {

        private Optional<Map<String, InputProcessor>> commandMap = Optional.empty();

        private Optional<InputProcessor> processUnrecognizedCommand = Optional.empty(),
                processor = Optional.empty();

        private Optional<Color> backgroundColor = Optional.empty(),
                defaultForegroundColor = Optional.empty();

        private Optional<Font> font = Optional.empty();

        private Optional<String> prompt = Optional.empty();

        private Optional<Boolean> resetColorAfterEachMsg = Optional.empty();

        private Optional<Integer> charPrintDelayMS = Optional.empty();

        private final JFrame frame;

        private final boolean setFrameLikeWindows10CMD;

        /**
         * Starts building a T-UI (text user interface) for the given JFrame, using the Builder Design Pattern.
         * @param frame The JFrame to build a TUI (text user interface) for.
         */
        public TuiBuilder(JFrame frame) {
            this.frame = frame;
            this.setFrameLikeWindows10CMD = false;
        }

        /**
         * Starts building a T-UI (text user interface) for the given JFrame, using the Builder Design Pattern.
         * @param frame The JFrame to build a TUI (text user interface) for.
         * @param setFrameLikeWindows10CMD Whether the JFrame's size and location should be set to match the Windows 10 CMD,
         *                            with the default close operation set to JFrame.EXIT_ON_CLOSE<br><br>
         *                            All values that would be set are inside Tui.FrameSettings
         */
        public TuiBuilder(JFrame frame, boolean setFrameLikeWindows10CMD) {
            this.frame = frame;
            this.setFrameLikeWindows10CMD = setFrameLikeWindows10CMD;
        }

        /** @param processor Processes all user input. */
        public Tui.TuiBuilder processor(InputProcessor processor) {
            this.processor = Optional.of(processor);
            return this;
        }

        /** @param commandMap A map to store commands and triggers. */
        public Tui.TuiBuilder commandMap(Map<String, InputProcessor> commandMap) {
            this.commandMap = Optional.of(commandMap);
            return this;
        }

        /** @param processUnrecognizedCommand How to process unrecognized commands. */
        public Tui.TuiBuilder processUnrecognizedCommand(InputProcessor processUnrecognizedCommand) {
            this.processUnrecognizedCommand = Optional.of(processUnrecognizedCommand);
            return this;
        }

        /** @param backgroundColor The background color of the console. */
        public Tui.TuiBuilder backgroundColor(Color backgroundColor) {
            this.backgroundColor = Optional.of(backgroundColor);
            return this;
        }

        /** @param defaultForegroundColor The default text color used with the Tui classes' print methods
         *              (doesn't change the color of previously printed text).<br>Default is Color.green */
        public Tui.TuiBuilder foregroundColor(Color defaultForegroundColor) {
            this.defaultForegroundColor = Optional.of(defaultForegroundColor);
            return this;
        }

        /** @param font The font of the console (for equally wide characters, use a monospaced font). */
        public Tui.TuiBuilder font(Font font) {
            this.font = Optional.of(font);
            return this;
        }

        /** @param prompt The prompt to display before each command. */
        public Tui.TuiBuilder prompt(String prompt) {
            this.prompt = Optional.of(prompt);
            return this;
        }

        /** @param resetColorAfterEachMsg (default true) Whether the text color in the console should be reset to
         *      the defaultForegroundColor after each message. */
        public Tui.TuiBuilder resetColorAfterEachMsg(boolean resetColorAfterEachMsg) {
            this.resetColorAfterEachMsg = Optional.of(resetColorAfterEachMsg);
            return this;
        }

        /** @param charPrintDelayMS The delay after each character is printed in milliseconds. */
        public Tui.TuiBuilder charPrintDelayMS(int charPrintDelayMS) {
            this.charPrintDelayMS = Optional.of(charPrintDelayMS);
            return this;
        }

        /**
         * Builds an immutable Tui object.
         * @return The Tui object.
         */
        public Tui build() {
            return new Tui(this);
        }

    }
}