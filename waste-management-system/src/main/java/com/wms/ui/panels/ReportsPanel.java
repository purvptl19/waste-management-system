package com.wms.ui.panels;

import com.wms.dao.ComplaintDAO;
import com.wms.dao.WasteRequestDAO;
import com.wms.util.RoundedPanel;
import com.wms.util.UITheme;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class ReportsPanel extends JPanel {

    public ReportsPanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG);
        setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        JLabel title = new JLabel("Reports & Analytics");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_DARK);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(1, 2, 20, 20));
        grid.setOpaque(false);

        try {
            Map<String, Integer> requestCounts = new WasteRequestDAO().getCountsByStatus();
            Map<String, Integer> complaintCounts = new ComplaintDAO().getCountsByStatus();
            grid.add(chartCard("Waste Requests by Status", requestCounts, UITheme.PRIMARY));
            grid.add(chartCard("Complaints by Status", complaintCounts, new Color(0x8B5CF6)));
        } catch (Exception e) {
            JLabel err = new JLabel("Failed to load report data: " + e.getMessage());
            err.setForeground(UITheme.DANGER);
            grid.add(err);
        }

        add(grid, BorderLayout.CENTER);
    }

    private JComponent chartCard(String title, Map<String, Integer> data, Color barColor) {
        RoundedPanel card = new RoundedPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel heading = new JLabel(title);
        heading.setFont(UITheme.FONT_HEADING);
        heading.setForeground(UITheme.TEXT_DARK);
        card.add(heading, BorderLayout.NORTH);

        card.add(new BarChart(data, barColor), BorderLayout.CENTER);
        return card;
    }

    /** Minimal self-contained bar chart, no external charting library required. */
    private static class BarChart extends JPanel {
        private final Map<String, Integer> data;
        private final Color barColor;

        BarChart(Map<String, Integer> data, Color barColor) {
            this.data = data == null ? new LinkedHashMap<>() : data;
            this.barColor = barColor;
            setOpaque(false);
            setPreferredSize(new Dimension(400, 280));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (data.isEmpty()) {
                g2.setColor(UITheme.TEXT_MUTED);
                g2.setFont(UITheme.FONT_BODY);
                g2.drawString("No data yet.", 20, 40);
                g2.dispose();
                return;
            }

            int max = data.values().stream().mapToInt(Integer::intValue).max().orElse(1);
            if (max == 0) max = 1;

            int padding = 20;
            int bottom = getHeight() - 40;
            int chartHeight = bottom - padding;
            int n = data.size();
            int gap = 24;
            int barWidth = Math.max(30, (getWidth() - padding * 2 - gap * (n - 1)) / Math.max(n, 1));

            int x = padding;
            g2.setFont(UITheme.FONT_SMALL);
            for (Map.Entry<String, Integer> entry : data.entrySet()) {
                int value = entry.getValue();
                int barHeight = (int) (((double) value / max) * chartHeight);
                int y = bottom - barHeight;

                g2.setColor(barColor);
                g2.fillRoundRect(x, y, barWidth, barHeight, 8, 8);

                g2.setColor(UITheme.TEXT_DARK);
                String valueStr = String.valueOf(value);
                int vw = g2.getFontMetrics().stringWidth(valueStr);
                g2.drawString(valueStr, x + (barWidth - vw) / 2, y - 6);

                g2.setColor(UITheme.TEXT_MUTED);
                String label = entry.getKey();
                int lw = g2.getFontMetrics().stringWidth(label);
                g2.drawString(label, x + (barWidth - lw) / 2, bottom + 16);

                x += barWidth + gap;
            }

            g2.setColor(UITheme.BORDER);
            g2.drawLine(padding, bottom, getWidth() - padding, bottom);

            g2.dispose();
        }
    }
}
