package com.bennavetta.jconsole.commands;

import com.bennavetta.jconsole.console.gui.Console;

public class ClearScreen implements InputProcessor {
    @Override
    public void process(Console console, String raw, String... args) {
        console.cls();
    }
}
