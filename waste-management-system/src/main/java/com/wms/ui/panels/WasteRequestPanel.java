package com.wms.ui.panels;

import com.wms.dao.UserDAO;
import com.wms.dao.WasteRequestDAO;
import com.wms.model.User;
import com.wms.model.WasteRequest;
import com.wms.util.RoundedButton;
import com.wms.util.Session;
import com.wms.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

public class WasteRequestPanel extends JPanel {

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final WasteRequestDAO requestDAO = new WasteRequestDAO();
    private final UserDAO userDAO = new UserDAO();
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private final boolean isCitizen = "CITIZEN".equals(Session.getCurrentUser().getRole());

    public WasteRequestPanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG);
        setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        add(buildHeader(), BorderLayout.NORTH);

        String[] cols = {"ID", "Citizen", "Waste Type", "Quantity", "Address", "Pickup Date", "Status", "Assigned Staff", "Remarks"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        table.setFont(UITheme.FONT_BODY);
        table.setRowHeight(30);
        table.getTableHeader().setFont(UITheme.FONT_BODY_BOLD);
        table.setSelectionBackground(UITheme.PRIMARY_LIGHT);
        table.setSelectionForeground(UITheme.TEXT_DARK);

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

        JLabel title = new JLabel(isCitizen ? "My Pickup Requests" : "Waste Pickup Requests");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_DARK);

        RoundedButton newBtn = new RoundedButton("+ New Request");
        newBtn.addActionListener(e -> openNewRequestDialog());

        header.add(title, BorderLayout.WEST);
        header.add(newBtn, BorderLayout.EAST);
        return header;
    }

    private JComponent buildActionBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 14));
        bar.setOpaque(false);

        if (!isCitizen) {
            RoundedButton updateBtn = new RoundedButton("Update Status / Assign", new Color(0x3B82F6), new Color(0x2563EB), Color.WHITE, 12);
            updateBtn.addActionListener(e -> openUpdateDialog());
            bar.add(updateBtn);
        } else {
            RoundedButton cancelBtn = new RoundedButton("Cancel Selected Request", UITheme.DANGER, UITheme.DANGER.darker(), Color.WHITE, 12);
            cancelBtn.addActionListener(e -> cancelSelected());
            bar.add(cancelBtn);
        }

        if (!isCitizen) {
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
            List<WasteRequest> requests;
            switch (current.getRole()) {
                case "CITIZEN": requests = requestDAO.getByUser(current.getUserId()); break;
                case "STAFF": requests = requestDAO.getAll(); break; // staff can view all, update assigned ones
                default: requests = requestDAO.getAll();
            }
            tableModel.setRowCount(0);
            for (WasteRequest r : requests) {
                tableModel.addRow(new Object[]{
                        r.getRequestId(), r.getCitizenName(), r.getWasteType(), r.getQuantity(),
                        r.getAddress(), r.getPickupDate() == null ? "" : sdf.format(r.getPickupDate()),
                        r.getStatus(), r.getAssignedStaffName() == null ? "-" : r.getAssignedStaffName(),
                        r.getRemarks() == null ? "" : r.getRemarks()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load requests: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openNewRequestDialog() {
        JTextField quantityField = new JTextField();
        JTextField addressField = new JTextField(Session.getCurrentUser().getAddress());
        JTextField dateField = new JTextField(sdf.format(new java.util.Date()));
        JTextArea remarksArea = new JTextArea(3, 20);
        String[] wasteTypes = {"Organic", "Recyclable", "Electronic (E-Waste)", "Hazardous", "Bulk Item", "General"};
        JComboBox<String> typeCombo = new JComboBox<>(wasteTypes);

        JPanel form = new JPanel(new GridLayout(0, 1, 4, 4));
        form.add(labeled("Waste Type", typeCombo));
        form.add(labeled("Quantity (e.g. 3 bags, 50kg)", quantityField));
        form.add(labeled("Pickup Address", addressField));
        form.add(labeled("Preferred Pickup Date (yyyy-MM-dd)", dateField));
        form.add(labeled("Remarks (optional)", new JScrollPane(remarksArea)));

        int result = JOptionPane.showConfirmDialog(this, form, "New Pickup Request",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        try {
            WasteRequest r = new WasteRequest();
            r.setUserId(Session.getCurrentUser().getUserId());
            r.setWasteType((String) typeCombo.getSelectedItem());
            r.setQuantity(quantityField.getText().trim());
            r.setAddress(addressField.getText().trim());
            r.setRemarks(remarksArea.getText().trim());
            try {
                r.setPickupDate(new Date(sdf.parse(dateField.getText().trim()).getTime()));
            } catch (Exception parseEx) {
                r.setPickupDate(new Date(System.currentTimeMillis()));
            }
            requestDAO.create(r);
            refresh();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to create request: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openUpdateDialog() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a request first.");
            return;
        }
        int requestId = (int) tableModel.getValueAt(row, 0);
        String currentStatus = (String) tableModel.getValueAt(row, 6);

        String[] statuses = {"PENDING", "SCHEDULED", "COLLECTED", "CANCELLED"};
        JComboBox<String> statusCombo = new JComboBox<>(statuses);
        statusCombo.setSelectedItem(currentStatus);

        JComboBox<String> staffCombo = new JComboBox<>();
        staffCombo.addItem("-- Unassigned --");
        java.util.List<User> staffList;
        try {
            staffList = userDAO.getByRole("STAFF");
        } catch (Exception e) {
            staffList = java.util.Collections.emptyList();
        }
        for (User s : staffList) staffCombo.addItem(s.getFullName() + "#" + s.getUserId());

        JTextArea remarksArea = new JTextArea((String) tableModel.getValueAt(row, 8), 3, 20);

        JPanel form = new JPanel(new GridLayout(0, 1, 4, 4));
        form.add(labeled("Status", statusCombo));
        form.add(labeled("Assign Staff", staffCombo));
        form.add(labeled("Remarks", new JScrollPane(remarksArea)));

        int result = JOptionPane.showConfirmDialog(this, form, "Update Request #" + requestId,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        try {
            Integer staffId = null;
            String sel = (String) staffCombo.getSelectedItem();
            if (sel != null && sel.contains("#")) {
                staffId = Integer.parseInt(sel.substring(sel.lastIndexOf('#') + 1));
            }
            requestDAO.updateStatusAndAssignment(requestId, (String) statusCombo.getSelectedItem(),
                    staffId, remarksArea.getText().trim());
            refresh();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Update failed: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cancelSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a request first.");
            return;
        }
        int requestId = (int) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Cancel this pickup request?", "Confirm",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            requestDAO.updateStatusAndAssignment(requestId, "CANCELLED", null, "Cancelled by citizen");
            refresh();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a request first.");
            return;
        }
        int requestId = (int) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete request #" + requestId + " permanently?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            requestDAO.delete(requestId);
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
