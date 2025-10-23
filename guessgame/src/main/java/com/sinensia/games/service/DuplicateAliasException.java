package com.sinensia.games.service;

public class DuplicateAliasException extends RuntimeException {

    public DuplicateAliasException(String alias) {
        super("El alias '%s' ya está registrado.".formatted(alias));
    }
}
