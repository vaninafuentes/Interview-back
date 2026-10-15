package com.example.interviewback.controller;

import com.example.interviewback.dto.CreateQuestionRequest;
import com.example.interviewback.model.Question;
import com.example.interviewback.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para el recurso "preguntas de entrevista".
 *
 * Capa: CONTROLLER
 * ----------------
 * La capa de controller es el punto de entrada de las peticiones HTTP.
 * Su única responsabilidad es:
 *   1. Recibir la petición HTTP y extraer los parámetros
 *   2. Delegar la lógica al Service
 *   3. Retornar la respuesta HTTP adecuada (código de estado + body)
 *
 * Anotaciones clave:
 *   - @RestController: combina @Controller + @ResponseBody.
 *     Todas las respuestas se serializan automáticamente a JSON.
 *   - @RequestMapping("/api/questions"): prefijo de URL para todos los endpoints.
 *     Todos los métodos de esta clase están bajo /api/questions.
 *
 * Endpoints:
 *   GET  /api/questions       → lista todas las preguntas
 *   GET  /api/questions/{id}  → obtiene una pregunta por ID
 *   POST /api/questions       → crea una pregunta nueva
 */
@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    /**
     * GET /api/questions
     * Retorna la lista completa de preguntas.
     * Código HTTP: 200 OK
     */
    @GetMapping
    public List<Question> getAllQuestions() {
        return questionService.getAllQuestions();
    }

    /**
     * GET /api/questions/{id}
     * Retorna una pregunta específica por su ID.
     * Código HTTP: 200 OK, o 404 Not Found si no existe (lo mapea GlobalExceptionHandler)
     *
     * @param id el ID de la pregunta (extraído de la URL)
     */
    @GetMapping("/{id}")
    public Question getQuestionById(@PathVariable Long id) {
        return questionService.getQuestionById(id);
    }

    /**
     * POST /api/questions
     * Crea una nueva pregunta.
     * Código HTTP: 201 Created, o 400 si el body no pasa la validación.
     *
     * IMPORTANTE: recibe un DTO y no la entidad Question directamente.
     * Si recibiera la entidad, un cliente podría mandar un `id` en el body y JPA
     * haría merge, sobrescribiendo una pregunta existente (mass assignment).
     * Con el DTO, el `id` lo asigna siempre la base de datos.
     *
     * @param request datos de la pregunta a crear (texto, categoría, dificultad)
     * @return la pregunta creada con su ID asignado
     */
    @PostMapping
    public ResponseEntity<Question> createQuestion(@Valid @RequestBody CreateQuestionRequest request) {
        Question created = questionService.createQuestion(
                new Question(request.getText(), request.getCategory(), request.getDifficulty()));
        // Retornamos 201 CREATED en vez del 200 por defecto,
        // siguiendo las convenciones REST para creación de recursos.
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
