package com.sinensia.games.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Formulario de alta de jugadores.
 */
public class PlayerForm {

    @NotBlank(message = "Indica un alias para unirte a la partida.")
    @Size(max = 40, message = "El alias no puede superar los 40 caracteres.")
    private String alias;

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
