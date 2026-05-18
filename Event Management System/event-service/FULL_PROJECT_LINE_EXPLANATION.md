# Full Project Line Explanation (event-service)

This document explains the project file-by-file and line-by-line for all maintained source/config files.

## Scope
- Included: `pom.xml`, `src/main/resources/application.properties`, all Java files under `src/main/java`, and test file under `src/test/java`.
- Excluded: generated artifacts under `target/`.

---

## `pom.xml` (1-136)

- L1-L5: XML declaration + Maven project root and schema references.
- L6-L11: Declares Spring Boot parent POM (`spring-boot-starter-parent`, `3.2.5`).
- L13-L17: Project coordinates and metadata (`groupId`, `artifactId`, `version`, `name`, `description`).
- L19-L22: Build properties (Java 21, Spring Cloud BOM version).
- L24-L104: Dependency list for Eureka, OpenFeign, OpenAPI, Actuator, Web, Validation, JPA, Security, Devtools, MySQL, Lombok, JWT libs, and test dependencies.
- L106-L116: Imports Spring Cloud dependency BOM via dependency management.
- L118-L134: Build plugin configuration for Spring Boot Maven plugin and Lombok exclusion.
- L136: Closes project XML.

---

## `src/main/resources/application.properties` (1-20)

- L1: Sets service name to `event-service`.
- L2: Runs on port `8084`.
- L3-L5: Eureka registration and health-check behavior.
- L6-L10: MySQL datasource and Hibernate dialect configuration.
- L11: Comment listing valid DDL modes.
- L12: DDL mode set to `update`.
- L13: Enables SQL logging.
- L15: Comment for OpenAPI settings.
- L16-L17: OpenAPI docs and Swagger UI paths.
- L14, L18-L20: Blank separators.

---

## `src/main/java/com/event/eventservice/EventServiceApplication.java` (1-16)

- L1: Declares package.
- L3-L5: Imports Spring Boot + Feign types.
- L7: Marks class as Spring Boot application.
- L8: Enables Feign client scanning.
- L9: Declares main application class.
- L11-L13: Standard Java entry point calling `SpringApplication.run(...)`.
- L2, L10, L14-L16: Formatting/spacing + closing braces.

---

## `src/main/java/com/event/eventservice/config/OpenApiConfig.java` (1-28)

- L1: Package declaration.
- L3-L8: OpenAPI model + Spring config imports.
- L10: Marks class as configuration bean source.
- L11: Class declaration.
- L13: Declares `@Bean` factory.
- L14-L26: Builds and returns OpenAPI metadata (`title`, `description`, `version`, `contact`, `license`).
- L27-L28: Class close.
- L2, L9, L12: Spacing for readability.

---

## `src/main/java/com/event/eventservice/config/SecurityConfig.java` (1-17)

- L1: Package declaration.
- L3-L6: Security and config imports.
- L8: Declares configuration class.
- L9: Class declaration.
- L11: Bean declaration for `SecurityFilterChain`.
- L12: Method signature for security config.
- L13: Disables CSRF and permits all requests.
- L14: Returns built filter chain.
- L15-L16: Method/class closing braces.
- L2, L7, L10, L17: Formatting lines.

---

## `src/main/java/com/event/eventservice/models/Event.java` (1-83)

- L1: Package declaration.
- L3-L7: JPA, validation, Lombok, and time imports.
- L9-L12: Entity/table mapping and Lombok `@Data` annotation; class declaration.
- L14-L16: Primary key field with identity generation.
- L18-L21: `name` field with required + size constraints.
- L23-L25: `status` required field.
- L27-L29: `category` required field.
- L31-L33: Optional long `description` with size limit and `TEXT` column.
- L35-L37: `location` required field.
- L39-L42: `eventDate` required + future-or-present + column mapping.
- L44-L47: `startTime` required + mapped column.
- L48-L50: `endTime` required + mapped column.
- L52-L55: `totalCapacity` required, minimum 1.
- L57-L60: `availableSeats` required, minimum 0.
- L62-L65: `ticketPrice` required, minimum 0.0.
- L67-L69: `organizerId` required.
- L71-L72: `createdAt` mapped column.
- L74-L75: `updatedAt` mapped column.
- L77: `image` field.
- L79-L80: Soft delete field `isDeleted` defaults to false.
- L81-L83: Closing braces + final spacing.

