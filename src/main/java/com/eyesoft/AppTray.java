package com.eyesoft;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * AppTray — System tray icon and menu.
 * All errors logged with EyeLogger context.
 */
public class AppTray {

    public static TrayIcon trayIcon;

    public static void setupTray() {
        EyeLogger.info("AppTray", "Initializing system tray");

        if (!SystemTray.isSupported()) {
            EyeLogger.warn("AppTray", "SystemTray not supported on this platform — tray icon skipped");
            return;
        }

        try {
            // ── Tray icon image (red square)
            BufferedImage iconImage = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
            Graphics g = iconImage.getGraphics();
            g.setColor(Color.RED);
            g.fillRect(0, 0, 16, 16);
            g.dispose();

            // ── Popup menu
            PopupMenu popup = new PopupMenu();

            MenuItem settingsItem = new MenuItem("Time");
            settingsItem.addActionListener(e -> {
                EyeLogger.info("AppTray", "User opened Settings from tray");
                try {
                    Settings.showWindow();
                } catch (Exception ex) {
                    EyeLogger.error("AppTray", "Failed to open Settings window", ex);
                }
            });

            MenuItem exitItem = new MenuItem("Exit");
            exitItem.addActionListener(e -> {
                EyeLogger.info("AppTray", "User requested exit from tray");
                try {
                    Main.shutdown();
                } catch (Exception ex) {
                    EyeLogger.error("AppTray", "Error during shutdown from tray", ex);
                    System.exit(1);
                }
            });

            popup.add(settingsItem);
            popup.add(exitItem);

            trayIcon = new TrayIcon(iconImage, "EyeSoft", popup);
            trayIcon.setImageAutoSize(true);

            // Double-click on tray icon also opens settings
            trayIcon.addActionListener(e -> {
                EyeLogger.info("AppTray", "Tray icon double-clicked — opening Settings");
                try {
                    Settings.showWindow();
                } catch (Exception ex) {
                    EyeLogger.error("AppTray", "Failed to open Settings on tray double-click", ex);
                }
            });

            SystemTray.getSystemTray().add(trayIcon);
            EyeLogger.info("AppTray", "Tray icon added successfully");

        } catch (AWTException e) {
            EyeLogger.error("AppTray", "Failed to add tray icon to SystemTray", e);
        } catch (Exception e) {
            EyeLogger.error("AppTray", "Unexpected error during tray setup", e);
        }
    }
}