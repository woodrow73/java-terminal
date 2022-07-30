package com.bennavetta.jconsole.commands;

import com.bennavetta.jconsole.gui.Console;

public class ClearScreen implements InputProcessor {
    @Override
    public void process(String raw, String[] rawSplit, Console console) {
        console.cls();
    }
}
