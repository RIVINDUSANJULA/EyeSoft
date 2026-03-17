//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
package com.eyesoft;

import java.util.concurrent.*;

import java.io.IOException;
import java.awt.TrayIcon;

import java.util.prefs.Preferences;


public class Main {

    static Preferences prefs = Preferences.userNodeForPackage(Main.class);
    public static int waitSeconds = prefs.getInt("savedWaitTime", 30);
    public static int breakSeconds = prefs.getInt("savedBreakTime", 20);
    
    private static ExecutorService executor = Executors.newSingleThreadExecutor();
    private static Future<?> currentTask;

    void main() {
        AppTray.setupTray();
        startT();
    }

    public static void startT() {

        if (currentTask != null && !currentTask.isDone()) {
            currentTask.cancel(true);
        }

        currentTask = executor.submit(() -> {
            try {
                // First wait happens before the first break. We'll start with waiting.
                while (!Thread.currentThread().isInterrupted()) {
                    
                    int notifyAdvance = Math.min(10, waitSeconds / 2); // 10 secs or half the wait
                    int initialWait = waitSeconds - notifyAdvance;

                    if (initialWait > 0) {
                        Thread.sleep(initialWait * 1000L);
                    }

                    if (notifyAdvance > 0) {
                        if (AppTray.trayIcon != null) {
                            AppTray.trayIcon.displayMessage("Upcoming Break", "Your break will start in " + notifyAdvance + " seconds.", TrayIcon.MessageType.INFO);
                        }
                        Thread.sleep(notifyAdvance * 1000L);
                    }

                    Process process = null;
                    try {
                        //Start ScreenBlocker
                        String screenblockpath = System.getProperty("java.class.path");
                        ProcessBuilder processBuilder = new ProcessBuilder("java", "-Dapple.awt.UIElement=true", "-cp", screenblockpath, "com.eyesoft.ScreenBlocker");
                        processBuilder.inheritIO();
                        process = processBuilder.start();

                        Thread.sleep(breakSeconds * 1000L);
                    } finally {
                        if (process != null && process.isAlive()) {
                            process.destroy();
                        }
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

    public static void shutdown () {
        executor.shutdownNow();
        System.exit(0);
    }
}