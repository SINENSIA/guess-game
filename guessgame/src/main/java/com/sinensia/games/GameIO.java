package com.sinensia.games;

/**
 * Abstracción minimalista para desacoplar la interacción de entrada/salida.
 * Implementaciones típicas: consola real, mocks en tests, interfaces gráficas, etc.
 */
public interface GameIO {

    /**
     * Muestra un mensaje al usuario por el canal deseado.
     */
    void print(String mensaje);

    /**
     * Recupera la siguiente entrada del usuario.
     */
    String read();
}
