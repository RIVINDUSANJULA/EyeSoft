package com.eyesoft;

import java.io.*;
import java.nio.file.*;

// Deals with making EyeSoft launch automatically when the user logs in.
// On macOS this works by writing a LaunchAgent plist file that the OS picks up at login.
public class StartupManager {

    private static final String LABEL      = "com.eyesoft";
    private static final String PLIST_NAME = LABEL + ".plist";

    // The plist lives here — macOS reads this folder on login
    private static final Path PLIST_PATH = Paths.get(
        System.getProperty("user.home"), "Library", "LaunchAgents", PLIST_NAME
    );

    // Check whether the user has already enabled run-at-login
    public static boolean isEnabled() {
        boolean exists = Files.exists(PLIST_PATH);
        EyeLogger.info("StartupManager", "Startup enabled check: " + exists + " (plist: " + PLIST_PATH + ")");
        return exists;
    }

    // Turn run-at-login on or off
    public static void setEnabled(boolean enable) {
        if (enable) {
            install();
        } else {
            uninstall();
        }
    }

    // Write the plist file and tell macOS to load it right away
    private static void install() {
        try {
            // We need to know which java binary and classpath to use so launchd can start us correctly
            String classPath = System.getProperty("java.class.path");
            String javaPath  = ProcessHandle.current().info().command().orElse("java");

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

            Files.createDirectories(PLIST_PATH.getParent());
            Files.writeString(PLIST_PATH, plist, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            // Register the plist with launchd so it takes effect without needing a reboot
            runLaunchctl("load", PLIST_PATH.toString());

            EyeLogger.info("StartupManager", "Launch Agent installed and loaded: " + PLIST_PATH);

        } catch (IOException e) {
            EyeLogger.error("StartupManager", "Could not write the LaunchAgent plist file", e);
        } catch (Exception e) {
            EyeLogger.error("StartupManager", "Something went wrong while setting up startup", e);
        }
    }

    // Remove the plist file and unload it from launchd
    private static void uninstall() {
        try {
            if (Files.exists(PLIST_PATH)) {
                runLaunchctl("unload", PLIST_PATH.toString());
                Files.delete(PLIST_PATH);
                EyeLogger.info("StartupManager", "Launch Agent removed: " + PLIST_PATH);
            } else {
                // Nothing to remove — probably was already disabled
                EyeLogger.warn("StartupManager", "Tried to disable startup but the plist wasn't there: " + PLIST_PATH);
            }
        } catch (IOException e) {
            EyeLogger.error("StartupManager", "Could not delete the LaunchAgent plist file", e);
        } catch (Exception e) {
            EyeLogger.error("StartupManager", "Something went wrong while removing the startup item", e);
        }
    }

    // Run a launchctl command (load or unload) for the given plist path
    private static void runLaunchctl(String cmd, String path) throws IOException, InterruptedException {
        try {
            ProcessBuilder pb = new ProcessBuilder("launchctl", cmd, path);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            int exitCode = p.waitFor();
            if (exitCode != 0) {
                EyeLogger.warn("StartupManager", "launchctl " + cmd + " finished with a non-zero exit code: " + exitCode);
            } else {
                EyeLogger.info("StartupManager", "launchctl " + cmd + " completed successfully");
            }
        } catch (IOException e) {
            EyeLogger.error("StartupManager", "launchctl " + cmd + " couldn't be executed", e);
            throw e;
        }
    }
}
