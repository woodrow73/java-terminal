package com.bennavetta.jconsole.commands;

import com.bennavetta.jconsole.console.gui.Console;

public class EndProgram implements InputProcessor {
    @Override
    public void process(Console console, String raw, String... args) {
        System.exit(0);
    }
}
