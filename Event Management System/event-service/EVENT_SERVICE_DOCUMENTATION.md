# Event Service Module Documentation

## 1) Module Summary

`event-service` is a Spring Boot microservice responsible for event lifecycle management in an event management ecosystem.

Core responsibilities:
- Create, update, soft-delete, and query events.
- Manage seat inventory (`availableSeats`) for bookings/cancellations.
- Expose ticket-pricing and capacity details for other services.
- Trigger downstream notifications, ticket cleanup, and feedback workflows via Feign clients.

Entry point:
- `src/main/java/com/event/eventservice/EventServiceApplication.java`

Base URL defaults:
- `http://localhost:8084`
- API base path: `/events`

---

## 2) Tech Stack and Dependencies

From `pom.xml`:
- Java `21`
- Spring Boot `3.2.5`
- Spring Cloud BOM `2023.0.4`
- Web: `spring-boot-starter-web`
- Validation: `spring-boot-starter-validation`
- Persistence: `spring-boot-starter-data-jpa` + MySQL connector
- Security: `spring-boot-starter-security` (currently configured to permit all requests)
- Service Discovery: Eureka client
- Inter-service calls: OpenFeign
- API docs: springdoc OpenAPI UI
- Actuator enabled dependency
- JWT libs present (`jjwt-*`) but not currently wired into request authentication flow

---

## 3) Package-Level Architecture

- `controller`
  - `EventController`: REST endpoints under `/events`.
- `service`
  - `EventService`: business interface.
  - `EventServiceImpl`: core business logic and Feign orchestration.
  - `NotificationClient`, `TicketClient`, `FeedbackClient`: downstream integrations.
- `repository`
  - `EventRepository`: Spring Data JPA query methods for active/deleted and filtered retrieval.
- `models`
  - `Event`: JPA entity for event records.
- `dto`
  - `EventDTO`, `FeedbackDTO`, `NotificationRequestDTO`.
- `exceptions`
  - Domain exceptions + `GlobalExceptionHandler` with unified error response body.
- `config`
  - `SecurityConfig`: permits all requests; CSRF disabled.
  - `OpenApiConfig`: OpenAPI metadata.

---

## 4) Data Model (`Event`)

File: `src/main/java/com/event/eventservice/models/Event.java`

Main fields:
- Identity: `eventId`
- Descriptive: `name`, `category`, `description`, `location`, `image`
- Scheduling: `eventDate`, `startTime`, `endTime`
- Capacity/pricing: `totalCapacity`, `availableSeats`, `ticketPrice`
- Ownership: `organizerId`
- Operational: `status`, `createdAt`, `updatedAt`, `isDeleted`

Validation highlights:
- Mandatory fields enforced with Jakarta validation annotations.
- `eventDate` must be current/future.
- `totalCapacity >= 1`, `availableSeats >= 0`, `ticketPrice >= 0.0`.

Soft-delete strategy:
- Logical deletion via `isDeleted=true`.
- Most reads filter on `isDeleted=false`.

---

## 5) API Endpoints

Controller file: `src/main/java/com/event/eventservice/controller/EventController.java`

### Core CRUD

- `POST /events`
  - Creates event.
  - Request body: `Event`
  - Returns: created `Event`

- `GET /events`
  - Lists all active events.
  - Returns: `List<Event>`

- `GET /events/{id}`
  - Fetch event by ID (active only).
  - Returns: `Event`

- `PUT /events/{id}`
  - Updates event details.
  - Request body: `Event`
  - Returns: updated `Event`

- `DELETE /events/{id}`
  - Soft-deletes event and triggers ticket/notification side effects.
  - Returns: success message string

### Ticket/Seat Utilities

- `GET /events/{id}/ticket-info`
  - Returns minimal ticket/capacity view.
  - Response: `EventDTO` (`eventId`, `ticketPrice`, `totalCapacity`, `availableSeats`)

- `PUT /events/{id}/reduce-seats?count={n}`
  - Decreases available seats.
  - Returns: `200 OK` with empty body

- `PUT /events/{id}/increase-seats?count={n}`
  - Increases available seats up to `totalCapacity`.
  - Returns: `200 OK` with empty body

### Workflow Trigger

- `POST /events/{id}/feedback`
  - Initiates feedback workflow through `feedback-service`.
  - Returns: success message string

### Search/Filter

- `GET /events/search?category=&location=&date=`
  - Dynamic filter: category/location/date combinations.
  - Returns: `List<Event>`

- `GET /events/category/{category}`
  - Returns events by category.

- `GET /events/location/{location}`
  - Returns events by location.

- `GET /events/organizer/{organizerId}`
  - Returns events by organizer.

- `GET /events/range?start={ISO_DATE_TIME}&end={ISO_DATE_TIME}`
  - Returns events with `startTime` in given range.

---

## 6) Business Logic Notes (`EventServiceImpl`)

File: `src/main/java/com/event/eventservice/service/EventServiceImpl.java`

- On create:
  - Initializes `availableSeats = totalCapacity`, `isDeleted=false`, `status="Active"`, timestamps.
  - Triggers notification through `notification-service`.

- On update:
  - Updates mutable fields.
  - If capacity changes, adjusts `availableSeats` by delta.
  - Triggers notification.

