package com.bennavetta.jconsole.gui;

import com.formdev.flatlaf.FlatDarculaLaf;
import org.apache.commons.lang3.SystemUtils;

import javax.swing.*;
import java.awt.*;

public class FlatLafWrapper {

    /**
     * Uses the flatlaf library to customize the look and feel of the console to FlatDarculaLaf.
     * Uses a default scrollBarWidth of 12
     */
    public static void customizeLaf() {
        customizeLaf(12);
    }

    /**
     * Uses the flatlaf library to customize the look and feel of the console to FlatDarculaLaf.
     * Uses a default scrollBarWidth of 12
     * @param defaultFont The default font to use throughout the UI.
     */
    public static void customizeLaf(Font defaultFont) { customizeLaf(12, defaultFont); }

    /**
     * Uses the flatlaf library to customize the look and feel of the console to FlatDarculaLaf.
     * @param scrollBarWidth The width of the scroll bar.
     */
    public static void customizeLaf(int scrollBarWidth) {
        // if the OS is Mac, allow the title bar to be dark
        if(SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_MAC_OSX)
            System.setProperty("apple.awt.application.appearance", "system");

        try {
            UIManager.setLookAndFeel(new FlatDarculaLaf());
        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("Could not set up Look and Feel");
            e.printStackTrace();
        }

        UIManager.put("ScrollBar.showButtons", true);
        UIManager.put("ScrollBar.width", scrollBarWidth);
    }

    /**
     * Uses the flatlaf library to customize the look and feel of the console to FlatDarculaLaf.
     * @param scrollBarWidth The width of the scroll bar.
     * @param defaultFont The default font to use throughout the UI.
     */
    public static void customizeLaf(int scrollBarWidth, Font defaultFont) {
        // if the OS is Mac, allow the title bar to be dark
        if(SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_MAC_OSX)
            System.setProperty("apple.awt.application.appearance", "system");

        try {
            UIManager.setLookAndFeel(new FlatDarculaLaf());
        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("Could not set up Look and Feel");
            e.printStackTrace();
        }

        UIManager.put("ScrollBar.showButtons", true);
        UIManager.put("ScrollBar.width", scrollBarWidth);
        UIManager.put("defaultFont", defaultFont);
    }
}
