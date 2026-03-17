package com.eyesoft;

import javax.swing.*;
import java.awt.*;
import java.util.prefs.Preferences;

public class Settings {

    private static JFrame frame = null;

    public static void showWindow() {
        if (frame != null && frame.isVisible()) {
            frame.toFront();
            return;
        }

        frame = new JFrame("Settings");

        frame.setType(Window.Type.UTILITY);
        // Hide from Windows
        //Windows Person Fix It Pleaseeeee

        frame.setSize(300, 150);



        // Do Not Kill Background Task
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 20));
        frame.setLocationRelativeTo(null);
        frame.setAlwaysOnTop(true);

        JTabbedPane tabbedPane = new JTabbedPane();
        JPanel timingPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        timingPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel waitLabel = new JLabel("Wait Time (seconds):");
        JTextField waitTimeField = new JTextField(String.valueOf(Main.waitSeconds), 5);

        JLabel breakLabel = new JLabel("Break Time (seconds):");
        JTextField breakTimeField = new JTextField(String.valueOf(Main.breakSeconds), 5);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            try {
                int newWait = Integer.parseInt(waitTimeField.getText().trim());
                int newBreak = Integer.parseInt(breakTimeField.getText().trim());

                Main.waitSeconds = newWait;
                Main.breakSeconds = newBreak;

                Preferences prefs = Preferences.userNodeForPackage(Main.class);
                prefs.putInt("savedWaitTime", newWait);
                prefs.putInt("savedBreakTime", newBreak);
                Main.startT();

                frame.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Please enter valid numbers for both fields.", "Input Error", JOptionPane.ERROR_MESSAGE);
                //Setting Error
            }
        });

        timingPanel.add(waitLabel);
        timingPanel.add(waitTimeField);
        timingPanel.add(breakLabel);
        timingPanel.add(breakTimeField);
        timingPanel.add(new JLabel("")); // Empty cell for alignment
        timingPanel.add(saveButton);

        JPanel aboutPanel = new JPanel(new BorderLayout());

        JLabel aboutLabel = new JLabel("<html><center><b>EyeSoft Blocker</b><br>Take care of your eyes!<br>Version 1.0</center></html>", SwingConstants.CENTER);
        aboutPanel.add(aboutLabel, BorderLayout.CENTER);


        tabbedPane.addTab("Timing", timingPanel);
        tabbedPane.addTab("About", aboutPanel);


        frame.add(tabbedPane);
        frame.setVisible(true);
    }
}