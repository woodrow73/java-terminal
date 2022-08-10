/**
 * Copyright (C) 2012 Ben Navetta <ben.navetta@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * -----------------------------------------------------------------------
 * Modified by: woodrow73 https://github.com/woodrow73
 */
package com.bennavetta.jconsole.tui.console;

import com.bennavetta.jconsole.completion.CompletionSource;
import com.bennavetta.jconsole.commands.InputProcessor;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class Console extends JScrollPane implements KeyListener, MouseWheelListener, ComponentListener, MouseListener {
	private static final long serialVersionUID = -5260432287332359321L;

    /** Whether ANSI colors should be enabled */
    public boolean enableANSI;

    /** Whether the console's text color should be reset to Console.FOREGROUND after each message */
    @Setter
    public boolean resetColorAfterEachMsg;

    /** Inherits from JTextPane, adds functionality for colored text */
    @Getter
    private ColorPane textPane;

    /** foreground color for the text */
    @Getter
    private Color foreground;
	
	private ConsoleDocument doc;		// Holder of all text on the window

    @Setter
    private String prompt;			    // The prompt to display before each user input
	
	private Font font;
	
    private boolean wasInFocus = true;	// Used to focus the screen when typing/scrolling.
	
	private ArrayList<String> prompts = new ArrayList<String>();                // List of previously run commands
   	private ArrayList<String> DOCUMENT_HARDCOPY = new ArrayList<String>();      // List of all lines since last cls.
    private String currentCommand = "";                                    // The current command being written, constantly being updated
    private int currentPosition = 0;                                       // The line, as referenced in "DOCUMENT_HARDCOPY" that is at the top of the window
    private int currentCommandnum = 0;                                     // The current command number, as referenced in "prompts," that the user is
                                                                           //  accessing, based on arrow keys.
	
	private InputProcessor[] processors = { InputProcessor.NO_OP };		   // Processors of input, as name implies.
	
	private CompletionSource completionSource = new NoOpCompletionSource();
	
	private MutableAttributeSet defaultStyle;
    
    /**
     * Class used internally, no need to understand it.
     *
     * @author Joey Patel
     * @author pateljo@northvilleschools.net (valid until 06/18)
     * @param  lineNumber the line number to be checked
     * @returns boolean value of the comparison -> Is it in focus?
     */
    private boolean isInFocus(int lineNumber) {
        boolean toReturn = wasInFocus;
        wasInFocus = lineNumber < DOCUMENT_HARDCOPY.size();
        return toReturn;
    }
    
    /**
     * Scrolls the document by a specified number of lines, in a direction specified by 
     * positive/negative integer value. 
     *
     * @param  distance the number of lines to scroll
     */
    public void scroll(int distance) {
        getViewport().setViewPosition(new Point(0, getViewport().getViewPosition().y + distance * font.getSize()));
    }

	public CompletionSource getCompletionSource() {
		return completionSource;
	}

	public void setCompletionSource(CompletionSource completionSource) {
		this.completionSource = completionSource;
	}
    
	public InputProcessor[] getProcessors() {
		return processors;
	}

	public void setProcessors(InputProcessor... processors) {
		this.processors = processors;
	}

    /**
     * Initializes the console; a JScrollPane that contains a JTextPane.
     *
     * @param background the background color of the console
     * @param foreground the foreground color of the console
     * @param font the font of the console
     * @param prompt the prompt of the console that precedes the user's input
     * @param enableANSI whether ANSI colors should be enabled
     * @param resetColorAfterEachMsg whether the console's text color should be reset after each message
     */
	public Console(Color background, Color foreground, Font font, String prompt, boolean enableANSI,
                   boolean resetColorAfterEachMsg) {
	    super();
        textPane = new ColorPane(this, foreground);
        this.enableANSI = enableANSI;
        this.resetColorAfterEachMsg = resetColorAfterEachMsg;
        this.foreground = foreground;

        setBorder(BorderFactory.createEmptyBorder());

        setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        setViewportView(textPane);

        doc = new ConsoleDocument(this, textPane);
        doc.setConsole(this);
        textPane.setDocument(doc);
        
        DOCUMENT_HARDCOPY.add("");
        
        textPane.setBackground(background);

        textPane.setCaretColor(foreground);
        textPane.addCaretListener(doc);
        doc.setCaret(textPane.getCaret());

        setConsoleFont(font);
        MutableAttributeSet attrs = textPane.getInputAttributes();

        StyleConstants.setForeground(attrs, foreground);
        textPane.getStyledDocument().setCharacterAttributes(0, doc.getLength() + 1, attrs, false);
        defaultStyle = attrs;

        this.prompt = prompt;
        doc.write(this.prompt, defaultStyle, true);
        
        textPane.addKeyListener(this); //catch tabs, enters, and up/down arrows for autocomplete and input processing
        textPane.addMouseWheelListener(this);
        textPane.addMouseListener(this);
	}

    public void setConsoleFont(Font font) { // if the method name is the same as super.setFont(), it will override the superclass's method causing a NPE.
        this.font = font;
        MutableAttributeSet attrs = textPane.getInputAttributes();
        StyleConstants.setFontFamily(attrs, font.getFamily());
        StyleConstants.setFontSize(attrs, font.getSize());
        StyleConstants.setItalic(attrs, (font.getStyle() & Font.ITALIC) != 0);
        StyleConstants.setBold(attrs, (font.getStyle() & Font.BOLD) != 0);
    }

    public void setConsoleForeground(Color foreground) { // if the method name is the same as super.setForeground(), it will override the superclass's method causing a NPE.
        this.foreground = foreground;
        textPane.setColorCurrent(foreground);
        textPane.setCaretColor(foreground);
        StyleConstants.setForeground(textPane.getInputAttributes(), foreground);
    }
	
    /**
     * "Clears" the terminal window...
     *          ...by replacing it with a new one - this was the easiest way to do it.
     *
     * @author Joey Patel
     * @author pateljo@northvilleschools.net (valid until 06/18)
     */
    public void cls() {
        doc = new ConsoleDocument(this, textPane);
        doc.setConsole(this);
        textPane.setDocument(doc);
        doc.setCaret(textPane.getCaret());
        DOCUMENT_HARDCOPY = new ArrayList<String>();
        DOCUMENT_HARDCOPY.add("");
        currentPosition = 0;
    }

    /** Prints text to the console.
     *
     * @param text to print
     */
	public void print(String text)
	{
		doc.write(text, defaultStyle, true);
	}

    /** Prints text to the console with a newline at the end.
     *
     * @param text to print
     */
    public void println(String text)
    {
        doc.write(text + "\n", defaultStyle, true);
    }

    /** Prints text to the console in color - ignores ANSI.
     *
     * @param text to print
     * @param color what color to make the text
     */
    public void print(String text, Color color)
    {
        doc.write(text, defaultStyle, color, true);
    }

    /** Prints text to the console in color with a newline at the end - ignores ANSI.
     *
     * @param text to print
     * @param color what color to make the text
     */
    public void println(String text, Color color) {
        doc.write(text + "\n", defaultStyle, color, true);
    }

    /** Prints a newline to the console. */
    public void println() { println(""); }
	
	public void remove(int offset, int length) {
		try {
            textPane.getStyledDocument().remove(offset, length);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
	
	public ConsoleDocument getConsoleDocument()
	{
		return this.doc;
	}

	public void keyTyped(KeyEvent e) {
		if(e.getKeyChar() == '\t') {
			//don't append autocomplete tabs to the document
			e.consume();
		}
	}

	public void keyPressed(KeyEvent e) {
		// Is the cursor in a valid position?
        if (!doc.isCursorValid())
            doc.makeCursorValid();
            
        //TAB -> AUTOCOMPLETE
        if(e.getKeyCode() == KeyEvent.VK_TAB) {
            e.consume();
            String input = doc.getUserInput().trim();
            
            List<String> completions = completionSource.complete(input);
            if(completions == null || completions.isEmpty()) {
                //no completions
                Toolkit.getDefaultToolkit().beep();
            }
            else if(completions.size() == 1) { //only one match - print it
                String toInsert = completions.get(0);
                toInsert = toInsert.substring(input.length());
                doc.write(toInsert, defaultStyle, false);
                //don't trigger processing because the user might not agree with the autocomplete
            }
            else {
                StringBuilder help = new StringBuilder();
                help.append('\n');
                for(String str : completions) {
                    help.append(' ');
                    help.append(str);
                }
                help.append("\n" + prompt);
                doc.write(help.toString(), defaultStyle, true);
                doc.write(input, defaultStyle, false);
            }
        }
        
        //UP ARROW -> FILL IN A PREV COMMAND
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            e.consume(); //Don't actually go up a row
            
            //Get current input
            String currentInput = doc.getUserInput().trim();

            //If there's no previous commands, beep and return
            if (currentCommandnum <= 0) {
                currentCommandnum = 0; //It should never be less than zero, but you never know...
                Toolkit.getDefaultToolkit().beep();
                return;
            }
            
            //remove the current input from console and, if it's null, initialize it to an empty string.
            if (currentInput != null && currentInput != "") { //not sure which one it returns, but it doesn't really matter
                this.remove(doc.getLimit(),currentInput.length());
            } else {
                currentInput = "";                            //In case it's null... this may be unnecessary
            }
            
            //If it's something the user just typed, save it for later, just in case.
            if (currentCommandnum >= prompts.size()) {
                currentCommandnum = prompts.size();
                currentCommand = currentInput;      //save the current command, for down arrow use.
            }
            
            //move on to actually processing the command, now that all extraneous cases are taken care of.
            
            //based on previous checks, currentCommandnum should be in the range of 1 to prompts.size() before change.
            //after change, it should be in the range of 0 to (prompts.size() - 1), valid for indexing prompts.
            currentCommandnum--; //update command number. (lower num = older command)
            
            //Index prompts and write the replacement.
            String replacementCommand = prompts.get(currentCommandnum);
            doc.write(replacementCommand, defaultStyle, false);
            
            //Similar to tab, don't trigger processing because the user might not agree with the autocomplete
        }
        
        //DOWN ARROW -> FILL IN A NEWER COMMAND
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            e.consume(); //pretty sure you can't go down, but if you can... don't.
            
            //If you've exhausted the list and replaced the line with the current command, beep and return
            if (currentCommandnum >= prompts.size()) {
                Toolkit.getDefaultToolkit().beep();
                return;
            }
            
            
            currentCommandnum++;
            
            //Now, regardless of where you are in the list of commands, you're going to need to replace text.
            String currentInput = doc.getUserInput().trim();
            if (currentInput != null && currentInput != "")  //not sure which one it returns, but it doesn't really matter
                this.remove(doc.getLimit(),currentInput.length());
            
            
            //If you've exhausted the list but not yet replaced the line with the current command...
            if (currentCommandnum == prompts.size()) {
                doc.write(currentCommand, defaultStyle, false);
                return;
            }
            
            //If, for some reason, the list is not in range (lower bound), make it in range.
            if (currentCommandnum < 0)
                currentCommandnum = 0;
            
            //finally, write in the new command.
            doc.write(prompts.get(currentCommandnum), defaultStyle, false);
        }

        // hacky fix for a bug where the foreground color of user's input changes to the prompt color
        // when backspace is pressed while the caret is immediately after the prompt.
        if(e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            new Thread(() -> {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) { ex.printStackTrace(); }
                SwingUtilities.invokeLater(() -> setConsoleForeground(foreground) );
            }).start();
        }
	}

	public void keyReleased(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_ENTER) {
            DOCUMENT_HARDCOPY.set(DOCUMENT_HARDCOPY.size()-1,prompt + doc.getUserInput());
            if (!DOCUMENT_HARDCOPY.get(DOCUMENT_HARDCOPY.size()-1).endsWith("\n"))
                DOCUMENT_HARDCOPY.set(DOCUMENT_HARDCOPY.size()-1,DOCUMENT_HARDCOPY.get(DOCUMENT_HARDCOPY.size()-1) + "\n");
            DOCUMENT_HARDCOPY.add("");
            String line = doc.getUserInput().trim();
            String[] args = parseLine(line);
            prompts.add(line);
            currentCommandnum = prompts.size();

            for(InputProcessor processor : processors)
                processor.process(this, line, args);

            doc.write(prompt, defaultStyle, true);
        }
	}
	
    public void mouseWheelMoved(MouseWheelEvent e) {
        this.scroll(e.getWheelRotation() * 3);
    }
    
    public void componentHidden(ComponentEvent e) {}
    public void componentShown(ComponentEvent e) {}
    public void componentMoved(ComponentEvent e) {}
    public void componentResized(ComponentEvent evt) {}
    
    public void mouseExited(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {
    }
    public void mouseClicked(MouseEvent e) {}
    
    String mostRecentSelectedText = "";
    
    public void mouseReleased(MouseEvent e) {
        if (textPane.getSelectedText() != null) // See if they selected something
            mostRecentSelectedText = textPane.getSelectedText();
        else
            mostRecentSelectedText = "";
        if (e.isPopupTrigger())
            doPop(e);
    }
    
    private void doPop(MouseEvent e) {
        PopUp menu = new PopUp();
        menu.show(e.getComponent(), e.getX(), e.getY());
    }
    
    private class PopUp extends JPopupMenu {
        JMenuItem copyButton;
        public PopUp() {
            copyButton = new JMenuItem(new AbstractAction("copy") {
                public void actionPerformed(ActionEvent e) {
                    if(!mostRecentSelectedText.equals("")) {
                        StringSelection selection = new StringSelection(mostRecentSelectedText);
                        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                        clipboard.setContents(selection, selection);
                    }
                }
            });
            add(copyButton);
        }
    }

    private static String[] parseLine(String line) {
        List<String> args = new ArrayList<String>();
        StringBuilder current = new StringBuilder();
        char[] chars = line.toCharArray();
        boolean inQuotes = false;
        for (char c :chars) {
            if (c == '"') {
                if (current.length() > 0) {
                    args.add(current.toString());
                    current.setLength(0);
                }
                inQuotes = !inQuotes;
            }
            else if (inQuotes) {
                current.append(c);
            }
            else if (c == ' ') {
                if (current.length() > 0) {
                    args.add(current.toString());
                    current.setLength(0);
                }
            }
            else {
                current.append(c);
            }
        }

        args.add(current.toString().trim());

        return args.toArray(new String[0]);
    }
	
	private static class NoOpCompletionSource implements CompletionSource {
		public List<String> complete(String input)
		{
			return null;
		}
	}

}
