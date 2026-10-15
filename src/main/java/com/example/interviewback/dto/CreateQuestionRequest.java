package com.example.interviewback.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO de entrada para crear una pregunta: POST /api/questions
 *
 * Capa: DTO
 * ---------
 * ¿Por qué un DTO y no la entidad Question directamente?
 *
 * Si el controller recibiera la entidad, un cliente podría enviar un `id` en el body
 * y JPA haría merge en vez de insert, sobrescribiendo una pregunta existente
 * (mass assignment). Con el DTO, el `id` lo asigna siempre la base de datos y
 * el cliente solo puede enviar los tres campos que le corresponden.
 *
 * Además, las validaciones (@NotBlank) se ejecutan antes de tocar la base,
 * así que un body incompleto devuelve 400 en vez de reventar con un error de
 * constraint de MySQL.
 */
public class CreateQuestionRequest {

    @NotBlank(message = "El texto de la pregunta es obligatorio")
    private String text;

    @NotBlank(message = "La categoría es obligatoria")
    private String category;

    @NotBlank(message = "La dificultad es obligatoria")
    private String difficulty;

    /** Constructor vacío requerido por Jackson para deserializar el JSON. */
    public CreateQuestionRequest() {
    }

    public CreateQuestionRequest(String text, String category, String difficulty) {
        this.text = text;
        this.category = category;
        this.difficulty = difficulty;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }
}
