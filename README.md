# FocusForge ⚡

**A secure student productivity and task management REST API built with Java and Spring Boot.**

FocusForge helps students organize assignments, track deadlines, manage priorities, and monitor their productivity through a RESTful API. It includes JWT-based authentication, user-specific task ownership, database migrations, pagination, and interactive API documentation.

## ✨ Features

### Task Management
- Create, retrieve, update, and delete tasks.
- Set task titles, descriptions, priorities, completion status, and due dates.
- Search tasks by title.
- Filter tasks by completion status and priority.
- Sort tasks by ID, title, priority, or due date.
- Paginate task results.
- View productivity statistics, including completed and pending tasks.

### Authentication and Security
- Register accounts and log in with email and password.
- Secure passwords using BCrypt hashing.
- Authenticate API requests using JWT Bearer tokens.
- Restrict task access to the authenticated owner.
- Prevent users from reading, updating, or deleting other users' tasks.
- Validate request data and return structured error responses.

### Database and API Documentation
- PostgreSQL support for persistent application data.
- H2 support for local development and testing.
- Flyway database migrations.
- Interactive Swagger UI documentation.
- Automated service and controller tests.

## 🛠️ Tech Stack

| Technology | Purpose |
|---|---|
| Java 25 | Programming language |
| Spring Boot 4 | Application framework |
| Spring Web | REST API development |
| Spring Data JPA | Database access |
| Spring Security | Authentication and authorization |
| JWT | Stateless API authentication |
| BCrypt | Password hashing |
| PostgreSQL | Relational database |
| H2 | Development and test database |
| Flyway | Database schema migrations |
| Maven | Build and dependency management |
| JUnit, Mockito, MockMvc | Automated testing |
| Swagger / OpenAPI | Interactive API documentation |

## 🚀 Getting Started

### Prerequisites

Install the following:

- Java 25
- Git
- PostgreSQL, if using the PostgreSQL profile
- Visual Studio Code or another Java IDE

The project includes the Maven Wrapper, so a separate Maven installation is not required.

### 1. Clone the repository

```bash
git clone https://github.com/IMyash5/FocusForge.git
cd FocusForge/focusforge
```

Open the directory containing `pom.xml` in your editor.

### 2. Configure the JWT secret

The application reads its JWT signing secret from an environment variable. Generate a random 32-byte secret encoded as Base64.

In Windows PowerShell:

```powershell
$bytes = New-Object byte[] 32
[System.Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
$env:FOCUSFORGE_JWT_SECRET = [Convert]::ToBase64String($bytes)
```

Keep this secret private. Do not commit real secrets to GitHub.

### 3. Configure the database

For local development with H2, activate the H2 profile:

```powershell
$env:SPRING_PROFILES_ACTIVE = "h2"
```

The H2 configuration uses a file-based database at `./data/focusforge`.

For PostgreSQL, create a database named `focusforge`, then set the connection variables in PowerShell:

```powershell
$env:SPRING_PROFILES_ACTIVE = "postgres"
$env:FOCUSFORGE_DB_URL = "jdbc:postgresql://localhost:5432/focusforge"
$env:FOCUSFORGE_DB_USERNAME = "postgres"
$env:FOCUSFORGE_DB_PASSWORD = Read-Host "Enter PostgreSQL password"
```

Enter your own PostgreSQL username and password as appropriate. Do not place real credentials in source files.

Flyway applies the versioned migrations when the PostgreSQL profile starts.

### 4. Run the application

From the directory containing `pom.xml`, run:

```powershell
.\mvnw.cmd spring-boot:run
```

The API will be available at:

```text
http://localhost:8080
```

The existing `/hello` endpoint can be used as a simple application check.

### 5. Run automated tests

```powershell
.\mvnw.cmd clean test
```

To compile without running tests:

```powershell
.\mvnw.cmd clean compile
```

## 📚 API Documentation

After starting the application, open Swagger UI:

