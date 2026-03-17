package com.eyesoft;

import java.util.concurrent.*;
import java.io.IOException;
import java.awt.TrayIcon;
import java.util.prefs.Preferences;

// The heart of the app. Keeps track of user settings and runs the break loop in the background.
public class Main {

    // Load saved settings from disk, or fall back to sensible defaults
    static Preferences prefs = Preferences.userNodeForPackage(Main.class);
    public static int waitSeconds  = prefs.getInt("savedWaitTime",  600); // 10 minutes by default
    public static int breakSeconds = prefs.getInt("savedBreakTime", 20);  // 20 seconds by default
    public static boolean showNotification = prefs.getBoolean("showNotification", true);

    private static ExecutorService executor = Executors.newSingleThreadExecutor();
    private static Future<?> currentTask;

    public static void main(String[] args) {
        EyeLogger.info("Main", "EyeSoft starting up");
        try {
            AppTray.setupTray();
            startT();
            EyeLogger.info("Main", "Startup complete");
        } catch (Exception e) {
            EyeLogger.error("Main", "Fatal error during startup", e);
            System.exit(1);
        }
    }

    // Starts (or restarts) the break scheduler.
    // If a previous loop is still running, we cancel it first before starting fresh.
    public static void startT() {
        if (currentTask != null && !currentTask.isDone()) {
            currentTask.cancel(true);
        }

        currentTask = executor.submit(() -> {
            EyeLogger.info("Scheduler", "Break loop started — wait=" + waitSeconds + "s, break=" + breakSeconds + "s, notify=" + showNotification);
            try {
                // Keep cycling: wait, optionally notify, then show the break screen
                while (!Thread.currentThread().isInterrupted()) {

                    // Give the user a heads-up a few seconds before the break, then wait
                    int notifyAdvance = Math.min(10, waitSeconds / 2);
                    int initialWait   = waitSeconds - notifyAdvance;

                    if (initialWait > 0) {
                        Thread.sleep(initialWait * 1000L);
                    }

                    if (notifyAdvance > 0 && showNotification) {
                        if (AppTray.trayIcon != null) {
                            AppTray.trayIcon.displayMessage("Upcoming Break",
                                "Your break will start in " + notifyAdvance + " seconds.",
                                TrayIcon.MessageType.INFO);
                        }
                        Thread.sleep(notifyAdvance * 1000L);
                    } else if (notifyAdvance > 0) {
                        Thread.sleep(notifyAdvance * 1000L);
                    }

                    // Launch the break screen as a separate process so it can go full-screen
                    Process process = null;
                    try {
                        String classPath = System.getProperty("java.class.path");
                        ProcessBuilder pb = new ProcessBuilder(
                            "java", "-Dapple.awt.UIElement=true", "-cp", classPath, "com.eyesoft.ScreenBlocker"
                        );
                        pb.inheritIO();
                        process = pb.start();
                        Thread.sleep(breakSeconds * 1000L);
                    } finally {
                        if (process != null && process.isAlive()) {
                            process.destroy();
                        }
                    }
                }
            } catch (InterruptedException e) {
                // This is normal — happens when we restart the timer or shut down
                EyeLogger.info("Scheduler", "Break loop interrupted (normal on restart/shutdown)");
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                EyeLogger.error("Scheduler", "Failed to launch the break screen process", e);
            } catch (Exception e) {
                EyeLogger.error("Scheduler", "Something unexpected went wrong in the break loop", e);
            }
        });
    }

    // Cleanly stop everything and close the app
    public static void shutdown() {
        EyeLogger.info("Main", "Shutdown requested — stopping executor and exiting");
        try {
            executor.shutdownNow();
        } catch (Exception e) {
            EyeLogger.error("Main", "Ran into a problem while shutting down", e);
        } finally {
            System.exit(0);
        }
    }
}