package com.wms.ui.panels;

import com.wms.dao.ComplaintDAO;
import com.wms.model.Complaint;
import com.wms.model.User;
import com.wms.util.RoundedButton;
import com.wms.util.Session;
import com.wms.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ComplaintPanel extends JPanel {

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final ComplaintDAO complaintDAO = new ComplaintDAO();
    private final boolean isCitizen = "CITIZEN".equals(Session.getCurrentUser().getRole());

    public ComplaintPanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG);
        setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        add(buildHeader(), BorderLayout.NORTH);

        String[] cols = {"ID", "Citizen", "Subject", "Description", "Status", "Created", "Resolved"};
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

        JLabel title = new JLabel(isCitizen ? "My Complaints" : "Citizen Complaints");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_DARK);

        if (isCitizen) {
            RoundedButton newBtn = new RoundedButton("+ File Complaint");
            newBtn.addActionListener(e -> openNewComplaintDialog());
            header.add(newBtn, BorderLayout.EAST);
        }

        header.add(title, BorderLayout.WEST);
        return header;
    }

    private JComponent buildActionBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 14));
        bar.setOpaque(false);

        if (!isCitizen) {
            RoundedButton updateBtn = new RoundedButton("Update Status", new Color(0x3B82F6), new Color(0x2563EB), Color.WHITE, 12);
            updateBtn.addActionListener(e -> updateStatus());
            bar.add(updateBtn);

            RoundedButton deleteBtn = new RoundedButton("Delete", UITheme.DANGER, UITheme.DANGER.darker(), Color.WHITE, 12);
            deleteBtn.addActionListener(e -> deleteSelected());
            bar.add(deleteBtn);
        }

        RoundedButton refreshBtn = new RoundedButton("Refresh", UITheme.TEXT_MUTED, UITheme.TEXT_DARK, Color.WHITE, 12);
        refreshBtn.addActionListener(e -> refresh());
        bar.add(refreshBtn);

        return bar;
    }

    private void refresh() {
        try {
            User current = Session.getCurrentUser();
            List<Complaint> complaints = isCitizen
                    ? complaintDAO.getByUser(current.getUserId())
                    : complaintDAO.getAll();

            tableModel.setRowCount(0);
            for (Complaint c : complaints) {
                tableModel.addRow(new Object[]{
                        c.getComplaintId(), c.getCitizenName(), c.getSubject(), c.getDescription(),
                        c.getStatus(),
                        c.getCreatedAt() == null ? "" : c.getCreatedAt().toString().substring(0, 16),
                        c.getResolvedAt() == null ? "-" : c.getResolvedAt().toString().substring(0, 16)
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load complaints: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openNewComplaintDialog() {
        JTextField subjectField = new JTextField();
        JTextArea descArea = new JTextArea(4, 20);

        JPanel form = new JPanel(new GridLayout(0, 1, 4, 4));
        form.add(labeled("Subject", subjectField));
        form.add(labeled("Description", new JScrollPane(descArea)));

        int result = JOptionPane.showConfirmDialog(this, form, "File a Complaint",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        if (subjectField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Subject is required.");
            return;
        }

        try {
            Complaint c = new Complaint();
            c.setUserId(Session.getCurrentUser().getUserId());
            c.setSubject(subjectField.getText().trim());
            c.setDescription(descArea.getText().trim());
            complaintDAO.create(c);
            refresh();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to submit complaint: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateStatus() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a complaint first.");
            return;
        }
        int complaintId = (int) tableModel.getValueAt(row, 0);
        String[] statuses = {"OPEN", "IN_PROGRESS", "RESOLVED"};
        String selected = (String) JOptionPane.showInputDialog(this, "New status:", "Update Complaint #" + complaintId,
                JOptionPane.PLAIN_MESSAGE, null, statuses, tableModel.getValueAt(row, 4));
        if (selected == null) return;
        try {
            complaintDAO.updateStatus(complaintId, selected);
            refresh();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Update failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a complaint first.");
            return;
        }
        int complaintId = (int) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete complaint #" + complaintId + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            complaintDAO.delete(complaintId);
            refresh();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Delete failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
