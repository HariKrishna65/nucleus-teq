# Interview Tracker Project Overview

## 1. Project Purpose

Interview Tracker is a capstone web application for managing recruitment workflows. It supports three main roles:

- Candidate: registers, verifies email, sets password, applies to jobs, and tracks application progress.
- HR: creates job descriptions, reviews candidates, assigns panel members, moves candidates through stages, selects or rejects candidates.
- Panel: views assigned interviews and submits feedback.

The application has a static HTML/CSS/JavaScript frontend and a Spring Boot backend with PostgreSQL.

## 2. Folder Structure

- `frontend/html`: UI pages such as login, register, dashboard, apply, candidates, panel dashboard, HR dashboard, and verify-password.
- `frontend/css`: shared styling in `style.css` and page-specific CSS files.
- `frontend/js`: browser logic and API calls.
- `backend/tracker`: Spring Boot backend project.
- `backend/tracker/src/main/java/com/interview/tracker/controller`: REST API controllers.
- `backend/tracker/src/main/java/com/interview/tracker/service`: business logic.
- `backend/tracker/src/main/java/com/interview/tracker/entity`: JPA entities.
- `backend/tracker/src/main/java/com/interview/tracker/repository`: Spring Data JPA repositories.
- `backend/tracker/src/main/java/com/interview/tracker/dto`: request and response DTOs.
- `backend/tracker/src/test`: backend test cases.

## 3. Technology Stack

- Frontend: HTML, CSS, JavaScript.
- Backend: Java 17, Spring Boot 3.2.0.
- Database: PostgreSQL.
- Security: Spring Security, JWT bearer tokens.
- Validation: Jakarta Bean Validation annotations such as `@NotBlank`, `@Email`, `@Pattern`, `@Min`, and `@NotNull`.
- Testing: JUnit 5, Mockito, Spring MockMvc.
- Build: Maven wrapper.

## 4. Backend Architecture

The backend follows a common layered structure:

- Controller layer receives HTTP requests and maps endpoints.
- DTO layer validates incoming request data.
- Service layer contains business rules.
- Repository layer talks to PostgreSQL through Spring Data JPA.
- Entity layer maps Java objects to database tables.
- Exception handler converts validation and bad request errors into clean responses.

Important backend files:

- `TrackerApplication.java`: Spring Boot entry point.
- `SecurityConfig.java`: endpoint authorization rules and JWT resource server setup.
- `JwtService.java`: creates JWT tokens after login.
- `GlobalExceptionHandler.java`: returns structured bad request and validation errors.
- `UserService.java`: registration, login, verification, password setup, reset.
- `HrController.java`: HR workflow actions.
- `EmailService.java`: sends verification, password setup, reset, and panel assignment emails.

## 5. Main Entities

- `User`: login account with name, email, password, role, verification token, and profile data.
- `Candidate`: candidate application data, linked to `User` and `JobDescription`.
- `JobDescription`: job title, description, skills, experience range, salary range.
- `Panel`: interview panel profile.
- `Interview`: candidate interview with panel assignment, time, round, focus area, meeting link, and status.
- `Feedback`: panel or HR feedback for an interview.

Workflow constants:

- `Stage`: `PROFILING`, `SCREENING`, `L1_TECH`, `L2_TECH`, `HR_ROUND`, `SELECTED`, `REJECTED`.
- `StageStatus`: stores progress status for candidate stages.

Roles:

- `HR`
- `PANEL`
- `CANDIDATE`

## 6. Important API Endpoints

Auth endpoints under `/auth`:

- `POST /auth/register`: candidate registration.
- `POST /auth/login`: login and JWT creation.
- `POST /auth/verify`: verify email.
- `POST /auth/verify-and-set-password`: verify token and allow password setup.
- `POST /auth/set-password`: set or reset password.
- `POST /auth/forgot-password`: request password reset.
- `POST /auth/resend-verification`: resend verification email.
- `POST /auth/create-test-user`: create direct test users.

