# TutorBot — Backend

This repository contains the backend for the TutorBot application — a Spring Boot service that provides user management, course/topic management, PDF ingestion and retrieval-augmented generation (RAG) using embeddings.

This README is a documentation-focused overview of the backend: what it contains, how it is organized, the main responsibilities of components, design decisions and where to look in the code. It intentionally omits step-by-step run instructions (those belong in a CONTRIBUTING or RUNNING document).

Table of contents
- Project purpose
- High-level architecture
- Important packages & responsibilities
- Data stores & persistence
- Authentication & authorization
- RAG / Embeddings and ingestion
- Key design decisions and trade-offs
- Testing, CI and environment notes
- Where to look for specific features
- Contribution & license

## Project purpose

TutorBot is intended as an AI-powered tutor backend. It supports:
- User registration, authentication and session-based authorization
- Management of courses, topics and course materials
- Uploading and ingesting PDF documents into an embedding store
- Retrieval-augmented generation (RAG) workflows to answer or summarize using retrieved document segments

## High-level architecture

- Spring Boot application (Java)
- Modular code organized by domain (auth, user, course, topic, ai)
- Session-based security (server-side sessions and JSESSIONID cookie)
- A RAG pipeline that creates embeddings for document segments and stores them in a persistent embedding store
- An AiAssistant abstraction that wraps the LLM interactions and prompt templates

## Important packages & responsibilities

- `ch.frupp.tutorbot.auth`
  - Security wiring: custom `AuthenticationProvider`, `CustomUserDetailsService` and `SecurityConfig` (Spring Security configuration)
  - Responsible for authentication, user lookup, and integrating with Spring Security's session management

- `ch.frupp.tutorbot.user`
  - User entity, DTOs, repository and controller
  - Registration, login and logout endpoints live here

- `ch.frupp.tutorbot.course`
  - Course entities, controllers and services
  - Course materials (uploading PDFs, linking to courses and users) and endpoints for course-specific operations

- `ch.frupp.tutorbot.course.material` (or subpackage)
  - Logic for handling uploaded files, ingestion into the embedding store, and material metadata mapping

- `ch.frupp.tutorbot.course.topic`
  - Topic entity, DTO and business logic for topic creation, summaries and associations with quizzes

- `ch.frupp.tutorbot.ai`
  - `AiAssistant` interfaces and configuration (`RAGConfiguration`, `AiAssistantConfig`)
  - Responsible for assembly of retrieval pipelines, prompt templates and LLM calls

## Data stores & persistence

This project uses multiple persistence layers depending on the data type:

- Relational DB (Postgres)
  - Used for relational data and for persistent embedding stores (`pgvector`) when configured
  - Spring Data JPA repositories map Java entities to relational tables
  - DDL and migration considerations: any non-standard DDL (extensions like `pgvector`) must be available on the Postgres instance

- MongoDB
  - Used in parts of the project where document-style models are convenient
  - Spring Data MongoDB repositories map `@Document` classes to collections

- Embedding stores
  - Persistent options include `pgvector` (Postgres), and other stores supported by langchain4j. The embedding store choice impacts persistence and retrieval capabilities.

Notes on Spring Data repositories
- Spring Data provides repository interfaces (JPA or MongoDB) which Spring will implement at runtime. The mapping only happens if the respective Spring Data starter is on the classpath and configured (for example, `spring-boot-starter-data-mongodb` for MongoDB). If both drivers are present, both repository types may be active.
- There is no magic: Spring does not automatically wire unrelated DBs unless their auto-configuration is enabled via dependencies and application configuration.

## Authentication & authorization

- The backend uses session-based authentication. When a user successfully authenticates, an Authentication object is stored in the HTTP session and a JSESSIONID cookie is issued to the client.
- The project uses a custom `UserAuthenticationProvider` and a `CustomUserDetailsService` to load users from the configured user repository.
- Security-related classes and beans are centralized in the `auth` package. Be cautious when defining beans (e.g. converting `@Component` to a `@Bean` in a configuration class) to avoid circular dependencies or duplicate bean registration.

## RAG / Embeddings and ingestion

- The ingestion pipeline reads PDFs (or other supported formats), splits them into segments and creates embeddings per segment. Each embedding is stored with metadata including `userId` and `courseId` to enable scoped retrieval later.
- Document splitting strategy (paragraph-based splitting) is implemented in the ingestion service. The splitting strategy matters for retrieval granularity and prompt size.
- The embedding store should be persistent to avoid re-ingesting documents on every startup. Supported persistent stores include Postgres with `pgvector` and other backends supported by langchain4j.
- When retrieving context for a user query, the retrieval pipeline should filter by metadata (for example `userId` and `courseId`) so only the relevant segments are considered.

## Key design decisions and trade-offs

- Session-based auth (server-side) vs stateless tokens (JWT)
  - The project currently prefers session-based authentication (server-managed sessions and cookies). This avoids storing tokens on the client but requires session storage on the server and correct handling of security context saving after programmatic authentication.

- Persisting documents vs persisting embeddings
  - Persisting embeddings allows skipping repeated ingestion. Persisting raw documents makes re-processing possible (e.g., to change splitting or embedding model).
  - Typical approach: persist embeddings and keep raw documents if you need to reprocess; otherwise store only embeddings and minimal provenance metadata (uploader, course, original filename).

- MongoDB embedded documents vs references
  - Embedding a full sub-document (`List<Quiz>`) inside a Topic document is convenient for fast reads and denormalized views but duplicates data.
  - Storing references (IDs) is normalized and avoids duplication; choosing between them depends on query patterns and update/delete semantics. If you want cascade deletes (delete quizzes when a topic is deleted), an explicit application-level delete or database-level cascading (supported in relational DBs) is needed.

## Testing, CI and environment notes

- Unit and integration tests should be isolated: create and tear down any required users/data in tests or use `@WithMockUser` where appropriate.
- Avoid hardcoding third-party API keys in the repository. Use environment variables or external configuration for secrets. The codebase already contains support to configure different LLM providers via properties.

## Where to look for specific features

- Authentication: `src/main/java/ch/frupp/tutorbot/auth`
- User endpoints: `src/main/java/ch/frupp/tutorbot/user`
- Course endpoints & material ingestion: `src/main/java/ch/frupp/tutorbot/course`
- Topics: `src/main/java/ch/frupp/tutorbot/course/topic`
- AI / RAG wiring: `src/main/java/ch/frupp/tutorbot/ai`
- Tests: `src/test/java`

## Contribution & license

- This README is documentation-only. If you want to add run or contribution instructions, add a separate `CONTRIBUTING.md` or `RUNNING.md` so this file remains focused on project documentation.
- Add your license identifier and contribution guidelines before publishing the repository publicly.

## Contact & maintainers

- Project maintainer (contact): owner of this workspace


(End of documentation README)
