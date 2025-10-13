package com.sinensia.games;

/**
 * Abstracción minimalista para desacoplar la interacción de entrada/salida.
 * <p>
 * Representa la cara del patrón <strong>Strategy</strong> que consume {@link AppGame}: cada implementación
 * define su forma de mostrar mensajes y leer entradas sin alterar la lógica del juego.
 * </p>
 *
 * @author sinensia
 * @version 0.0.2
 */
public interface GameIO {

    /**
     * Muestra un mensaje al usuario por el canal deseado.
     *
     * @param mensaje texto que se quiere mostrar
     */
    void print(String mensaje);

    /**
     * Recupera la siguiente entrada del usuario.
     *
     * @return texto capturado desde el canal asociado
     */
    String read();
}
