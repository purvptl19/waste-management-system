package com.wms.util;

import javax.swing.*;
import java.awt.*;

/** Centralised colour palette / fonts, and look-and-feel bootstrap. */
public final class UITheme {

    // Eco-friendly green/teal palette
    public static final Color PRIMARY        = new Color(0x1F8A70);
    public static final Color PRIMARY_DARK   = new Color(0x146356);
    public static final Color PRIMARY_LIGHT  = new Color(0xE8F6F1);
    public static final Color ACCENT         = new Color(0xFFB100);
    public static final Color DANGER         = new Color(0xE5484D);
    public static final Color BG             = new Color(0xF4F7F6);
    public static final Color CARD_BG        = Color.WHITE;
    public static final Color TEXT_DARK      = new Color(0x1F2937);
    public static final Color TEXT_MUTED     = new Color(0x6B7280);
    public static final Color BORDER         = new Color(0xE2E8E6);

    public static final Font FONT_TITLE    = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_HEADING  = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_BODY     = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_BODY_BOLD= new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_SMALL    = new Font("Segoe UI", Font.PLAIN, 12);

    private UITheme() { }

    /** Tries to install FlatLaf (if the dependency is on the classpath); falls back to Nimbus/System. */
    public static void initLookAndFeel() {
        try {
            Class<?> flatLightLaf = Class.forName("com.formdev.flatlaf.FlatLightLaf");
            UIManager.setLookAndFeel((LookAndFeel) flatLightLaf.getDeclaredConstructor().newInstance());
        } catch (Exception ex) {
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception ignored) {
                // fall back to default cross-platform L&F
            }
        }

        UIManager.put("control", BG);
        UIManager.put("Table.showGrid", true);
        UIManager.put("Table.gridColor", BORDER);
        UIManager.put("Table.rowHeight", 30);
    }
}
