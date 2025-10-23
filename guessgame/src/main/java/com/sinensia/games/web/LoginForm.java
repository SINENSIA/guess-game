package com.sinensia.games.web;

import jakarta.validation.constraints.NotBlank;

public class LoginForm {

    @NotBlank(message = "Introduce tu alias.")
    private String alias;

    @NotBlank(message = "Introduce tu contraseña.")
    private String password;

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
