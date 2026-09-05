# 🔐 FinTrust — Secure Vault & Fraud Detection System

FinTrust is a **secure Spring Boot backend** designed to simulate security workflows used in fintech applications. The system combines **JWT authentication, role-based authorization, Redis-based session security, suspicious-login detection, password recovery, and secure file storage using MongoDB GridFS**.

The application is containerized with Docker and deployed on **AWS EC2**, with **MongoDB Atlas** as the managed database and **Redis running as a Docker container**. A GitHub Actions CI/CD pipeline automatically builds and publishes new Docker images and deploys them to EC2 whenever changes are pushed to the `main` branch.

---

## 🚀 Project Highlights

* 🔐 JWT-based authentication and role-based authorization
* 👤 Separate User and Admin authentication flows
* 🔑 Redis-backed session PIN verification
* 🔄 Secure password reset using Redis tokens
* 🚨 Failed-login tracking and suspicious activity detection
* 🔒 Automatic temporary account locking
* 📁 Secure file upload/download using MongoDB GridFS
* 📧 Email notifications using Java MailSender
* 🐳 Dockerized Spring Boot application
* ☁️ Deployed on AWS EC2
* 🍃 MongoDB Atlas for persistent application data
* ⚡ Redis running in Docker with persistent storage
* 🔄 Automated CI/CD using GitHub Actions
* 📦 Docker images published to Docker Hub
* 🔐 Environment-based configuration for secrets and credentials

---

# 🛠️ Tech Stack

### Backend

* Java 21
* Spring Boot
* Spring Security
* JWT
* Maven

### Databases & Storage

* MongoDB Atlas
* MongoDB GridFS
* Redis

### Security

* BCrypt password hashing
* JWT authentication
* Role-based access control
* Redis TTL-based security tokens
* Failed-login tracking
* Temporary account locking

### DevOps & Deployment

* Docker
* Docker Compose
* Docker Hub
* AWS EC2
* GitHub Actions
* Ubuntu

### Development Tools

* IntelliJ IDEA
* Postman
* Git & GitHub

---

# 🔐 Security Features

## 1. Authentication & Authorization

FinTrust provides separate authentication flows for users and administrators.

### Features

* User registration
* Admin registration
* User login
* Admin login
* JWT-based authentication
* Stateless authentication
* Role-based authorization
* BCrypt password hashing

Roles:

```text
ROLE_USER
ROLE_ADMIN
```

---

## 2. Redis-Based Session PIN

After successful login, FinTrust generates a temporary session PIN.

The PIN is:

* Generated during login
* Sent to the user's registered email
* Stored temporarily in Redis
* Protected using a **30-minute TTL**
* Removed when the user logs out
* Required for sensitive operations

This provides an additional security layer beyond JWT authentication.

---

## 3. Password Reset

FinTrust implements a secure password recovery workflow.

### Flow

```text
Forgot Password
       ↓
Generate secure token
       ↓
Store token in Redis
       ↓
Send reset link via email
       ↓
User submits new password
       ↓
Validate token
       ↓
Hash new password using BCrypt
       ↓
Save password in MongoDB
```

The password-reset mechanism is available for both users and administrators.

---

# 🚨 Suspicious Activity Detection

FinTrust tracks failed login attempts using Redis.

Example Redis key:

```text
FAILED_LOGIN:{username}
```

After **3 failed login attempts**:

```text
Failed Login
     ↓
Redis tracks attempts
     ↓
3 failed attempts
     ↓
Admin notification email
     ↓
Admin can lock account
```

### Account Locking

A locked account:

* Cannot log in
* Remains locked for 15 minutes
* Uses Redis TTL for automatic expiration
* Automatically becomes available after the lock period

This demonstrates a practical security workflow for detecting and responding to suspicious authentication activity.

---

# 📁 Secure File Vault

FinTrust provides secure file storage through **MongoDB GridFS**.

Supported operations include:

* Upload files
* List stored files
* Download files
* View files
* Delete files

Files are stored in MongoDB GridFS rather than directly on the application server.

---

# 📧 Email Notifications

Java MailSender is used for security-related email notifications, including:

* Session PIN delivery
* Password reset links
* Suspicious login notifications

---

# 🐳 Dockerization

FinTrust is packaged as a Docker image using Java 21.

### Dockerfile

The application uses:

```text
Eclipse Temurin 21 JRE
        ↓
Spring Boot JAR
        ↓
Docker Container
```

The Docker image is published to Docker Hub:

```text
anshm2674/fintrust-app
```

The application container is configured through environment variables rather than storing credentials inside the image.

---

# ☁️ AWS Deployment

FinTrust is deployed on an **AWS EC2 Ubuntu server**.

### Production Architecture