**[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

OpenAPI specification:

**[http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)**

Swagger lets you explore endpoints, inspect request and response formats, and execute API calls.

### Authentication workflow

1. Register an account using `POST /api/auth/register`.
2. Log in using `POST /api/auth/login`.
3. Copy the returned `accessToken`.
4. Click **Authorize** in Swagger UI and enter the token.
5. Execute protected task endpoints.

Access tokens expire after 30 minutes. Log in again to obtain a new token.

## 📡 API Endpoints

### Authentication

| Method | Endpoint | Description | Authentication |
|---|---|---|---|
| POST | `/api/auth/register` | Register a new account | Public |
| POST | `/api/auth/login` | Authenticate and obtain a JWT | Public |

### Tasks

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/tasks` | List, search, filter, sort, and paginate owned tasks |
| POST | `/api/tasks` | Create a task |
| GET | `/api/tasks/{id}` | Retrieve an owned task |
| PUT | `/api/tasks/{id}` | Update an owned task |
| DELETE | `/api/tasks/{id}` | Delete an owned task |
| GET | `/api/tasks/stats` | Retrieve the current user's task statistics |

All task endpoints require a valid JWT Bearer token.

The list endpoint supports the following query parameters:

| Parameter | Description |
|---|---|
| `search` | Search by task title |
| `completed` | Filter by completion status |
| `priority` | Filter by `LOW`, `MEDIUM`, or `HIGH` |
| `sortBy` | Sort by `id`, `title`, `priority`, or `dueDate` |
| `direction` | `asc` or `desc` |
| `page` | Zero-based page number |
| `size` | Page size from 1 to 100 |

Example:

```text
GET /api/tasks?priority=HIGH&completed=false&page=0&size=10
```

## 💡 Example Requests

### Register an account

`POST /api/auth/register`

```json
{
  "email": "student@example.com",
  "password": "YourSecurePassword123!"
}
```

### Log in

`POST /api/auth/login`

```json
{
  "email": "student@example.com",
  "password": "YourSecurePassword123!"
}
```

The response contains an access token for subsequent authenticated requests.

### Create a task

`POST /api/tasks`

Include the header:

```text
Authorization: Bearer <your-access-token>
Content-Type: application/json
```

Request body:

```json
{
  "title": "Complete Java assignment",
  "description": "Revise collections and multithreading",
  "priority": "HIGH",
  "dueDate": "2026-11-15"
}
```

### Update a task

`PUT /api/tasks/1`

```json
{
  "title": "Complete Java assignment",
  "description": "Assignment completed",
  "completed": true,
  "priority": "HIGH",
  "dueDate": "2026-11-15"
}
```

Use the actual task ID returned by the API.

### Retrieve task statistics

`GET /api/tasks/stats`

The response includes the total, completed, and pending task counts, along with the completion percentage for the authenticated user.

## 🔒 HTTP Status Codes

| Status | Meaning |
|---|---|
| `200 OK` | Successful retrieval or update |
| `201 Created` | Task successfully created |
| `204 No Content` | Task successfully deleted |
| `400 Bad Request` | Invalid request or validation failure |
| `401 Unauthorized` | Missing, invalid, or expired token |
| `404 Not Found` | Task not found or not owned by the authenticated user |

## 🧪 Testing

FocusForge includes automated tests for service logic and REST controller behavior.

The tests cover task operations, request validation, and authorization boundaries. Manual API verification has also been performed using PowerShell and Swagger UI.

Run the complete test suite with:

```powershell
.\mvnw.cmd clean test
```

## 🗺️ Roadmap

- [x] Core task CRUD operations
- [x] Task priorities, descriptions, and due dates
- [x] Search, filtering, sorting, and pagination
- [x] Productivity statistics
- [x] PostgreSQL integration and Flyway migrations
- [x] JWT authentication and user-specific task ownership
- [x] Swagger / OpenAPI documentation
- [ ] Build a user-facing frontend
- [ ] Deploy the application
- [ ] Add further integration and security tests

## 👨‍💻 Author

**Yash Srivastava**

- GitHub: [@IMyash5](https://github.com/IMyash5)

---

*FocusForge is an evolving portfolio project focused on backend development, REST API design, authentication, database management, and software engineering practices.*
