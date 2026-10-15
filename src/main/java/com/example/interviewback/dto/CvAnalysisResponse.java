package com.example.interviewback.dto;

import java.util.List;

/**
 * DTO de respuesta con el análisis del CV generado por Groq AI.
 *
 * Capa: DTO
 * ---------
 * Representa el JSON que el backend devuelve al frontend:
 *   POST /api/cv/analyze → 200 OK
 *
 * {
 *   "role": "Desarrollador Backend",
 *   "seniority": "Semi Senior",
 *   "skills": ["Java", "Spring Boot", "MySQL"],
 *   "summary": "Profesional con 3 años de experiencia en desarrollo de APIs REST."
 * }
 *
 * Campos:
 *   - role:      puesto o perfil profesional principal detectado en el CV
 *   - seniority: nivel de experiencia inferido (Junior, Semi Senior, Senior, No especificado)
 *   - skills:    habilidades técnicas concretas mencionadas en el CV
 *   - summary:   resumen profesional breve armado a partir del texto del CV
 */
public class CvAnalysisResponse {

    private String role;
    private String seniority;
    private List<String> skills;
    private String summary;

    public CvAnalysisResponse() {}

    public CvAnalysisResponse(String role, String seniority, List<String> skills, String summary) {
        this.role = role;
        this.seniority = seniority;
        this.skills = skills;
        this.summary = summary;
    }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getSeniority() { return seniority; }
    public void setSeniority(String seniority) { this.seniority = seniority; }

    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
}
