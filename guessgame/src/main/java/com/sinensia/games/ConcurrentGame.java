package com.sinensia.games;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Pequeña demo para ilustrar que {@link GuessGame} soporta accesos concurrentes.
 * Varios hilos intentan adivinar el mismo número hasta que uno acierta.
 */
public final class ConcurrentGame {

    private static final int NUM_JUGADORES = 3;
    private static final int VIDAS_POR_JUGADOR = 3;

    private ConcurrentGame() {
        // Evitamos instanciación: la clase solo ofrece el método main.
    }

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

    private static void jugar(GuessGame juego, String nombre) {
        Random rnd = new Random();
        while (!juego.isTerminado()) {
            int intento = rnd.nextInt(10) + 1; // Se mantiene el rango válido (1-10).
            GuessGame.Estado estado = juego.verificarInput(String.valueOf(intento));

            switch (estado) {
                case SUCCESS -> System.out.println(nombre + " adivinó el número!");
                case FAILED -> System.out.println(nombre + " probó con " + intento);
                case ENDED -> System.out.println(nombre + " detectó que la partida terminó.");
                default -> {
                    // Estados INVALID u OUTOFRANGE no deberían aparecer en esta demo.
                }
            }

            try {
                Thread.sleep(200L + rnd.nextInt(200)); // Pausa aleatoria para no saturar el juego.
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Buenas prácticas: restablecemos la interrupción.
            }
        }
    }
}
