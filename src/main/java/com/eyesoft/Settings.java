package com.eyesoft;

import javax.swing.*;
import java.awt.*;
import java.util.prefs.Preferences;

// The preferences window. Opens from the tray and lets the user adjust break settings,
// toggle startup behaviour, and view app info. Uses a tabbed layout with a dark theme.
public class Settings {

    // Colors that make up the dark theme
    private static final Color BG         = new Color(43, 43, 43);
    private static final Color BG_TOOLBAR = new Color(55, 55, 55);
    private static final Color BORDER_CLR = new Color(70, 70, 70);
    private static final Color TEXT       = new Color(220, 220, 220);
    private static final Color TEXT_MUTED = new Color(155, 155, 155);
    private static final Color ORANGE     = new Color(220, 130, 50);

    private static JFrame frame = null;

    // Open the window, or bring it to the front if it's already open
    public static void showWindow() {
        EyeLogger.info("Settings", "Opening preferences window");
        if (frame != null && frame.isVisible()) {
            frame.toFront();
            return;
        }

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception e) {
                EyeLogger.warn("Settings", "Could not apply cross-platform look and feel: " + e.getMessage());
            }

            try {
                frame = new JFrame("EyeSoft Preferences");
                frame.setType(Window.Type.UTILITY);
                frame.setSize(560, 620);
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.setLocationRelativeTo(null);
                frame.setAlwaysOnTop(true);
                frame.setResizable(false);
                frame.getContentPane().setBackground(BG);
                frame.setLayout(new BorderLayout());

                // Build the tab bar at the top and the panels that go inside each tab
                JPanel toolbar       = buildToolbar();
                JPanel cardContainer = new JPanel(new CardLayout());
                cardContainer.setBackground(BG);

                JPanel mainPanel     = buildMainSettingsPanel();
                JPanel schedulePanel = buildSchedulePanel();
                JPanel aboutPanel    = buildAboutPanel();
                cardContainer.add(mainPanel,     "Settings");
                cardContainer.add(schedulePanel, "Schedule");
                cardContainer.add(aboutPanel,    "About");

                CardLayout cards = (CardLayout) cardContainer.getLayout();

                // Hook up each tab button so clicking it shows the right panel
                for (Component c : toolbar.getComponents()) {
                    if (c instanceof JButton btn) {
                        btn.addActionListener(e -> {
                            try {
                                cards.show(cardContainer, btn.getActionCommand());
                                for (Component tb : toolbar.getComponents()) {
                                    if (tb instanceof JButton b) b.setBackground(BG_TOOLBAR);
                                }
                                btn.setBackground(BG.darker());
                            } catch (Exception ex) {
                                EyeLogger.error("Settings", "Couldn't switch to tab: " + btn.getActionCommand(), ex);
                            }
                        });
                    }
                }

                // Highlight the Settings tab as the default on open
                for (Component c : toolbar.getComponents()) {
                    if (c instanceof JButton b && "Settings".equals(b.getActionCommand())) {
                        b.setBackground(BG.darker());
                        break;
                    }
                }
                cards.show(cardContainer, "Settings");

                frame.add(toolbar, BorderLayout.NORTH);
                frame.add(cardContainer, BorderLayout.CENTER);
                frame.setVisible(true);
                EyeLogger.info("Settings", "Preferences window opened successfully");
            } catch (Exception e) {
                EyeLogger.error("Settings", "Something went wrong while building the preferences window", e);
            }
        });
    }

    // Creates the row of tabs at the top of the window
    private static JPanel buildToolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bar.setBackground(BG_TOOLBAR);
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_CLR));
        bar.setPreferredSize(new Dimension(560, 72));
        bar.add(makeTabBtn("⚙", "Settings"));
        bar.add(makeTabBtn("⏰", "Schedule"));
        bar.add(makeTabBtn("ℹ", "About"));
        return bar;
    }

    // Creates a single tab button with an icon above the label
    private static JButton makeTabBtn(String icon, String label) {
        JButton b = new JButton("<html><center><font size='5'>" + icon + "</font><br>"
                + "<font size='2'>" + label + "</font></center></html>");
        b.setActionCommand(label);
        b.setPreferredSize(new Dimension(90, 72));
        b.setForeground(TEXT);
        b.setBackground(BG_TOOLBAR);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // The main Settings tab — lets the user enable run-at-login and reset all defaults
    private static JPanel buildMainSettingsPanel() {
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBackground(BG);
        root.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        root.add(buildSectionHeader("Startup"));
        root.add(Box.createVerticalStrut(6));
        root.add(styledLabel("Control whether EyeSoft launches automatically when you log in.", TEXT_MUTED, 12));
        root.add(Box.createVerticalStrut(14));

        boolean startupEnabled = StartupManager.isEnabled();
        JCheckBox startupCB = makeCheckBox("Run EyeSoft when Mac starts", startupEnabled);
        startupCB.addActionListener(e -> {
            try {
                boolean enable = startupCB.isSelected();
                EyeLogger.info("Settings", "Run at startup toggled: " + enable);
                StartupManager.setEnabled(enable);
            } catch (Exception ex) {
                EyeLogger.error("Settings", "Couldn't change startup preference", ex);
                // Snap the checkbox back if something went wrong
                startupCB.setSelected(!startupCB.isSelected());
            }
        });
        root.add(startupCB);
        root.add(Box.createVerticalStrut(28));

        root.add(divider());
        root.add(Box.createVerticalStrut(20));

        root.add(buildSectionHeader("Restore Defaults"));
        root.add(Box.createVerticalStrut(6));
        root.add(styledLabel("Reset all settings to their original values.", TEXT_MUTED, 12));
        root.add(Box.createVerticalStrut(14));

        JButton defaultBtn = makeActionButton("Restore all defaults");
        defaultBtn.addActionListener(e -> {
            try {
                EyeLogger.info("Settings", "Restoring all defaults from Settings tab");
                Main.breakSeconds     = 20;
                Main.waitSeconds      = 600;
                Main.showNotification = true;
                saveInt("savedBreakTime",       20);
                saveInt("savedWaitTime",        600);
                saveBoolean("showNotification", true);
                Main.startT();
                frame.dispose();
                showWindow();
                EyeLogger.info("Settings", "All defaults restored and window refreshed");
            } catch (Exception ex) {
                EyeLogger.error("Settings", "Something went wrong while restoring defaults", ex);
            }
        });
        root.add(defaultBtn);

        return root;
    }

    // The Schedule tab — lets the user set how long breaks are and how often they happen
    private static JPanel buildSchedulePanel() {
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBackground(BG);
        root.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        root.add(buildSectionHeader("Short Break"));
        root.add(Box.createVerticalStrut(6));
        root.add(styledLabel("Short breaks help rest your eyes and reduce screen fatigue.", TEXT_MUTED, 12));
        root.add(Box.createVerticalStrut(14));

        // How many seconds the break screen stays up
        JSlider breakSlider = makeSlider(5, 300, Main.breakSeconds, 1, s -> {
            Main.breakSeconds = s;
            saveInt("savedBreakTime", s);
        });
        root.add(sliderRow("Break for:", breakSlider, v -> v + " seconds"));
        root.add(Box.createVerticalStrut(10));

        // Whether to show a heads-up notification before the break kicks in
        JCheckBox notifCB = makeCheckBox("Show notification before break starts", Main.showNotification);
        notifCB.addActionListener(e -> {
            try {
                Main.showNotification = notifCB.isSelected();
                saveBoolean("showNotification", Main.showNotification);
                EyeLogger.info("Settings", "Notification preference changed to: " + Main.showNotification);
            } catch (Exception ex) {
                EyeLogger.error("Settings", "Couldn't save the notification preference", ex);
            }
        });
        root.add(notifCB);
        root.add(Box.createVerticalStrut(18));

        root.add(divider());
        root.add(Box.createVerticalStrut(18));

        root.add(buildSectionHeader("Waiting Time"));
        root.add(Box.createVerticalStrut(6));
        root.add(styledLabel("How long to wait between breaks.", TEXT_MUTED, 12));
        root.add(Box.createVerticalStrut(14));

        // How many seconds to wait before the next break (restarts the timer when changed)
        JSlider waitSlider = makeSlider(60, 7200, Main.waitSeconds, 60, s -> {
            Main.waitSeconds = s;
            saveInt("savedWaitTime", s);
            Main.startT();
        });
        root.add(sliderRow("Every:", waitSlider, v -> {
            if (v < 60) return v + " seconds";
            return (v / 60) + " minute" + (v / 60 == 1 ? "" : "s");
        }));
        root.add(Box.createVerticalStrut(24));

        root.add(divider());
        root.add(Box.createVerticalStrut(16));

        JButton restoreBtn = makeActionButton("Restore defaults");
        restoreBtn.addActionListener(e -> {
            try {
                EyeLogger.info("Settings", "Restoring schedule defaults");
                Main.breakSeconds     = 20;
                Main.waitSeconds      = 600;
                Main.showNotification = true;
                saveInt("savedBreakTime",       20);
                saveInt("savedWaitTime",        600);
                saveBoolean("showNotification", true);
                Main.startT();
                frame.dispose();
                showWindow();
                EyeLogger.info("Settings", "Schedule defaults restored");
            } catch (Exception ex) {
                EyeLogger.error("Settings", "Something went wrong while restoring schedule defaults", ex);
            }
        });
        root.add(restoreBtn);

        return root;
    }

    // Small interfaces used to pass lambdas into the slider helpers
    @FunctionalInterface
    interface IntConsumer { void accept(int value); }
    @FunctionalInterface
    interface Labeller    { String label(int value); }

    // Lays out a label on the left, a slider in the middle, and the current value on the right
    private static JPanel sliderRow(String prefix, JSlider slider, Labeller labeller) {
        JLabel valueLabel = styledLabel(labeller.label(slider.getValue()), TEXT, 13);
        valueLabel.setPreferredSize(new Dimension(90, 20));

        slider.addChangeListener(e -> valueLabel.setText(labeller.label(slider.getValue())));

        JLabel prefixLabel = styledLabel(prefix, TEXT, 13);
        prefixLabel.setPreferredSize(new Dimension(80, 20));

        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(BG);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(prefixLabel, BorderLayout.WEST);
        row.add(slider,      BorderLayout.CENTER);
        row.add(valueLabel,  BorderLayout.EAST);
        return row;
    }

    // Creates a slider and calls the callback only when the user stops dragging
    private static JSlider makeSlider(int min, int max, int value, int tickSpacing, IntConsumer onChange) {
        JSlider s = new JSlider(min, max, value);
        s.setBackground(BG);
        s.setForeground(TEXT);
        s.setPaintTicks(true);
        s.setPaintLabels(false);
        s.setMinorTickSpacing(tickSpacing);
        s.setOpaque(false);
        UIManager.put("Slider.thumb", ORANGE);

        s.addChangeListener(e -> {
            if (!s.getValueIsAdjusting()) {
                try {
                    onChange.accept(s.getValue());
                } catch (Exception ex) {
                    EyeLogger.error("Settings", "Couldn't save the slider value: " + s.getValue(), ex);
                }
            }
        });
        return s;
    }

    // A simple styled checkbox that fits the dark theme
    private static JCheckBox makeCheckBox(String text, boolean selected) {
        JCheckBox cb = new JCheckBox(text, selected);
        cb.setBackground(BG);
        cb.setForeground(TEXT);
        cb.setFocusPainted(false);
        cb.setFont(new Font("Dialog", Font.PLAIN, 13));
        cb.setAlignmentX(Component.LEFT_ALIGNMENT);
        return cb;
    }

    // A text label with the right color and size for the dark UI
    private static JLabel styledLabel(String text, Color color, int size) {
        JLabel l = new JLabel(text);
        l.setForeground(color);
        l.setFont(new Font("Dialog", Font.PLAIN, size));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    // A bold header used to title each section within a panel
    private static JPanel buildSectionHeader(String title) {
        JLabel lbl = new JLabel(title);
        lbl.setForeground(TEXT);
        lbl.setFont(new Font("Dialog", Font.BOLD, 14));
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setBackground(BG);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lbl);
        return p;
    }

    // A thin horizontal line used to visually separate sections
    private static JSeparator divider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_CLR);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        return sep;
    }

    // A dark-styled button for actions like restoring defaults
    private static JButton makeActionButton(String text) {
        JButton b = new JButton(text);
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setBackground(new Color(70, 70, 70));
        b.setForeground(TEXT);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // Shorthand helpers to write a value to the saved preferences
    private static void saveInt(String key, int value) {
        Preferences.userNodeForPackage(Main.class).putInt(key, value);
    }

    private static void saveBoolean(String key, boolean value) {
        Preferences.userNodeForPackage(Main.class).putBoolean(key, value);
    }

    // The About tab — just shows the app name and version
    private static JPanel buildAboutPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(BG);

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.insets = new Insets(8, 0, 8, 0);

        JLabel title = new JLabel("👁  EyeSoft");
        title.setFont(new Font("Dialog", Font.BOLD, 22));
        title.setForeground(TEXT);

        JLabel sub = new JLabel("Take care of your eyes.");
        sub.setFont(new Font("Dialog", Font.PLAIN, 14));
        sub.setForeground(TEXT_MUTED);

        JLabel ver = new JLabel("Version 1.1");
        ver.setFont(new Font("Dialog", Font.PLAIN, 12));
        ver.setForeground(TEXT_MUTED);

        g.gridy = 0; p.add(title, g);
        g.gridy = 1; p.add(sub,   g);
        g.gridy = 2; p.add(ver,   g);
        return p;
    }
}