- On delete:
  - Marks event as deleted.
  - Calls `ticket-service` cleanup endpoint.
  - Sends notification.

- Seat management:
  - `reduceSeats`: throws `InsufficientSeatsException` if not enough seats.
  - `increaseSeats`: throws `SeatCapacityException` if exceeding capacity.

- Search:
  - Branches through all category/location/date combinations.
  - Falls back to all active events when no filters are provided.

---

## 7) Inter-Service Contracts (Feign)

- `NotificationClient` (`@FeignClient("notification-service")`)
  - Method: `GET /api/v1/notifications/send` with request body `NotificationRequestDTO`

- `TicketClient` (`@FeignClient(name="ticket-service")`)
  - Method: `GET /tickets/event/{eventId}` for event-linked ticket deletion

- `FeedbackClient` (`@FeignClient(name="feedback-service")`)
  - Method: `POST /api/feedback/initiate` with `FeedbackDTO`

Note:
- Feign endpoints are expected to resolve through service discovery (`Eureka`) using service names.

---

## 8) Exception Handling and Error Responses

Main file:
- `src/main/java/com/event/eventservice/exceptions/GlobalExceptionHandler.java`

Response format:
- `ErrorResponse { message, status, timestamp }`

Mapped handlers include:
- `EventNotFoundException` -> `404`
- `InvalidEventException` -> `400`
- `EventAlreadyDeletedException` -> `404`
- `InsufficientSeatsException` -> `409`
- `SeatCapacityException` -> `400`
- Validation errors (`MethodArgumentNotValidException`) -> `400`
- Fallback `Exception` -> `500`

---

## 9) Runtime Configuration

File: `src/main/resources/application.properties`

Current defaults:
- App name: `event-service`
- Port: `8084`
- Eureka server: `http://localhost:8761/eureka/`
- Data source: MySQL (`eventmanagementdb`) with username/password `root`
- Hibernate DDL: `update`
- SQL logging: enabled
- OpenAPI docs: `/v3/api-docs`
- Swagger UI: `/swagger-ui.html`

Security behavior:
- All HTTP requests are currently permitted (`SecurityConfig`).
- CSRF is disabled.

---

## 10) Build, Run, and Test

### Prerequisites
- JDK 21
- Maven wrapper (`mvnw.cmd` already included)
- MySQL running with a database matching configured URL
- Optional but expected in microservice mode: Eureka server and dependent services (`notification-service`, `ticket-service`, `feedback-service`)

### Build

```powershell
Set-Location "C:\Project\event-service"
.\mvnw.cmd clean package
```

### Run

```powershell
Set-Location "C:\Project\event-service"
.\mvnw.cmd spring-boot:run
```

### Test

```powershell
Set-Location "C:\Project\event-service"
.\mvnw.cmd test
```

Current automated tests in repository:
- `EventServiceApplicationTests` contains only context-load verification.

---

## 11) Observed Gaps and Risks

1. Exception type inconsistency in service layer:
   - `EventServiceImpl` often throws generic `RuntimeException` (for not found/capacity validation) instead of domain exceptions already defined.
   - This can bypass specific handler semantics and return generic `500` in some flows.

2. Feign HTTP method semantics appear unusual:
   - `NotificationClient` and `TicketClient` use `GET` with side-effect operations (send/delete).
   - Non-idempotent operations are typically `POST`/`DELETE`.

3. Notification payload issue during event creation:
   - `request.setEventId(event.getEventId())` occurs before persistence, so ID may be `null` at send time.

4. Security is effectively open:
   - All requests are permitted and CSRF is disabled.
   - JWT dependencies exist in `pom.xml` but auth filters/token validation are not present.

5. Typographical/quality issues in messages and naming:
   - Example: "Capacity nust be greater than 0", "delered", method `deletedTicketByEvent`.
   - Does not break runtime, but affects maintainability and API clarity.

6. Test coverage is minimal:
   - No controller/service/repository integration tests for functional behavior, validation paths, or downstream failure handling.

---

## 12) Recommended Next Improvements

- Replace generic exceptions in service layer with existing domain exceptions and align all mapped statuses.
- Revisit Feign endpoint methods and paths to match REST semantics and downstream contracts.
- Send notification after persistence on create (or include generated ID in a second step).
- Introduce proper authentication/authorization policy if required in production.
- Add unit/integration tests for CRUD, search combinations, seat boundaries, and soft-delete behaviors.
- Externalize secrets (DB credentials) via environment variables or secure config management.

---

## 13) Key File Index

- `pom.xml`
- `src/main/resources/application.properties`
- `src/main/java/com/event/eventservice/EventServiceApplication.java`
- `src/main/java/com/event/eventservice/controller/EventController.java`
- `src/main/java/com/event/eventservice/service/EventService.java`
- `src/main/java/com/event/eventservice/service/EventServiceImpl.java`
- `src/main/java/com/event/eventservice/repository/EventRepository.java`
- `src/main/java/com/event/eventservice/models/Event.java`
- `src/main/java/com/event/eventservice/config/SecurityConfig.java`
- `src/main/java/com/event/eventservice/config/OpenApiConfig.java`
- `src/main/java/com/event/eventservice/exceptions/GlobalExceptionHandler.java`
- `src/test/java/com/event/eventservice/EventServiceApplicationTests.java`

