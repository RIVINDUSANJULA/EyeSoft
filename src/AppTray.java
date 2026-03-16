import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.JOptionPane;

public class AppTray {

    public static void setupTray() {
        if (!SystemTray.isSupported()) {
            return;
        }


        //Tray UI
        BufferedImage iconImage = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
        Graphics g = iconImage.getGraphics();
        g.setColor(Color.RED);
        g.fillRect(0, 0, 16, 16);

        PopupMenu popup = new PopupMenu();

        // Menu
        MenuItem settingsItem = new MenuItem("Time");
        settingsItem.addActionListener(e -> {

            Settings.showWindow();
        });

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(e -> {
            Main.shutdown();
        });

        popup.add(settingsItem);
        popup.add(exitItem);

        TrayIcon trayIcon = new TrayIcon(iconImage, "EyeSoft", popup);
        trayIcon.setImageAutoSize(true);

        try {
            SystemTray.getSystemTray().add(trayIcon);
        } catch (AWTException e) {
            System.err.println("Tray Error");
        }
    }
    }