package com.sinensia.games.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sinensia.games.repository.JugadorRepository;
import com.sinensia.games.service.dto.PlayerRankingEntry;

@Service
public class RankingService {

    private final JugadorRepository jugadorRepository;

    public RankingService(JugadorRepository jugadorRepository) {
        this.jugadorRepository = jugadorRepository;
    }

    public List<PlayerRankingEntry> obtenerRanking() {
        return jugadorRepository.fetchRanking();
    }
}
