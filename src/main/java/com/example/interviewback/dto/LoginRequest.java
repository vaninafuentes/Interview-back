package com.example.interviewback.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO de entrada para el endpoint POST /api/auth/login.
 *
 * @NotBlank → no puede ser null, vacío ni solo espacios
 * @Email    → valida formato de email
 */
public class LoginRequest {

    @NotBlank(message = "El email es requerido.")
    @Email(message = "Ingresa un email válido.")
    private String email;

    // A propósito NO se valida el formato ni el largo de la contraseña acá:
    // un usuario registrado antes de las reglas de contraseña segura puede tener
    // una más corta, y rechazarlo en el login lo dejaría afuera de su propia cuenta.
    // La verificación real es contra el hash guardado (BCrypt), en AuthService.
    @NotBlank(message = "La contraseña es requerida.")
    private String password;

    public LoginRequest() {}

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