```text
                    Internet
                       │
                       ▼
                AWS EC2 Instance
                       │
             ┌─────────┴─────────┐
             │                   │
             ▼                   ▼
      FinTrust Container     Redis Container
       Spring Boot 21          Redis 7
             │                   │
             │                   │
             └─────────┬─────────┘
                       │
                       ▼
                 MongoDB Atlas
```

### Production Components

| Component             | Technology     |
| --------------------- | -------------- |
| Application           | Spring Boot    |
| Runtime               | Java 21        |
| Application Container | Docker         |
| Cache / Session Store | Redis 7        |
| Persistent Database   | MongoDB Atlas  |
| File Storage          | MongoDB GridFS |
| Server                | AWS EC2        |
| OS                    | Ubuntu         |
| Container Management  | Docker Compose |

Redis uses a Docker named volume so Redis data can persist when the container is recreated.

---

# 🔄 CI/CD Pipeline

FinTrust uses **GitHub Actions** for continuous integration and deployment.

Every push to the `main` branch triggers the pipeline.

```text
Developer pushes code
          ↓
       GitHub
          ↓
   GitHub Actions
          ↓
    Maven Build
          ↓
    Docker Build
          ↓
    Docker Hub Push
          ↓
      SSH to EC2
          ↓
    Docker Compose Pull
          ↓
    Container Update
          ↓
     New version live
```

### CI/CD workflow performs:

1. Checkout source code
2. Set up Java 21
3. Build the Spring Boot application
4. Build the Docker image
5. Authenticate with Docker Hub
6. Push the latest image to Docker Hub
7. Connect to AWS EC2
8. Pull the latest Docker image
9. Restart/update the application container
10. Remove unused Docker images

Deployment credentials are stored using **GitHub Actions Secrets** rather than being committed to the repository.

---

# ⚙️ Environment Configuration

Sensitive configuration is provided through environment variables.

Example:

```text
MONGO_URI
REDIS_HOST
REDIS_PORT
REDIS_PASSWORD
JWT_SECRET
MAIL_USERNAME
MAIL_PASSWORD
```

Secrets are intentionally excluded from Git using `.gitignore`.

The same Docker image can therefore be used across environments while changing only the configuration.

---

# 🔌 API Endpoints

## Authentication

```text
POST /api/register/user
POST /api/register/admin

POST /api/login/user
POST /api/login/admin
```

## Password Reset

```text
POST /api/auth/forgot-password
POST /api/auth/reset-password
```

## Admin

```text
GET /api/admin/all-users
GET /api/admin/suspicious-activity
GET /api/admin/suspicious-activity/{userId}
GET /api/admin/lock-user/{username}
```

## Secure Vault

```text
POST   /api/vault/upload
GET    /api/vault/download/{filename}
POST   /api/vault/view/{filename}
DELETE /api/vault/delete/{filename}
GET    /api/vault/files
POST   /api/vault/logout
```

---

# 🏃 Running Locally

### 1. Clone the repository

```bash
git clone https://github.com/Ansh-26-74/FinTrust.git
cd FinTrust
```

### 2. Configure environment variables

Create a `.env` file containing your local configuration.

Do **not** commit the `.env` file.

### 3. Build the application

```bash
./mvnw clean package
```

### 4. Build the Docker image

```bash
docker build -t anshm2674/fintrust-app:latest .
```

### 5. Run using Docker Compose

```bash
docker compose up -d
```

---

# 📊 Current Architecture

```text
                    ┌─────────────────────┐
                    │       Client        │
                    │  Postman / Browser  │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │    Spring Boot API  │
                    │      Port 8080      │
                    └──────────┬──────────┘
                               │
             ┌─────────────────┼─────────────────┐
             │                 │                 │
             ▼                 ▼                 ▼
      Spring Security       Redis          MongoDB Atlas
             │                 │                 │
             ▼                 ▼                 ▼
          JWT Auth        Sessions/Tokens     User Data
          RBAC            PINs/Locks          GridFS Files
             │
             ▼
       Java MailSender
             │
             ▼
          Email
```

---

# 🔮 Future Improvements

Planned improvements include:

* HTTPS with a reverse proxy
* Production domain configuration
* AWS monitoring and health checks
* Spring Boot Actuator
* Application metrics
* Centralized logging
* Improved deployment rollback strategy
* Automated integration testing in CI/CD

---

# 👨‍💻 Author

**Ansh Mishra**

Java Backend Developer | Spring Boot | REST APIs | Docker | AWS

GitHub:
https://github.com/Ansh-26-74

---

> 🚧 FinTrust is an actively developed backend project focused on secure fintech workflows, containerization, cloud deployment, and production-oriented DevOps practices.
