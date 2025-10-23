package com.sinensia.games.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.sinensia.games.model.Jugador;
import com.sinensia.games.service.dto.PlayerRankingEntry;

public interface JugadorRepository extends JpaRepository<Jugador, Long> {

    Optional<Jugador> findByAliasIgnoreCase(String alias);

    @Query("""
            select new com.sinensia.games.service.dto.PlayerRankingEntry(
                j.id,
                j.alias,
                coalesce(sum(case when p.resultado = 'GANADA' then 1 else 0 end), 0),
                coalesce(sum(case when p.resultado = 'PERDIDA' then 1 else 0 end), 0),
                count(p),
                case
                    when coalesce(sum(case when p.resultado in ('GANADA', 'PERDIDA') then 1 else 0 end), 0) = 0
                        then 0d
                    else (1.0d * coalesce(sum(case when p.resultado = 'GANADA' then 1 else 0 end), 0)) /
                        coalesce(sum(case when p.resultado in ('GANADA', 'PERDIDA') then 1 else 0 end), 0)
                end
            )
            from Jugador j
            left join j.partidas p
            group by j.id, j.alias
            order by
                case
                    when coalesce(sum(case when p.resultado in ('GANADA', 'PERDIDA') then 1 else 0 end), 0) = 0
                        then 0d
                    else (1.0d * coalesce(sum(case when p.resultado = 'GANADA' then 1 else 0 end), 0)) /
                        coalesce(sum(case when p.resultado in ('GANADA', 'PERDIDA') then 1 else 0 end), 0)
                end desc,
                coalesce(sum(case when p.resultado = 'PERDIDA' then 1 else 0 end), 0),
                j.alias asc
            """)
    List<PlayerRankingEntry> fetchRanking();
}
