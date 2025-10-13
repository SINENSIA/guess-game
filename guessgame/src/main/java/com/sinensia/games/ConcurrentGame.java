package com.sinensia.games;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

/**
 * Orquesta partidas compartidas entre múltiples jugadores procedentes de la
 * Web.
 * <p>
 * Sustituye la antigua demo de hilos manuales por una fachada preparada para
 * Spring Boot. Mantiene un único {@link GuessGame} como núcleo thread-safe,
 * asigna vidas independientes a cada jugador y compone respuestas para la capa
 * MVC. Gracias a las estructuras concurrentes y a la sincronización interna, la
 * clase es segura para ser usada desde múltiples peticiones simultáneas.
 * </p>
 */
@Service
public class ConcurrentGame {

    private static final int VIDAS_POR_JUGADOR = 3;
    private static final int MIN = 1;
    private static final int MAX = 10;

    private final Map<String, PlayerState> players = new ConcurrentHashMap<>();

    private volatile GuessGame game;
    private volatile String winnerId;
    private volatile int currentSecret;

    public ConcurrentGame() {
        startNewGame();
    }

    /**
     * Registra un jugador nuevo o devuelve el existente si el alias coincide.
     *
     * @param alias nombre mostrable
     * @return vista inmutable del jugador
     */
    public synchronized PlayerView registerPlayer(String alias) {
        Objects.requireNonNull(alias, "alias no puede ser null");
        String normalized = alias.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("El alias no puede quedar vacío");
        }

        Optional<PlayerState> existing = players.values().stream()
                .filter(p -> p.aliasEquals(normalized))
                .findFirst();
        if (existing.isPresent()) {
            return existing.get().view();
        }

        PlayerState state = new PlayerState(UUID.randomUUID().toString(), normalized, VIDAS_POR_JUGADOR);
        players.put(state.id(), state);
        return state.view();
    }

    /**
     * Recupera el estado actual de un jugador registrado.
     *
     * @param playerId identificador del jugador
     * @return vista inmutable
     */
    public synchronized PlayerView getPlayer(String playerId) {
        return findPlayerOrThrow(playerId).view();
    }

    /**
     * Procesa un intento y devuelve información para refrescar la interfaz.
     *
     * @param playerId identificador del jugador
     * @param guess    número introducido
     * @return resultado del intento
     */
    public synchronized GuessResult submitGuess(String playerId, String guess) {
        PlayerState player = findPlayerOrThrow(playerId);

        if (player.isEliminated()) {
            return buildResult(player, GuessGame.Estado.FAILED,
                    "Ya no te quedan vidas. Espera a que reinicien la partida.");
        }

        GuessGame.Estado estado = game.verificarInput(guess);
        String feedback = switch (estado) {
            case SUCCESS -> {
                winnerId = player.id();
                player.markWinner();
                yield "¡Correcto! Has descubierto el número " + currentSecret + ".";
            }
            case FAILED -> {
                player.consumeLife();
                yield "No es ese... Te quedan " + player.lives() + " vidas.";
            }
            case INVALID -> "Entrada inválida. Introduce únicamente números enteros.";
            case OUTOFRANGE -> "Fuera de rango. Elige un valor entre " + MIN + " y " + MAX + ".";
            case ERROR -> "Se produjo un error inesperado. Inténtalo nuevamente más tarde.";
            case ENDED -> {
                String winner = winnerAlias().orElse("otro jugador");
                yield "La partida ya concluyó. Ganó " + winner + ".";
            }
        };

        return buildResult(player, estado, feedback);
    }

    /**
     * Reinicia la partida manteniendo a los jugadores conectados.
     */
    public synchronized void resetGame() {
        startNewGame();
        players.values().forEach(p -> p.reset(VIDAS_POR_JUGADOR));
    }

    /**
     * Devuelve un resumen del tablero para la vista.
     *
     * @return estado actual de la partida
     */
    public synchronized GameSummary snapshot() {
        List<PlayerView> board = players.values().stream()
                .sorted(Comparator.comparing(PlayerState::alias, String.CASE_INSENSITIVE_ORDER))
                .map(PlayerState::view)
                .toList();
        boolean finished = isFinished();
        return new GameSummary(board, finished, winnerAlias().orElse(null),
                finished ? currentSecret : null, VIDAS_POR_JUGADOR, allEliminated());
    }

    private GuessResult buildResult(PlayerState player, GuessGame.Estado estado, String feedback) {
        return new GuessResult(player.view(), estado, feedback, isFinished(),
                winnerAlias().orElse(null), shouldRevealSecret() ? currentSecret : null);
    }

    private boolean shouldRevealSecret() {
        return isFinished();
    }

    private boolean isFinished() {
        return game.isTerminado() || allEliminated();
    }

    private boolean allEliminated() {
        return !players.isEmpty() && players.values().stream().allMatch(PlayerState::isEliminated);
    }

    private Optional<String> winnerAlias() {
        if (winnerId == null) {
            return Optional.empty();
        }
        PlayerState winner = players.get(winnerId);
        return Optional.ofNullable(winner).map(PlayerState::alias);
    }

    private PlayerState findPlayerOrThrow(String playerId) {
        PlayerState state = players.get(playerId);
        if (state == null) {
            throw new IllegalArgumentException("Jugador no registrado: " + playerId);
        }
        return state;
    }

    private void startNewGame() {
        this.currentSecret = GameRandom.nextInt(MIN, MAX + 1);
        this.game = new GuessGame(VIDAS_POR_JUGADOR, currentSecret);
        this.winnerId = null;
    }

    /**
     * Estado inmutable expuesto a la capa web.
     */
    public record PlayerView(String id, String alias, int lives, boolean eliminated, boolean winner) {
    }

    /**
     * Resultado devuelto tras cada jugada.
     */
    public record GuessResult(PlayerView player, GuessGame.Estado estado, String message,
            boolean finished, String winnerAlias, Integer secret) {
    }

    /**
     * Información agregada del tablero y estado general.
     */
    public record GameSummary(List<PlayerView> players, boolean finished, String winnerAlias,
            Integer secret, int maxLives, boolean everyoneLost) {
    }

    private static final class PlayerState {
        private final String id;
        private final String alias;
        private int lives;
        private boolean eliminated;
        private boolean winner;

        private PlayerState(String id, String alias, int lives) {
            this.id = id;
            this.alias = alias;
            this.lives = lives;
        }

        private String id() {
            return id;
        }

        private String alias() {
            return alias;
        }

        private boolean aliasEquals(String other) {
            return alias.equalsIgnoreCase(other);
        }

        private int lives() {
            return lives;
        }

        private boolean isEliminated() {
            return eliminated;
        }

        private void consumeLife() {
            if (lives > 0) {
                lives--;
                if (lives == 0) {
                    eliminated = true;
                }
            }
        }

        private void reset(int newLives) {
            this.lives = newLives;
            this.eliminated = false;
            this.winner = false;
        }

        private void markWinner() {
            this.winner = true;
        }

        private PlayerView view() {
            return new PlayerView(id, alias, lives, eliminated, winner);
        }
    }
}
