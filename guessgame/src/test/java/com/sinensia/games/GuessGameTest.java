package com.sinensia.games;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class GuessGameTest {

    /**
     * Implementación mínima de GameIO para pruebas: almacena entradas y salidas en memoria.
     */
    static class MockIO implements GameIO {
        private final List<String> inputs = new ArrayList<>();
        private final List<String> outputs = new ArrayList<>();
        private int readIndex = 0;

        MockIO(String... entradas) {
            for (String e : entradas) {
                inputs.add(e);
            }
        }

        @Override
        public void print(String mensaje) {
            outputs.add(mensaje);
        }

        @Override
        public String read() {
            return inputs.get(readIndex++);
        }

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