Candidate endpoints under `/candidates`:

- `POST /candidates`: create candidate application with optional resume upload.
- `GET /candidates?userId=...`: get candidate data scoped by user.
- `GET /candidates/{id}/resume`: download resume.

Job endpoints under `/jd`:

- `POST /jd`: create job description.
- `GET /jd`: list job descriptions.
- `GET /jd/{id}`: get one job description.
- `DELETE /jd/{id}`: delete job description.

HR endpoints under `/hr`:

- `GET /hr/candidates`: list candidates with progress and feedback summary.
- `GET /hr/candidates/{id}`: candidate detail with feedback and interview history.
- `POST /hr/panels`: create panel account and profile.
- `POST /hr/candidates/{id}/assign-panel`: assign panel members to a candidate round.
- `POST /hr/candidates/{id}/advance`: move candidate to next stage.
- `POST /hr/candidates/{id}/reject`: reject candidate.
- `POST /hr/candidates/{id}/select`: select candidate.
- `DELETE /hr/candidates/{id}`: delete candidate.

Interview endpoints under `/interviews`:

- `POST /interviews`: schedule interview.
- `GET /interviews/{id}`: get interview with role-based access.
- `GET /interviews?panelId=...`: get interviews by panel.
- `GET /interviews/panel`: list panel profiles.
- `POST /interviews/panel`: create panel profile.

Feedback endpoints under `/feedback`:

- `POST /feedback`: submit feedback.
- `GET /feedback/interview/{interviewId}`: list feedback for an interview.

## 7. Frontend Pages

- `index.html`: landing/home page.
- `login.html`: login form and forgot password flow.
- `register.html`: candidate registration.
- `verify-password.html`: token verification and password setup page.
- `dashboard.html`: candidate dashboard.
- `apply.html`: apply for a job.
- `jd.html`: job listing and HR job creation.
- `candidates.html`: HR candidate list and actions.
- `candidate-detail.html`: candidate details for HR.
- `assign-panel.html`: panel assignment form.
- `hr-main-dashboard.html` and `hr-dashboard.html`: HR dashboards.
- `panel-dashboard.html`: panel interview view.
- `feedback.html` and `hr-feedback.html`: feedback flows.
- `panel-management.html`: panel management.

Frontend API helper:

- `frontend/js/api.js` stores `API_URL = "http://localhost:8080"` and wraps fetch calls.

## 8. Main User Flows

Candidate registration:

1. Candidate submits registration form.
2. Backend creates user with `emailVerified = false`.
3. EmailService sends verification and password setup link.
4. User opens `verify-password.html?token=...`.
5. Frontend calls `/auth/verify-and-set-password`.
6. User sets password through `/auth/set-password`.
7. User logs in and receives JWT.

Candidate application:

1. Candidate logs in.
2. Candidate views jobs from `/jd`.
3. Candidate applies through `/candidates` with job, phone, experience, and optional resume.
4. HR sees application in candidate dashboard.

HR workflow:

1. HR views candidates through `/hr/candidates`.
2. HR advances stages or assigns panel members.
3. Panel assignment creates interviews and sends emails.
4. HR waits for panel feedback where required.
5. HR selects or rejects candidate with comments.

Panel workflow:

1. Panel logs in.
2. Panel sees assigned interviews.
3. Panel submits feedback through `/feedback`.

## 9. Security Overview

Spring Security is configured in `SecurityConfig.java`.

- Public auth endpoints include register, login, verify, set-password, forgot-password, resend-verification, and create-test-user.
- Public job listing is allowed through `GET /jd`.
- HR endpoints require role `HR`.
- Candidate endpoints require role `CANDIDATE` or `HR`.
- Feedback and interview endpoints are restricted by role.
- JWT claims include role and user data needed by the frontend.

## 10. Validation Overview

