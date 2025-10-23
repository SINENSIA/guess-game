package com.sinensia.games.web;

import com.sinensia.games.web.validation.PasswordMatches;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Formulario de alta de jugadores.
 */
@PasswordMatches
public class PlayerForm {

    @NotBlank(message = "Indica un alias para unirte a la partida.")
    @Size(max = 40, message = "El alias no puede superar los 40 caracteres.")
    private String alias;

    @NotNull(message = "Indica tu edad.")
    @Min(value = 18, message = "Debes ser mayor de edad para jugar.")
    private Integer edad;

    @NotBlank(message = "La contraseña es obligatoria.")
    @Size(min = 6, max = 60, message = "La contraseña debe tener entre 6 y 60 caracteres.")
    private String password;

    @NotBlank(message = "Confirma la contraseña.")
    private String passwordConfirm;

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Integer getEdad() {
        return edad;
    }

    public void setEdad(Integer edad) {
        this.edad = edad;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordConfirm() {
        return passwordConfirm;
    }

    public void setPasswordConfirm(String passwordConfirm) {
        this.passwordConfirm = passwordConfirm;
    }
}
