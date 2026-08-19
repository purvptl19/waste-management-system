package com.wms.ui;

import com.wms.dao.UserDAO;
import com.wms.model.User;
import com.wms.util.RoundedButton;
import com.wms.util.UITheme;

import javax.swing.*;
import java.awt.*;

public class RegisterDialog extends JDialog {

    private final JTextField nameField = new JTextField();
    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JTextField emailField = new JTextField();
    private final JTextField phoneField = new JTextField();
    private final JTextField addressField = new JTextField();
    private final JLabel messageLabel = new JLabel(" ");

    public RegisterDialog(Frame owner) {
        super(owner, "Create Citizen Account", true);
        setSize(420, 480);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(UITheme.CARD_BG);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        panel.setBackground(UITheme.CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);

        int row = 0;
        row = addField(panel, gbc, row, "Full name", nameField);
        row = addField(panel, gbc, row, "Username", usernameField);
        row = addField(panel, gbc, row, "Password", passwordField);
        row = addField(panel, gbc, row, "Email", emailField);
        row = addField(panel, gbc, row, "Phone", phoneField);
        row = addField(panel, gbc, row, "Address", addressField);

        messageLabel.setForeground(UITheme.DANGER);
        messageLabel.setFont(UITheme.FONT_SMALL);
        gbc.gridy = row++; panel.add(messageLabel, gbc);

        RoundedButton createBtn = new RoundedButton("Create Account");
        createBtn.addActionListener(e -> onRegister());
        gbc.gridy = row; gbc.insets = new Insets(14, 0, 0, 0);
        panel.add(createBtn, gbc);

        setContentPane(panel);
    }

    private int addField(JPanel panel, GridBagConstraints gbc, int row, String label, JTextField field) {
        JLabel l = new JLabel(label);
        l.setFont(UITheme.FONT_SMALL);
        l.setForeground(UITheme.TEXT_MUTED);
        field.setFont(UITheme.FONT_BODY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        gbc.gridy = row++; panel.add(l, gbc);
        gbc.gridy = row++; panel.add(field, gbc);
        return row;
    }

    private void onRegister() {
        String name = nameField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (name.isEmpty() || username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Name, username and password are required.");
            return;
        }

        try {
            UserDAO dao = new UserDAO();
            if (dao.usernameExists(username)) {
                messageLabel.setText("That username is already taken.");
                return;
            }
            User u = new User();
            u.setFullName(name);
            u.setUsername(username);
            u.setPassword(password);
            u.setEmail(emailField.getText().trim());
            u.setPhone(phoneField.getText().trim());
            u.setAddress(addressField.getText().trim());
            u.setRole("CITIZEN");
            dao.register(u);

            JOptionPane.showMessageDialog(this, "Account created! You can now sign in.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception ex) {
            messageLabel.setText("Registration failed: " + ex.getMessage());
        }
    }
}
