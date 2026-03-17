package com.eyesoft;

import javax.swing.*;
import java.awt.*;
import java.util.prefs.Preferences;

/**
 * Settings — EyeSoft Preferences Window
 * Dark-themed with tabs: Schedule | About
 * Implements GitHub issue #2:
 *   - Schedule tab with Short Break (adjustable time + notification toggle)
 *   - Waiting Time (adjustable interval)
 */
public class Settings {

    // ── Dark palette ───────────────────────────────────────────────────────────
    private static final Color BG          = new Color(43, 43, 43);
    private static final Color BG_TOOLBAR  = new Color(55, 55, 55);
    private static final Color BORDER_CLR  = new Color(70, 70, 70);
    private static final Color TEXT        = new Color(220, 220, 220);
    private static final Color TEXT_MUTED  = new Color(155, 155, 155);
    private static final Color ORANGE      = new Color(220, 130, 50);   // slider thumb

    private static JFrame frame = null;

    // ── Entry point ────────────────────────────────────────────────────────────
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
                EyeLogger.warn("Settings", "Failed to set cross-platform LAF: " + e.getMessage());
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

                // ── Toolbar tabs
                JPanel toolbar       = buildToolbar();
                JPanel cardContainer = new JPanel(new CardLayout());
                cardContainer.setBackground(BG);

                JPanel schedulePanel = buildSchedulePanel();
                JPanel aboutPanel    = buildAboutPanel();
                cardContainer.add(schedulePanel, "Schedule");
                cardContainer.add(aboutPanel,    "About");

                CardLayout cards = (CardLayout) cardContainer.getLayout();

