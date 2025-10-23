package com.sinensia.games.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sinensia.games.model.Partida;
import com.sinensia.games.model.Partida.Resultado;
import com.sinensia.games.repository.PartidaRepository;

@Service
public class PartidaService {

    private static final Logger log = LoggerFactory.getLogger(PartidaService.class);

    private final PartidaRepository partidaRepository;

    public PartidaService(PartidaRepository partidaRepository) {
        this.partidaRepository = partidaRepository;
    }

    @Transactional
    public Partida crearPartida(Partida partida) {
        return partidaRepository.save(partida);
    }

    @Transactional
    public void finalizarPartida(Long partidaId, Resultado resultado) {
        actualizarResultado(partidaId, resultado, true);
    }

    @Transactional
    public void abandonarPartida(Long partidaId) {
        actualizarResultado(partidaId, Resultado.ABANDONADA, true);
    }

    private void actualizarResultado(Long partidaId, Resultado resultado, boolean marcarFin) {
        if (partidaId == null) {
            return;
        }
        Optional<Partida> maybe = partidaRepository.findById(partidaId);
        if (maybe.isEmpty()) {
            log.warn("No se ha encontrado la partida {} para actualizar a {}", partidaId, resultado);
            return;
        }
        Partida partida = maybe.get();
        if (partida.getResultado() != Resultado.EN_CURSO) {
            return;
        }
        partida.setResultado(resultado);
        if (marcarFin) {
            partida.setFin(LocalDateTime.now());
        }
        partidaRepository.save(partida);
    }

    public Optional<Partida> buscarPorId(Long partidaId) {
        return partidaRepository.findById(partidaId);
    }
}
