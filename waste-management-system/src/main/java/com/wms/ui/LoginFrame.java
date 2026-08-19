package com.wms.ui;

import com.wms.dao.UserDAO;
import com.wms.db.DBConnection;
import com.wms.model.User;
import com.wms.util.RoundedButton;
import com.wms.util.RoundedPanel;
import com.wms.util.Session;
import com.wms.util.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginFrame extends JFrame {

    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JLabel messageLabel = new JLabel(" ");

    public LoginFrame()
    {
        setTitle("Waste Management System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(920, 560);
        setMinimumSize(new Dimension(760, 480));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.BG);

        add(buildBrandPanel(), BorderLayout.WEST);
        add(buildFormPanel(), BorderLayout.CENTER);
    }

    /** Left branding panel with a solid green background. */
    private JComponent buildBrandPanel() {
        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0, 0, UITheme.PRIMARY, 0, getHeight(), UITheme.PRIMARY_DARK);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        panel.setPreferredSize(new Dimension(360, 0));

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));

        JLabel icon = new JLabel("♻"); // recycle symbol
        icon.setFont(new Font("Segoe UI", Font.PLAIN, 64));
        icon.setForeground(Color.WHITE);
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Waste Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("System");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        subtitle.setForeground(new Color(255, 255, 255, 200));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel tagline = new JLabel("<html><center>Smarter collection.<br>Cleaner cities.</center></html>");
        tagline.setFont(UITheme.FONT_BODY);
        tagline.setForeground(new Color(255, 255, 255, 170));
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);
        tagline.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        inner.add(icon);
        inner.add(Box.createVerticalStrut(12));
        inner.add(title);
        inner.add(subtitle);
        inner.add(tagline);

        panel.add(inner);
        return panel;
    }

    private JComponent buildFormPanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(UITheme.BG);

        RoundedPanel card = new RoundedPanel();
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(380, 400));
        card.setBorder(BorderFactory.createEmptyBorder(36, 36, 36, 36));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);

        JLabel heading = new JLabel("Welcome back");
        heading.setFont(UITheme.FONT_TITLE);
        heading.setForeground(UITheme.TEXT_DARK);

        JLabel sub = new JLabel("Sign in to manage waste operations");
        sub.setFont(UITheme.FONT_SUBTITLE);
        sub.setForeground(UITheme.TEXT_MUTED);

        gbc.gridy = 0; card.add(heading, gbc);
        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 20, 0); card.add(sub, gbc);

        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.gridy = 2; card.add(fieldLabel("Username"), gbc);
        gbc.gridy = 3; card.add(styledField(usernameField), gbc);

        gbc.gridy = 4; gbc.insets = new Insets(14, 0, 6, 0); card.add(fieldLabel("Password"), gbc);
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.gridy = 5; card.add(styledField(passwordField), gbc);

        messageLabel.setForeground(UITheme.DANGER);
        messageLabel.setFont(UITheme.FONT_SMALL);
        gbc.gridy = 6; gbc.insets = new Insets(10, 0, 0, 0); card.add(messageLabel, gbc);

        RoundedButton loginBtn = new RoundedButton("Sign In");
        loginBtn.addActionListener(this::onLogin);
        gbc.gridy = 7; gbc.insets = new Insets(14, 0, 8, 0); card.add(loginBtn, gbc);

        JButton registerLink = new JButton("<html><u>Create a citizen account</u></html>");
        registerLink.setBorderPainted(false);
        registerLink.setContentAreaFilled(false);
        registerLink.setForeground(UITheme.PRIMARY);
        registerLink.setFont(UITheme.FONT_SMALL);
        registerLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        registerLink.addActionListener(e -> new RegisterDialog(this).setVisible(true));
        gbc.gridy = 8; gbc.insets = new Insets(4, 0, 0, 0); card.add(registerLink, gbc);

        JLabel hint = new JLabel("<html><center>Demo login &mdash; admin / admin123</center></html>");
        hint.setFont(UITheme.FONT_SMALL);
        hint.setForeground(UITheme.TEXT_MUTED);
        gbc.gridy = 9; gbc.insets = new Insets(16, 0, 0, 0); card.add(hint, gbc);

        // pressing Enter in password field submits
        passwordField.addActionListener(this::onLogin);

        wrapper.add(card);
        return wrapper;
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_SMALL);
        l.setForeground(UITheme.TEXT_MUTED);
        return l;
    }

    private JComponent styledField(JTextField field) {
        field.setFont(UITheme.FONT_BODY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        return field;
    }

    private void onLogin(ActionEvent e) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter username and password.");
            return;
        }

        try {
            if (!DBConnection.testConnection()) {
                messageLabel.setText("Cannot reach the database. Check db.properties.");
                return;
            }
            User user = new UserDAO().authenticate(username, password);
            if (user == null) {
                messageLabel.setText("Invalid username or password.");
                return;
            }
            Session.setCurrentUser(user);
            dispose();
            SwingUtilities.invokeLater(() -> new DashboardFrame().setVisible(true));
        } catch (Exception ex) {
            messageLabel.setText("Login failed: " + ex.getMessage());
        }
    }
}
