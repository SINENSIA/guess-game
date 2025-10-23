package com.sinensia.games.web;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Formulario de intentos en la interfaz web.
 */
public class GuessForm {

    @NotBlank(message = "Introduce un numero antes de enviar tu intento.")
    @Pattern(regexp = "\\d{1,3}", message = "El numero debe ser un entero positivo.")
    @Min(1)
    @Max(10)
    private String guess;

    public String getGuess() {
        return guess;
    }

    public void setGuess(String guess) {
        this.guess = guess;
    }
}
