

TutorBot — AI tutor website

TutorBot is an AI tutor that helps learners interactively study course materials with LLM assistance. It is more than a generic chat wrapper: TutorBot implements a full retrieval-augmented generation (RAG) pipeline against the user's course documents so answers are grounded in those materials.

Tech stack and rationale
- Java + Spring Boot — mature ecosystem, strong typing, and production-ready frameworks. Project uses idiomatic Spring: controllers, service layer, and configuration via `TutorBotApplication`.
- Spring Data JPA (Hibernate) — transactional, relational mapping for users, roles, and metadata where ACID guarantees are important.
- Spring Security — centralized authentication and authorization with a custom `UserDetailsService` and clear separation between credential concerns and business logic.
- PostgreSQL + `pgvector` — user/auth data lives in Postgres; embeddings are stored with `pgvector` when keeping semantic vectors colocated with relational data is desirable (simpler infra, transactional consistency).
- MongoDB — flexible document storage for extracted text and chunked documents; well-suited for variable-length text segments and more complex Json data structures (Quizzes).
- langchain4j RAG pipeline — the AI flow follows: ingest → chunk → embed → index → retrieve (metadata filtering) → generate. The orchestration is decoupled from storage so vector backends can be swapped.
- Docker & Docker Compose — container-first packaging and multi-stage builds via `backend/compose.yaml` for reproducible deployments and environment-driven configuration.

Concise architecture notes
- Code lives under `backend/src/main/java/ch/frupp/lecturevault/` (packages: `auth`, `ai`, `course`, `user`).
- The `ai` package separates ingestion (PDF parsing, chunking), embedding persistence, and query-time retrieval + prompt orchestration.
- Persistence strategy mixes Postgres (structured user/auth data, embeddings via `pgvector`) and MongoDB (document chunks and provenance) to balance transactional integrity with schema flexibility.

Files of interest
- `backend/src/main/java/ch/frupp/lecturevault/` — main backend code (controllers, services, repositories)
- `backend/compose.yaml` — container orchestration example (local dev environment, no production deployment yet)
- `backend/src/test/java` — unit and integration tests demonstrating testability and mocking of AI providers


