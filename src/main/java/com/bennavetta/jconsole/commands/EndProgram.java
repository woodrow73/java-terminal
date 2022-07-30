package com.bennavetta.jconsole.commands;

import com.bennavetta.jconsole.gui.Console;

public class EndProgram implements InputProcessor {
    @Override
    public void process(String raw, String[] rawSplit, Console console) {
        System.exit(0);
    }
}
