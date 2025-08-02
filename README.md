# 🔐 FinTrust – Secure Vault & Fraud Detection System

**FinTrust** is an AI-powered secure vault system designed for fintech applications. It offers secure file access, session protection, and suspicious activity monitoring with admin-controlled responses. Built with Spring Boot, MongoDB, Redis, and JWT, it's designed to simulate real-world security workflows used by companies like JPMorgan and Mastercard.

---

## 🧠 Core Technologies

- **Spring Boot**
- **MongoDB** (NoSQL database)
- **Redis** (in-memory store for tokens, PINs, and locks)
- **Spring Security** with **JWT**
- **Java MailSender** for email notifications
- **BCrypt** for secure password storage

---

## ✅ Features Implemented

### 🔐 Authentication & Authorization

- ✅ Separate **User and Admin registration/login**
- ✅ **JWT-based authentication**
    - Stateless login system
    - Role-based access control (`ROLE_USER`, `ROLE_ADMIN`)
- ✅ **BCrypt password hashing** for secure storage
- ✅ **Session PIN System**
    - 4–6 digit PIN generated on login
    - Sent via email and stored temporarily in Redis
    - Used for sensitive operations

---

### 🔁 Session PIN System

- ✅ Required for sensitive operations (e.g., listing users as admin)
- ✅ PIN is:
    - Stored in Redis with 30-minute TTL
    - Auto-deleted upon logout
    - Verified before performing secure tasks

---

### 🔒 Password Reset System

- ✅ **Forgot Password Flow:**
    - Accepts email
    - Generates secure token
    - Stores token in Redis
    - Sends reset link via email
- ✅ **Reset Password Flow:**
    - Token verification
    - Accepts new password
    - Password is hashed and saved in MongoDB
- ✅ Applicable for both users and admins

---

### ⚠ Suspicious Activity Detection & User Lock

- ✅ Tracks login attempts using Redis key: `FAILED_LOGIN:{username}`
- ✅ On **3 failed attempts**:
    - Admin notified via email
    - Email contains a secure link to lock the user
- ✅ **Admin Lock Endpoint:**
    - `POST /api/admin/lock-user/{username}`
- ✅ Lock Mechanism:
    - Locked users cannot log in
    - Lock expires automatically after 15 minutes (handled via Redis TTL)
    - Unlock handled automatically after duration passes

---

## 🚀 How to Run

1. **Clone the repository**
2. **Configure `application.yml`** with:
    - MongoDB connection string
    - Redis host/port
    - Email credentials for MailSender
3. **Run the application**
   ```bash
   ./mvnw spring-boot:run

4. **api_access:**
   ```url
   http://localhost:8080/api/

---

## 🙋‍♂️ Author

**Ansh Mishra**  
📧 [anshm2674@gmail.com](mailto:anshm2674@gmail.com)  
🌐 [GitHub](https://github.com/Ansh-26-74)  
🔗 [LinkedIn](https://www.linkedin.com/in/ansh-mishra-07170431a/)

> ⚠️ This is an actively developed project. More Polishing is been done!

