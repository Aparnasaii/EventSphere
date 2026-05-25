# EventSphere — Event Management Platform

A full-stack microservices application for discovering, booking, and managing events.
Built with Spring Boot (Java 17), Angular 16, MySQL, Stripe, and Eureka service discovery.

---

## Features

### For Attendees
- Browse upcoming events with category, location, and date filters
- Book free or paid tickets — paid bookings flow through Stripe Checkout
- View registered events, past events, and ticket history
- Cancel tickets before the event starts
- Leave star-ratings and comments after attending
- Dashboard reminders for pending feedback and today's events
- Password reset via email

### For Organizers
- Create, edit, and delete their own events
- Track ticket bookings and remaining seat capacity
- Receive notifications when attendees book or cancel
- View aggregated feedback (average rating + all reviews)

### For Admins
- Approve / reject pending organizer registrations
- Suspend, reactivate, and delete users
- Full system overview (total users, organizers, attendees, pending, suspended)
- View every event and every users feedback

### System
- JWT-based authentication routed through an API Gateway
- Eureka service registry for inter-service discovery
- Asynchronous email notifications (SendGrid for bulk, Gmail SMTP for transactional)
- Database-driven email templates (welcome, ticket confirmed, event updated, feedback request, etc.)
- Stripe Checkout integration with success / cancel webhooks
- Automatic feedback-cycle initiation after events end
- Role-based authorization on every endpoint (`ROLE_ADMIN`, `ROLE_ORGANIZER`, `ROLE_ATTENDEE`)

---

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | Angular 17, TypeScript, Lucide Icons, RxJS, Reactive Forms |
| Backend | Spring Boot 3.x, Spring Security, Spring Cloud (OpenFeign, Gateway, Eureka) |
| Database | MySQL 8 |
| Auth | JWT (HMAC-SHA256) |
| Payments | Stripe Checkout (test mode) |
| Mail | SendGrid + Gmail SMTP |
| Build | Maven, npm |
| Java | 21 |

---

## Architecture

```
                         ┌──────────────────┐
                         │  Angular (4200)  │
                         └────────┬─────────┘
                                  │
                                  ▼
                       ┌─────────────────────┐
                       │  API Gateway (8081) │  ← JWT validation
                       └──┬───┬───┬───┬───┬──┘
                          │   │   │   │   │
        ┌─────────────────┘   │   │   │   └─────────────────┐
        ▼                     ▼   ▼   ▼                     ▼
┌────────────────┐   ┌────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│  User (8082)   │   │  Event (8083)  │  │ Ticket (8084)   │  │ Feedback (8086) │
└────────────────┘   └────────────────┘  └─────────────────┘  └─────────────────┘
        │                     │                  │                     │
        └──────────┬──────────┴──────────┬───────┘                     │
                   ▼                     ▼                             ▼
          ┌────────────────────┐  ┌─────────────────────┐    ┌─────────────────────┐
          │  Eureka (8761)     │  │ Notification (8085) │◄───┴─────────────────────┘
          └────────────────────┘  └─────────────────────┘
                                        │
                                        ▼
                                  SMTP + SendGrid
```

Each service is independently deployable, has its own database tables, and registers itself with the Eureka registry. Inter-service calls go through Feign clients (logical service names, not hard-coded hosts).

---

## Project Structure

```
Internship Project/
├── Event Management System/
│   ├── api-gateway/         (port 8081 — JWT auth + routing)
│   ├── eureka_server/       (port 8761 — service discovery)
│   ├── user-service/        (port 8082 — users, auth, password reset)
│   ├── event-service/       (port 8083 — events, categories, seats)
│   ├── ticket-service/      (port 8084 — bookings, Stripe checkout)
│   ├── feedback-service/    (port 8086 — ratings & reviews)
│   └── notification-service/(port 8085 — email templates + sending)
│
├── Frontend/                (Angular 17 SPA — port 4200)
│   ├── src/app/components/
│   │   ├── auth/            (login, register, password reset)
│   │   ├── dashboard/
│   │   ├── events/          (list, detail, create/edit form)
│   │   ├── tickets/         (my-tickets, booking flow)
│   │   ├── feedback/
│   │   └── admin/           (manage-users, pending-organizers)
│   └── src/app/services/    (HTTP services per backend resource)
│
├── .env.example             (template for required env vars)
├── .gitignore
└── README.md
```

---

## Prerequisites

- Java 17+
- Node.js 18+ and npm
- Maven 3.9+
- MySQL 8 running on `localhost:3306`
- A Stripe test account (free) — get a key from https://dashboard.stripe.com/test/apikeys
- A SendGrid account (free tier) — for sending bulk emails
- A Gmail account with an App Password — for transactional emails

---

## Setup

### 1. Clone the repository

```powershell
git clone https://github.com/aparna-cts781/EventSphere.git
cd EventSphere
```

### 2. Create the MySQL database

```sql
CREATE DATABASE eventmanagementdb;
```

All services use this single database. Tables auto-create on first run (`spring.jpa.hibernate.ddl-auto=update`).

### 3. Set environment variables (Windows PowerShell — one-time, permanent)

```powershell
[Environment]::SetEnvironmentVariable("STRIPE_SECRET_KEY", "sk_test_your_key_here", "User")
[Environment]::SetEnvironmentVariable("MAIL_PASSWORD",     "SG.your_sendgrid_key", "User")
[Environment]::SetEnvironmentVariable("MAIL_USERNAME",     "your.gmail@gmail.com", "User")
[Environment]::SetEnvironmentVariable("MAIL_FROM",         "your.gmail@gmail.com", "User")
```

