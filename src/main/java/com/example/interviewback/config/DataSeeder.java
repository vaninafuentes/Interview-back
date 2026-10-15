package com.example.interviewback.config;

import com.example.interviewback.model.Question;
import com.example.interviewback.repository.QuestionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seed de datos iniciales para la tabla de preguntas.
 *
 * Capa: CONFIG
 * ------------
 * Implementa CommandLineRunner, una interfaz de Spring Boot que se ejecuta
 * automáticamente al arrancar la aplicación (después de que el contexto
 * se haya inicializado completamente).
 *
 * Lógica:
 *   1. Verifica si la tabla "questions" está vacía (count == 0)
 *   2. Si está vacía, inserta el banco de preguntas semilla
 *   3. Si ya tiene datos, no hace nada (idempotente)
 *
 * El banco semilla es MULTI-RUBRO: cubre atención al cliente, ventas,
 * administración, recursos humanos, marketing digital, logística,
 * contabilidad y tecnología. Las categorías son las mismas que se muestran
 * en pantalla al configurar un simulacro (y llegan al Perfil del usuario
 * como claves del mapa categoryAverages), por eso van en español.
 *
 * Esto es útil para desarrollo: cada vez que arrancas la app tenés
 * datos de prueba listos sin necesidad de scripts SQL manuales.
 */
@Component
// Los datos semilla son para desarrollo y demo: con el perfil "prod" no se ejecuta.
@Profile("!prod")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final QuestionRepository questionRepository;

    public DataSeeder(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @Override
    public void run(String... args) {
        // Solo insertar si la tabla está vacía para no duplicar datos
        if (questionRepository.count() > 0) {
            log.info("[DataSeeder] La tabla de preguntas ya tiene datos. Seed omitido.");
            return;
        }

        log.info("[DataSeeder] Insertando banco de preguntas semilla multi-rubro...");

        List<Question> questions = List.of(
                // --- Atención al Cliente ---
                new Question(
                        "¿Cómo manejás un cliente enojado que reclama por un producto defectuoso?",
                        "Atención al Cliente", "EASY"
                ),
                new Question(
                        "¿Qué hacés cuando un cliente te consulta por algo que no sabés y no hay nadie disponible para ayudarte en el momento?",
                        "Atención al Cliente", "MEDIUM"
                ),
                new Question(
                        "Contame una situación en la que tuviste que atender varios reclamos urgentes al mismo tiempo. ¿Cómo decidiste cuál atender primero?",
                        "Atención al Cliente", "HARD"
                ),

                // --- Ventas y Comercial ---
                new Question(
                        "Contame una vez que hayas tenido que convencer a un cliente indeciso.",
                        "Ventas y Comercial", "EASY"
                ),
                new Question(
                        "¿Cómo hacés el seguimiento de un cliente potencial que te dijo 'lo voy a pensar' y no volvió a contestar?",
                        "Ventas y Comercial", "MEDIUM"
                ),
                new Question(
                        "Estás a pocos días del cierre del mes y vas por debajo del objetivo de ventas. ¿Qué estrategia aplicás?",
                        "Ventas y Comercial", "HARD"
                ),

                // --- Administración ---
                new Question(
                        "¿Cómo organizás tus tareas cuando tenés varios vencimientos la misma semana?",
                        "Administración", "EASY"
                ),
                new Question(
                        "Detectás un error en una factura que ya fue enviada a un cliente. ¿Cómo lo resolvés?",
                        "Administración", "MEDIUM"
                ),

                // --- Recursos Humanos ---
                new Question(
                        "¿Qué preguntás en una entrevista de selección para evaluar si un candidato encaja con el puesto y con el equipo?",
                        "Recursos Humanos", "MEDIUM"
                ),
                new Question(
                        "Dos empleados del mismo equipo tienen un conflicto que está afectando el clima laboral. ¿Cómo lo abordás?",
                        "Recursos Humanos", "HARD"
                ),

                // --- Marketing Digital ---
                new Question(
                        "¿Qué métricas mirarías para saber si una campaña en redes sociales está funcionando?",
                        "Marketing Digital", "EASY"
                ),
                new Question(
                        "¿Cómo definís el público objetivo de una campaña para un producto nuevo?",
                        "Marketing Digital", "MEDIUM"
                ),
                new Question(
                        "Una campaña consumió todo el presupuesto del mes y no generó ventas. ¿Cómo analizás qué pasó y qué harías distinto?",
                        "Marketing Digital", "HARD"
                ),

                // --- Logística ---
                new Question(
                        "¿Cómo verificás que un pedido esté completo y en condiciones antes de despacharlo?",
                        "Logística", "EASY"
                ),
                new Question(
                        "El proveedor no entrega a tiempo una reposición de stock y ya tenés pedidos comprometidos. ¿Cómo lo resolvés?",
                        "Logística", "HARD"
                ),

                // --- Contabilidad y Finanzas ---
                new Question(
                        "¿Cómo registrás y archivás los comprobantes de gastos para que la rendición sea prolija?",
                        "Contabilidad y Finanzas", "EASY"
                ),
                new Question(
                        "¿Qué controles hacés antes de cerrar un balance mensual?",
                        "Contabilidad y Finanzas", "MEDIUM"
                ),
                new Question(
                        "Encontrás una diferencia entre el extracto bancario y tu registro contable. ¿Cómo investigás hasta encontrar la causa?",
                        "Contabilidad y Finanzas", "HARD"
                ),

                // --- Tecnología ---
                new Question(
                        "¿Cómo te organizás cuando tenés que trabajar en varios tickets o tareas técnicas al mismo tiempo?",
                        "Tecnología", "EASY"
                ),
                new Question(
                        "¿Cómo le explicarías un problema técnico complejo a una persona sin conocimientos técnicos?",
                        "Tecnología", "MEDIUM"
                ),
                new Question(
                        "Un servicio funciona bien en tu máquina pero falla en producción. ¿Cómo arrancás a investigarlo?",
                        "Tecnología", "MEDIUM"
                ),
                new Question(
                        "Tenés que diseñar una solución para un sistema que recibirá un pico grande de usuarios y no puede caerse. ¿Por dónde empezás?",
                        "Tecnología", "HARD"
                )
        );

        questionRepository.saveAll(questions);
        log.info("[DataSeeder] {} preguntas semilla insertadas correctamente", questions.size());
    }
}
