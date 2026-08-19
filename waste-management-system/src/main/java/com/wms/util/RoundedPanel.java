package com.wms.util;

import javax.swing.*;
import java.awt.*;

/** A JPanel with rounded corners and a subtle border, used as a "card" container. */
public class RoundedPanel extends JPanel {

    private final int arc;
    private final Color bg;

    public RoundedPanel() { this(16, UITheme.CARD_BG); }

    public RoundedPanel(int arc, Color bg) {
        this.arc = arc;
        this.bg = bg;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(bg);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
        g2.setColor(UITheme.BORDER);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
        g2.dispose();
        super.paintComponent(g);
    }
}