**Close and reopen** every PowerShell window after running these — env vars only load at shell startup.

Optional overrides (defaults shown):

| Variable | Default |
|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/eventmanagementdb` |
| `DB_USERNAME` | `root` |
| `DB_PASSWORD` | `root` |
| `JWT_SECRET` | built-in placeholder (override in production!) |
| `ADMIN_EMAIL` | `admin@event.com` |
| `ADMIN_PASSWORD` | `admin123` |

### 4. Load email templates into the database

Connect to MySQL and run the SQL in `Event Management System/sql/notification_templates.sql`
(or use the inline INSERTs in the project docs).

The 9 templates required are:
`WELCOME_USER`, `TICKET_CONFIRMED`, `TICKET_PENDING`, `TICKET_FAILED`, `TICKET_CANCELLED`,
`EVENT_CREATED`, `EVENT_UPDATED`, `EVENT_DELETED`, `FEEDBACK_REQUEST`.

### 5. Start the backend services (each in its own terminal)

Start them **in this order**:

```powershell
# 1. Service registry first
cd "Event Management System\eureka_server"
mvn spring-boot:run

# 2. API gateway
cd "Event Management System\api-gateway"
mvn spring-boot:run

# 3. Microservices (any order)
cd "Event Management System\user-service"          ; mvn spring-boot:run
cd "Event Management System\event-service"         ; mvn spring-boot:run
cd "Event Management System\ticket-service"        ; mvn spring-boot:run
cd "Event Management System\feedback-service"      ; mvn spring-boot:run
cd "Event Management System\notification-service"  ; mvn spring-boot:run
```

Once all are up, check Eureka at http://localhost:8761 — all 6 services should appear as `UP`.

### 6. Start the frontend

```powershell
cd Frontend
npm install
ng serve
```

Open http://localhost:4200 in your browser.

### 7. Login as admin

| Field | Value |
|---|---|
| Email | `admin@event.com` |
| Password | `admin123` |

The admin user is auto-created on first run of `user-service`. Change `ADMIN_PASSWORD` env var before deployment to anything other than this default.

---

## Key Endpoints

| Service | Endpoint | Purpose |
|---|---|---|
| API Gateway | `POST /users/api/auth/login` | Authenticate, returns JWT |
| User | `POST /users/api/users/register` | New attendee or organizer signup |
| User | `PUT /users/api/users/admin/suspend/{id}` | Admin suspends a user |
| User | `DELETE /users/api/users/admin/{id}` | Admin deletes a user |
| Event | `POST /events` | Organizer creates a new event |
| Event | `POST /events/{id}/feedback` | Initiate the feedback cycle |
| Ticket | `POST /tickets/book` | Book a ticket (Stripe redirect if paid) |
| Ticket | `GET /tickets/payment/success?session_id=...` | Stripe success webhook |
| Feedback | `PUT /feedbacks/feedback/submit/{id}` | Attendee submits rating |
| Feedback | `GET /feedbacks/feedback/event/{eId}/user/{uId}` | Fetch a users feedback |
| Notification | `POST /notifications/send` | Trigger any template by name |

---

## Security

- All passwords are **BCrypt-hashed** before storage.
- JWTs are signed with HMAC-SHA256 and validated on every gateway request.
- Suspended / inactive / rejected users are blocked from logging in.
- Admin accounts cannot be suspended or deleted (UI- and backend-enforced).
- Method-level `@PreAuthorize` enforces role boundaries on every controller.
- No secrets are committed to git — all read from environment variables via `${VAR:default}` placeholders.
- `.gitignore` excludes `target/`, `node_modules/`, `.env`, `.idea/`, etc.

---

## Common Issues

| Problem | Fix |
|---|---|
| `Connection refused` on port 8761 | Start `eureka_server` first |
| `Data truncated for column 'status'` | Run `ALTER TABLE users MODIFY COLUMN status VARCHAR(20);` |
| `Template not found: TICKET_FAILED` | Load all 9 notification templates into the database |
| `Stripe: Amount must be at least 50 INR` | Stripe Indias minimum is 50; raise the event price |
| `403 on /admin/...` endpoint | Make sure youre logged in as admin; check the JWT in DevTools |
| `Port 8082 already in use` | An old user-service process is still running — kill it |
| Suspend / delete button does nothing | Restart `user-service` after backend code changes |
| Feedback form not ready | Auto-triggers on first attendee visit after event ends |

---

## Development Tips

- Backend hot reload: include `spring-boot-devtools` (already a dependency)
- Frontend hot reload: `ng serve` watches automatically
- View live API logs: each services console shows formatted SQL + filter chain
- Database admin: use MySQL Workbench or DBeaver
- Test Stripe payments: use card `4242 4242 4242 4242`, any future expiry, any CVC

---

## Roadmap / Ideas

- Real-time notifications via WebSockets
- Event image uploads (Cloudinary / S3)
- Multi-language support
- Mobile app (React Native)
- Analytics dashboard for organizers (revenue, attendance trends)
- QR-code ticket scanning at the door
- Refund flow for cancelled paid tickets

---

## License

MIT — feel free to fork, study, and adapt.
