package com.bennavetta.jconsole.commands;

import com.bennavetta.jconsole.gui.Console;

public class Help implements InputProcessor {

    public String commandName = "help";

    @Override
    public void process(String raw, String[] rawSplit, Console console) {
        console.write(String.format("Commands:%n  %-22sEchoes back text in color%n  %-22sClear screen%n  "
                        + "%-22sPrint an ASCII castle%n  %-22sExit program%n  %-22sList commands",
                "echo <text>", "cls", "castle", "exit", "help"));
    }

}
