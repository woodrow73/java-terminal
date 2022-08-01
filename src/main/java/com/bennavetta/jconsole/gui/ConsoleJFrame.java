package com.bennavetta.jconsole.gui;

import com.bennavetta.jconsole.completion.DefaultCompletionSource;
import com.bennavetta.jconsole.commands.InputProcessor;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class ConsoleJFrame extends JFrame {

    private final Console console;

    /** Whether to enable ANSI colors using ANSI escape sequences. Default is true */
    private boolean enableAnsi = true;

    public static final Dimension defaultJFrameSize = new Dimension(670, 435);

    /**
     * Sets the UI's look and feel to a dark theme, and creates the console.
     * Sets the JFrame's size (670, 435), location (30, 30) & close operation (EXIT_ON_CLOSE) - all of which can be overwritten.
     *
     * setVisible(true) must be called to show the console.
     *
     * @param commandMap A map to store commands and triggers. Multiple strings can be used for the same command, but multiple
     *                   commands may not be referenced by the same string.
     * @param processUnrecognizedCommand How to process unrecognized commands.
     * @param backgroundColor The background color of the console.
     * @param foregroundColor The text color of the console.
     * @param font The font of the console (for equally wide characters, use a monospaced font).
     * @param prompt The prompt to display before each command.
     * @param resetColorAfterEachMsg Whether the text color in the console should be reset to
     *                               the param foregroundColor after each message.
     */
    public ConsoleJFrame(Map<String, InputProcessor> commandMap, InputProcessor processUnrecognizedCommand,
                        Color backgroundColor, Color foregroundColor, Font font, String prompt,
                         boolean resetColorAfterEachMsg) {

        console = new Console(backgroundColor, foregroundColor, font, prompt, enableAnsi, resetColorAfterEachMsg);

        // String commands go here as well.
        console.setCompletionSource(new DefaultCompletionSource(commandMap.keySet().stream().sorted().toArray(String[]::new)));

        console.setProcessor(new InputProcessor() { // This processor breaks a statement into args and passes them to the matching command
            public void process(Console console, String raw, String... args)
            {
                // process list of arguments
                if (args.length > 0 && commandMap.containsKey(args[0].toLowerCase()))
                    commandMap.get(args[0].toLowerCase()).process(console, raw, args);
                else
                    processUnrecognizedCommand.process(console, raw, args);
            }
        });

        this.add(console);
        this.addComponentListener(console);

        this.setSize(defaultJFrameSize);
        console.setScreenHeight((int) this.getContentPane().getSize().getHeight());
        this.setLocation(new Point(30, 30));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    /** @return The console contained in this JFrame - a JScrollPane that contains a JTextPane (named textPane) */
    public Console getConsole() {
        return console;
    }

    /** @param enableAnsi Whether to enable ANSI colors using ANSI escape sequences. Default is true */
    public void setEnableAnsi(boolean enableAnsi) {
        this.enableAnsi = enableAnsi;
        console.enableANSI = enableAnsi;
    }

    /** @return Whether ANSI escape sequences are enabled */
    public boolean getEnableAnsi() { return enableAnsi; }

    /** Writes text to the console with an added newline.
     *  Use write(String text, boolean newline) to write without a newline.
     *
     * @param text to write
     */
    public void write(String text) {
        console.write(text);
    }

    /** Writes text to the console.
     *
     * @param text to write
     * @param newline whether to add a newline
     */
    public void write(String text, boolean newline) {
        console.write(text, newline);
    }

    /** Writes text to the console in color with an added newline; ignores ANSI escape sequences.
     *  Use write(String text, Color color, boolean newline) to write without a newline.
     *
     * @param text to write
     * @param color what color to make the text
     */
    public void write(String text, Color color) {
        console.write(text, color);
    }

    /** Writes text to the console ignoring ANSI escape sequences.
     *
     * @param text to write
     * @param color what color to make the text
     * @param newline whether to add a newline
     */
    public void write(String text, Color color, boolean newline) {
        console.write(text, color, newline);
    }


    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        console.setScreenHeight((int) this.getContentPane().getSize().getHeight());
    }

    @Override
    public void setSize(Dimension d) {
        super.setSize(d);
        console.setScreenHeight((int) this.getContentPane().getSize().getHeight());
    }

    @Override
    public void setPreferredSize(Dimension preferredSize) {
        super.setPreferredSize(preferredSize);
        console.setScreenHeight((int) this.getContentPane().getSize().getHeight());
    }

}
