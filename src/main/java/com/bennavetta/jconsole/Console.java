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
 */
package com.bennavetta.jconsole;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class Console extends JScrollPane implements KeyListener, MouseWheelListener, ComponentListener, MouseListener
{	
	private static final long serialVersionUID = -5260432287332359321L;

    /** Whether ANSI colors should be enabled */
    public boolean enableANSI;

    /** Whether the console's text color should be reset to Console.FOREGROUND after each message */
    public boolean resetColorAfterEachMsg;

    /** Inherits from JTextPane, adds functionality for colored text */
    public ColorPane textPane;

    /** initial foreground color for the text */
    public final Color FOREGROUND;
	
	private ConsoleDocument doc;		// Holder of all text on the window

    private String prompt;			    // Structured as such: [connection][subprompt][prompt]
    private String subPrompt = "";		// Subprompt: one character at the end of the prompt
    public String connection = "Home>";	// Computer name, IP, or Domain name (for use with networking)
    private String path = "C:\\";		// Current Directory
	
	private final Font f;			// Keeps track of the current font for line counting.
	
    private boolean wasInFocus = true;	// Used to focus the screen when typing/scrolling.
	
	private ArrayList<String> prompts = new ArrayList<String>();                // List of previously run commands
   	private ArrayList<String> DOCUMENT_HARDCOPY = new ArrayList<String>();      // List of all lines since last cls.
    private String currentCommand = "";                                    // The current command being written, constantly being updated
    private int currentPosition = 0;                                       // The line, as referenced in "DOCUMENT_HARDCOPY" that is at the top of the window
    private int currentCommandnum = 0;                                     // The current command number, as referenced in "prompts," that the user is
                                                                           //  accessing, based on arrow keys.
	
	private InputProcessor processor = new NoOpInputProcessor();		   // Processor of input, as name implies.
	
	private CompletionSource completionSource = new NoOpCompletionSource();
	
	private MutableAttributeSet defaultStyle;



    /**
     * Sets the maximum number of lines that can fit on a window of the specified height.
     * Note that this is the height of the text area, not the entire window. (do not send
     * the raw window height, subtract the borders.)
     *
     * @author Joey Patel
     * @author pateljo@northvilleschools.net (valid until 06/18)
     * @param  heightInPixels   The height of the window, in pixels
     */
    public void setScreenHeight(int heightInPixels) { //in pixels
        FontMetrics fm = textPane.getFontMetrics(f);
        int height = fm.getMaxDescent(); //haha, getMaxDecent works too, but was quickly depricated!
        height += fm.getMaxAscent();
    }
    
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
        getViewport().setViewPosition(new Point(0, getViewport().getViewPosition().y + distance * f.getSize()));
    }

	public CompletionSource getCompletionSource() {
		return completionSource;
	}

	public void setCompletionSource(CompletionSource completionSource) {
		this.completionSource = completionSource;
	}

	
    /**
     * Sets the console's prompt, overwriting the connection, path, and sub-prompt.
     *
     * @param  prompt to become the new prompt
     */
    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    /**
     * Sets the one-character subprompt of the console's prompting system, then updates the
     * entire prompt.
     *
     * @author Joey Patel
     * @author pateljo@northvilleschools.net (valid until 06/18)
     * @param  subPrompt to become the new sub-prompt
     */
    public void setSubPrompt(String subPrompt) {
        this.subPrompt = subPrompt;
        this.prompt = this.connection + this.path + this.subPrompt;
    }
    
    /**
     * Sets the directory filepath of the console's prompting system, then updates the
     * entire prompt.
     *
     * @author Joey Patel
     * @author pateljo@northvilleschools.net (valid until 06/18)
     * @param  path to become the new path
     */
    public void setPath(String path) {
        this.path = path;
        this.prompt = this.connection + this.path + this.subPrompt;
    }
    
    /**
     * Returns the current path.
     *
     * @author Joey Patel
     * @author pateljo@northvilleschools.net (valid until 06/18)
     * @returns the console's current filepath.
     */
    public String getPath() {
        return path;
    }
    
	public InputProcessor getProcessor() {
		return processor;
	}

	public void setProcessor(InputProcessor processor) {
		this.processor = processor;
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
                   boolean resetColorAfterEachMsg)
	{
	    super();
        textPane = new ColorPane(this, foreground);
        this.enableANSI = enableANSI;
        this.resetColorAfterEachMsg = resetColorAfterEachMsg;
        this.FOREGROUND = foreground;

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
        
        f = font;
        MutableAttributeSet attrs = textPane.getInputAttributes();
        StyleConstants.setFontFamily(attrs, font.getFamily());
        StyleConstants.setFontSize(attrs, font.getSize());
        StyleConstants.setItalic(attrs, (font.getStyle() & Font.ITALIC) != 0);
        StyleConstants.setBold(attrs, (font.getStyle() & Font.BOLD) != 0);
        StyleConstants.setForeground(attrs, foreground);
        textPane.getStyledDocument().setCharacterAttributes(0, doc.getLength() + 1, attrs, false);
        defaultStyle = attrs;
        
        
        //this.prompt = this.connection + this.path + prompt;
        this.prompt = prompt;
        doc.write(this.prompt, defaultStyle, true);
        
        textPane.addKeyListener(this); //catch tabs, enters, and up/down arrows for autocomplete and input processing
        textPane.addMouseWheelListener(this);
        textPane.addMouseListener(this);
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

    /** Writes text to the console with an added newline.
     *  Use write(String text, boolean newline) to write without a newline.
     *
     * @param text to write
     */
	public void write(String text)
	{
		doc.write(text + "\n", defaultStyle, true);
	}

    /** Writes text to the console.
     *
     * @param text to write
     * @param newline whether to add a newline
     */
    public void write(String text, boolean newline)
    {
        doc.write(text + (newline ? "\n" : ""), defaultStyle, true);
    }

    /** Writes text to the console in color with an added newline; ignores ANSI.
     *  Use write(String text, Color color, boolean newline) to write without a newline.
     *
     * @param text to write
     * @param color what color to make the text
     */
    public void write(String text, Color color)
    {
        doc.write(text + "\n", defaultStyle, color, true);
    }

    /** Writes text to the console ignoring ANSI.
     *
     * @param text to write
     * @param color what color to make the text
     * @param newline whether to add a newline
     */
    public void write(String text, Color color, boolean newline)
    {
        doc.write(text + (newline ? "\n" : ""), defaultStyle, color, true);
    }

    /** Writes text with each word given a color of the rainbow, with each character being a slightly different shade.
     *
     * @param text to write
     * @param newline whether to add a newline
     */
    public void writeRainbowWords(String text, boolean newline)
    {
        for(int i = 0, wordCount = 0; i < text.length(); i++)
        {
            if(text.charAt(i) == ' ') {
                int nextNonSpace = i + Util.indexOfRegex("\\S", text.substring(i));
                if(nextNonSpace == -1) {
                    doc.write(text.substring(i), defaultStyle, true);
                    break;
                }
                else {
                    doc.write(text.substring(i, nextNonSpace), defaultStyle, true);
                    wordCount += i == 0 ? 0 : 1;
                    i = nextNonSpace - 1;
                    continue;
                }
            }
            else {
                doc.write(text.substring(i, i + 1), defaultStyle,
                        ColorUtil.getCloseColor(ColorUtil.rainbow[wordCount % ColorUtil.rainbow.length], .21), true);
            }
        }

        if(newline)
            doc.write("\n", defaultStyle, true);
    }

    /** Writes text with each character given a color of the rainbow
     *
     * @param text to write
     * @param newline whether to add a newline
     */
    public void writeRainbowCharacters(String text, boolean newline)
    {
        for(int i = 0, nonSpaceCount = 0; i < text.length(); i++) {
            if(text.charAt(i) != ' ') {
                doc.write(text.substring(i, i + 1), defaultStyle, ColorUtil.getCloseColor(
                        ColorUtil.almostRainbow[nonSpaceCount % ColorUtil.almostRainbow.length], .21), true);
                nonSpaceCount++;
            }
            else {
                doc.write(text.substring(i, i + 1), defaultStyle, true);
            }
        }

        if(newline)
            doc.write("\n", defaultStyle, true);
    }
	
	public void remove(int offset, int length)
	{
		try
		{
            textPane.getStyledDocument().remove(offset, length);
		}
		catch (BadLocationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ConsoleDocument getConsoleDocument()
	{
		return this.doc;
	}

	public void keyTyped(KeyEvent e)
	{
		if(e.getKeyChar() == '\t')
		{
			//don't append autocomplete tabs to the document
			e.consume();
		}
	}

	public void keyPressed(KeyEvent e)
	{
		// Is the cursor in a valid position?
        if (!doc.isCursorValid())
            doc.makeCursorValid();
            
        //TAB -> AUTOCOMPLETE
        if(e.getKeyCode() == KeyEvent.VK_TAB)
        {
            e.consume();
            String input = doc.getUserInput().trim();
            
            List<String> completions = completionSource.complete(input);
            if(completions == null || completions.isEmpty())
            {
                //no completions
                Toolkit.getDefaultToolkit().beep();
            }
            else if(completions.size() == 1) //only one match - print it
            {
                String toInsert = completions.get(0);
                toInsert = toInsert.substring(input.length());
                doc.write(toInsert, defaultStyle, false);
                //don't trigger processing because the user might not agree with the autocomplete
            }
            else
            {
                StringBuilder help = new StringBuilder();
                help.append('\n');
                for(String str : completions)
                {
                    help.append(' ');
                    help.append(str);
                }
                help.append("\n" + prompt);
                doc.write(help.toString(), defaultStyle, true);
                doc.write(input, defaultStyle, false);
            }
        }
        
        //UP ARROW -> FILL IN A PREV COMMAND
        if (e.getKeyCode() == KeyEvent.VK_UP)
        {
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
            if (currentCommandnum < 0) {
                currentCommandnum = 0;
            }
            
            //finally, write in the new command.
            doc.write(prompts.get(currentCommandnum), defaultStyle, false);
        }
	}

	public void keyReleased(KeyEvent e)
	{
        if(e.getKeyCode() == KeyEvent.VK_ENTER)
        {
            DOCUMENT_HARDCOPY.set(DOCUMENT_HARDCOPY.size()-1,prompt + doc.getUserInput());
            if (!DOCUMENT_HARDCOPY.get(DOCUMENT_HARDCOPY.size()-1).endsWith("\n"))
                DOCUMENT_HARDCOPY.set(DOCUMENT_HARDCOPY.size()-1,DOCUMENT_HARDCOPY.get(DOCUMENT_HARDCOPY.size()-1) + "\n");
            DOCUMENT_HARDCOPY.add("");
            String line = doc.getUserInput().trim();
            String[] args = parseLine(line);
            prompts.add(line);
            currentCommandnum = prompts.size();
            processor.process(line, args, this);
            doc.write(prompt, defaultStyle, true);


        }
	}
	
    public void mouseWheelMoved(MouseWheelEvent e) {
        this.scroll(e.getWheelRotation() * 3);
    }
    
    public void componentHidden(ComponentEvent e) {}
    public void componentShown(ComponentEvent e) {}
    public void componentMoved(ComponentEvent e) {}
    public void componentResized(ComponentEvent evt) {
        this.setScreenHeight((int)(((JFrame)evt.getSource()).getContentPane().getSize().getHeight()));
    }
    
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
    
    private void doPop(MouseEvent e){
        PopUp menu = new PopUp();
        menu.show(e.getComponent(), e.getX(), e.getY());
    }
    
    private class PopUp extends JPopupMenu {
        JMenuItem copyButton;
        public PopUp(){
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

    private static String[] parseLine(String line)
    {
        List<String> args = new ArrayList<String>();
        StringBuilder current = new StringBuilder();
        char[] chars = line.toCharArray();
        boolean inQuotes = false;
        for (char c :chars)
        {
            if (c == '"')
            {
                if (current.length() > 0)
                {
                    args.add(current.toString());
                    current.setLength(0);
                }
                inQuotes = !inQuotes;
            }
            else if (inQuotes)
            {
                current.append(c);
            }
            else if (c == ' ')
            {
                if (current.length() > 0)
                {
                    args.add(current.toString());
                    current.setLength(0);
                }
            }
            else
            {
                current.append(c);
            }
        }

        args.add(current.toString().trim());

        return args.toArray(new String[0]);
    }
    
	private static class NoOpInputProcessor implements InputProcessor
	{
		public void process(String raw, String[] text, Console console) {}
	}
	
	private static class NoOpCompletionSource implements CompletionSource
	{
		public List<String> complete(String input)
		{
			return null;
		}
	}
}
