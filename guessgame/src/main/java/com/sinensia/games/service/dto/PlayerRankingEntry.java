package com.sinensia.games.service.dto;

public record PlayerRankingEntry(
        Long jugadorId,
        String alias,
        long ganadas,
        long perdidas,
        long total,
        double acierto) {

    public long pendientes() {
        return total - ganadas - perdidas;
    }

    public double porcentajeAcierto() {
        return acierto * 100.0d;
    }
}
