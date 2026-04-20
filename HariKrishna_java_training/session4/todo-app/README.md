# TODO Application Enhancement

This project is an enhanced Spring Boot TODO application with added logging, a dummy notification service, and unit test coverage.

## Features

- `SLF4J` logging in `TodoController` and `TodoServiceImpl`
- Dummy notification service in `NotificationServiceClient`
- Notification call executed when a TODO is created
- Unit tests using `JUnit 5`, `Mockito`, and `MockMvc`
- Code coverage reporting with JaCoCo

## Implementation details

- `TodoServiceImpl` now logs create, read, update, and delete operations.
- `TodoController` logs incoming REST requests and response details.
- `NotificationServiceClient` is a dummy Spring service that logs notifications such as:
  - `Notification sent for new TODO: <title>`

## Running the application

From the `todo-app` directory:

```powershell
./mvnw spring-boot:run
```

## Running tests

Execute unit tests and generate coverage reports:

```powershell
./mvnw test
./mvnw jacoco:report
```

Coverage output is generated in `target/site/jacoco/index.html`.

## Notes

- The notification client is intentionally simple and used to simulate another service call.
- The project uses `spring-boot-starter-test` for test support, including Mockito and MockMvc.