---

## `src/main/java/com/event/eventservice/repository/EventRepository.java` (1-30)

- L1: Package declaration.
- L2-L6: Imports JPA repo base type, model, and date/time/list types.
- L8: Repository interface extending `JpaRepository<Event, Long>`.
- L9: Comment for basic search methods.
- L10-L13: Methods for category/location/date/all with active (`isDeleted=false`) filter.
- L14: Comment for combined search.
- L15: Category + location active query.
- L16: Comment for active events.
- L17: Status + active query.
- L18: Comment for organizer-based queries.
- L19-L21: Organizer queries (all, active, deleted).
- L22: Query for past events by `endTime`.
- L23: Query for time range by `startTime`.
- L24: Existence check for active event by id.
- L25-L28: Additional combo queries with date.
- L29-L30: Interface closing + spacing.

---

## `src/main/java/com/event/eventservice/service/EventService.java` (1-24)

- L1: Package declaration.
- L3-L7: Imports model/date types/list.
- L9: Interface declaration.
- L10-L14: Core CRUD method contracts.
- L15-L17: Seat and feedback action contracts.
- L18-L22: Query and filter contract methods.
- L23-L24: Interface close + spacing.

---

## `src/main/java/com/event/eventservice/service/EventServiceImpl.java` (1-234)

- L1: Package declaration.
- L3-L17: Imports DTOs, exceptions, model/repository, transaction, logging, Spring annotations, and Java types.
- L18-L19: Marks class as service implementation.
- L21: Logger declaration.
- L23-L30: Injects repository and three Feign clients.

### `createEvent` (L31-L53)
- L31-L32: Method override + signature.
- L33: Logs create attempt.
- L34-L37: Validates total capacity and throws runtime error if invalid.
- L38-L42: Initializes seats, delete flag, status, created/updated timestamps.
- L43-L47: Builds notification payload for new event.
- L48-L49: Logs and sends notification.
- L50-L52: Saves event and returns persisted entity.
- L53: Method close.

### `getAllEvents` (L54-L60)
- L54-L55: Override + signature.
- L56: Log start.
- L57: Fetch active events only.
- L58-L59: Log count and return list.
- L60: Method close.

### `getEventById` (L61-L71)
- L61-L62: Override + signature.
- L63: Log requested id.
- L64-L68: Find by id, filter deleted, else throw runtime exception.
- L69-L70: Log success and return event.
- L71: Method close.

### `updateEvent` (L72-L100)
- L72-L73: Override + signature.
- L74-L75: Log and fetch existing event.
- L76-L83: Copy mutable fields from request.
- L84-L89: If capacity provided, compute diff and adjust capacity/seats.
- L90-L94: Build update notification payload.
- L95-L96: Log and send notification.
- L97-L99: Persist and return updated event.
- L100: Method close.

### `deleteEvent` (L101-L117)
- L101-L102: Override + signature.
- L103-L105: Log, fetch event, and mark as deleted.
- L106-L107: Trigger ticket cleanup for this event.
- L108-L112: Build delete notification payload.
- L113-L114: Send notification.
- L115-L116: Save deleted state and log completion.
- L117: Method close.

### `reduceSeats` (L118-L130)
- L118-L120: Override + transactional + signature.
- L121-L123: Log and load event.
- L123-L126: Guard insufficient seats; throw `InsufficientSeatsException`.
- L127-L128: Decrement seats and persist.
- L129-L130: Log updated capacity and close.

### `increaseSeats` (L131-L144)
- L131-L133: Override + transactional + signature.
- L134-L136: Log and load event.
- L136-L140: Guard overflow beyond `totalCapacity`; throw `SeatCapacityException`.
- L141-L142: Increment seats and save.
- L143-L144: Log updated capacity and close.

