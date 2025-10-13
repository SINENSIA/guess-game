package com.sinensia.games;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Núcleo del juego de adivinar números.
 * Mantiene el estado compartido de forma segura para permitir escenarios con múltiples hilos.
 */
public class GuessGame {

    private static final Random RAND = new Random();
    private static final int MIN_PERMITIDO = 1;
    private static final int MAX_PERMITIDO = 10;

    private final int numeroSecreto;
    private final int maxVidas;

    /**
     * Bloqueo interno para proteger el acceso concurrente al estado mutable.
     * Optamos por un {@code Object} privado para encapsular la sincronización.
     */
    private final Object lock = new Object();

    /**
     * Marca si alguien ganó ya la partida.
     * Usamos un {@link AtomicBoolean} para poder consultar el estado sin bloquear.
     */
    private final AtomicBoolean terminado = new AtomicBoolean(false);

    public GuessGame(int maxVidas) {
        this(maxVidas, RAND.nextInt(MIN_PERMITIDO, MAX_PERMITIDO + 1));
    }

    /**
     * Constructor pensado para pruebas: permite fijar el número esperado.
     */
    public GuessGame(int maxVidas, int numeroSecretoFijo) {
        this.numeroSecreto = numeroSecretoFijo;
        this.maxVidas = maxVidas;
    }

    /**
     * Revisa la entrada del jugador y devuelve el estado de la jugada.
     * El método es thread-safe: toda la lógica se ejecuta dentro de un bloque
     * {@code synchronized} para garantizar consistencia cuando varios hilos
     * intentan adivinar a la vez.
     */
    public Estado verificarInput(String numero) {
        synchronized (lock) {
            if (terminado.get()) {
                // Si alguien ya ganó, rechazamos nuevos intentos (comportamiento actual).
                return Estado.FAILED;
            }

            try {
                int numeroInt = Integer.parseInt(numero);
                if (numeroInt < MIN_PERMITIDO || numeroInt > MAX_PERMITIDO) {
                    return Estado.OUTOFRANGE;
                }

                if (numeroInt == numeroSecreto) {
                    terminado.set(true); // Bloquea intentos futuros: ya tenemos ganador.
                    return Estado.SUCCESS;
                }
                return Estado.FAILED;

            } catch (NumberFormatException e) {
                return Estado.INVALID; // Entradas no numéricas.
            }
        }
    }

    public boolean isTerminado() {
        return terminado.get();
    }

    public int getNumeroSecreto() {
        return numeroSecreto;
    }

    public int getMaxVidas() {
        return maxVidas;
    }

    /**
     * Posibles respuestas al procesar un intento de adivinanza.
     */
    public enum Estado {
        SUCCESS, FAILED, INVALID, OUTOFRANGE, ENDED
    }
}
