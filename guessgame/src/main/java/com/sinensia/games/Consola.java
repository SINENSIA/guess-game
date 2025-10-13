package com.sinensia.games;

import java.util.Scanner;

/**
 * Implementación concreta de {@link GameIO} que usa la consola estándar.
 * <p>
 * En términos del patrón <strong>Strategy</strong>, esta clase es una estrategia concreta que
 * {@link AppGame} puede utilizar para interactuar con la persona usuaria.
 * </p>
 *
 * @author sinensia
 * @version 0.0.2
 */
public class Consola implements GameIO {

    /**
     * Mantenemos un único {@link Scanner} asociado a {@code System.in}.
     * No lo cerramos explícitamente para no clausurar la entrada estándar de la JVM.
     */
    private final Scanner scanner = new Scanner(System.in);

    /**
     * Crea la estrategia de consola inicializando el {@link Scanner} asociado a la entrada estándar.
     */
    public Consola() {
        // El scanner ya está preparado en el campo final; no se requiere lógica adicional.
    }

    /**
     * Muestra el mensaje directamente en la salida estándar.
     *
     * @param mensaje texto que se desea imprimir
     */
    @Override
    public void print(String mensaje) {
        System.out.println(mensaje);
    }

    /**
     * Lee el siguiente token de entrada desde la consola.
     *
     * @return cadena introducida por la persona usuaria
     */
    @Override
    public String read() {
        // Usamos next() para consumir el siguiente token introducido por el jugador.
        return scanner.next();
    }
}
