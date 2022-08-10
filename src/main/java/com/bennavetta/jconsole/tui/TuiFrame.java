package com.bennavetta.jconsole.tui;

import com.bennavetta.jconsole.commands.InputProcessor;

import java.awt.*;
import java.util.*;

/**
 * Uses a mutable Builder Design Pattern with the inner class Builder.<br><br>
 * <p>
 * Constructs a T-UI (text user interface) that handles both input and output.
 * </p>
 * @author woodrow73
 */
public class TuiFrame extends TuiParent {

    private TuiFrame(TuiFrame.Builder builder) {
        super(builder);
    }

    /** Uses a mutable Builder Design Pattern, creating a TuiFrame object when the build() method is called */
    public static class Builder {

        protected Optional<Map<String, InputProcessor>> commandMap = Optional.empty();

        protected Optional<InputProcessor> processUnrecognizedCommand = Optional.empty(),
                processor = Optional.empty();

        protected Optional<Color> backgroundColor = Optional.empty(),
                defaultForegroundColor = Optional.empty();

        protected Optional<Font> font = Optional.empty();

        protected Optional<String> prompt = Optional.empty();

        protected Optional<Integer> charPrintDelayMS = Optional.empty();

        protected Optional<Dimension> frameSize = Optional.empty();

        protected Optional<Point> frameLocation = Optional.empty();

        protected boolean resetColorAfterEachMsg;

        protected final String frameTitle;

        /**
         * Starts building a T-UI (text user interface) using the Builder Design Pattern.
         * @param frameTitle The title of the window
         * @param resetColorAfterEachMsg Whether the text color in the console should be reset to
         *                               the defaultForegroundColor after each message.
         */
        public Builder(String frameTitle, boolean resetColorAfterEachMsg) {
            this.frameTitle = frameTitle;
            this.resetColorAfterEachMsg = resetColorAfterEachMsg;
        }

        /** @param processor Processes all user input.
         *  @return This Builder object for method chaining. */
        public TuiFrame.Builder processor(InputProcessor processor) {
            this.processor = Optional.of(processor);
            return this;
        }

        /** @param commandMap A map to store commands and triggers.
         *  @return This Builder object for method chaining. */
        public TuiFrame.Builder commandMap(Map<String, InputProcessor> commandMap) {
            this.commandMap = Optional.of(commandMap);
            return this;
        }

        /** @param processUnrecognizedCommand How to process unrecognized commands.
         *  @return This Builder object for method chaining. */
        public TuiFrame.Builder processUnrecognizedCommand(InputProcessor processUnrecognizedCommand) {
            this.processUnrecognizedCommand = Optional.of(processUnrecognizedCommand);
            return this;
        }

        /** @param backgroundColor The background color of the console.
         *  @return This Builder object for method chaining. */
        public TuiFrame.Builder backgroundColor(Color backgroundColor) {
            this.backgroundColor = Optional.of(backgroundColor);
            return this;
        }

        /** @param defaultForegroundColor The default text color used with the Tui classes' print methods
         *              (doesn't change the color of previously printed text).<br>Default is Color.green
         *  @return This Builder object for method chaining. */
        public TuiFrame.Builder foregroundColor(Color defaultForegroundColor) {
            this.defaultForegroundColor = Optional.of(defaultForegroundColor);
            return this;
        }

        /** @param font The font of the console (for equally wide characters, use a monospaced font).
         *  @return This Builder object for method chaining. */
        public TuiFrame.Builder font(Font font) {
            this.font = Optional.of(font);
            return this;
        }

        /** @param prompt The prompt to display before each command.
         *  @return This Builder object for method chaining. */
        public TuiFrame.Builder prompt(String prompt) {
            this.prompt = Optional.of(prompt);
            return this;
        }

        /** @param charPrintDelayMS The delay after each character is printed in milliseconds.
         *  @return This Builder object for method chaining. */
        public TuiFrame.Builder charPrintDelayMS(int charPrintDelayMS) {
            this.charPrintDelayMS = Optional.of(charPrintDelayMS);
            return this;
        }

        /** @param resetColorAfterEachMsg Whether the text color in the console should be reset to
         *      the defaultForegroundColor after each message.
         *  @return This TuiBuilder object for method chaining. */
        public TuiFrame.Builder resetColorAfterEachMsg(boolean resetColorAfterEachMsg) {
            this.resetColorAfterEachMsg = resetColorAfterEachMsg;
            return this;
        }

        /**
         * @param width The width of the frame.
         * @param height The height of the frame.
         * @return This Builder object for method chaining. */
        public TuiFrame.Builder size(int width, int height) {
            this.frameSize = Optional.of(new Dimension(width, height));
            return this;
        }

        /**
         * Set the location of the frame relative to the top-left corner of the screen.
         * @param x The x coordinate of the frame relative to the top-left corner of the screen.
         * @param y The y coordinate of the frame relative to the top-left corner of the screen.
         * @return This Builder object for method chaining. */
        public TuiFrame.Builder location(int x, int y) {
            this.frameLocation = Optional.of(new Point(x, y));
            return this;
        }

        /**
         * Builds a TuiFrame object.
         * @return The TuiFrame object made using this Builder's settings.
         */
        public TuiFrame build() {
            return new TuiFrame(this);
        }

    }
}