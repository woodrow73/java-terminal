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
 * 
 * Welcome to the DEMO! Want to know how to get started with these classes 
 * quickly and easily? You're in the right place! A comprehensive guide below
 * covers a lot of helpful tips when making a console-based application.
 * It could still use some improvement, though, and of course each application
 * will need to be custom built to the application's specialized needs.
 */
package com.bennavetta.jconsole;

import com.formdev.flatlaf.FlatDarculaLaf;

import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.*;

public class DemoConsole
{

	private static final String CONSOLE_NAME = "Console Demo";  // JFrame's Title

    private static final int FONT_SIZE = 14;
    private static Dimension consoleSize = new Dimension(670, 435);
    
    private static final String ICON_IMAGE_FILE = "/icon.png";  // Resource path to the console's icon
    
    private static final Color BACKGROUND_COLOR = Color.BLACK;      // The background color
    private static final Color FOREGROUND_COLOR = Color.GREEN;      // The text color
	
	private static final Map<String, InputProcessor> commandMap = new HashMap<>(10); // A hashmap to store commands and triggers.


	public static void main(String[] args)
	{
        // Apply a dark theme to the UI using flatlaf
        customizeLaf();
        
    //STEP 1: Initialize and define all commands, in the form of InputProcessors.
        
        InputProcessor clearScreen = new InputProcessor() {
            public void process(String raw, String[] args, Console console) {
                console.cls();
            }
        };
        
        InputProcessor terminateProgram = new InputProcessor() {
            public void process(String raw, String[] args, Console console) {
                System.exit(0);
            }
        };
        
        InputProcessor echo = new InputProcessor() {
            public void process(String raw, String[] args, Console console) {
                if (args.length <= 1)
                    console.write("Usage: echo <text>");
                else {
                    console.write(raw.substring(raw.indexOf(' ') + 1));
                }
            }
        };

        InputProcessor help = new InputProcessor() {
            public void process(String raw, String[] args, Console console) {
                console.write("Commands:  help | echo <text> | cls | close | exit");
            }
        };

        InputProcessor IDontUnderstand = new InputProcessor() {
            public void process(String raw, String[] args, Console console) {
                console.write("Command not recognized. Use \"help\" to list commands.");
            }
        };
        
    // STEP 2: Link all of these command codes to a one-word String command:
        
        commandMap.put("cls",clearScreen);          //String command does not need to match variable name from above
        
        commandMap.put("close",terminateProgram);
        commandMap.put("exit",terminateProgram);    //Multiple strings can be used for the same command, but multiple 
                                                    //commands may not be referenced by the same string.
        
        commandMap.put("echo",echo);                //String command COULD be the same as the variable name, if you want.
        
        commandMap.put("help",help);
    // STEP 3: Initialize the JFrame:
        
		JFrame frame = new JFrame(CONSOLE_NAME);
        try {frame.setIconImage(new ImageIcon(ImageIO.read(DemoConsole.class.getResource(ICON_IMAGE_FILE))).getImage());}
        catch (Exception e) { e.printStackTrace(); }
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Init console
		Console console = new Console(BACKGROUND_COLOR, FOREGROUND_COLOR,
				new Font(Font.MONOSPACED, Font.BOLD, FONT_SIZE), "Demo Console> ");

        // String commands go here as well.
		console.setCompletionSource(new DefaultCompletionSource("help", "echo", "cls", "close", "exit"));
		
        console.setProcessor(new InputProcessor() { // This processor breaks a statement into args and passes them to the matching
                                                    // command defined in the hashmap above (the part in step 2)
			private int requests = 0;
            
            public void process(String raw, String[] args, Console console)
            {
                //1. Print for debugging:
                System.out.println("Got Req. " + ++requests + ": '" + args[0] + "'");
                
                System.out.println("asked: " + Arrays.toString(args));
                //4. Process list of arguments
                if (args.length > 0 && commandMap.containsKey(args[0].toLowerCase()))
                    commandMap.get(args[0].toLowerCase()).process(raw, args, console);
                else
                    IDontUnderstand.process(raw, args, console);
            }
		});

		frame.add(console);
        frame.addComponentListener(console);
        frame.setSize(consoleSize);
        console.setScreenHeight((int) frame.getContentPane().getSize().getHeight());
        frame.setLocation(new Point(30, 30));
		frame.setVisible(true);
	}

    private static void customizeLaf() {
        try {
            UIManager.setLookAndFeel(new FlatDarculaLaf() {
                //removing the content of this method because it was responsible for producing an annoying beep sound
                @Override
                public void provideErrorFeedback(Component component) {}
            });
        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("Could not set up Look and Feel");
            e.printStackTrace();
        }

        UIManager.put("ScrollBar.showButtons", true);
        UIManager.put("ScrollBar.width", 12);
        UIManager.put("defaultFont", new Font(Font.MONOSPACED, Font.PLAIN, FONT_SIZE));
    }

}
