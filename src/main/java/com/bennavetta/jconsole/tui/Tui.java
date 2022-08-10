package com.bennavetta.jconsole.tui;

import com.bennavetta.jconsole.commands.InputProcessor;

import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 * Uses a mutable Builder Design Pattern with the inner class Builder.<br><br>
 * <p>
 * Given a JFrame, it'll construct a T-UI (text user interface) inside, handling both input and output -
 * multiple Tui objects can be created for a JFrame, though only one can be selected at a time.<br>
 * To select a Tui object, either call the applySettingsToFrame() method directly, or indirectly through
 * any of the <b>print</b> or <b>nextFoo</b> or <b>queueBackground</b> methods.
 * </p>
 * @author woodrow73
 */
public class Tui extends TuiParent {

    private Tui(Tui.Builder builder) {
        super(builder);
    }

    /** Uses a mutable Builder Design Pattern, creating a Tui object when the build() method is called. */
    public static class Builder {

        protected Optional<Map<String, InputProcessor>> commandMap = Optional.empty();

        protected Optional<InputProcessor> processUnrecognizedCommand = Optional.empty(),
                processor = Optional.empty();

        protected Optional<Color> backgroundColor = Optional.empty(),
                defaultForegroundColor = Optional.empty();

        protected Optional<Font> font = Optional.empty();

        protected Optional<String> prompt = Optional.empty();

        protected Optional<Integer> charPrintDelayMS = Optional.empty();

        protected final JFrame frame;

        protected boolean resetColorAfterEachMsg;

        protected final boolean setFrameLikeWindows10CMD;

        /**
         * Starts building a T-UI (text user interface) for the given JFrame, using the Builder Design Pattern.
         * @param frame The JFrame to build a TUI (text user interface) for.
         * @param resetColorAfterEachMsg Whether the text color in the console should be reset to
         *                               the defaultForegroundColor after each message.
         */
        public Builder(JFrame frame, boolean resetColorAfterEachMsg) {
            this.frame = frame;
            this.resetColorAfterEachMsg = resetColorAfterEachMsg;
            this.setFrameLikeWindows10CMD = false;
        }

        /**
         * Starts building a T-UI (text user interface) for the given JFrame, using the Builder Design Pattern.
         * @param frame The JFrame to build a TUI (text user interface) for.
         * @param resetColorAfterEachMsg Whether the text color in the console should be reset to
         *                               the defaultForegroundColor after each message.
         * @param setFrameLikeWindows10CMD Whether the JFrame's size and location should be set to match the Windows 10 CMD,
         *                            with the default close operation set to JFrame.EXIT_ON_CLOSE<br><br>
         *                            All values that would be set are inside Tui.FrameSettings
         */
        public Builder(JFrame frame, boolean resetColorAfterEachMsg, boolean setFrameLikeWindows10CMD) {
            this.frame = frame;
            this.setFrameLikeWindows10CMD = setFrameLikeWindows10CMD;
            this.resetColorAfterEachMsg = resetColorAfterEachMsg;
        }

        /** @param processor Processes all user input.
         *  @return This Builder object for method chaining. */
        public Tui.Builder processor(InputProcessor processor) {
            this.processor = Optional.of(processor);
            return this;
        }

        /** @param commandMap A map to store commands and triggers.
         *  @return This Builder object for method chaining. */
        public Tui.Builder commandMap(Map<String, InputProcessor> commandMap) {
            this.commandMap = Optional.of(commandMap);
            return this;
        }

        /** @param processUnrecognizedCommand How to process unrecognized commands.
         *  @return This Builder object for method chaining. */
        public Tui.Builder processUnrecognizedCommand(InputProcessor processUnrecognizedCommand) {
            this.processUnrecognizedCommand = Optional.of(processUnrecognizedCommand);
            return this;
        }

        /** @param backgroundColor The background color of the console.
         *  @return This Builder object for method chaining. */
        public Tui.Builder backgroundColor(Color backgroundColor) {
            this.backgroundColor = Optional.of(backgroundColor);
            return this;
        }

        /** @param defaultForegroundColor The default text color used with the Tui classes' print methods
         *              (doesn't change the color of previously printed text).<br>Default is Color.green
         *  @return This Builder object for method chaining. */
        public Tui.Builder foregroundColor(Color defaultForegroundColor) {
            this.defaultForegroundColor = Optional.of(defaultForegroundColor);
            return this;
        }

        /** @param font The font of the console (for equally wide characters, use a monospaced font).
         *  @return This Builder object for method chaining. */
        public Tui.Builder font(Font font) {
            this.font = Optional.of(font);
            return this;
        }

        /** @param prompt The prompt to display before each command.
         *  @return This Builder object for method chaining. */
        public Tui.Builder prompt(String prompt) {
            this.prompt = Optional.of(prompt);
            return this;
        }

        /** @param resetColorAfterEachMsg Whether the text color in the console should be reset to
         *      the defaultForegroundColor after each message.
         *  @return This Builder object for method chaining. */
        public Tui.Builder resetColorAfterEachMsg(boolean resetColorAfterEachMsg) {
            this.resetColorAfterEachMsg = resetColorAfterEachMsg;
            return this;
        }

        /** @param charPrintDelayMS The delay after each character is printed in milliseconds.
         *  @return This Builder object for method chaining. */
        public Tui.Builder charPrintDelayMS(int charPrintDelayMS) {
            this.charPrintDelayMS = Optional.of(charPrintDelayMS);
            return this;
        }

        /**
         * Builds a Tui object.
         * @return The Tui object made using this Builder's settings.
         */
        public Tui build() {
            return new Tui(this);
        }

    }
}