DTO validation is used before service logic runs.

Examples:

- `LoginRequest`: email is required and must be valid; password is required.
- `RegisterRequest`: name, email, role, and phone are required; phone uses a pattern.
- `SetPasswordRequest`: token is required; password must contain uppercase, lowercase, number, special character, and at least 8 characters.
- `CreatePanelRequest`: validates panel name, email, organization, designation, and expertise.
- Entities such as `Candidate`, `JobDescription`, `Interview`, and `Feedback` also contain validation annotations.

`GlobalExceptionHandler` catches validation errors and returns a response with:

- `timestamp`
- `status`
- `error`
- `message`
- `fields`

This makes API errors easier for frontend and reviewers to understand.

## 11. Logging Overview

Logging uses SLF4J.

Examples:

- `AuthController` logs registration, login, and password reset requests without logging passwords.
- `UserService` logs registration success, login success, and login or registration rejection reasons.
- `EmailService` logs email sending success or failure.
- `HrController` logs panel onboarding.

Review point: passwords and tokens should not be logged.

## 12. Testing Overview

Current test files:

- `AuthControllerValidationTest`: MockMvc tests for login and set-password validation.
- `HrControllerTest`: unit tests for candidate progress, rejection validation, and past interview time validation.
- `JwtServiceTest`: verifies JWT generation shape.
- `TrackerApplicationTests`: verifies Spring context loads.

Important test command:

```powershell
cd "C:\Users\hari krishna\OneDrive\Attachments\Desktop\nucleus-teq\Capstone_interviewtracker\backend\tracker"
.\mvnw.cmd test
```

Expected result:

- 8 tests pass.

## 13. Build And Run Commands

Run backend tests:

```powershell
cd "C:\Users\hari krishna\OneDrive\Attachments\Desktop\nucleus-teq\Capstone_interviewtracker\backend\tracker"
.\mvnw.cmd test
```

Build backend runnable JAR:

```powershell
.\mvnw.cmd package
```

Run backend:

```powershell
java -jar target\tracker-0.0.1-SNAPSHOT-exec.jar
```

Open frontend:

```text
Capstone_interviewtracker/frontend/html/index.html
```

Set password page:

```text
Capstone_interviewtracker/frontend/html/verify-password.html?token=YOUR_TOKEN
```

## 14. Database And Configuration

Main config file:

- `backend/tracker/src/main/resources/application.properties`

Important properties:

- `spring.datasource.url`: PostgreSQL database URL.
- `spring.datasource.username`: database username.
- `spring.datasource.password`: database password.
- `spring.jpa.hibernate.ddl-auto=update`: updates schema automatically.
- `spring.mail.*`: SMTP settings.
- `app.frontend.url`: frontend link used in emails.
- `app.jwt.secret`: JWT signing secret.
- `app.demo.seed`: enables or disables demo data seeding.

Review note: real deployments should not commit real database passwords, mail app passwords, or JWT secrets.

## 15. Review Talking Points

- The project separates frontend and backend cleanly.
- Backend follows controller, service, repository, entity, DTO layers.
- Security uses JWT and role-based authorization.
- Validation is handled at DTO/entity level and centralized in `GlobalExceptionHandler`.
- Logs are added for important auth and email actions without exposing sensitive data.
- HR workflow includes panel assignment, stage movement, feedback tracking, select, reject, and delete.
- Tests cover validation, JWT creation, HR business rules, and Spring context loading.
- Runnable backend artifact is `tracker-0.0.1-SNAPSHOT-exec.jar`.

## 16. Possible Improvements To Mention

- Move secrets out of `application.properties` into environment variables.
- Add more integration tests for candidate apply, feedback submission, and full HR workflow.
- Add pagination and filtering on backend candidate lists.
- Replace browser alerts with styled frontend notifications.
- Add refresh token or token expiry handling on the frontend.
- Add API documentation with Swagger/OpenAPI.
