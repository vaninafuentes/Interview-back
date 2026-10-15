package com.example.interviewback.service;

import com.example.interviewback.dto.CvAnalysisResponse;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

/**
 * Servicio de análisis de CV en PDF.
 *
 * Capa: SERVICE
 * -------------
 * Flujo del análisis:
 *   1. Valida el archivo recibido (presente, no vacío, PDF y ≤ 10 MB)
 *   2. Extrae el texto plano del PDF con Apache PDFBox 3.x
 *   3. Verifica que el PDF tenga texto extraíble (los escaneados no lo tienen)
 *   4. Delega el análisis del texto a GroqService (IA)
 *
 * Códigos de error que puede lanzar:
 *   400 → archivo ausente, vacío, no PDF, > 10 MB o PDF ilegible/dañado
 *   422 → PDF válido pero sin texto extraíble (ej: escaneado como imagen)
 *   429 → rate limit de la IA (lo lanza GroqService)
 *   502 → fallo del servicio de IA (lo lanza GroqService)
 */
@Service
public class CvAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(CvAnalysisService.class);

    /** Tamaño máximo aceptado para el PDF (10 MB, igual que el límite de multipart). */
    private static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024;

    /** Mínimo de caracteres extraídos para considerar que el PDF tiene texto útil. */
    private static final int MIN_TEXT_LENGTH = 50;

    private static final String PDF_CONTENT_TYPE = "application/pdf";
    private static final String PDF_EXTENSION = ".pdf";

    private final GroqService groqService;

    public CvAnalysisService(GroqService groqService) {
        this.groqService = groqService;
    }

    /**
     * Analiza un CV en PDF y devuelve rol, seniority, skills y resumen.
     *
     * @param file archivo PDF subido en el campo "file" del multipart
     * @return análisis estructurado generado por la IA
     */
    public CvAnalysisResponse analyzeCv(MultipartFile file) {
        validateFile(file);

        log.info("[CvAnalysisService] Analizando CV | archivo={} | bytes={}",
                file.getOriginalFilename(), file.getSize());

        String cvText = extractText(file);

        if (cvText.length() < MIN_TEXT_LENGTH) {
            log.warn("[CvAnalysisService] PDF sin texto extraíble | archivo={} | chars={}",
                    file.getOriginalFilename(), cvText.length());
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "No se pudo extraer texto del PDF. Verificá que no sea un documento escaneado como imagen.");
        }

        log.info("[CvAnalysisService] Texto extraído del CV | chars={}", cvText.length());

        CvAnalysisResponse response = groqService.analyzeCv(cvText);

        log.info("[CvAnalysisService] Análisis completado | role={} | seniority={}",
                response.getRole(), response.getSeniority());

        return response;
    }

    // -------------------------------------------------------------------------
    // Validaciones del archivo
    // -------------------------------------------------------------------------

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se recibió ningún archivo. Adjuntá tu CV en formato PDF en el campo 'file'.");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El archivo supera el tamaño máximo permitido de 10 MB.");
        }

        if (!isPdf(file)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El archivo debe estar en formato PDF.");
        }
    }

    /**
     * Acepta el archivo si el content-type es application/pdf o si la extensión es .pdf.
     * Algunos clientes (ej: React Native) envían "application/octet-stream" en lugar
     * del content-type real, por eso también miramos el nombre del archivo.
     */
    private boolean isPdf(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null && contentType.equalsIgnoreCase(PDF_CONTENT_TYPE)) {
            return true;
        }

        String filename = file.getOriginalFilename();
        return filename != null && filename.toLowerCase().endsWith(PDF_EXTENSION);
    }

    // -------------------------------------------------------------------------
    // Extracción de texto (Apache PDFBox 3.x)
    // -------------------------------------------------------------------------

    /**
     * Extrae el texto plano del PDF usando la API de PDFBox 3.x.
     *
     * Si el PDF está dañado o protegido con contraseña, PDFBox lanza IOException
     * (InvalidPasswordException es subclase de IOException) y respondemos 400.
     */
    private String extractText(MultipartFile file) {
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            return text == null ? "" : text.trim();

        } catch (IOException e) {
            log.warn("[CvAnalysisService] No se pudo leer el PDF '{}': {}",
                    file.getOriginalFilename(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se pudo leer el PDF. Verificá que el archivo no esté dañado ni protegido con contraseña.");
        }
    }
}
