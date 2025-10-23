package com.sinensia.games.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sinensia.games.model.Partida;

public interface PartidaRepository extends JpaRepository<Partida, Long> {

    List<Partida> findByJugadorIdOrderByInicioDesc(Long jugadorId);
}
