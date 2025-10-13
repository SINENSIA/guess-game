package com.sinensia.games;

import java.time.Instant;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utilidad centralizada para registro de eventos.
 * <p>
 * Se implementa como clase estática para evitar patrones de Singleton innecesarios (regla Sonar java:S6548).
 * Toda la aplicación delega aquí, lo que permite controlar formato y niveles desde un único punto.
 * </p>
 */
public final class GameLogger {

    private static final Logger LOGGER = Logger.getLogger("com.sinensia.games");

    private GameLogger() {
        // Evitamos instanciación.
    }

    public static void info(String message) {
        LOGGER.log(Level.INFO, () -> format(message));
    }

    public static void warn(String message) {
        LOGGER.log(Level.WARNING, () -> format(message));
    }

    public static void error(String message, Throwable exception) {
        Objects.requireNonNull(exception, "exception no puede ser null");
        LOGGER.log(Level.SEVERE, format(message), exception);
    }

    private static String format(String message) {
        return "[" + Instant.now() + "] " + message;
    }
}
