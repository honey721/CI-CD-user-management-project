# 📬 Notification + Audit Service

> **Consumer microservice** for the USER_MGMT system.  
> Listens to RabbitMQ events published by `user-mgmt-service` and reacts with email notifications + audit logging.

---

## 🏗️ How It Fits in the Architecture

```
┌─────────────────────────┐         ┌──────────────┐         ┌──────────────────────────────┐
│   user-mgmt-service     │──────▶  │  RabbitMQ    │──────▶  │  notification-audit-service  │
│   (Producer)  :8081     │  events │  :5672       │  queues │  (Consumer)          :8082   │
│                         │         │              │         │                              │
│  POST /api/users/       │         │ user.events  │         │  → Sends Welcome Email       │
│       register    ──────┼────────▶│   .exchange  │────────▶│  → Sends Login Alert Email   │
│  POST /api/users/       │         │              │         │  → Saves Audit Log to DB     │
│       login       ──────┼────────▶│              │         │  → Exposes Audit REST APIs   │
└─────────────────────────┘         └──────────────┘         └──────────────────────────────┘
         │                                                                  │
   MySQL :3307                                                        MySQL :3308
   (usermgmt DB)                                                     (auditdb DB)
```

---

## ✨ What This Service Does

### 1. 📧 Email Notifications
| Event Received        | Email Sent               |
|-----------------------|--------------------------|
| `user.registered`     | 🎉 Welcome Email         |
| `user.loggedin`       | 🔐 Login Alert Email     |

### 2. 📋 Audit Trail
Every event is persisted to its own MySQL database (`auditdb`) with:
- Event type
- User email & username  
- Original event timestamp
- Processing timestamp
- Email delivery status
- Error message (if failed)

### 3. 🔍 Audit REST APIs

| Method | Endpoint                        | Description                    |
|--------|---------------------------------|--------------------------------|
| GET    | `/audit/logs`                   | Latest 20 logs (all users)     |
| GET    | `/audit/logs/user/{email}`      | All logs for a specific user   |
| GET    | `/audit/logs/type/{eventType}`  | Filter by USER_REGISTERED etc. |
| GET    | `/audit/stats`                  | Totals + daily breakdown       |
| GET    | `/audit/health`                 | Service health check           |

---

## 🚀 Running the Full System

### Prerequisites
- Docker & Docker Compose installed
- Both projects cloned side by side:

```
USER_MGMT_SYSTEM/
├── user-mgmt-service/           ← existing producer
└── notification-audit-service/  ← this project
    └── docker-compose.yml       ← runs everything
```

### Start Everything
```bash
cd notification-audit-service
docker-compose up --build
```

### Service URLs
| Service              | URL                         |
|----------------------|-----------------------------|
| USER_MGMT API        | http://localhost:8081       |
| Audit API            | http://localhost:8082/audit |
| RabbitMQ UI          | http://localhost:15672      |
| RabbitMQ credentials | guest / guest               |

---

## 🧪 Testing the Flow

### Step 1 — Register a user (hits producer)
```bash
curl -X POST http://localhost:8081/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"username": "john", "email": "john@example.com", "password": "123456", "role": "USER"}'
```

### Step 2 — Check audit log (hits consumer)
```bash
curl http://localhost:8082/audit/logs/user/john@example.com
```

### Step 3 — Login (hits producer)
```bash
curl -X POST http://localhost:8081/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"email": "john@example.com", "password": "123456"}'
```

### Step 4 — Check stats
```bash
curl http://localhost:8082/audit/stats
```

---

## ⚙️ Email Configuration

Edit `application.properties` or pass as Docker environment variables:

**For development (Mailtrap — catches emails without sending):**
```properties
spring.mail.host=smtp.mailtrap.io
spring.mail.port=2525
spring.mail.username=your_mailtrap_username
spring.mail.password=your_mailtrap_password
```
Sign up free at https://mailtrap.io

**For production (Gmail SMTP):**
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your@gmail.com
spring.mail.password=your_app_password
```

---

## 📂 Project Structure

```
notification-audit-service/
├── src/main/java/com/example/notificationaudit/
│     ├── NotificationAuditApplication.java   ← Entry point
│     ├── listener/
│     │     └── UserEventListener.java        ← RabbitMQ consumers (CORE)
│     ├── service/
│     │     ├── EmailService.java             ← Sends HTML emails
│     │     └── AuditService.java             ← Saves & queries audit logs
│     ├── controller/
│     │     └── AuditController.java          ← REST API
│     ├── entity/
│     │     └── AuditLog.java                 ← DB entity
│     ├── repository/
│     │     └── AuditLogRepository.java       ← JPA queries
│     ├── dto/
│     │     ├── UserRegisteredEvent.java      ← Matches producer payload
│     │     └── UserLoggedInEvent.java        ← Matches producer payload
│     └── config/
│           └── RabbitMQConfig.java           ← Exchange, Queue, Binding setup
├── src/main/resources/
│     ├── application.properties
│     └── templates/
│           ├── welcome-email.html            ← Thymeleaf email template
│           └── login-alert-email.html        ← Thymeleaf email template
├── Dockerfile
├── docker-compose.yml                        ← Full system compose
└── pom.xml
```

---

## 🔮 Future Enhancements

- **Dead Letter Queue (DLQ)** — Re-queue failed messages automatically
- **SMS Notifications** — Add Twilio for SMS alerts on login
- **Spring Cloud Gateway** — Unified API gateway for both services
- **Analytics Service** — A third microservice consuming the same events for dashboards
- **Pagination** on audit log APIs
