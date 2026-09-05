# 📚 User Management System – Spring Boot + Docker + MySQL + RabbitMQ

A production-ready **User Management System** built using modern enterprise-level technologies. This project demonstrates authentication, authorization, event publishing, and full Dockerized deployment.

---

# 🚀 Tech Stack

* **Spring Boot 3.x**
* **Spring Security + JWT Authentication**
* **Role-Based Authorization**
* **MySQL (Dockerized)**
* **RabbitMQ (Event Messaging)**
* **Spring Data JPA + Hibernate**
* **Docker + Docker Compose**
* **Maven**

---

# ✨ Features

## 🔐 Authentication & Authorization

* JWT-based authentication
* Stateless API
* Secure password hashing using BCrypt
* Role-based authorization using:

  ```java
  @PreAuthorize("hasAuthority('ADMIN')")
  ```
* In-built roles: `ADMIN`, `USER`

## 👤 User Features

* Register new users
* User login (returns JWT)
* View authenticated user profile
* Assign roles (ADMIN only)

## 🛡 Role Management

* Create new roles
* Assign multiple roles to a user
* Default `ADMIN` and `USER` roles auto-created using `CommandLineRunner`

## 📡 RabbitMQ Event System

Events published automatically:

* `user.registered`
* `user.loggedin`

Helps simulate real microservice communication.

---

# 📂 Project Structure

```
src/
 ├── controller/
 │     ├── AuthController.java
 │     ├── UserRoleController.java
 │     ├── RoleController.java
 │
 ├── entity/
 │     ├── User.java
 │     ├── Role.java
 │
 ├── repository/
 │     ├── UserRepository.java
 │     ├── RoleRepository.java
 │
 ├── config/
 │     ├── SecurityConfig.java
 │     ├── JWTFilter.java
 │     ├── DataInitializer.java
 │
 ├── service/
 │     ├── UserService.java
 │     ├── EventPublisherService.java
 │     ├── JWTUtil.java
 │
 ├── UserManagementApplication.java
```

---

# 🗄 Database Schema (ER Diagram)

## **Users Table**

| Column   | Type    | Notes           |
| -------- | ------- | --------------- |
| id       | BIGINT  | Primary Key     |
| username | VARCHAR | Unique          |
| email    | VARCHAR | Unique          |
| password | VARCHAR | Hashed (BCrypt) |

## **Roles Table**

| Column | Type    | Notes       |
| ------ | ------- | ----------- |
| id     | BIGINT  | Primary Key |
| name   | VARCHAR | ADMIN/USER  |

## **user_roles (Join Table)**

| user_id | role_id |
| ------- | ------- |

---

# 🐳 Docker Setup (App + MySQL + RabbitMQ)

Run the complete system using **Docker Compose**.

## **docker-compose.yml**

```yaml
version: "3.8"
services:
  mysql:
    image: mysql:8.0
    container_name: mysql
    environment:
      MYSQL_ROOT_PASSWORD: rootpassword
      MYSQL_DATABASE: usermgmt
    ports:
      - "3307:3306"
    volumes:
      - mysql-data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-prootpassword"]
      interval: 10s
      timeout: 5s
      retries: 5

  rabbitmq:
    image: rabbitmq:3.11-management
    container_name: rabbitmq
    ports:
      - "5672:5672"
      - "15672:15672"

  app:
    build: .
    container_name: userapp
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/usermgmt?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: rootpassword
      SPRING_RABBITMQ_HOST: rabbitmq
    depends_on:
      mysql:
        condition: service_healthy
      rabbitmq:
        condition: service_started
    ports:
      - "8081:8081"

volumes:
  mysql-data:
```

---

# 🛠 Dockerfile

```dockerfile
# Build Stage
FROM maven:3.9.2-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run Stage
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

# ▶️ Running the Project

### 🟩 Step 1: Build & Start

```bash
docker-compose up --build
```

### 🟩 Step 2: Access Services

* **App:** [http://localhost:8081](http://localhost:8081)
* **MySQL:** localhost:3307
* **RabbitMQ UI:** [http://localhost:15672](http://localhost:15672)

  * username: guest
  * password: guest

---

# 🧪 Testing APIs

## 🔸 1. Register User

```
POST /api/users/register
```

Body:

```json
{
  "username": "john",
  "email": "john@example.com",
  "password": "123456",
  "role": "USER"
}
```

## 🔸 2. Login

```
POST /api/users/login
```

Response:

```json
{
  "token": "<jwt-token>"
}
```

## 🔸 3. Create Role (ADMIN Only)

```
POST /api/roles
Authorization: Bearer <ADMIN_TOKEN>
```

Body:

```json
{
  "name": "MANAGER"
}
```

## 🔸 4. Assign Role to User

```
POST /api/users/{id}/roles
Authorization: Bearer <ADMIN_TOKEN>
```

Body:

```json
["ADMIN"]
```

---

# 📩 RabbitMQ Events

### Event: `user.registered`

Payload:

```json
{
  "id": 5,
  "email": "john@example.com",
  "username": "john",
  "time": "2025-01-01T10:20:00"
}
```

### Event: `user.loggedin`

Payload:

```json
{
  "email": "john@example.com",
  "time": "2025-01-01T10:25:00"
}
```

---

# 📘 Conclusion

This project showcases:

✔ JWT Authentication
✔ Role-Based Authorization
✔ RabbitMQ Event Publishing
✔ Full Docker Deployment
✔ Clean Modular Architecture
✔ Real MySQL Database
✔ Developer-friendly setup

--
