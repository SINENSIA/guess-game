package com.sinensia.games;

import java.util.Random;
import java.util.Scanner;

/**
 * Variante autónoma del juego para ejecutarlo directamente desde la consola.
 */
public class TwrGame {

    private static final Random RAND = new Random();
    private static final int VIDAS = 3;
    private static final int MIN = 1;
    private static final int MAX = 10;

    private final int numeroSecreto;

    public TwrGame() {
        numeroSecreto = RAND.nextInt(MIN, MAX + 1);
    }

    /**
     * Valida el número propuesto por el jugador manteniendo la lógica original.
     * IMPORTANTE: si el número está fuera del rango, se indica como entrada inválida.
     */
    public Estado verificarInput(String numero) {
        try {
            int numeroInt = Integer.parseInt(numero);
            if (numeroInt < MIN || numeroInt > MAX) {
                return Estado.INVALID; // Se preserva el comportamiento original de esta versión.
            }
            return (numeroInt == numeroSecreto) ? Estado.SUCCESS : Estado.FAILED;
        } catch (NumberFormatException e) {
            return Estado.INVALID;
        }
    }

    /**
     * Bucle principal del juego (versión simple, mono-hilo).
     */
    public void play() {
        try (Scanner scanner = new Scanner(System.in)) { // try-with-resources gestiona el cierre automáticamente.
            System.out.println("Bienvenido al juego de adivinar el número (1-10)");
            System.out.println("(Tienes " + VIDAS + " intentos)");

            for (int vidasRestantes = VIDAS; vidasRestantes > 0; vidasRestantes--) {
                System.out.print("Introduce un número: ");
                String input = scanner.next();
                Estado resultado = verificarInput(input);

                switch (resultado) {
                    case SUCCESS -> {
                        System.out.println("¡Enhorabuena! El número era " + numeroSecreto);
                        return; // Termina el juego al acertar.
                    }
                    case FAILED -> System.out.println("No es ese... Te quedan " + (vidasRestantes - 1) + " vidas.");
                    case INVALID -> System.out.println("Entrada no válida. Debes poner un número del 1 al 10.");
                }
            }

            System.out.println("Game Over. El número era " + numeroSecreto);
        }
    }

    enum Estado {
        SUCCESS, FAILED, INVALID
    }

    public static void main(String[] args) {
        new TwrGame().play();
    }
}
