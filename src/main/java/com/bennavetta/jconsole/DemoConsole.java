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

import com.bennavetta.jconsole.commands.*;
import com.bennavetta.jconsole.gui.Console;
import com.bennavetta.jconsole.gui.ConsoleJFrame;
import com.bennavetta.jconsole.gui.FlatLafWrapper;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class DemoConsole
{

    public static void main(String[] args)
    {
        // Set the look and feel to FlatDarculaLaf
        FlatLafWrapper.customizeLaf();

        // A map to store commands and triggers. Multiple strings can be used for the same command, but not vice versa.
        Map<String, InputProcessor> commands = new HashMap<>();
        commands.put("castle", new Castle());
        commands.put("cls", new ClearScreen());
        commands.put("echo", new Echo());
        commands.put("help", new Help());
        commands.put("exit", new EndProgram());
        commands.put("close", new EndProgram());

        // How to process unrecognized commands.
        InputProcessor processUnrecognizedCommand = new InputProcessor() {
            @Override
            public void process(String raw, String[] rawSplit, Console console) {
                console.write("Unrecognized command. Enter 'help' for a list of commands.");
            }
        };

        ConsoleJFrame frame = new ConsoleJFrame(commands, processUnrecognizedCommand, Color.black, Color.green,
                new Font(Font.MONOSPACED, Font.BOLD, 14), "Demo Console> ", true, true, true);

        frame.setTitle("Demo Console");
        frame.setVisible(true);
    }

}