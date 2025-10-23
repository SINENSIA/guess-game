package com.sinensia.games.service;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Credenciales inválidas. Revisa alias y contraseña.");
    }
}
