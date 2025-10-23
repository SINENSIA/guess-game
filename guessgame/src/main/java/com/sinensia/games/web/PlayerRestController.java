package com.sinensia.games.web;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sinensia.games.ConcurrentGame;
import com.sinensia.games.ConcurrentGame.PlayerView;
import com.sinensia.games.model.Partida;
import com.sinensia.games.service.DuplicateAliasException;
import com.sinensia.games.service.InvalidCredentialsException;
import com.sinensia.games.service.PlayerRegistrationService;
import com.sinensia.games.service.PlayerRegistrationService.RegistrationResult;
import com.sinensia.games.service.RankingService;
import com.sinensia.games.service.dto.PlayerRankingEntry;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/players")
public class PlayerRestController {

    private final PlayerRegistrationService registrationService;
    private final ConcurrentGame concurrentGame;
    private final RankingService rankingService;

    public PlayerRestController(PlayerRegistrationService registrationService, ConcurrentGame concurrentGame,
            RankingService rankingService) {
        this.registrationService = registrationService;
        this.concurrentGame = concurrentGame;
        this.rankingService = rankingService;
    }

    @PostMapping
    public ResponseEntity<?> registerPlayer(@Valid @RequestBody PlayerForm form, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }
        RegistrationResult registration = registrationService.registrarJugador(form);
        PlayerView view = concurrentGame.registerPlayer(registration.jugador().getAlias());
        Partida partida = registration.partida();
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "playerId", view.id(),
                "alias", view.alias(),
                "jugadorId", registration.jugador().getId(),
                "partidaId", partida.getId(),
                "vidas", view.lives(),
                "mensaje", "Jugador creado correctamente"));
    }

    @ExceptionHandler(DuplicateAliasException.class)
    public ResponseEntity<?> handleDuplicateAlias(DuplicateAliasException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<?> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", ex.getMessage()));
    }

    @GetMapping("/ranking")
    public List<PlayerRankingEntry> obtenerRanking() {
        return rankingService.obtenerRanking();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginForm form, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }
        var jugador = registrationService.autenticarJugador(form);
        PlayerView view = concurrentGame.registerPlayer(jugador.getAlias());
        Partida partida = registrationService.iniciarNuevaPartida(jugador.getId());
        return ResponseEntity.ok(Map.of(
                "playerId", view.id(),
                "alias", view.alias(),
                "jugadorId", jugador.getId(),
                "partidaId", partida.getId(),
                "vidas", view.lives(),
                "mensaje", "Login exitoso. Nueva partida creada."));
    }
}
