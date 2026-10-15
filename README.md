# Interview-back

API REST de **Interview**, una app para practicar entrevistas de trabajo con IA.
Spring Boot + MySQL + Groq (LLM).

Este repositorio es el backend. El frontend (React Native + Expo) está en `Interview-front`.

---

## 🧰 Stack

| Tecnología | Versión | Para qué |
|---|---|---|
| Java | 26 (toolchain) → bytecode 21 | Lenguaje |
| Spring Boot | 3.5.4 | Framework (Web, Data JPA, Security, Validation) |
| JJWT | 0.12.6 | Generación y validación de tokens JWT (HS256) |
| MySQL | 8 | Persistencia |
| Groq | API | Generación de preguntas, evaluación de respuestas y análisis de CV |
| Apache PDFBox | 3.0.3 | Extracción de texto de los CVs en PDF |
| Gradle (Kotlin DSL) | — | Build |

---

## 🏗️ Arquitectura

Separación en capas: el controller recibe y delega, el service tiene la lógica,
el repository accede a la base.

```
src/main/java/com/example/interviewback/
├── config/       → AppConfig, CorsConfig, SecurityConfig,
│                   DataSeeder (datos iniciales) y GlobalExceptionHandler
├── controller/   → AuthController, QuestionController,
│                   InterviewController, CvController
├── dto/          → Objetos de entrada y salida de la API
├── model/        → Entidades JPA: User, Question, InterviewSession
├── repository/   → Interfaces de Spring Data JPA
├── security/     → JwtUtil (firma y valida) y JwtAuthFilter (filtro por request)
└── service/      → AuthService, QuestionService, InterviewService,
                    CvAnalysisService y GroqService (cliente de la IA)
```

**Decisiones de diseño:**

- **Las entidades nunca se exponen.** Todas las respuestas salen por DTOs hechos a
  mano, así `User.password` no puede filtrarse en un JSON.
- **Sin SQL construido a mano.** No hay `@Query` ni native queries: todo son
  derived queries de Spring Data con parámetros vinculados.
- **La identidad sale siempre del token**, nunca del body del request. No hay forma
  de pedir los datos de otro usuario.
- **Contrato de error uniforme:** todas las respuestas de error tienen la forma
  `{ "error": "...", "message": "..." }`, que es la que sabe leer el interceptor
  de Axios del frontend.

---

## 🔌 Endpoints

Todos bajo `/api`. Los que requieren JWT esperan el header `Authorization: Bearer <token>`.

### Autenticación (públicos)

| Método | Ruta | Qué hace |
|---|---|---|
| POST | `/api/auth/register` | Registra un usuario y devuelve el JWT (201, 409 si el email ya existe) |
| POST | `/api/auth/login` | Autentica y devuelve el JWT (200, 401 si las credenciales fallan) |

### Banco de preguntas

| Método | Ruta | Qué hace |
|---|---|---|
| GET | `/api/questions` | Lista todas las preguntas |
| GET | `/api/questions/{id}` | Una pregunta por ID (404 si no existe) |
| POST | `/api/questions` | Crea una pregunta (recibe un DTO validado) |

### Simulacros de entrevista

| Método | Ruta | Qué hace |
|---|---|---|
| POST | `/api/interview/generate` | Genera N preguntas con IA sobre un tema y dificultad |
| POST | `/api/interview/evaluate` | Evalúa una respuesta con IA y persiste la sesión |
| POST | `/api/interview/hint` | Devuelve una pista sin revelar la respuesta |
| GET | `/api/interview/history` | Historial completo del usuario autenticado |
| GET | `/api/interview/stats` | Racha, promedio, mejor nota y promedio por categoría |

### Análisis de CV

| Método | Ruta | Qué hace |
|---|---|---|
| POST | `/api/cv/analyze` | Recibe un PDF (`multipart`, campo `file`, máx. 10 MB) y devuelve rol, seniority, skills y resumen extraídos por IA |

---

## 🚀 Cómo ejecutar

```bash
# Levantar MySQL (el backend espera el puerto 3307)
docker run -d --name interview-mysql -p 3307:3306 \
  -e MYSQL_ROOT_PASSWORD=root \
  mysql:8

# Correr el backend
./gradlew bootRun
```

Queda escuchando en `http://localhost:8080`.

En el primer arranque, Hibernate crea las tablas (`ddl-auto=update`) y el
`DataSeeder` inserta el banco de preguntas inicial: **22 preguntas de 8 rubros**
(atención al cliente, ventas, administración, RRHH, marketing, logística,
contabilidad y tecnología). El seeder es idempotente y no corre con el perfil `prod`.

---

## ⚙️ Configuración

Todo vive en `src/main/resources/application.properties`.

### API key de Groq (necesaria para las funciones de IA)

La generación de preguntas, la evaluación y el análisis de CV usan
[Groq](https://console.groq.com), que tiene plan gratuito.

1. Sacá una key en **https://console.groq.com/keys**
2. Definila como variable de entorno `GROQ_API_KEY`:

**IntelliJ IDEA** — recomendado, queda guardada entre reinicios:
```
Run → Edit Configurations… → Environment variables → GROQ_API_KEY=gsk_...
```

**PowerShell** — ojo, solo dura esa terminal:
```powershell
$env:GROQ_API_KEY = "gsk_tu_api_key_aqui"
```

> ⚠️ **La key nunca se commitea.** Se lee con `${GROQ_API_KEY:NOT_CONFIGURED}`,
> así que si no está definida el backend **arranca igual**: el resto de la API
> funciona y las funciones de IA devuelven un error claro, en vez de fallar de
> forma confusa o inventar datos.

### Variables opcionales

| Propiedad | Default | Para qué |
|---|---|---|
| `JWT_SECRET` | valor de desarrollo | Firma de los tokens. **Cambiarlo invalida los tokens ya emitidos.** En producción hay que definirlo con un valor propio. |
| `groq.model` | `qwen/qwen3.8-27b` | Modelo de Groq. Está en properties para poder cambiarlo sin recompilar: Groq retira modelos de su tier Preview cada tanto. |
| `spring.jpa.show-sql` | `true` | Muestra las queries SQL en consola. Conviene apagarlo en producción. |

---

## 🔐 Autenticación

1. El cliente se registra o inicia sesión y recibe un **JWT firmado con HS256** (7 días de vigencia).
2. En cada request protegido manda `Authorization: Bearer <token>`.
3. `JwtAuthFilter` valida la firma y la expiración, y **vuelve a buscar el usuario
   en la base**: si el usuario fue eliminado, su token deja de servir aunque siga vigente.
4. La API es *stateless*: no hay sesiones en el servidor. Como contrapartida, no hay
   revocación de tokens: un token robado es válido hasta que expire.

---

## 📝 Tests

```bash
./gradlew test
```

> ⚠️ Hoy el único test es el `contextLoads()` de la plantilla, que **necesita MySQL
> corriendo** en el puerto 3307. Falta cobertura sobre la lógica de negocio
> (cálculo de racha, parseo de las respuestas de la IA) y sobre la seguridad
> (que los endpoints protegidos devuelvan 401 sin token).
