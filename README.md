# Event Board

Event Board is a Java EE web application for creating university events and registering students for them.

The project was created as an introductory JavaPro test task. It uses classic Servlets, JSP, clean JDBC, PostgreSQL, Maven, JUnit 5, and Mockito.

## Features

- View all upcoming events at `/events`.
- Show available seats for each event.
- Create a new event from the main page.
- View one event at `/event?id=...`.
- Show registered participants for an event.
- Register a student by name and email.
- Prevent registration when there are no free seats.
- Prevent duplicate email registration for the same event.
- Use Post-Redirect-Get after successful POST requests.

## Tech Stack

- Java 21
- Apache Tomcat 10+
- Jakarta Servlet API 6
- JSP
- PostgreSQL
- JDBC only
- Maven
- JUnit 5
- Mockito
- Lombok

## Project Structure

```text
src/main/java/com/eventboard
├── config          # Manual application context and DB connection factory
├── controller      # Servlets
├── dto             # Request and view DTOs
├── exception       # Application exceptions
├── model           # Event and Participant entities
├── repository      # DAO interfaces
├── repository/jdbc # JDBC DAO implementations
└── service         # Business logic

src/main/resources
├── application.properties
└── schema.sql

src/main/webapp
├── index.jsp
└── WEB-INF/views
    ├── events.jsp
    └── event-details.jsp

src/test/java/com/eventboard
├── controller
├── dto
└── service
```

## Database Setup

Create a PostgreSQL database:

```sql
CREATE DATABASE event_board;
```

Connect to the database and run the schema:

```bash
psql -U postgres -d event_board -f src/main/resources/schema.sql
```

If you already have duplicate participants and the unique email index cannot be created, clear registrations for the test project and run the schema again:

```sql
DELETE FROM participants;
```

The schema creates two main tables:

```sql
events (
    id,
    title,
    event_date,
    max_seats
)

participants (
    id,
    event_id,
    student_name,
    student_email
)
```

It also creates:

- Foreign key from `participants.event_id` to `events.id`.
- `CHECK (max_seats > 0)`.
- Unique index `uq_participants_event_email_lower` to prevent duplicate participant emails for the same event.

## Application Properties

Update `src/main/resources/application.properties` for your local PostgreSQL credentials:

```properties
db.url=jdbc:postgresql://localhost:5432/event_board
db.username=postgres
db.password=your_password
```

## Build

Build the WAR file:

```bash
mvn clean package
```

The generated file will be:

```text
target/event-board.war
```

## Run With Tomcat

1. Install Apache Tomcat 10 or newer.
2. Build the project:

```bash
mvn clean package
```

3. Copy the generated WAR file:

```text
target/event-board.war
```

to:

```text
TOMCAT_HOME/webapps/
```

4. Start Tomcat:

```bash
TOMCAT_HOME/bin/startup.bat
```

On Linux/macOS:

```bash
TOMCAT_HOME/bin/startup.sh
```

5. Open the application:

```text
http://localhost:8080/event-board/
```

The index page redirects to:

```text
http://localhost:8080/event-board/events
```

## Run From IntelliJ IDEA

1. Open the project as a Maven project.
2. Make sure Project SDK is Java 21.
3. Add a local Tomcat 10+ run configuration.
4. Add artifact `event-board:war exploded` or `event-board.war`.
5. Set application context to:

```text
/event-board
```

6. Start Tomcat and open:

```text
http://localhost:8080/event-board/events
```

## Tests

Run all tests:

```bash
mvn test
```

The test suite covers:

- `EventServiceImpl` validation and business logic.
- Atomic participant registration flow.
- Duplicate email handling.
- DTO calculations for registered and available seats.
- Servlet GET and POST flows with Mockito.

## Business Rules

- Event title cannot be blank.
- Event date is required.
- Event date cannot be in the past.
- Event date cannot be more than one year in the future.
- Maximum seats must be greater than zero.
- Participant name cannot be blank.
- Participant email is required.
- Participant email must belong to an allowed domain:
  - `stud.duikt.edu.ua`
  - `gmail.com`
  - `hotmail.com`
  - `outlook.com`
- A student email can be registered only once for the same event.
- Registration is rejected when all seats are taken.

## Architecture

The application follows MVC and layered architecture:

- Model: `Event`, `Participant`, DTO classes.
- View: JSP pages in `WEB-INF/views`.
- Controller: `EventsServlet`, `EventDetailsServlet`.
- Repository: DAO interfaces and JDBC implementations.
- Service: `EventServiceImpl` with validation and business logic.

Dependencies are initialized manually through `ApplicationContext`.

SQL injection protection is handled through `PreparedStatement`.

JDBC resources are closed with try-with-resources.

Successful POST requests use redirect instead of forward to follow the PRG pattern.

## Atomic Registration

Participant registration uses `saveIfFreeSeats(...)` in `JdbcParticipantRepository`.

The repository method:

1. Starts a transaction.
2. Locks the selected event row with `SELECT ... FOR UPDATE`.
3. Counts already registered participants.
4. Rolls back and returns `false` when there are no free seats.
5. Inserts the participant and commits when seats are available.

This prevents overbooking when two users try to register for the same last seat at the same time.
