package com.bennavetta.jconsole.gui;

import com.formdev.flatlaf.FlatDarculaLaf;

import javax.swing.*;
import java.awt.*;

public class FlatLafWrapper {

    /**
     * Uses the flatlaf library to customize the look and feel of the console to FlatDarculaLaf.
     */
    public static void customizeLaf() {
        customizeLaf(12);
    }

    /**
     * Uses the flatlaf library to customize the look and feel of the console to FlatDarculaLaf.
     * @param scrollBarWidth The width of the scroll bar.
     */
    public static void customizeLaf(int scrollBarWidth) {
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
        UIManager.put("ScrollBar.width", scrollBarWidth);
        //UIManager.put("defaultFont", new Font(Font.MONOSPACED, Font.PLAIN, 14));
    }
}
