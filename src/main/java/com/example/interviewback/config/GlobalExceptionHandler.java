package com.example.interviewback.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manejador global de excepciones.
 *
 * Captura cualquier excepción no manejada en los controllers y:
 *   1. La loguea con el stack trace completo (para debug)
 *   2. Devuelve un JSON de error legible al frontend
 *
 * Sin esto, Spring Boot devuelve un HTML de error genérico
 * que Axios no puede parsear, causando "Network Error" en el frontend.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maneja errores de validación (@Valid / @NotNull / @NotBlank).
     * Ejemplo: questionId null, userAnswer vacío.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationError(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));

        log.warn("[GlobalExceptionHandler] Validation error: {}", errors);

        return ResponseEntity.badRequest().body(Map.of(
                "error", "Validación fallida",
                "message", errors
        ));
    }

    /**
     * Maneja errores de negocio (404 pregunta no encontrada, 401, etc.).
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException ex) {
        log.warn("[GlobalExceptionHandler] ResponseStatusException: {} - {}", ex.getStatusCode(), ex.getReason());

        return ResponseEntity.status(ex.getStatusCode()).body(Map.of(
                "error", ex.getReason() != null ? ex.getReason() : "Error",
                "message", ex.getReason() != null ? ex.getReason() : "Error en la petición"
        ));
    }

    /**
     * Maneja archivos que superan el límite configurado en multipart
     * (spring.servlet.multipart.max-file-size = 10MB, ver POST /api/cv/analyze).
     *
     * Tomcat rechaza la petición antes de llegar al controller, así que sin este
     * handler el catch-all respondería 500 en lugar del 400 que espera el frontend.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        log.warn("[GlobalExceptionHandler] Archivo demasiado grande: {}", ex.getMessage());

        return ResponseEntity.badRequest().body(Map.of(
                "error", "Archivo demasiado grande",
                "message", "El archivo supera el tamaño máximo permitido de 10 MB."
        ));
    }

    /**
     * Maneja errores genéricos de multipart (petición mal formada, parte faltante,
     * límite de tamaño excedido, etc.) devolviendo 400 en lugar de 500.
     */
    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<Map<String, Object>> handleMultipartException(MultipartException ex) {
        log.warn("[GlobalExceptionHandler] Error de multipart: {}", ex.getMessage());

        return ResponseEntity.badRequest().body(Map.of(
                "error", "Petición de archivo inválida",
                "message", "No se pudo procesar el archivo enviado. Verificá el formulario e intentá nuevamente."
        ));
    }

    /**
     * Body JSON malformado o ilegible (por ejemplo `{` sin cerrar).
     *
     * Sin este handler la excepción cae al catch-all y devuelve 500,
     * cuando lo correcto es 400.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadableBody(HttpMessageNotReadableException ex) {
        log.warn("[GlobalExceptionHandler] Body ilegible: {}", ex.getMessage());

        return ResponseEntity.badRequest().body(Map.of(
                "error", "Petición inválida",
                "message", "El cuerpo de la petición no es un JSON válido."
        ));
    }

    /**
     * Parámetro de ruta o query con el tipo equivocado.
     * Ejemplo: GET /api/questions/abc cuando se espera un Long.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("[GlobalExceptionHandler] Tipo de parámetro inválido: {} = {}", ex.getName(), ex.getValue());

        return ResponseEntity.badRequest().body(Map.of(
                "error", "Parámetro inválido",
                "message", "El parámetro '" + ex.getName() + "' tiene un formato inválido."
        ));
    }

    /**
     * Ruta inexistente. Sin este handler, el catch-all devolvería 500 en vez de 404.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoResourceFound(NoResourceFoundException ex) {
        log.warn("[GlobalExceptionHandler] Ruta no encontrada: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "error", "Recurso no encontrado",
                "message", "La ruta solicitada no existe."
        ));
    }

    /**
     * Método HTTP no soportado para una ruta existente.
     * Ejemplo: DELETE /api/questions/1 cuando solo hay GET y POST.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        log.warn("[GlobalExceptionHandler] Método HTTP no soportado: {}", ex.getMethod());

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(Map.of(
                "error", "Método no permitido",
                "message", "El método HTTP " + ex.getMethod() + " no está permitido en esta ruta."
        ));
    }

    /**
     * Violación de una restricción de la base de datos (unique, not null, tamaño de columna).
     * El caso más frecuente es un email duplicado al registrarse.
     *
     * Se devuelve un mensaje GENÉRICO a propósito: el mensaje crudo de Hibernate/MySQL
     * puede incluir nombres de tablas, columnas y constraints.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.error("[GlobalExceptionHandler] Violación de integridad de datos", ex);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "error", "Conflicto de datos",
                "message", "Los datos enviados entran en conflicto con un registro existente."
        ));
    }

    /**
     * Catch-all: cualquier excepción no manejada.
     * Loguea el stack trace completo para facilitar el debug.
     *
     * IMPORTANTE: al cliente se le devuelve un mensaje GENÉRICO, nunca `ex.getMessage()`.
     * El mensaje crudo de una excepción no contemplada puede filtrar detalles internos
     * (nombres de tablas, constraints, rutas del servidor). El detalle queda en el log.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        // Log completo con stack trace para que aparezca en la consola de IntelliJ
        log.error("[GlobalExceptionHandler] Error interno no manejado: {}", ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Error interno del servidor",
                "message", "Ocurrió un error inesperado. Intentá nuevamente más tarde."
        ));
    }
}
