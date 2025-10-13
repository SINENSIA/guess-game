package com.sinensia.games;

import java.util.Objects;

import com.sinensia.games.GuessGame.Estado;

/**
 * Orquesta la interacción del jugador con la lógica del juego.
 * Se le inyectan sus dependencias para facilitar pruebas y reutilización.
 */
public class AppGame {

    private final GuessGame game; // Lógica del juego (reglas y validaciones).
    private final GameIO io; // Canal de entrada/salida (consola, mocks, etc.).

    public AppGame(GuessGame game, GameIO io) {
        // Validamos dependencias para evitar NullPointerException accidentales.
        this.game = Objects.requireNonNull(game, "game no puede ser null");
        this.io = Objects.requireNonNull(io, "io no puede ser null");
    }

    /**
     * Arranca el bucle principal del juego:
     * 1. Se dan la bienvenida y vidas iniciales.
     * 2. Se solicita un número hasta acertar o quedarse sin vidas.
     * 3. Se informa en cada iteración del resultado del intento.
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

    public static void main(String[] args) {
        GuessGame juego = new GuessGame(3);
        GameIO consola = new Consola();
        new AppGame(juego, consola).start();
    }
}