                // Wire tab buttons from toolbar
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
                                EyeLogger.error("Settings", "Error switching to tab: " + btn.getActionCommand(), ex);
                            }
                        });
                    }
                }

                // Pre-select Schedule tab
                for (Component c : toolbar.getComponents()) {
                    if (c instanceof JButton b && "Schedule".equals(b.getActionCommand())) {
                        b.setBackground(BG.darker());
                        break;
                    }
                }
                cards.show(cardContainer, "Schedule");

                frame.add(toolbar, BorderLayout.NORTH);
                frame.add(cardContainer, BorderLayout.CENTER);
                frame.setVisible(true);
                EyeLogger.info("Settings", "Preferences window opened successfully");
            } catch (Exception e) {
                EyeLogger.error("Settings", "Failed to build or show preferences window", e);
            }
        });
    }

    // ── Toolbar ────────────────────────────────────────────────────────────────
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

    // ── Schedule tab ───────────────────────────────────────────────────────────
    private static JPanel buildSchedulePanel() {
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBackground(BG);
        root.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // ── Short Break section
        root.add(buildSectionHeader("Short Break"));
        root.add(Box.createVerticalStrut(6));
        root.add(styledLabel("Short breaks help rest your eyes and reduce screen fatigue.", TEXT_MUTED, 12));
        root.add(Box.createVerticalStrut(14));

        // Break duration slider (5s – 300s)
        JSlider breakSlider = makeSlider(5, 300, Main.breakSeconds, 1, s -> {
            Main.breakSeconds = s;
            saveInt("savedBreakTime", s);
        });
        root.add(sliderRow("Break for:", breakSlider, v -> v + " seconds"));
        root.add(Box.createVerticalStrut(10));

        // Notification checkbox
        JCheckBox notifCB = makeCheckBox("Show notification before break starts", Main.showNotification);
        notifCB.addActionListener(e -> {
            try {
                Main.showNotification = notifCB.isSelected();
                saveBoolean("showNotification", Main.showNotification);
                EyeLogger.info("Settings", "Notification pref changed to: " + Main.showNotification);
            } catch (Exception ex) {
                EyeLogger.error("Settings", "Failed to save notification preference", ex);
            }
        });
        root.add(notifCB);
        root.add(Box.createVerticalStrut(18));

        // Divider
        root.add(divider());
        root.add(Box.createVerticalStrut(18));

        // ── Waiting Time section
        root.add(buildSectionHeader("Waiting Time"));
        root.add(Box.createVerticalStrut(6));
        root.add(styledLabel("How long to wait between breaks.", TEXT_MUTED, 12));
        root.add(Box.createVerticalStrut(14));

        // Wait interval slider (60s – 7200s)
        JSlider waitSlider = makeSlider(60, 7200, Main.waitSeconds, 60, s -> {
            Main.waitSeconds = s;
            saveInt("savedWaitTime", s);
            Main.startT();  // restart timer with new wait
        });
        root.add(sliderRow("Every:", waitSlider, v -> {
            if (v < 60) return v + " seconds";
            return (v / 60) + " minute" + (v / 60 == 1 ? "" : "s");
        }));
        root.add(Box.createVerticalStrut(24));

        // Divider + Restore defaults
        root.add(divider());
        root.add(Box.createVerticalStrut(16));

        JButton restoreBtn = makeActionButton("Restore defaults");
        restoreBtn.addActionListener(e -> {
            try {
                EyeLogger.info("Settings", "Restoring defaults");
                Main.breakSeconds     = 20;
                Main.waitSeconds      = 600;
                Main.showNotification = true;
                saveInt("savedBreakTime",       20);
                saveInt("savedWaitTime",        600);
                saveBoolean("showNotification", true);
                Main.startT();
                frame.dispose();
                showWindow();
                EyeLogger.info("Settings", "Defaults restored successfully");
            } catch (Exception ex) {
                EyeLogger.error("Settings", "Failed to restore defaults", ex);
            }
        });
        root.add(restoreBtn);

        return root;
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    @FunctionalInterface
    interface IntConsumer { void accept(int value); }
    @FunctionalInterface
    interface Labeller    { String label(int value); }

    /** Builds a row: [label] [slider] [value label] */
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

    private static JSlider makeSlider(int min, int max, int value, int tickSpacing, IntConsumer onChange) {
        JSlider s = new JSlider(min, max, value);
        s.setBackground(BG);
        s.setForeground(TEXT);
        s.setPaintTicks(true);
        s.setPaintLabels(false);
        s.setMinorTickSpacing(tickSpacing);
        s.setOpaque(false);
        // Orange thumb via UI defaults
        UIManager.put("Slider.thumb", ORANGE);

        // Hashtable<Integer, JLabel> labels = new Hashtable<>();
        // labels.put(min, styledLabel(String.valueOf(min), TEXT_MUTED, 10));
        // labels.put(max, styledLabel(String.valueOf(max), TEXT_MUTED, 10));
        // s.setLabelTable(labels);

        s.addChangeListener(e -> {
            if (!s.getValueIsAdjusting()) {
                try {
                    onChange.accept(s.getValue());
                } catch (Exception ex) {
                    EyeLogger.error("Settings", "Failed to apply slider value: " + s.getValue(), ex);
                }
            }
        });
        return s;
    }

    private static JCheckBox makeCheckBox(String text, boolean selected) {
        JCheckBox cb = new JCheckBox(text, selected);
        cb.setBackground(BG);
        cb.setForeground(TEXT);
        cb.setFocusPainted(false);
        cb.setFont(new Font("Dialog", Font.PLAIN, 13));
        cb.setAlignmentX(Component.LEFT_ALIGNMENT);
        return cb;
    }

    private static JLabel styledLabel(String text, Color color, int size) {
        JLabel l = new JLabel(text);
        l.setForeground(color);
        l.setFont(new Font("Dialog", Font.PLAIN, size));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

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

    private static JSeparator divider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_CLR);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        return sep;
    }

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

    private static void saveInt(String key, int value) {
        Preferences.userNodeForPackage(Main.class).putInt(key, value);
    }

    private static void saveBoolean(String key, boolean value) {
        Preferences.userNodeForPackage(Main.class).putBoolean(key, value);
    }

    // ── About tab ──────────────────────────────────────────────────────────────
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