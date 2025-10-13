package com.sinensia.games;

import java.util.Objects;

import com.sinensia.games.GuessGame.Estado;

/**
 * Orquesta la interacción del jugador con la lógica del juego.
 * <p>
 * Implementa el patrón <strong>Strategy</strong> al recibir una implementación de {@link GameIO},
 * permitiendo intercambiar el canal de entrada/salida (consola real, interfaz gráfica, mocks en tests, etc.)
 * sin modificar el flujo principal. Además, sigue el principio de inversión de dependencias al recibir
 * sus colaboraciones desde el exterior.
 * </p>
 *
 * @author sinensia
 * @version 0.0.2
 */
public class AppGame {

    private final GuessGame game; // Lógica del juego (reglas y validaciones).
    private final GameIO io; // Canal de entrada/salida (consola, mocks, etc.).

    /**
     * Crea la aplicación de consola inyectando la lógica del juego y el canal de E/S.
     *
     * @param game lógica central que conoce el número secreto
     * @param io   estrategia de interacción con la persona usuaria
     * @throws NullPointerException si alguna dependencia es nula
     */
    public AppGame(GuessGame game, GameIO io) {
        // Validamos dependencias para evitar NullPointerException accidentales.
        this.game = Objects.requireNonNull(game, "game no puede ser null");
        this.io = Objects.requireNonNull(io, "io no puede ser null");
    }

    /**
     * Arranca el ciclo de juego clásico:
     * <ol>
     *     <li>Da la bienvenida e informa del número de vidas.</li>
     *     <li>Pide intentos hasta acertar o agotar las vidas.</li>
     *     <li>Comunica el resultado de cada jugada con mensajes claros.</li>
     * </ol>
     * Esta operación personifica el patrón <strong>Template Method</strong> informal,
     * porque define la secuencia fija de pasos mientras delega la entrada/salida a {@link GameIO}.
     */
    public void start() {
        int vidasRestantes = game.getMaxVidas();
        io.print("Bienvenido al juego de adivinar (1-10)");
        io.print("Tienes " + vidasRestantes + " vidas.");

        // Bucle de juego: continúa mientras queden vidas disponibles.
        while (vidasRestantes > 0) {
            io.print("Introduce un número:");
            String input = io.read(); // Delegamos la lectura para desacoplar de la consola.
            Estado resultado = game.verificarInput(input);

            switch (resultado) {
                case SUCCESS -> {
                    io.print("¡Correcto! El número era " + game.getNumeroSecreto());
                    return; // Salimos: partida ganada.
                }
                case FAILED -> {
                    vidasRestantes--; // Decrementamos vidas solo cuando el intento es incorrecto.
                    io.print("No es ese... Te quedan " + vidasRestantes + " vidas.");
                }
                case INVALID -> io.print("Entrada inválida. Usa números del 1 al 10.");
                case OUTOFRANGE -> io.print("Número fuera de rango");
                // El enum tiene más estados reservados para escenarios concurrentes.
                // Si llegaran aquí, simplemente ignoramos la entrada (comportamiento actual).
            }
        }
        // Llegamos aquí cuando el jugador pierde todas sus vidas.
        io.print("Game Over. El número era " + game.getNumeroSecreto());
    }

    /**
     * Punto de entrada convencional para ejecutar el juego desde la consola estándar.
     * Instancia la lógica con 3 vidas y utiliza la consola como estrategia de E/S.
     *
     * @param args parámetros de línea de comandos (no utilizados)
     */
    public static void main(String[] args) {
        GuessGame juego = new GuessGame(3);
        GameIO consola = new Consola();
        new AppGame(juego, consola).start();
    }
}
