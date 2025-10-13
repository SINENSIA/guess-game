package com.sinensia.games;

import java.util.Scanner;

/**
 * Implementación de {@link GameIO} que usa la consola estándar.
 */
public class Consola implements GameIO {

    /**
     * Mantenemos un único {@link Scanner} asociado a {@code System.in}.
     * No lo cerramos explícitamente para no clausurar la entrada estándar de la JVM.
     */
    private final Scanner scanner = new Scanner(System.in);

    @Override
    public void print(String mensaje) {
        System.out.println(mensaje);
    }

    @Override
    public String read() {
        // Usamos next() para consumir el siguiente token introducido por el jugador.
        return scanner.next();
    }
}