### `triggerFeedback` (L146-L153)
- L146-L147: Override + signature.
- L148-L150: Log, fetch event, and prepare `FeedbackDTO`.
- L151: Call feedback service.
- L152-L153: Log completion and close.

### `searchEvents` (L155-L201)
- L155-L156: Override + signature.
- L157-L158: Log request and create result variable.
- L160-L197: Branch through all filter combinations (category/location/date) and fallback to active events.
- L199-L200: Log result count and return.
- L201: Method close.

### Remaining query methods (L202-L232)
- L202-L208: `getEventsByCategory` fetches active by category.
- L210-L216: `getEventsByLocation` fetches active by location.
- L218-L224: `getEventsByOrganizer` fetches active by organizer id.
- L226-L232: `getEventsByTimeRange` fetches active by start-time window.
- L233-L234: Class close + spacing.

---

## `src/main/java/com/event/eventservice/service/NotificationClient.java` (1-15)

- L1: Package declaration.
- L3-L5: Feign and Spring web imports.
- L7: DTO import.
- L9: Feign client binding to `notification-service`.
- L10: Interface declaration.
- L11: Declares remote mapping path.
- L12: Method to send notification request body.
- L13-L15: Closing lines.

---

## `src/main/java/com/event/eventservice/service/TicketClient.java` (1-12)

- L1: Package declaration.
- L3-L5: Feign + mapping imports.
- L7: Feign client binding to `ticket-service`.
- L8: Interface declaration.
- L9: Remote endpoint mapping for event ticket cleanup.
- L10: Method contract with path variable.
- L11-L12: Closing lines.

---

## `src/main/java/com/event/eventservice/service/FeedbackClient.java` (1-13)

- L1: Package declaration.
- L3-L6: DTO + Feign + mapping imports.
- L8: Feign client binding to `feedback-service`.
- L9: Interface declaration.
- L10: Remote endpoint mapping for feedback initiation.
- L11: Method contract with request body.
- L12-L13: Closing lines.

---

## `src/main/java/com/event/eventservice/controller/EventController.java` (1-147)

- L1: Package declaration.
- L3-L6: Java time/list imports.
- L7-L13: Validation, logging, Spring annotations, HTTP imports.
- L15-L17: Domain imports.
- L19-L21: REST controller declaration + base route `/events`.
- L23: Logger field.
- L25-L26: Injected `EventService` dependency.
- L28: Comment indicating CRUD section.

### CRUD endpoints
- L30-L36: `POST /events` create event.
- L38-L44: `GET /events` list all events.
- L46-L52: `GET /events/{id}` fetch one event.
- L54-L60: `PUT /events/{id}` update event.
- L62-L68: `DELETE /events/{id}` soft-delete event.
- L69-L75: `GET /events/{id}/ticket-info` returns `EventDTO`.

### Seat + feedback endpoints
- L76: Section comment.
- L78-L84: `PUT /events/{id}/reduce-seats`.
- L86-L92: `PUT /events/{id}/increase-seats`.
- L94-L100: `POST /events/{id}/feedback`.

### Search/filter endpoints
- L102: Section comment.
- L103-L112: `GET /events/search` with optional category/location/date.
- L114-L120: `GET /events/category/{category}`.
- L122-L128: `GET /events/location/{location}`.
- L130-L136: `GET /events/organizer/{organizerId}`.
- L138-L146: `GET /events/range` with start/end datetime params.
- L147: Class close.

---

## `src/main/java/com/event/eventservice/dto/EventDTO.java` (1-16)

- L1: Package declaration.
- L3-L5: Lombok imports.
- L7-L9: Lombok annotations for getters/setters/constructors.
- L10: DTO class declaration.
- L11-L14: Fields exposed for ticket summary.
- L15-L16: Class close + spacing.

---

## `src/main/java/com/event/eventservice/dto/FeedbackDTO.java` (1-14)

