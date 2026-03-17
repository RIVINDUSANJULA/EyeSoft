package com.eyesoft;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.*;

// One place to handle all logging for EyeSoft.
// Writes to the console and to a rotating log file in ~/Library/Logs/EyeSoft.log.
public class EyeLogger {

    private static final Logger LOGGER = Logger.getLogger("EyeSoft");
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    static {
        try {
            LOGGER.setUseParentHandlers(false);

            // Set up a console handler that prints clean, readable lines
            ConsoleHandler ch = new ConsoleHandler();
            ch.setFormatter(new SimpleFormatter() {
                @Override
                public synchronized String format(LogRecord lr) {
                    String time   = LocalDateTime.now().format(FMT);
                    String level  = lr.getLevel().getName();
                    String msg    = lr.getMessage();
                    String prefix = level.equals("SEVERE") ? "[ERROR]" :
                                    level.equals("WARNING") ? "[WARN] " : "[INFO] ";
                    return time + " " + prefix + " " + msg + "\n";
                }
            });
            ch.setLevel(Level.ALL);
            LOGGER.addHandler(ch);

            // Also write to a file so we can look back at what happened — up to 1 MB, keeps 3 old copies
            FileHandler fh = new FileHandler(
                System.getProperty("user.home") + "/Library/Logs/EyeSoft.log",
                1_000_000, 3, true
            );
            fh.setFormatter(new SimpleFormatter());
            fh.setLevel(Level.ALL);
            LOGGER.addHandler(fh);

            LOGGER.setLevel(Level.ALL);
        } catch (Exception e) {
            // If the file logger fails, just fall back to the console so we don't crash on startup
            System.err.println("[EyeLogger] Could not set up the log file: " + e.getMessage());
        }
    }

    public static void info(String context, String msg) {
        LOGGER.info("[" + context + "] " + msg);
    }

    public static void warn(String context, String msg) {
        LOGGER.warning("[" + context + "] " + msg);
    }

    public static void error(String context, String msg, Throwable t) {
        LOGGER.log(Level.SEVERE, "[" + context + "] " + msg +
            (t != null ? " | " + t.getClass().getSimpleName() + ": " + t.getMessage() : ""), t);
    }

    public static void error(String context, String msg) {
        error(context, msg, null);
    }
}
