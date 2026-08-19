package com.wms.ui;

import com.wms.model.User;
import com.wms.ui.panels.*;
import com.wms.util.Session;
import com.wms.util.UITheme;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class DashboardFrame extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    private final Map<String, JButton> navButtons = new LinkedHashMap<>();
    private JPanel sidebar;

    public DashboardFrame() {
        User user = Session.getCurrentUser();
        setTitle("Waste Management System - " + user.getFullName() + " (" + user.getRole() + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 720);
        setMinimumSize(new Dimension(980, 600));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(buildSidebar(), BorderLayout.WEST);
        add(buildTopBar(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);

        showCard("Home");
    }

    private JComponent buildSidebar() {
        sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UITheme.PRIMARY_DARK);
        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        JLabel logo = new JLabel("\u267B  WMS");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logo.setForeground(Color.WHITE);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        logo.setBorder(BorderFactory.createEmptyBorder(0, 0, 24, 0));
        sidebar.add(logo);

        String role = Session.getCurrentUser().getRole();

        addNavButton("Home", "\u2302  Dashboard");
        if (role.equals("CITIZEN")) {
            addNavButton("Requests", "\uD83D\uDDD1  My Pickup Requests");
            addNavButton("Complaints", "\u26A0  My Complaints");
        } else {
            addNavButton("Requests", "\uD83D\uDDD1  Waste Requests");
            addNavButton("Complaints", "\u26A0  Complaints");
        }
        if (role.equals("ADMIN")) {
            addNavButton("Users", "\uD83D\uDC65  Manage Users");
        }
        if (role.equals("ADMIN") || role.equals("STAFF")) {
            addNavButton("Reports", "\uD83D\uDCCA  Reports");
        }

        sidebar.add(Box.createVerticalGlue());

        JButton logout = new JButton("\u21B0  Logout");
        logout.setAlignmentX(Component.CENTER_ALIGNMENT);
        logout.setMaximumSize(new Dimension(190, 40));
        logout.setForeground(Color.WHITE);
        logout.setBackground(UITheme.PRIMARY_DARK.darker());
        logout.setFocusPainted(false);
        logout.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        logout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logout.addActionListener(e -> logout());
        sidebar.add(logout);
        sidebar.add(Box.createVerticalStrut(10));

        return sidebar;
    }

    private void addNavButton(String key, String label) {
        JButton btn = new JButton(label);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(230, 44));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setForeground(Color.WHITE);
        btn.setBackground(UITheme.PRIMARY_DARK);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 24, 10, 16));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        btn.setFont(UITheme.FONT_BODY);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> showCard(key));
        navButtons.put(key, btn);
        sidebar.add(btn);
    }

    private void showCard(String key) {
        cardLayout.show(contentPanel, key);
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            entry.getValue().setBackground(entry.getKey().equals(key) ? UITheme.PRIMARY : UITheme.PRIMARY_DARK);
        }
    }

    private JComponent buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER));
        bar.setPreferredSize(new Dimension(0, 56));

        User u = Session.getCurrentUser();
        JLabel welcome = new JLabel("  Welcome, " + u.getFullName());
        welcome.setFont(UITheme.FONT_BODY_BOLD);
        welcome.setForeground(UITheme.TEXT_DARK);

        JLabel roleTag = new JLabel(u.getRole() + "  ");
        roleTag.setFont(UITheme.FONT_SMALL);
        roleTag.setForeground(UITheme.PRIMARY);

        bar.add(welcome, BorderLayout.WEST);
        bar.add(roleTag, BorderLayout.EAST);
        return bar;
    }

    private JComponent buildContent() {
        contentPanel.setBackground(UITheme.BG);
        contentPanel.add(new HomePanel(), "Home");
        contentPanel.add(new WasteRequestPanel(), "Requests");
        contentPanel.add(new ComplaintPanel(), "Complaints");

        String role = Session.getCurrentUser().getRole();
        if (role.equals("ADMIN")) {
            contentPanel.add(new UserManagementPanel(), "Users");
        }
        if (role.equals("ADMIN") || role.equals("STAFF")) {
            contentPanel.add(new ReportsPanel(), "Reports");
        }
        return contentPanel;
    }

    private void logout() {
        Session.clear();
        dispose();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
