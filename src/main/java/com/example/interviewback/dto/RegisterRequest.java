package com.example.interviewback.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para el endpoint POST /api/auth/register.
 */
public class RegisterRequest {

    // @Size(max = 255) en nombre y email porque esas columnas son VARCHAR(255):
    // sin el tope, un valor más largo revienta en la base con un error de constraint
    // en vez de devolver un 400 legible.
    @NotBlank(message = "El nombre es requerido.")
    @Size(max = 255, message = "El nombre no puede superar los 255 caracteres.")
    private String name;

    @NotBlank(message = "El email es requerido.")
    @Email(message = "Ingresa un email válido.")
    @Size(max = 255, message = "El email no puede superar los 255 caracteres.")
    private String email;

    /**
     * Contraseña con reglas de seguridad.
     *
     * Esta es la validación que REALMENTE manda: el frontend replica las mismas
     * reglas solo para dar feedback inmediato, pero un cliente podría saltearlas.
     *
     * El @Pattern exige, en cualquier orden: al menos una minúscula, una mayúscula
     * y un carácter que no sea letra ni número.
     */
    @NotBlank(message = "La contraseña es requerida.")
    @Size(min = 8, max = 100, message = "La contraseña debe tener al menos 8 caracteres.")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[^A-Za-z0-9]).*$",
            message = "La contraseña debe incluir al menos una minúscula, una mayúscula y un carácter especial."
    )
    private String password;

    public RegisterRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
