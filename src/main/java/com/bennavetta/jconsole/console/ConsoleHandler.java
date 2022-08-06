package com.bennavetta.jconsole.console;

import com.bennavetta.jconsole.completion.DefaultCompletionSource;
import com.bennavetta.jconsole.commands.InputProcessor;
import com.bennavetta.jconsole.console.gui.Console;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Handles creation and handling of Console objects.<br>
 * One instance per console; There is only one ConsoleHandler per JFrame, but there can be multiple Tui objects per ConsoleHandler/JFrame<br>
 * Package-private
 * @author woodrow73
 */
class ConsoleHandler {

    /** All instances; a one-to-one relationship of ConsoleHandler instances to JFrame instances (unique instances passed into Tui) */
    public static Map<JFrame, ConsoleHandler> instances = new LinkedHashMap<>();

    /** A queue of background color transitions to make */
    private ExecutorService backgroundTransitions = Executors.newSingleThreadExecutor();

    /** Whether any tasks are currently being executed inside interruptingBackgroundTransitions */
    private AtomicBoolean runningBackgroundTransitions = new AtomicBoolean(false);

    /** The number of queued interruptingBackgroundTransitions tasks - when this hits 0,
     * runningInterruptingBackgroundTransitions will be set to false */
    private AtomicInteger backgroundTransitionTaskCounter = new AtomicInteger(0);

    /** The settings currently applied to this console */
    @Getter
    public Tui currentSettings;

    /** Unique identifier for this instance of ConsoleHandler - the index inside ConsoleHandler.instances */
    @Getter
    private final int uniqueID;

    /** The console being handled by this ConsoleHandler - a JScrollPane that contains a JTextPane */
    @Getter
    private Console console;

    /** Whether the console's background color has been set at least once in this ConsoleHandler */
    private boolean backgroundHasBeenSet = false;

    /**
     * Creates a Console object and adds it to the Tui's JFrame.<br>
     * There is only one ConsoleHandler per JFrame, but there can be multiple Tui objects per ConsoleHandler/JFrame
     *
     * @throws IllegalStateException if a ConsoleHandler instance already exists for the given JFrame
     */
    public ConsoleHandler(Tui consoleSettings) throws IllegalStateException {
        JFrame frame = consoleSettings.getFrame();
        if(instances.containsKey(frame))
            throw new IllegalStateException("ConsoleHandler already exists for this JFrame");

        instances.put(frame, this);
        uniqueID = instances.size() - 1;
        Tui.allUserInputLogs.put(uniqueID, new CopyOnWriteArrayList<>());

        console = new Console(consoleSettings.getBackgroundColor(), consoleSettings.getDefaultForegroundColor(),
                consoleSettings.getFont(), consoleSettings.getPrompt(), true,
                consoleSettings.getResetColorAfterEachMsg());

        frame.add(console);
        frame.addComponentListener(console);

        setConsoleSettings(consoleSettings, true);
    }

