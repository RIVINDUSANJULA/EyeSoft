package com.eyesoft;

import javax.swing.*;
import java.awt.*;

public class ScreenBlocker {
    public static void main(String[] args) {
        JFrame frame = new JFrame();

        frame.setType(Window.Type.UTILITY);
        // Windows Person Check - Rivindu

        frame.setUndecorated(true);
        frame.setAlwaysOnTop(true);
        frame.getContentPane().setBackground(Color.BLACK);

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();

        if (gd.isFullScreenSupported()) {
            gd.setFullScreenWindow(frame);
        } else {
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setVisible(true);
        }
    }
}