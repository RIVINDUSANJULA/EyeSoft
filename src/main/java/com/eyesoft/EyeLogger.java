package com.eyesoft;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.*;

/**
 * EyeLogger — Centralized, context-aware application logger.
 * Logs to both console (ANSI-colored) and a rotating file log.
 */
public class EyeLogger {

    private static final Logger LOGGER = Logger.getLogger("EyeSoft");
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    static {
        try {
            LOGGER.setUseParentHandlers(false);

            // Console handler with simple formatting
            ConsoleHandler ch = new ConsoleHandler();
            ch.setFormatter(new SimpleFormatter() {
                @Override
                public synchronized String format(LogRecord lr) {
                    String time = LocalDateTime.now().format(FMT);
                    String level = lr.getLevel().getName();
                    String msg = lr.getMessage();
                    String prefix = level.equals("SEVERE") ? "[ERROR]" :
                                    level.equals("WARNING") ? "[WARN] " : "[INFO] ";
                    return time + " " + prefix + " " + msg + "\n";
                }
            });
            ch.setLevel(Level.ALL);
            LOGGER.addHandler(ch);

            // File handler
            FileHandler fh = new FileHandler(
                System.getProperty("user.home") + "/Library/Logs/EyeSoft.log",
                1_000_000, 3, true  // 1 MB, 3 rotating files, append
            );
            fh.setFormatter(new SimpleFormatter());
            fh.setLevel(Level.ALL);
            LOGGER.addHandler(fh);

            LOGGER.setLevel(Level.ALL);
        } catch (Exception e) {
            // Fallback: let JUL use defaults if file logger fails
            System.err.println("[EyeLogger] Failed to init file logger: " + e.getMessage());
        }
    }

    // ── Public API ──────────────────────────────────────────────────────────────

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
