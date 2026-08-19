package com.wms.util;

import javax.swing.*;
import java.awt.*;

/** A flat, rounded-corner button with hover/press shading, used for a modern look. */
public class RoundedButton extends JButton {

    private final Color baseColor;
    private final Color hoverColor;
    private final Color textColor;
    private final int arc;

    public RoundedButton(String text) {
        this(text, UITheme.PRIMARY, UITheme.PRIMARY_DARK, Color.WHITE, 12);
    }

    public RoundedButton(String text, Color base, Color hover, Color textColor, int arc) {
        super(text);
        this.baseColor = base;
        this.hoverColor = hover;
        this.textColor = textColor;
        this.arc = arc;
        setFont(UITheme.FONT_BODY_BOLD);
        setForeground(textColor);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 22));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color fill = getModel().isRollover() || getModel().isPressed() ? hoverColor : baseColor;
        if (!isEnabled()) fill = fill.darker().darker();
        g2.setColor(fill);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
        g2.dispose();
        super.paintComponent(g);
    }
}
