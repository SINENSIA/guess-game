package com.sinensia.games;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Núcleo del juego de adivinar números.
 * <p>
 * Encapsula el número secreto y el conteo de vidas aplicando el patrón <strong>Monitor Object</strong>:
 * todas las operaciones sensibles se ejecutan dentro de un bloqueo interno para garantizar
 * la consistencia cuando se invoca desde múltiples hilos (ver {@link #verificarInput(String)}).
 * También expone lectura segura del estado mediante {@link AtomicBoolean}, lo que permite comprobar
 * si se ha terminado la partida sin entrar en secciones críticas.
 * </p>
 *
 * @author sinensia
 * @version 0.0.2
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

    /**
     * Crea una partida con un número secreto aleatorio.
     *
     * @param maxVidas número máximo de intentos permitidos
     */
    public GuessGame(int maxVidas) {
        this(maxVidas, RAND.nextInt(MIN_PERMITIDO, MAX_PERMITIDO + 1));
    }

    /**
     * Constructor pensado para pruebas: permite fijar el número esperado.
     *
     * @param maxVidas          número máximo de intentos
     * @param numeroSecretoFijo valor que se desea adivinar en la partida
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
     *
     * @param numero intento del jugador tal y como se recibe (texto libre)
     * @return estado que describe si se acertó, falló o si la entrada es inválida
     */
    public Estado verificarInput(String numero) {
        synchronized (lock) {
            if (terminado.get()) {
                // Si alguien ya ganó, notificamos explícitamente el estado para clientes concurrentes.
                return Estado.ENDED;
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

            } catch (NumberFormatException _) {
                GameLogger.warn("Entrada no numérica detectada en GuessGame.verificarInput");
                return Estado.INVALID; // Entradas no numéricas.
            } catch (Exception e) {
                GameLogger.error("BUG: error no controlado en GuessGame.verificarInput", e);
                return Estado.ERROR;
            }
        }
    }

    /**
     * Indica si la partida ya cuenta con una persona ganadora.
     *
     * @return {@code true} si el número ha sido descubierto
     */
    public boolean isTerminado() {
        return terminado.get();
    }

    /**
     * Devuelve el número secreto en curso (principalmente útil para informes o pruebas).
     *
     * @return número secreto configurado para la partida
     */
    public int getNumeroSecreto() {
        return numeroSecreto;
    }

    /**
     * Recupera el número máximo de vidas con el que se creó la partida.
     *
     * @return número máximo de intentos disponibles
     */
    public int getMaxVidas() {
        return maxVidas;
    }

    /**
     * Posibles respuestas al procesar un intento de adivinanza.
     */
    public enum Estado {
        /** La persona jugadora acertó el número. */
        SUCCESS,
        /** Intento válido pero erróneo; la partida continúa. */
        FAILED,
        /** La entrada no se pudo parsear como entero. */
        INVALID,
        /** La entrada numérica quedó fuera del rango permitido. */
        OUTOFRANGE,
        /** Error general no previsto; se registra para el equipo de desarrollo. */
        ERROR,
        /** Se devuelve cuando la partida ya fue resuelta por otro participante. */
        ENDED
    }
}
