<h1 align="center">📱 Collaborative Todo App</h1>

<p align="center">
  A real-time collaborative task management application built with a
  <b>Ktor backend</b> and an <b>Android Jetpack Compose frontend</b>.
  <br/>
  Built as a <b>full-stack Kotlin project</b> with clean architecture,
  proper state management, and production-ready error handling.
</p>

<hr/>

## 🚀 Project Overview

This app allows users to create, manage, and share tasks while receiving **real-time updates**.  
It was built as a learning-focused project to deeply understand **modern Android development**, **backend APIs**, and **real-time systems** using Kotlin.

---
## 📸 App Screenshots

<table align="center">
  <tr>
    <th>Login</th>
    <th>Create Account</th>
    <th>Dashboard</th>
  </tr>
  <tr>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/a65b7bc3-8083-42d9-afc5-1fdad06774f7" width="220"/>
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/b7d72aae-b1cd-4a9a-96c1-45fea25fd6ba" width="220"/>
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/9c3b18df-442a-46e1-a0cd-3a273d02a326" width="220"/>
    </td>
  </tr>
</table>

<br/>

<table align="center">
  <tr>
    <th>My Tasks</th>
    <th>Task Details</th>
    <th>Edit Task</th>
  </tr>
  <tr>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/999ee425-a88e-4331-8e49-10ed8e1f18f8" width="220"/>
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/b6e97ad4-098f-4a89-b0f4-7a1eedb714ee" width="220"/>
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/c418d5dd-5679-4264-a78d-43898e3ca268" width="220"/>
    </td>
  </tr>
</table>

<br/>

<table align="center">
  <tr>
    <th>Create Task</th>
  </tr>
  <tr>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/28193515-5680-4403-9c7e-9f712497c1c7" width="220"/>
    </td>
  </tr>
</table>


---

## ✨ Features

### 🔐 Authentication
- User registration & login
- JWT-based authentication with refresh tokens
- Secure password hashing using bcrypt
- Token validation & session handling

### 📝 Task Management
- Create, update, delete tasks
- Priority levels (Low / Medium / High)
- Mark tasks as completed
- Search & filter tasks
- Pagination support

### 👥 Collaboration
- Share tasks with other users
- Permission-based access (View / Edit)
- View tasks shared with you

### 🔔 Real-time Updates
- Live task updates via WebSockets
- Instant synchronization across users
- Online/offline connection handling

### 📱 Android App
- 100% Jetpack Compose UI
- MVVM architecture with clean separation
- StateFlow-based reactive state management
- Offline support with DataStore
- Comprehensive loading & error states

---

## 🛠️ Tech Stack

### Backend
- **Framework:** Ktor 2.3.3
- **Language:** Kotlin
- **Database:** PostgreSQL 18
- **ORM:** JetBrains Exposed
- **Authentication:** JWT + bcrypt
- **Real-time:** Native WebSockets
- **Connection Pooling:** HikariCP
- **Serialization:** Kotlinx Serialization
- **Concurrency:** Kotlin Coroutines

### Android
- **UI:** Jetpack Compose + Material Design 3
- **Architecture:** MVVM + Repository Pattern
- **Dependency Injection:** Koin
- **Networking:** Retrofit + OkHttp
- **Real-time:** Java-WebSocket
- **Local Storage:** DataStore Preferences
- **Async:** Kotlin Coroutines + Flow

---

## 🏗️ Architecture

### Backend

- Clean layered architecture
- Environment-based configuration
- Centralized error handling
- WebSocket broadcast groups
- Production-ready logging
  
### Android
- Single Activity Architecture
- Navigation Compose
- ViewModel-driven state management
- Validation logic in ViewModels
- Authentication & network state handling

## 🧪 Error Handling & Reliability

- Network errors (timeout, SSL, connectivity)
- Authentication & authorization errors
- Validation errors with user-friendly messages
- Safe state updates using `StateFlow`
- Retry & fallback mechanisms

## 🏃 Building & Running (Backend)

| Task | Description |
|-----|------------|
| `./gradlew test` | Run tests |
| `./gradlew build` | Build project |
| `./gradlew buildFatJar` | Build executable JAR |
| `./gradlew run` | Run server |
| `./gradlew buildImage` | Build Docker image |
| `./gradlew runDocker` | Run using Docker |

If the server starts successfully, you should see:
 - Application started in X seconds.
 - Responding at http://0.0.0.0:8080

---

## 📚 What I Learned

- Full-stack development using Kotlin
- Designing REST APIs with Ktor
- Implementing JWT authentication
- Real-time systems with WebSockets
- Clean architecture & MVVM
- State management with StateFlow
- Production-ready error handling

---

## 🚧 Future Improvements
- Push notifications
- Role-based access control
- More unit & integration tests
- Kotlin Multiplatform shared modules

---

## 🤝 Contributing
This project was built mainly for learning purposes.  
Feedback, suggestions, and improvements are always welcome!

---

## 👨‍💻 Author

**Mathew Charles**  
Aspiring Android Developer  
Learning full-stack Kotlin, one project at a time 🚀
