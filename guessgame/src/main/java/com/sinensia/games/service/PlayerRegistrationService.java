package com.sinensia.games.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sinensia.games.model.Jugador;
import com.sinensia.games.model.Partida;
import com.sinensia.games.repository.JugadorRepository;
import com.sinensia.games.web.PlayerForm;
import com.sinensia.games.web.LoginForm;

@Service
public class PlayerRegistrationService {

    private static final Logger log = LoggerFactory.getLogger(PlayerRegistrationService.class);

    private final JugadorRepository jugadorRepository;
    private final PartidaService partidaService;

    public PlayerRegistrationService(JugadorRepository jugadorRepository, PartidaService partidaService) {
        this.jugadorRepository = jugadorRepository;
        this.partidaService = partidaService;
    }

    @Transactional
    public RegistrationResult registrarJugador(PlayerForm form) {
        jugadorRepository.findByAliasIgnoreCase(form.getAlias())
                .ifPresent(existing -> {
                    throw new DuplicateAliasException(form.getAlias());
                });

        Jugador jugador = new Jugador();
        jugador.setAlias(form.getAlias().trim());
        jugador.setEdad(form.getEdad());
        jugador.setPassword(form.getPassword());

        Jugador saved = jugadorRepository.save(jugador);
        Partida partida = partidaService.crearPartida(new Partida(saved));

        log.info("Jugador registrado: alias={}, edad={}, partidaId={}", saved.getAlias(), saved.getEdad(),
                partida.getId());
        System.out.printf("Jugador registrado -> alias: %s, edad: %d, partidaId: %d%n", saved.getAlias(),
                saved.getEdad(), partida.getId());

        return new RegistrationResult(saved, partida);
    }

    @Transactional
    public Partida iniciarNuevaPartida(Long jugadorId) {
        Jugador jugador = jugadorRepository.findById(jugadorId)
                .orElseThrow(() -> new IllegalArgumentException("Jugador no encontrado: " + jugadorId));
        Partida partida = partidaService.crearPartida(new Partida(jugador));
        log.info("Nueva partida para jugador {} con id {}", jugador.getAlias(), partida.getId());
        System.out.printf("Nueva partida -> alias: %s, partidaId: %d%n", jugador.getAlias(), partida.getId());
        return partida;
    }

    @Transactional(readOnly = true)
    public Jugador autenticarJugador(LoginForm form) {
        Jugador jugador = jugadorRepository.findByAliasIgnoreCase(form.getAlias())
                .orElseThrow(InvalidCredentialsException::new);
        if (!jugador.getPassword().equals(form.getPassword())) {
            throw new InvalidCredentialsException();
        }
        log.info("Jugador autenticado: {}", jugador.getAlias());
        System.out.printf("Login jugador -> alias: %s%n", jugador.getAlias());
        return jugador;
    }

    public record RegistrationResult(Jugador jugador, Partida partida) {
    }
}
