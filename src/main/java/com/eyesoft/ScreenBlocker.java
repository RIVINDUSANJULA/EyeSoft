package com.eyesoft;

import javax.swing.*;
import java.awt.*;

/**
 * ScreenBlocker — Full-screen break overlay window.
 * Launched as a separate process by Main's scheduler.
 * All errors logged with EyeLogger context.
 */
public class ScreenBlocker {

    public static void main(String[] args) {
        EyeLogger.info("ScreenBlocker", "Break screen starting");

        try {
            JFrame frame = new JFrame();
            frame.setType(Window.Type.UTILITY);
            frame.setUndecorated(true);
            frame.setAlwaysOnTop(true);
            frame.getContentPane().setBackground(Color.BLACK);
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

            try {
                GraphicsDevice gd = ge.getDefaultScreenDevice();

                if (gd.isFullScreenSupported()) {
                    EyeLogger.info("ScreenBlocker", "Entering full-screen exclusive mode");
                    gd.setFullScreenWindow(frame);
                } else {
                    EyeLogger.warn("ScreenBlocker", "Full-screen exclusive mode not supported — falling back to maximized window");
                    frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                    frame.setVisible(true);
                }
            } catch (Exception e) {
                EyeLogger.error("ScreenBlocker", "Failed to set full-screen mode — falling back to maximized window", e);
                frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                frame.setVisible(true);
            }

            EyeLogger.info("ScreenBlocker", "Break screen displayed successfully");

        } catch (HeadlessException e) {
            EyeLogger.error("ScreenBlocker", "Cannot display break screen — headless environment detected", e);
        } catch (Exception e) {
            EyeLogger.error("ScreenBlocker", "Unexpected error while displaying break screen", e);
        }
    }
}