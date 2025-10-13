package com.sinensia.games;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Fachada simple sobre {@link ThreadLocalRandom} para no repetir lógica de
 * generación de números.
 * <p>
 * Evita el patrón Singleton tradicional; simplemente expone métodos estáticos
 * reutilizables.
 * </p>
 */
public final class GameRandom {

    private GameRandom() {
        // Utilidad estática.
    }

    public static int nextInt(int bound) {
        return ThreadLocalRandom.current().nextInt(bound);
    }

    public static int nextInt(int origin, int bound) {
        return ThreadLocalRandom.current().nextInt(origin, bound);
    }
}
