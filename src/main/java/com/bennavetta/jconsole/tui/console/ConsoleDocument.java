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

import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import javax.swing.text.*;
import java.awt.*;

public class ConsoleDocument extends DefaultStyledDocument implements CaretListener {

	private Caret caret;
	
	private static final long serialVersionUID = -1270788544217141905L;

	private Console console;
	private ColorPane textPane;

	private int limit;
	
	public void setConsole(Console console) {
        this.console = console;
    }

	public ConsoleDocument(Console console, ColorPane textPane) {
		this.console = console;
		this.textPane = textPane;
	}
	
    public void write(String text, MutableAttributeSet attrs, boolean updateLimit) {
        try {
			if(console.enableANSI) {
				textPane.appendANSI(text, attrs);
				if(console.resetColorAfterEachMsg) {
					StyleConstants.setForeground(attrs, console.getForeground());
				}
			}
			else {
				insertString(getLength(), text, attrs);
			}

			if(updateLimit) {
				limit = getLength();
				caret.setDot(limit);
			}
			else {
				caret.setDot(getLength());
			}
        }
        catch(BadLocationException e) {
            e.printStackTrace();
        }
    }

	public void write(String text, MutableAttributeSet attrs, Color color, boolean updateLimit) {
		try {
			textPane.setColorCurrent(color);
			StyleConstants.setForeground(attrs, color);

			if(text.contains("\u001B")) {
				String nonAnsiText = text.substring(0, text.indexOf("\u001B"));
				insertString(getLength(), nonAnsiText, attrs);

				if(console.enableANSI) {
					textPane.appendANSI(text, attrs);
				}
				else {
					insertString(getLength(), text.substring(nonAnsiText.length()), attrs);
					textPane.setCaretColor(color);
				}
			}
			else {
				insertString(getLength(), text, attrs);
				textPane.setCaretColor(color);
			}

			if(updateLimit) {
				limit = getLength();
				caret.setDot(limit);
			}
			else {
				caret.setDot(getLength());
			}

			if(console.resetColorAfterEachMsg) {
				textPane.setColorCurrent(console.getForeground());
				textPane.setCaretColor(console.getForeground());
				StyleConstants.setForeground(attrs, console.getForeground());
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getUserInput() {
		try {
			return getText(limit, getLength() - limit);
		}
		catch (BadLocationException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public void remove(int offs, int len) throws BadLocationException {
		if(offs < limit) {
			return;
		}
		super.remove(offs, len);
	}

	public void setCaret(Caret caret)
	{
		this.caret = caret;
	}
	
	public int getLimit() {
        return limit;
    }
    
    public boolean isCursorValid() {
        return caret.getDot() >= limit;
    }
    
    public void makeCursorValid() {
        if(caret.getDot() < limit)
             caret.setDot(limit);
    }
    
    public void caretUpdate(CaretEvent e) {} // Moved to "MakeCursorValid" so that the user can still copy text
	
}
