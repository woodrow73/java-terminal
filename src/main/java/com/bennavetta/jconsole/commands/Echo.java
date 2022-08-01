package com.bennavetta.jconsole.commands;

import com.bennavetta.jconsole.util.ColorUtil;
import com.bennavetta.jconsole.gui.Console;

public class Echo implements InputProcessor {
    @Override
    public void process(Console console, String raw, String... args) {
        if (args.length <= 1)
            console.write("Usage: echo <text>");
        else
            console.write(raw.substring(raw.indexOf(' ') + 1), ColorUtil.getContrastingColor(console.getBackground()));
    }
}
