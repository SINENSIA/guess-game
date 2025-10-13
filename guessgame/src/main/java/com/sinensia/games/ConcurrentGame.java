package com.sinensia.games;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Pequeña demo para ilustrar que {@link GuessGame} soporta accesos concurrentes.
 * <p>
 * Aplica el patrón <strong>Executor</strong> (basado en {@link ExecutorService}) para lanzar múltiples
 * hilos trabajadores que comparten la misma instancia de juego.
 * También recurre a dos singletons:
 * <ul>
 *     <li>{@link GameRandom} para reutilizar la fuente pseudoaleatoria.</li>
 *     <li>{@link GameLogger} para registrar eventos y excepciones de forma centralizada.</li>
 * </ul>
 * Cada hilo ejerce de "cliente" independiente y demuestra cómo el bloqueo interno de {@link GuessGame}
 * evita condiciones de carrera.
 * </p>
 *
 * @author sinensia
 * @version 0.0.2
 */
public final class ConcurrentGame {

    private static final int NUM_JUGADORES = 3;
    private static final int VIDAS_POR_JUGADOR = 3;
    private ConcurrentGame() {
        // Evitamos instanciación: la clase solo ofrece el método main.
    }

    /**
     * Ejecuta la simulación concurrente creando un pool fijo de jugadores virtuales.
     *
     * @param args parámetros de línea de comandos (no utilizados)
     */
    public static void main(String[] args) {
        GuessGame juego = new GuessGame(VIDAS_POR_JUGADOR); // número secreto común
        try (ExecutorService pool = Executors.newFixedThreadPool(NUM_JUGADORES)) {

            for (int i = 1; i <= NUM_JUGADORES; i++) {
                String nombre = "Jugador-" + i;
                pool.submit(() -> jugar(juego, nombre));
            }

            pool.shutdown(); // Rechaza nuevas tareas; los hilos activos terminan solos.
        }
    }

    /**
     * Rutina que ejecuta cada jugador virtual: genera intentos aleatorios hasta detectar un ganador.
     *
     * @param juego  instancia compartida y thread-safe
     * @param nombre etiqueta de identificación del "jugador"
     */
    private static void jugar(GuessGame juego, String nombre) {
        while (!juego.isTerminado()) {
            int intento = GameRandom.nextInt(1, 11); // Se mantiene el rango válido (1-10).
            GuessGame.Estado estado = juego.verificarInput(String.valueOf(intento));

            switch (estado) {
                case SUCCESS -> GameLogger.info(nombre + " adivinó el número!");
                case FAILED -> GameLogger.info(nombre + " probó con " + intento);
                case INVALID -> GameLogger.warn(nombre + " introdujo un valor no numérico.");
                case OUTOFRANGE -> GameLogger.warn(nombre + " salió del rango permitido con " + intento);
                case ERROR -> GameLogger.error("BUG: estado de error inesperado en ConcurrentGame", new IllegalStateException("Estado ERROR en hilo " + nombre));
                case ENDED -> GameLogger.info(nombre + " detectó que la partida terminó.");
            }

            try {
                Thread.sleep(200L + GameRandom.nextInt(200)); // Pausa aleatoria para no saturar el juego.
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Buenas prácticas: restablecemos la interrupción.
                GameLogger.error("BUG: interrupción inesperada en " + nombre, e);
            }
        }
    }
}
