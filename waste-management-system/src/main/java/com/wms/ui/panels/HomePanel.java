package com.wms.ui.panels;

import com.wms.dao.ComplaintDAO;
import com.wms.dao.WasteRequestDAO;
import com.wms.model.User;
import com.wms.util.RoundedPanel;
import com.wms.util.Session;
import com.wms.util.UITheme;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class HomePanel extends JPanel {

    public HomePanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG);
        setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        User user = Session.getCurrentUser();

        JLabel heading = new JLabel("Overview");
        heading.setFont(UITheme.FONT_TITLE);
        heading.setForeground(UITheme.TEXT_DARK);

        JLabel sub = new JLabel("Here's what's happening with waste operations today.");
        sub.setFont(UITheme.FONT_SUBTITLE);
        sub.setForeground(UITheme.TEXT_MUTED);

        JPanel headerBox = new JPanel();
        headerBox.setOpaque(false);
        headerBox.setLayout(new BoxLayout(headerBox, BoxLayout.Y_AXIS));
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerBox.add(heading);
        headerBox.add(Box.createVerticalStrut(4));
        headerBox.add(sub);

        add(headerBox, BorderLayout.NORTH);
        add(buildStatCards(user), BorderLayout.CENTER);
    }

    private JComponent buildStatCards(User user) {
        JPanel grid = new JPanel(new GridLayout(2, 3, 18, 18));
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createEmptyBorder(24, 0, 0, 0));

        try {
            WasteRequestDAO reqDao = new WasteRequestDAO();
            ComplaintDAO compDao = new ComplaintDAO();
            Map<String, Integer> reqCounts = reqDao.getCountsByStatus();
            Map<String, Integer> compCounts = compDao.getCountsByStatus();

            int totalRequests = reqCounts.values().stream().mapToInt(Integer::intValue).sum();
            int pending = reqCounts.getOrDefault("PENDING", 0);
            int scheduled = reqCounts.getOrDefault("SCHEDULED", 0);
            int collected = reqCounts.getOrDefault("COLLECTED", 0);
            int totalComplaints = compCounts.values().stream().mapToInt(Integer::intValue).sum();
            int openComplaints = compCounts.getOrDefault("OPEN", 0);

            grid.add(statCard("Total Pickup Requests", String.valueOf(totalRequests), UITheme.PRIMARY));
            grid.add(statCard("Pending", String.valueOf(pending), UITheme.ACCENT));
            grid.add(statCard("Scheduled", String.valueOf(scheduled), new Color(0x3B82F6)));
            grid.add(statCard("Collected", String.valueOf(collected), new Color(0x22C55E)));
            grid.add(statCard("Total Complaints", String.valueOf(totalComplaints), new Color(0x8B5CF6)));
            grid.add(statCard("Open Complaints", String.valueOf(openComplaints), UITheme.DANGER));

        } catch (Exception e) {
            JLabel err = new JLabel("Could not load statistics: " + e.getMessage());
            err.setForeground(UITheme.DANGER);
            grid.add(err);
        }

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(grid, BorderLayout.NORTH);
        return wrap;
    }

    private JComponent statCard(String label, String value, Color accent) {
        RoundedPanel card = new RoundedPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setForeground(accent);

        JLabel labelLabel = new JLabel(label);
        labelLabel.setFont(UITheme.FONT_BODY);
        labelLabel.setForeground(UITheme.TEXT_MUTED);

        JPanel textBox = new JPanel();
        textBox.setOpaque(false);
        textBox.setLayout(new BoxLayout(textBox, BoxLayout.Y_AXIS));
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        labelLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        textBox.add(valueLabel);
        textBox.add(Box.createVerticalStrut(6));
        textBox.add(labelLabel);

        card.add(textBox, BorderLayout.CENTER);
        return card;
    }
}
