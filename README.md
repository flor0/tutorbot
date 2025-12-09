
# TutorBot

This repository contains a full-stack application for managing lecture/course materials and AI-assisted features. The primary focus of this README is the backend, which is implemented as a Spring Boot application in Java.

**Project At A Glance**
- **Backend:** Java + Spring Boot (see `backend/`)
- **Frontend:** TypeScript + Vite (see `frontend/`)
- **Build tools:** Maven (wrapper in `backend/`) and npm/yarn for the frontend

**Backend (Spring Boot)**
- **Entry point:** `backend/src/main/java/ch/frupp/lecturevault/TutorBotApplication.java`
- **Key packages:**
	- `ai/` — AI assistant, RAG configuration, and PDF ingestion (`AiAssistant.java`, `RAGConfiguration.java`, `PDFIngestionService.java`, `IngestionResult.java`)
	- `auth/` — security and user authentication (`SecurityConfig.java`, `UserAuthenticationProvider.java`, `CustomUserDetailsService.java`)
	- `course/` — course, topic, material, quiz and summary controllers and services (`CourseController.java`, `TopicController.java`, `CourseMaterialController.java`)
	- `user/` — user management (`UserController.java`, `UserService.java`)

**Configuration**
- **Properties file:** `backend/src/main/resources/application.properties` — update DB settings, ports, and other properties here.
- **AI keys / secrets:** Set environment variables for external AI providers (see `AiAssistantConfig.java` and `AiAssistant.java`).

**Run Backend Locally (Windows PowerShell)**
- Start in development mode:

```
cd backend
.\mvnw.cmd spring-boot:run
```

- Build a JAR and run it:

```
cd backend
.\mvnw.cmd -DskipTests package
java -jar target/*.jar
```

- Run with Docker Compose (uses `backend/compose.yaml`):

```
cd backend
docker compose -f compose.yaml up --build
```

**Run Backend Tests**

```
cd backend
.\mvnw.cmd test
```

**Frontend Quick Start**
- The frontend is a Vite + React app in `frontend/`.

```
cd frontend
npm install
npm run dev
```

**API Overview**
- Controllers live under `backend/src/main/java/ch/frupp/lecturevault/` — look for `*Controller.java` files for REST endpoints.
- Important controllers: `CourseController`, `CourseMaterialController`, `TopicController`, `UserController`.

**Developer Notes**
- The AI features (RAG, PDF ingestion) are implemented under `ai/`. See `PDFIngestionService.java` for PDF processing and `AiAssistant.java` for how AI calls are orchestrated.
- Authentication is implemented with Spring Security (`auth/`). Update `SecurityConfig.java` if you need to change auth rules.
- To change database settings or add migrations, edit `application.properties` or add Flyway/Liquibase as needed.

**Contributing**
- Fork the repo, create a feature branch, open a pull request with a clear description and tests where applicable.

**Contact & Support**
- For questions about the backend implementation, inspect the Java packages listed above or open an issue.

---
Generated README focused on backend usage and development. For more details check the source files under `backend/src/main/java/ch/frupp/lecturevault/`.

