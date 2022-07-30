package com.bennavetta.jconsole.commands;

import com.bennavetta.jconsole.util.ColorUtil;
import com.bennavetta.jconsole.gui.Console;

public class Castle implements InputProcessor {
    @Override
    public void process(String raw, String[] rawSplit, Console console) {
        console.write(ColorUtil.getCastle(), true);
    }
}
