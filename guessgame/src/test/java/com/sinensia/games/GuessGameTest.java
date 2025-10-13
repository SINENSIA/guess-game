package com.sinensia.games;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Batería de pruebas de aceptación para {@link AppGame} y {@link GuessGame}.
 * <p>
 * Cubre dos escenarios fundamentales:
 * <ul>
 * <li>Flujo de éxito tras uno o más intentos fallidos.</li>
 * <li>Gestión de entradas inválidas y comunicación del mensaje
 * correspondiente.</li>
 * </ul>
 * Al usar un <em>mock manual</em> (objeto falso) de {@link GameIO}, aislamos la
 * lógica de negocio
 * del canal real de entrada/salida.
 * </p>
 *
 * @author sinensia
 * @version 0.0.3
 */
class GuessGameTest {

    /**
     * Mock sencillo de {@link GameIO} que guarda entradas y salidas en memoria.
     * Ilustra el patrón <strong>Test Double</strong>, concretamente la variante
     * Mock,
     * para observar las interacciones entre la aplicación y la capa de E/S.
     */
    static class MockIO implements GameIO {
        private final List<String> inputs = new ArrayList<>();
        private final List<String> outputs = new ArrayList<>();
        private int readIndex = 0;

        /**
         * Carga las entradas simuladas que leerá {@link AppGame}.
         *
         * @param entradas secuencia de tokens que se devolverán en orden
         */
        MockIO(String... entradas) {
            for (String e : entradas) {
                inputs.add(e);
            }
        }

        /**
         * Registra los mensajes emitidos por la aplicación para que los asserts los
         * inspeccionen.
         *
         * @param mensaje texto que la aplicación desea mostrar
         */
        @Override
        public void print(String mensaje) {
            outputs.add(mensaje);
        }

        /**
         * Devuelve el siguiente token simulado, reproduciendo la interacción
         * usuario-aplicación.
         *
         * @return entrada suministrada en el constructor
         */
        @Override
        public String read() {
            return inputs.get(readIndex++);
        }

        /**
         * Exposición de los mensajes capturados durante la prueba.
         *
         * @return lista de mensajes emitidos por la aplicación
         */
        List<String> getOutputs() {
            return outputs;
        }
    }

    @Test
    void testJuegoAciertaEnElSegundoIntento() {
        // Arrange: el número secreto será 7 y el jugador fallará antes de acertar.
        GuessGame juego = new GuessGame(3, 7);
        MockIO io = new MockIO("4", "7");
        AppGame app = new AppGame(juego, io);

        // Act
        app.start();

        // Assert: verificamos que se generó el mensaje de acierto.
        assertTrue(io.getOutputs().stream().anyMatch(s -> s.contains("Correcto")));
    }

    @Test
    void testEntradaInvalida() {
        // Arrange: solo hay una vida y la entrada no es numérica.
        GuessGame juego = new GuessGame(1, 5);
        MockIO io = new MockIO("abc", "5"); // tras la entrada inválida el jugador vuelve a intentar.
        AppGame app = new AppGame(juego, io);

        // Act
        app.start();

        // Assert: comprobamos que se informó la invalidez.
        assertTrue(io.getOutputs().stream().anyMatch(s -> s.contains("inválida")));
    }
}