    /**
     * Apply settings to the console.
     * @param consoleSettings The settings to apply.
     * @param setBackground Whether to set the background color of the console.
     */
    public void setConsoleSettings(Tui consoleSettings, boolean setBackground) {
        this.currentSettings = consoleSettings;
        Map<String, InputProcessor> commandMap = consoleSettings.getCommandMap();

        // in case a background transition was interrupted by a priority background transition, get the original color from the component itself
        Color originalColor = console.getTextPane().getBackground(),
                newBackground = consoleSettings.getBackgroundColor();

        // String commands go here as well.
        console.setCompletionSource(new DefaultCompletionSource(commandMap.keySet().stream().sorted().toArray(String[]::new)));

        console.setProcessors(consoleSettings.getProcessor(),
                // handle commands
                (Console console, String raw, String... args) -> {
                    // Log the user's input
                    Tui.allUserInputLogs.get(uniqueID).add(raw);
                    consoleSettings.getProcessor().process(console, raw, args);

                    // process commands and their arguments
                    if (args.length > 0 && commandMap.containsKey(args[0].toLowerCase()))
                        commandMap.get(args[0].toLowerCase()).process(console, raw, args);
                    else
                        consoleSettings.getProcessUnrecognizedCommand().process(console, raw, args);
                });

        console.setConsoleForeground(consoleSettings.getDefaultForegroundColor());
        console.setPrompt(consoleSettings.getPrompt());
        console.setConsoleFont(consoleSettings.getFont());
        console.setResetColorAfterEachMsg(consoleSettings.getResetColorAfterEachMsg());

        if(!backgroundHasBeenSet && setBackground) { // if the background has not been set yet, set it without transition
            console.getTextPane().setBackground(consoleSettings.getBackgroundColor());
            backgroundHasBeenSet = true;
        }
        else if(consoleSettings.backgroundColorQueue.length > 0) {
            setBackgroundTransitions(consoleSettings.backgroundColorQueueDuration, consoleSettings.backgroundColorQueue);
            if(consoleSettings.deleteBackgroundColorQueueAfterUse)
                consoleSettings.backgroundColorQueue = new Color[]{};
        }
        else if(originalColor.getRGB() != newBackground.getRGB() && setBackground) {
            setBackgroundTransitions(Tui.getBackgroundColorTransitionMS(), newBackground);
        }
    }

    /**
     * Add a background color transition to the queue of tasks inside interruptingBackgroundTransitions, the single thread executor.
     * This method will immediately interrupt the backgroundTransitions queue
     * @param newColor The new background color.
     */
    protected void addBackgroundTransitionToQueue(Color newColor, int durationMS) {
        runningBackgroundTransitions.set(true);
        backgroundTransitionTaskCounter.incrementAndGet();

        backgroundTransitions.submit(() -> {
            Color originalColor = console.getTextPane().getBackground();

            long start = System.nanoTime();
            BigInteger end = new BigInteger(String.valueOf(durationMS))
                    .multiply(new BigInteger("1000000"))
                    .add(new BigInteger(String.valueOf(start)));

                    //end = start + (durationMS * 1000000);

            // capped at 1 max; calculated from how close System.nanoTime() is to the 'end', relative to the 'start'
            double transitionCompletionPercent = 0;

            while(transitionCompletionPercent != 1) {
                try {
                    Thread.sleep(13);
                } catch(InterruptedException e) {
                    return; // if backgroundTransitions.shutdownNow() is called
                }

                transitionCompletionPercent = Math.min(1, (System.nanoTime() - start) / Double.parseDouble(
                            end.subtract(new BigInteger(String.valueOf(start))).toString()));
                Color shadeBetweenColors = new Color(
                        (int)((newColor.getRed() - originalColor.getRed()) * transitionCompletionPercent + originalColor.getRed()),
                        (int)((newColor.getGreen() - originalColor.getGreen()) * transitionCompletionPercent + originalColor.getGreen()),
                        (int)((newColor.getBlue() - originalColor.getBlue()) * transitionCompletionPercent + originalColor.getBlue()));

                try {
                    SwingUtilities.invokeAndWait(() -> console.getTextPane().setBackground(shadeBetweenColors) );
                } catch(InterruptedException e) {
                    return; // if backgroundTransitions.shutdownNow() is called
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            runningBackgroundTransitions.set(0 == backgroundTransitionTaskCounter.decrementAndGet());
        });
    }

    /**
     * Removes and stops all tasks inside backgroundTransitions, then submits new background transition tasks to the queue.
     * @param colors The colors to cycle through.
     * @param durationMS The duration of each transition in milliseconds.
     */
    protected void setBackgroundTransitions(int durationMS, Color... colors) {
        backgroundTransitions.shutdownNow();
        backgroundTransitions = Executors.newSingleThreadExecutor();
        for(Color color : colors)
            addBackgroundTransitionToQueue(color, durationMS);
    }

}