- L1: Package declaration.
- L3-L5: Lombok imports.
- L7-L9: Lombok annotations.
- L10: DTO class declaration.
- L11-L12: Feedback payload fields.
- L13-L14: Closing lines.

---

## `src/main/java/com/event/eventservice/dto/NotificationRequestDTO.java` (1-16)

- L1: Package declaration.
- L3-L5: Lombok imports.
- L7-L9: Lombok annotations.
- L10: DTO class declaration.
- L11-L14: Notification payload fields.
- L15-L16: Closing lines.

---

## `src/main/java/com/event/eventservice/exceptions/ErrorResponse.java` (1-34)

- L1: Package declaration.
- L2: Time import.
- L4: Class declaration.
- L5-L7: Error payload fields (`message`, `status`, `timestamp`).
- L8-L10: `getMessage()` getter.
- L11-L13: `setMessage(...)` setter.
- L14-L16: `getStatus()` getter.
- L17-L19: `setStatus(...)` setter.
- L20-L22: `getTimestamp()` getter.
- L23-L28: Constructor assigning all fields.
- L29-L31: `setTimestamp(...)` setter.
- L32-L34: Class close + spacing.

---

## `src/main/java/com/event/eventservice/exceptions/EventAlreadyDeletedException.java` (1-9)

- L1: Package declaration.
- L3: Exception class declaration.
- L4-L6: Constructor building message with event id.
- L8-L9: Closing lines.

---

## `src/main/java/com/event/eventservice/exceptions/EventNotFoundException.java` (1-12)

- L1: Package declaration.
- L3: Exception class declaration.
- L4-L6: Constructor for id-based message.
- L7-L9: Constructor for custom message.
- L11-L12: Closing lines.

---

## `src/main/java/com/event/eventservice/exceptions/InsufficientSeatsException.java` (1-12)

- L1: Package declaration.
- L3: Exception class declaration.
- L5-L7: Default constructor with generic message.
- L8-L10: Overloaded constructor with requested/available detail.
- L11-L12: Closing lines.

---

## `src/main/java/com/event/eventservice/exceptions/InvalidEventException.java` (1-10)

- L1: Package declaration.
- L3: Exception class declaration.
- L5-L7: Constructor taking custom validation message.
- L9-L10: Closing lines.

---

## `src/main/java/com/event/eventservice/exceptions/SeatCapacityException.java` (1-9)

- L1: Package declaration.
- L3: Exception class declaration.
- L4-L6: Constructor message for seat overflow action.
- L7-L9: Closing lines.

---

## `src/main/java/com/event/eventservice/exceptions/GlobalExceptionHandler.java` (1-103)

- L1: Package declaration.
- L3-L4: Java utility imports.
- L6-L12: Logging and Spring exception-handling imports.
- L14-L15: Declares global REST exception advisor class.
- L17: Logger declaration.
- L18-L28: Catch-all `Exception` handler returning `500` and `ErrorResponse`.
- L29-L39: Handles `EventNotFoundException` with `404`.
- L40-L50: Handles `InvalidEventException` with `400`.
- L51-L61: Handles `EventAlreadyDeletedException` with `404`.
- L62-L72: Handles `InsufficientSeatsException` with `409`.
- L73-L83: Handles `SeatCapacityException` with `400`.
- L84-L101: Handles validation errors (`MethodArgumentNotValidException`) by joining field messages and returning `400`.
- L102-L103: Class close.

---

## `src/test/java/com/event/eventservice/EventServiceApplicationTests.java` (1-14)

- L1: Package declaration.
- L3-L4: JUnit + Spring test imports.
- L6: Marks class as Spring Boot test.
- L7: Test class declaration.
- L9: Test annotation.
- L10-L11: Empty `contextLoads` smoke test.
- L12-L14: Closing lines.

---

## Notes

- This document intentionally maps each maintained file and line range in project order and explains the role of every line/block.
- If you want a literal one-row-per-line table for all files (thousands of rows including generated artifacts), I can produce that in a follow-up split into multiple markdown parts.
