package com.wms.ui.panels;

import com.wms.dao.UserDAO;
import com.wms.model.User;
import com.wms.util.RoundedButton;
import com.wms.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class UserManagementPanel extends JPanel {

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final UserDAO userDAO = new UserDAO();

    public UserManagementPanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG);
        setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        add(buildHeader(), BorderLayout.NORTH);

        String[] cols = {"ID", "Full Name", "Username", "Email", "Phone", "Role"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        table.setFont(UITheme.FONT_BODY);
        table.setRowHeight(30);
        table.getTableHeader().setFont(UITheme.FONT_BODY_BOLD);
        table.setSelectionBackground(UITheme.PRIMARY_LIGHT);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));
        add(scroll, BorderLayout.CENTER);

        add(buildActionBar(), BorderLayout.SOUTH);
        refresh();
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        JLabel title = new JLabel("Manage Users");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_DARK);

        RoundedButton addBtn = new RoundedButton("+ Add User");
        addBtn.addActionListener(e -> openUserDialog(null));

        header.add(title, BorderLayout.WEST);
        header.add(addBtn, BorderLayout.EAST);
        return header;
    }

    private JComponent buildActionBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 14));
        bar.setOpaque(false);

        RoundedButton editBtn = new RoundedButton("Edit Selected", new Color(0x3B82F6), new Color(0x2563EB), Color.WHITE, 12);
        editBtn.addActionListener(e -> editSelected());
        bar.add(editBtn);

        RoundedButton deleteBtn = new RoundedButton("Delete Selected", UITheme.DANGER, UITheme.DANGER.darker(), Color.WHITE, 12);
        deleteBtn.addActionListener(e -> deleteSelected());
        bar.add(deleteBtn);

        RoundedButton refreshBtn = new RoundedButton("Refresh", UITheme.TEXT_MUTED, UITheme.TEXT_DARK, Color.WHITE, 12);
        refreshBtn.addActionListener(e -> refresh());
        bar.add(refreshBtn);

        return bar;
    }

    private void refresh() {
        try {
            tableModel.setRowCount(0);
            for (User u : userDAO.getAll()) {
                tableModel.addRow(new Object[]{
                        u.getUserId(), u.getFullName(), u.getUsername(), u.getEmail(), u.getPhone(), u.getRole()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load users: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a user first.");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        openUserDialog(id);
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a user first.");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this user permanently?\n" +
                "(Related requests/complaints referencing this user may block deletion.)",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            userDAO.delete(id);
            refresh();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Delete failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Opens the Add/Edit dialog. Pass null userId to add a new user. */
    private void openUserDialog(Integer userId) {
        boolean isEdit = userId != null;
        User existing = null;
        if (isEdit) {
            try {
                existing = userDAO.getAll().stream().filter(u -> u.getUserId() == userId).findFirst().orElse(null);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Failed to load user: " + e.getMessage());
                return;
            }
        }

        JTextField nameField = new JTextField(existing != null ? existing.getFullName() : "");
        JTextField usernameField = new JTextField(existing != null ? existing.getUsername() : "");
        usernameField.setEditable(!isEdit); // username immutable after creation
        JPasswordField passwordField = new JPasswordField();
        JTextField emailField = new JTextField(existing != null ? existing.getEmail() : "");
        JTextField phoneField = new JTextField(existing != null ? existing.getPhone() : "");
        JTextField addressField = new JTextField(existing != null ? existing.getAddress() : "");
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{"ADMIN", "STAFF", "CITIZEN"});
        if (existing != null) roleCombo.setSelectedItem(existing.getRole());

        JPanel form = new JPanel(new GridLayout(0, 1, 4, 4));
        form.add(labeled("Full Name", nameField));
        form.add(labeled("Username", usernameField));
        form.add(labeled(isEdit ? "New Password (leave blank to keep current)" : "Password", passwordField));
        form.add(labeled("Email", emailField));
        form.add(labeled("Phone", phoneField));
        form.add(labeled("Address", addressField));
        form.add(labeled("Role", roleCombo));

        int result = JOptionPane.showConfirmDialog(this, form, isEdit ? "Edit User" : "Add User",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        try {
            if (isEdit) {
                User u = existing;
                u.setFullName(nameField.getText().trim());
                u.setEmail(emailField.getText().trim());
                u.setPhone(phoneField.getText().trim());
                u.setAddress(addressField.getText().trim());
                u.setRole((String) roleCombo.getSelectedItem());
                String newPass = new String(passwordField.getPassword());
                boolean changePassword = !newPass.isEmpty();
                if (changePassword) u.setPassword(newPass);
                userDAO.update(u, changePassword);
            } else {
                if (userDAO.usernameExists(usernameField.getText().trim())) {
                    JOptionPane.showMessageDialog(this, "Username already exists.");
                    return;
                }
                User u = new User();
                u.setFullName(nameField.getText().trim());
                u.setUsername(usernameField.getText().trim());
                u.setPassword(new String(passwordField.getPassword()));
                u.setEmail(emailField.getText().trim());
                u.setPhone(phoneField.getText().trim());
                u.setAddress(addressField.getText().trim());
                u.setRole((String) roleCombo.getSelectedItem());
                userDAO.register(u);
            }
            refresh();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Save failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel labeled(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout());
        JLabel l = new JLabel(label);
        l.setFont(UITheme.FONT_SMALL);
        p.add(l, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }
}
