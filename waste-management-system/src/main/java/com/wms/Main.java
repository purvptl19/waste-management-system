package com.wms;

import com.wms.ui.LoginFrame;
import com.wms.util.UITheme;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        UITheme.initLookAndFeel();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
