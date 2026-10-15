package com.example.interviewback.controller;

import com.example.interviewback.dto.CvAnalysisResponse;
import com.example.interviewback.model.User;
import com.example.interviewback.service.CvAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/**
 * Controlador REST para el análisis de CV en PDF.
 *
 * Endpoints:
 *   POST /api/cv/analyze → extrae rol, seniority, skills y resumen del CV con IA (requiere JWT)
 *
 * Formato de la petición: multipart/form-data con el campo "file" (el PDF).
 *
 * Respuestas posibles:
 *   200 → análisis generado correctamente
 *   400 → sin archivo, vacío, no es PDF, > 10 MB o PDF ilegible
 *   401 → sin token JWT válido
 *   422 → PDF sin texto extraíble (ej: escaneado como imagen)
 *   429 → rate limit de la IA
 *   502 → fallo del servicio de IA
 */
@RestController
@RequestMapping("/api/cv")
public class CvController {

    private static final Logger log = LoggerFactory.getLogger(CvController.class);

    private final CvAnalysisService cvAnalysisService;

    public CvController(CvAnalysisService cvAnalysisService) {
        this.cvAnalysisService = cvAnalysisService;
    }

    /**
     * POST /api/cv/analyze
     *
     * Recibe el CV en PDF, le extrae el texto y lo analiza con Groq AI
     * para devolver el puesto, seniority, skills y un resumen.
     *
     * @param file           PDF subido en el campo "file" del multipart
     *                       (required = false para poder responder 400 con un
     *                       mensaje claro en lugar de un error genérico)
     * @param authentication usuario autenticado via JWT
     * @return análisis del CV generado por la IA
     */
    @PostMapping("/analyze")
    public ResponseEntity<CvAnalysisResponse> analyzeCv(
            @RequestParam(value = "file", required = false) MultipartFile file,
            Authentication authentication
    ) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }

        log.info("[CvController] POST /api/cv/analyze | userId={} | archivo={}",
                user.getId(), file != null ? file.getOriginalFilename() : "null");

        CvAnalysisResponse response = cvAnalysisService.analyzeCv(file);
        return ResponseEntity.ok(response);
    }
}
