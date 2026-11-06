# Let's Play — RESTful CRUD API

A secure, scalable, and fully compliant **RESTful CRUD API** built with **Spring Boot 3.2.5** and **MongoDB**. The system manages **Users** and **Products** with role-based access control (RBAC), JWT authentication, rate limiting, and global error handling.

---

## Prerequisites

Before running the application, ensure you have the following installed on your environment:

- **Java JDK 21** (or Java 17+)
- **Apache Maven 3.9+**
- **MongoDB** (v7.0+ installed locally or portable binary)
- **cURL** or **Postman** (for API testing)

---

## Step-by-Step Setup & Running Guide

### 1. Start MongoDB Server
Ensure MongoDB is running locally on port `27017`.

If using a local binary (without `sudo` / system service):
```bash
mkdir -p ~/mongodb/data
~/mongodb/mongodb-linux-x86_64-ubuntu2204-7.0.14/bin/mongod --dbpath ~/mongodb/data
```
*(Verify log output shows: `waiting for connections on port 27017`)*

---

### 2. Configure Environment Variables (`.env`)
Create a `.env` file in the root directory of the project (same directory as `pom.xml`):

```env
SPRING_DATA_MONGODB_URI=mongodb://localhost:27017/your_database_name
JWT_SECRET=your_secret_key_here_must_be_at_least_256_bits
JWT_EXPIRATION=86400000
```

> **Note:** The `.env` file is excluded from Git tracking via `.gitignore` to prevent secret leaks.

---

### 3. Build and Run the Application
Open a new terminal window in the project root folder and execute:

```bash
# Clean and compile
mvn clean package -DskipTests

# Run the application
mvn spring-boot:run
```

The server will start on **`https://localhost:8080`**.

---

## Security & Role-Based Access Control (RBAC)

| Role | Permissions |
| :--- | :--- |
| **Public / Anonymous** | `GET /products`, `GET /products/{id}`, Register & Login |
| **ROLE_USER** | Perform CRUD on **their own** products (`POST`, `PUT`, `DELETE`) |
| **ROLE_ADMIN** | Manage **all** products + full access to User Management (`GET /api/users`, `DELETE /api/users/{id}`) |

### Features Included:
- **BCrypt Password Hashing:** Passwords are encrypted before persisting to MongoDB.
- **JWT Authentication:** Stateless Bearer tokens passed via `Authorization: Bearer <TOKEN>`.
- **Rate Limiting (Bucket4j):** Protects against brute-force attacks (20 requests/min per IP).
- **CORS Configured:** Broad cross-origin support enabled for front-end integration.

---

## Comprehensive API Testing Guide (Postman / cURL)

### 1. Register a Normal User
- **HTTP Method:** `POST`
- **URL:** `https://localhost:8080/api/auth/register`
- **Headers:** `Content-Type: application/json`
- **Body (JSON):**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "role": "USER"
}
```
- **Expected Status:** `201 Created`

---

### 2. Register an Admin User
- **HTTP Method:** `POST`
- **URL:** `https://localhost:8080/api/auth/register`
- **Headers:** `Content-Type: application/json`
- **Body (JSON):**
```json
{
  "name": "Admin User",
  "email": "admin@example.com",
  "password": "adminpassword123",
  "role": "ADMIN"
}
```
- **Expected Status:** `201 Created`

---

### 3. Login to Obtain JWT Token
- **HTTP Method:** `POST`
- **URL:** `https://localhost:8080/api/auth/login`
- **Headers:** `Content-Type: application/json`
- **Body (JSON):**
```json
{
  "email": "john@example.com",
  "password": "password123"
}
```
- **Expected Status:** `200 OK`
- **Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "email": "john@example.com",
  "role": "ROLE_USER"
}
```

---

### 4. Create a Product (Authenticated User)
- **HTTP Method:** `POST`
- **URL:** `https://localhost:8080/products`
- **Headers:** 
  - `Content-Type: application/json`
  - `Authorization: Bearer <YOUR_JWT_TOKEN>`
- **Body (JSON):**
```json
{
  "name": "PlayStation 5",
  "description": "Next-Gen Gaming Console",
  "price": 499.99
}
```
- **Expected Status:** `201 Created`

---

### 5. Get All Products (Public — No Auth Needed)
- **HTTP Method:** `GET`
- **URL:** `https://localhost:8080/products`
- **Expected Status:** `200 OK`

---

### 6. Update Product (Owner / Admin Only)
- **HTTP Method:** `PUT`
- **URL:** `https://localhost:8080/products/{PRODUCT_ID}`
- **Headers:** `Authorization: Bearer <YOUR_JWT_TOKEN>`
- **Body (JSON):**
```json
{
  "name": "PlayStation 5 Pro",
  "description": "Upgraded Console",
  "price": 699.99
}
```
- **Expected Status:** `200 OK` *(Returns `403 Forbidden` if accessed by non-owner user)*

---

### 7. Delete Product (Owner / Admin Only)
- **HTTP Method:** `DELETE`
- **URL:** `https://localhost:8080/products/{PRODUCT_ID}`
- **Headers:** `Authorization: Bearer <YOUR_JWT_TOKEN>`
- **Expected Status:** `204 No Content`

---

### 8. Admin Endpoint: Get All Users (Admin Only)
- **HTTP Method:** `GET`
- **URL:** `https://localhost:8080/api/users`
- **Headers:** `Authorization: Bearer <ADMIN_JWT_TOKEN>`
- **Expected Status:** `200 OK` *(Returns `403 Forbidden` if accessed by standard user)*

---

## Error Handling & Status Codes

All API errors are globally caught and returned as clean JSON responses:

- `400 Bad Request` — Validation failures (e.g. invalid email format, missing fields).
- `401 Unauthorized` — Invalid credentials or missing JWT token.
- `403 Forbidden` — Accessing restricted resources (e.g., standard user accessing admin endpoints).
- `404 Not Found` — Resource missing (e.g., product/user ID not found).
- `429 Too Many Requests` — Exceeding Bucket4j rate limit threshold.

## 🔒 Security & SSL Configuration

### 1. Generate SSL Certificate Keystore
You can generate a local PKCS12 keystore using Java's `keytool` utility:

```bash
keytool -genkeypair -alias letsplay -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore keystore.p12 -validity 365
```