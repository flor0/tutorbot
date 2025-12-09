# TutorBot — Backend

This repository contains the backend for "TutorBot" — a Spring Boot application that provides user authentication, course/topic management, PDF ingestion and RAG-style retrieval with embeddings.

This README explains how to run the backend locally, configure environment variables, run tests and troubleshoot common issues.

---

## Quick overview

- Stack: Java + Spring Boot
- Build: Maven wrapper (mvnw / mvnw.cmd)
- Persistent stores: Postgres (for relational data) and MongoDB (used in parts of the project)
- AI / RAG: langchain4j integrations, embedding stores (pgvector, etc.)
- Auth: session-based authentication (login endpoint stores Authentication in HTTP session, JSESSIONID cookie used by clients)

Project layout (important packages):
- `ch.frupp.lecturevault.auth` — security, authentication provider, user details
- `ch.frupp.lecturevault.user` — user model, controller, service, repository
- `ch.frupp.lecturevault.course` — courses, materials, upload endpoints
- `ch.frupp.lecturevault.ai` — AI assistant, configuration for RAG, embeddings
- `src/test` — unit tests

---

## Requirements

- Java 17+ (use the version required by the project's `pom.xml`)
- Docker & Docker Compose (recommended to run Postgres locally via `compose.yaml`)
- A valid API key for the chosen LLM provider (don't commit it) — configure via environment variables or `.env`


## Running locally

### 1) Using Maven wrapper (Windows PowerShell)

Open PowerShell in project root and run:

```powershell
# run the app
.\mvnw.cmd spring-boot:run

# run tests
.\mvnw.cmd test

# build a jar
.\mvnw.cmd -DskipTests package
```

On Unix/macOS use `./mvnw` the same way.

### 2) Using Docker Compose

If you want to run the Postgres (and other infrastructure) alongside the app, use the provided `compose.yaml`:

```powershell
# from project root
docker compose -f compose.yaml up --build
```

This will start the database containers. The backend can be started with `mvnw` afterwards or packaged into a Docker image.

### 3) Running the packaged jar

```powershell
# after building
java -jar target/*.jar
```

---

## Configuration and secrets

This project uses Spring Boot's configuration. Do NOT commit API keys or secrets.

A recommended approach is to keep local secrets in a `.env` file and import it from `application.properties`:

```
# in application.properties
spring.config.import=file:.env[.properties]
```

Example `.env` entries (DO NOT CHECK IN TO GIT):

```
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/tutorbot
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
SPRING_DATA_MONGODB_URI=mongodb://localhost:27017/tutorbotdb
GEMINI_API_KEY=your_gemini_api_key_here
OLLAMA_URL=http://localhost:11434
```

The app reads `application.properties` in `src/main/resources`. If you want to switch between LLM providers for development, comment/uncomment the relevant provider blocks in `application.properties` (the project already contains commented instructions to switch to Ollama for local testing).

---

## API overview

Important endpoints (subject to code changes in controllers):

- `POST /api/users/register` — register a new user (body: registration data)
- `POST /api/users/login` — login with username/password; stores Authentication in HTTP session and returns logged-in info
- `POST /api/users/logout` — logout endpoint (invalidates session)
- `GET /api/courses` — list courses for authenticated user
- `GET /api/courses/{id}` — course details
- `POST /api/courses/{courseId}/upload` — upload a PDF file for a course (ingests and stores embeddings)
- `GET /api/courses/{courseId}/materials` — checks whether the user/course has ingested materials
- `GET /api/topics` — list topics for authenticated user
- `POST /api/topics` — create a topic
- `DELETE /api/topics/{id}` — delete a topic

Authentication notes:
- The app uses session-based auth. After a successful `POST /api/users/login`, the server stores the Authentication in the session and sends a `JSESSIONID` cookie to the client; the client must send that cookie on subsequent requests.
- For testing, tests may use `@WithMockUser` to bypass real session auth.

---

## Tests

Run unit tests with:

```powershell
.\mvnw.cmd test
```

If tests fail related to Spring bean cycles or conflicting bean names, check the AI assistant/bean definitions (for example: `AiAssistant` bean double-registration) and `@Configuration` vs `@Service` annotated classes. See Troubleshooting below.

---

## Common troubleshooting

- Conflicting bean definitions (example: `Annotation-specified bean name 'aiAssistant' conflicts`): This typically happens if a bean is created twice — e.g. a `@Service` and a `@Configuration` factory both create a bean with the same name. Resolve by removing one registration or renaming the bean. The `AiAssistantConfig` class may define a bean while `AiAssistant` is annotated as a component — only one is needed.

- Postgres + pgvector: If you see `extension "vector" is not available`, remove old volumes and recreate the database container so the extension package is installed fresh, or use a Postgres image that already includes `pgvector`.

- Login/session 403 after login: Make sure your login flow stores the Authentication in the session (the default `HttpSessionSecurityContextRepository` is used by the Spring Security filters). The client must include the `JSESSIONID` cookie on subsequent requests. If you manually authenticate in a controller, store the security context using the configured SecurityContextRepository so subsequent requests recognize the authentication.

- Avoid committing API keys: use `.env` and `spring.config.import=file:.env[.properties]` as shown above.

---

## Embeddings and ingestion

- The project stores metadata with embeddings; include `userId` and `courseId` in the metadata so that later retrieval/filtering can return only documents belonging to a particular user and course.
- To avoid re-ingesting documents on every startup, persist embeddings in a persistent embedding store (Postgres/pgvector, or other persistent stores supported by langchain4j). See `RAGConfiguration` for how the embedding store is configured.
- To inspect embedding store size or find how many segments/documents exist, add a small helper method in the service that queries the embedding store API for count or list of documents.


---

## Contribution & conventions

- Keep secrets out of the repository. Add `.env` to `.gitignore`.
- Use the Maven wrapper to ensure consistent builds.
- Write tests for new controllers/services and keep authentication tests isolated (create and remove users as needed during tests or use `@WithMockUser` for simpler cases).

---

## Where to look in the code

- `src/main/java/ch/frupp/lecturevault/auth` — security configuration, `UserAuthenticationProvider`, `CustomUserDetailsService`.
- `src/main/java/ch/frupp/lecturevault/user` — data model and controller
- `src/main/resources/application.properties` — main configuration
- `compose.yaml` — docker compose set up for databases

---

If anything in this README is out of date (APIs or package names changed), please update this file accordingly. If you want, I can also add an `API.md` or an OpenAPI spec file next.


License: (add your license here)

Contact: (your contact info)

