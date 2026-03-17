package com.eyesoft;

import javax.swing.*;
import java.awt.*;

// This class is launched as a separate process when it's time for a break.
// It covers the whole screen with a black window so the user actually stops and rests.
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
                    // Some displays don't support true full-screen, so we maximize instead
                    EyeLogger.warn("ScreenBlocker", "Full-screen exclusive mode not supported — using maximized window instead");
                    frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                    frame.setVisible(true);
                }
            } catch (Exception e) {
                // If full-screen throws an error, still try to show something
                EyeLogger.error("ScreenBlocker", "Full-screen setup failed — falling back to maximized window", e);
                frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                frame.setVisible(true);
            }

            EyeLogger.info("ScreenBlocker", "Break screen displayed successfully");

        } catch (HeadlessException e) {
            EyeLogger.error("ScreenBlocker", "No display available — cannot show the break screen", e);
        } catch (Exception e) {
            EyeLogger.error("ScreenBlocker", "Something unexpected went wrong while showing the break screen", e);
        }
    }
}