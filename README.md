# FocusForge ⚡

**A student productivity and task management REST API built with Java and Spring Boot.**

FocusForge is a backend project designed to help students organize their work and manage tasks through a REST API. It currently supports creating, viewing, updating, and deleting tasks, with request validation and persistent storage during the application session.

## 🚀 Features

- Create a task with a title
- Retrieve all tasks
- Retrieve a task by ID
- Update a task's title and completion status
- Delete a task
- Validate task titles and reject blank input
- Return structured JSON error responses
- Store tasks using Spring Data JPA and an H2 in-memory database
- Automated service and controller tests

## 🛠️ Tech Stack

- Java 25
- Spring Boot 4
- Spring Data JPA
- H2 Database
- Maven
- JUnit and Mockito
- MockMvc

## ⚙️ Getting Started

### Prerequisites

- Java 25
- Git (optional, for cloning the repository)

### Run locally

1. Clone the repository:

   ```bash
   git clone https://github.com/IMyash5/FocusForge.git
   ```

2. Open the project folder containing `pom.xml` in VS Code.

3. In the terminal, navigate to that folder and run:

   **Windows PowerShell**

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

4. Open `http://localhost:8080/hello` to check the existing hello endpoint.

The application uses an H2 in-memory database for local development. Its data is not intended to persist after the database instance shuts down.

## 📡 API Endpoints

| Method | Endpoint          | Description           |
| ------ | ----------------- | --------------------- |
| GET    | `/hello`          | Test the application  |
| GET    | `/api/tasks`      | Retrieve all tasks    |
| POST   | `/api/tasks`      | Create a task         |
| GET    | `/api/tasks/{id}` | Retrieve a task by ID |
| PUT    | `/api/tasks/{id}` | Update a task         |
| DELETE | `/api/tasks/{id}` | Delete a task         |

### Example: Create a task

**Request:** `POST /api/tasks`

```json
{
  "title": "Complete Java assignment"
}
```

### Example: Update a task

**Request:** `PUT /api/tasks/1`

```json
{
  "title": "Complete Java assignment",
  "completed": true
}
```

## 🧪 Run Tests

On Windows, run:

```powershell
.\mvnw.cmd test
```

The project includes automated tests for service logic and REST controller behavior.

## 🗺️ Roadmap

- [ ] Add task priorities, descriptions, and due dates
- [ ] Add task search, filtering, and sorting
- [ ] Add productivity statistics
- [ ] Improve API documentation
- [ ] Build a user-facing frontend

## 👨‍💻 Author

**Yash Srivastava**

GitHub: [@IMyash5](https://github.com/IMyash5)
