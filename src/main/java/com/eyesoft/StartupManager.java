package com.eyesoft;

import java.io.*;
import java.nio.file.*;

/**
 * StartupManager — Handles macOS run-at-login via a LaunchAgent plist.
 * The plist is installed to ~/Library/LaunchAgents/com.eyesoft.plist
 * and tells launchd to run EyeSoft on login.
 */
public class StartupManager {

    private static final String LABEL      = "com.eyesoft";
    private static final String PLIST_NAME = LABEL + ".plist";
    private static final Path   PLIST_PATH = Paths.get(
        System.getProperty("user.home"), "Library", "LaunchAgents", PLIST_NAME
    );

    // ── Public API ─────────────────────────────────────────────────────────────

    /** Returns true if the LaunchAgent plist is currently installed. */
    public static boolean isEnabled() {
        boolean exists = Files.exists(PLIST_PATH);
        EyeLogger.info("StartupManager", "Startup enabled check: " + exists + " (plist: " + PLIST_PATH + ")");
        return exists;
    }

    /** Installs or removes the LaunchAgent plist based on the flag. */
    public static void setEnabled(boolean enable) {
        if (enable) {
            install();
        } else {
            uninstall();
        }
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private static void install() {
        try {
            // Resolve the jar/classpath currently running so launchd can call it
            String classPath = System.getProperty("java.class.path");
            String javaPath  = ProcessHandle.current()
                .info().command().orElse("java");

            String plist = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\""
                + " \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\n"
                + "<plist version=\"1.0\">\n"
                + "<dict>\n"
                + "    <key>Label</key>\n"
                + "    <string>" + LABEL + "</string>\n"
                + "    <key>ProgramArguments</key>\n"
                + "    <array>\n"
                + "        <string>" + javaPath + "</string>\n"
                + "        <string>-Dapple.awt.UIElement=true</string>\n"
                + "        <string>-cp</string>\n"
                + "        <string>" + classPath + "</string>\n"
                + "        <string>com.eyesoft.Main</string>\n"
                + "    </array>\n"
                + "    <key>RunAtLoad</key>\n"
                + "    <true/>\n"
                + "    <key>KeepAlive</key>\n"
                + "    <false/>\n"
                + "</dict>\n"
                + "</plist>\n";

            // Ensure LaunchAgents dir exists
            Files.createDirectories(PLIST_PATH.getParent());
            Files.writeString(PLIST_PATH, plist, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            // Load into launchd immediately
            runLaunchctl("load", PLIST_PATH.toString());

            EyeLogger.info("StartupManager", "Launch Agent installed and loaded: " + PLIST_PATH);

        } catch (IOException e) {
            EyeLogger.error("StartupManager", "Failed to write LaunchAgent plist", e);
        } catch (Exception e) {
            EyeLogger.error("StartupManager", "Unexpected error while installing startup item", e);
        }
    }

    private static void uninstall() {
        try {
            if (Files.exists(PLIST_PATH)) {
                runLaunchctl("unload", PLIST_PATH.toString());
                Files.delete(PLIST_PATH);
                EyeLogger.info("StartupManager", "Launch Agent removed: " + PLIST_PATH);
            } else {
                EyeLogger.warn("StartupManager", "Uninstall requested but plist not found: " + PLIST_PATH);
            }
        } catch (IOException e) {
            EyeLogger.error("StartupManager", "Failed to delete LaunchAgent plist", e);
        } catch (Exception e) {
            EyeLogger.error("StartupManager", "Unexpected error while removing startup item", e);
        }
    }

    private static void runLaunchctl(String cmd, String path) throws IOException, InterruptedException {
        try {
            ProcessBuilder pb = new ProcessBuilder("launchctl", cmd, path);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            int exitCode = p.waitFor();
            if (exitCode != 0) {
                EyeLogger.warn("StartupManager", "launchctl " + cmd + " exited with code " + exitCode);
            } else {
                EyeLogger.info("StartupManager", "launchctl " + cmd + " succeeded");
            }
        } catch (IOException e) {
            EyeLogger.error("StartupManager", "launchctl " + cmd + " failed to execute", e);
            throw e;
        }
    }
}
