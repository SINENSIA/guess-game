package com.sinensia.games;

import java.util.Random;
import java.util.Scanner;

/**
 * Variante autónoma del juego para ejecutarlo directamente desde la consola.
 * <p>
 * Representa una versión monohilo y auto-contenida del juego, útil para comparar con la
 * orquestación basada en {@link AppGame}. Mantiene un diseño procedural sencillo que facilita
 * la lectura para perfiles en aprendizaje.
 * </p>
 *
 * @author sinensia
 * @version 0.0.2
 */
public class TwrGame {

    private static final Random RAND = new Random();
    private static final int VIDAS = 3;
    private static final int MIN = 1;
    private static final int MAX = 10;

    private final int numeroSecreto;

    /**
     * Crea una instancia con un número secreto aleatorio.
     */
    public TwrGame() {
        numeroSecreto = RAND.nextInt(MIN, MAX + 1);
    }

    /**
     * Valida el número propuesto por el jugador manteniendo la lógica original.
     * IMPORTANTE: si el número está fuera del rango, se indica como entrada inválida.
     *
     * @param numero valor introducido por la persona usuaria
     * @return estado que indica acierto, fallo o entrada inválida
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
     * <p>
     * De forma similar a un <strong>Template Method</strong>, define la secuencia fija de pasos
     * (pedir, validar, informar) sin separar la entrada/salida en estrategias.
     * </p>
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

    /**
     * Estados posibles al validar la jugada en esta variante.
     */
    enum Estado {
        SUCCESS, FAILED, INVALID
    }

    /**
     * Permite ejecutar esta variante directamente desde la JVM.
     *
     * @param args argumentos de línea de comandos (no utilizados)
     */
    public static void main(String[] args) {
        new TwrGame().play();
    }
}
