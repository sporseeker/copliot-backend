# Copliot Backend — API Documentation

This document provides a focused, developer-friendly reference for the Copliot backend API (SpotSeeker Copilot mobile app). It covers how to run the service, key configuration, authentication, and a complete list of implemented endpoints with request/response examples.

Checklist
- [x] Run & build instructions
- [x] Configuration properties and security notes
- [x] Authentication flows and examples
- [x] Comprehensive endpoint reference (Auth, Partner, Partner Onboarding, User, Events, Tickets, Ticket Packages, File Upload, Admin, Analytics)
- [x] Error format and common responses
- [x] Development notes and H2 console

# Getting started

## Prerequisites
- Java 17+
- Maven 3.6+

## Run locally

1. Build:

   ```bash
   mvn clean install
   ```

2. Run:

   ```bash
   mvn spring-boot:run
   ```

By default the server listens on `http://localhost:8080`

## Run tests

```bash
mvn test
```

# Configuration

Key properties are set in `src/main/resources/application.properties`. Common properties you may want to change in development or production:

- Server
  - `server.port` — HTTP port (default 8080)

- Database
  - `spring.datasource.url` — e.g., `jdbc:h2:mem:copliot` for development
  - `spring.jpa.hibernate.ddl-auto` — `update` in dev

- JWT
  - `jwt.secret` — Replace in production
  - `jwt.expiration` — token TTL in milliseconds

- OTP
  - `otp.expiration` — OTP TTL in milliseconds
  - `otp.length` — digits

- Rate Limiting
  - `rate.limit.requests` — default 100
  - `rate.limit.duration` — default 60000 (ms)

- File upload
  - `spring.servlet.multipart.max-file-size`
  - `file.upload-dir`

# Authentication

- The API uses JWT Bearer tokens for authenticated endpoints.
- Obtain a token using one of the auth flows (OTP mobile verification or partner login).
- Include the header for protected routes:

  ```http
  Authorization: Bearer <token>
  ```

## Auth endpoints
Base path: `/api/auth`

1) Register (general user/partner/register flows)
```http
POST /api/auth/register
Content-Type: application/json
```

Request (example RegisterRequestDto):
```json
{
  "username": "partner_user",
  "password": "secret",
  "role": "PARTNER"
}
```

Response: LoginResponseDto (contains token + user info)

2) Login (username/password)
```http
POST /api/auth/login
Content-Type: application/json
```

Request (LoginRequestDto):
```json
{
  "username": "testpartner",
  "password": "password123"
}
```

Response: LoginResponseDto { token, type: "Bearer", user }

3) Mobile verification flows
- Request OTP
```http
POST /api/auth/otp/request
Content-Type: application/json
```

Request (OtpRequestDto):
```json
{
  "phoneNumber": "+1234567890"
}
```

Response: { "message": "OTP sent successfully" }

- Verify OTP
```http
POST /api/auth/otp/verify
Content-Type: application/json
```

Request (OtpVerifyDto):
```json
{
  "phoneNumber": "+1234567890",
  "code": "123456"
}
```

Response: AuthResponseDto — includes token and user data

- Resend OTP
```http
POST /api/auth/otp/resend
Content-Type: application/json
```

Request: same shape as /otp/request

Partner registration (multi-step)
- Step 1 (email)
```http
POST /api/auth/partner/register/step1-email
Content-Type: application/json
```

Request body: PartnerRegistrationEmailDto (email and related fields)

- Step 2 (mobile)
```http
POST /api/auth/partner/register/step2-mobile
Content-Type: application/json
```

Request body: PartnerRegistrationMobileDto

- Step 3 (verify OTP)
```http
POST /api/auth/partner/register/step3-verify-otp
Content-Type: application/json
```

Request body: PartnerRegistrationOtpDto

- Admin register (special)
```http
POST /api/auth/admin/register
Content-Type: application/json
```

Response: LoginResponseDto

# Partner endpoints
Base path: `/api/partners`

- Register partner (multipart)
```http
POST /api/partners/register
Content-Type: multipart/form-data
```

Form fields (PartnerRegistrationDto model)
- username
- password
- businessName
- contactPerson
- phoneNumber
- email
- address
- logo (file) — optional

Response: 201 Created with Partner JSON

- Get partner profile
```http
GET /api/partners/profile
Headers: Authorization: Bearer <token>
```

Response: Partner JSON model

- Update partner profile
```http
PUT /api/partners/profile
Headers: Authorization: Bearer <token>
Content-Type: multipart/form-data
```

Form fields (PartnerUpdateDto)
- businessName
- contactPerson
- phoneNumber
- email
- address
- logo (file) — optional

Response: Partner JSON

# Partner onboarding
Base path: `/api/partner`
All endpoints require Authentication (Bearer token)

- Save company profile
```http
POST /api/partner/company-profile
Content-Type: multipart/form-data (Model attributes)
```

Request: CompanyProfileDto (multipart / model attributes)
Response: { "message": "Company profile saved successfully" }

- Save organizer info
```http
POST /api/partner/organizer-info
Content-Type: multipart/form-data
```

Request: OrganizerInfoDto
Response: { "message": "Organizer information saved successfully" }

- Submit agreement
```http
POST /api/partner/agreement
Content-Type: multipart/form-data
```

Request: AgreementDto
Response: { "message": "Agreement accepted successfully" }

- Get application status
```http
GET /api/partner/application-status
Headers: Authorization: Bearer <token>
```

Response: ApplicationStatusDto

# User endpoints
Base path: `/api/users`

- Get user profile
```http
GET /api/users/profile
Headers: Authorization: Bearer <token>
```

Response: User JSON model

- Update user profile
```http
PUT /api/users/profile
Headers: Authorization: Bearer <token>
Content-Type: application/json
```

Request (UserUpdateDto):
```json
{
  "name": "John Doe",
  "email": "john@example.com"
}
```

Response: User JSON model

# Event endpoints
Base path: `/api/events`
All require Authorization: Bearer <token>

- Create event
```http
POST /api/events
Content-Type: multipart/form-data (EventCreateDto as model)
```

Response: EventDetailDto

- List events
```http
GET /api/events
```

Query params (optional): status, page, limit

Response: EventListResponseDto

- Get event by id
```http
GET /api/events/{id}
```

Response: EventDetailDto

- Update event
```http
PUT /api/events/{id}
Content-Type: multipart/form-data (EventUpdateDto as model)
```

Response: EventDetailDto

- Delete event
```http
DELETE /api/events/{id}
```

Response: { "success": true, "message": "Event deleted successfully" }

## Ticket packages
Base path: `/api/events/{eventId}/ticket-packages`

- Create ticket package
```http
POST /api/events/{eventId}/ticket-packages
Content-Type: application/json
```

Request: TicketPackageCreateDto
Response: TicketPackageListDto.TicketPackageDto

- List ticket packages
```http
GET /api/events/{eventId}/ticket-packages
```

Response: TicketPackageListDto

- Update ticket package
```http
PUT /api/events/{eventId}/ticket-packages/{packageId}
Content-Type: application/json
```

Request: TicketPackageUpdateDto
Response: TicketPackageListDto.TicketPackageDto

- Delete ticket package
```http
DELETE /api/events/{eventId}/ticket-packages/{packageId}
```

Response: { "success": true, "message": "Ticket package deleted" }

## Ticket operations (per-event)
Base path prefix: `/api/events/{id}`

- Generate ticket QR codes
```http
POST /api/events/{id}/generate-ticket-qr
Headers: Authorization: Bearer <token>
Content-Type: application/json
```

Request body (example):
```json
{
  "ticket_package_id": 5,
  "quantity": 3
}
```

Response: { "qr_codes": [ { ...ticket data... } ] }

- Validate ticket
```http
POST /api/events/{id}/validate-ticket
Content-Type: application/json
```

Request: TicketValidationRequestDto
Response: TicketValidationResponseDto

- Request withdrawal (finance)
```http
POST /api/events/{id}/finance/withdraw
Content-Type: application/json
```

Request: WithdrawalRequestDto
Response: WithdrawalResponseDto

# File upload
Base path: `/api/upload`

- Upload file
```http
POST /api/upload
Headers: Authorization: Bearer <token>
Content-Type: multipart/form-data
```

Form params:
- file (binary)
- type (string) — maps to FileUpload.FileType enum
- purpose (string) — maps to FileUpload.FilePurpose enum

Response: FileUploadResponseDto (file metadata / url)

- Delete file
```http
DELETE /api/upload/{fileId}
```

Response: { "success": true, "message": "File deleted successfully" }

# Event analytics
Paths under `/api/events/{id}`

- Event overview
```http
GET /api/events/{id}/overview
```

Response: EventOverviewDto

- Live stats
```http
GET /api/events/{id}/live-stats
```

Response: LiveStatsDto

- Finance - sales
```http
GET /api/events/{id}/finance/sales
```

Query params: period (default "all"), startDate, endDate
Response: FinanceSalesDto

- Finance - breakdown
```http
GET /api/events/{id}/finance/breakdown
```

Response: FinanceBreakdownDto

# Admin endpoints
Base path: `/api/admin`
Access-controlled: requires a user with ADMIN role (`@PreAuthorize("hasRole('ADMIN')")`)

- List partners
```http
GET /api/admin/partners
```

Query params: status (optional), page (default 0), limit (default 10)
Response: { partners: [...], total, page, limit }

- Update partner status
```http
PUT /api/admin/partners/{partnerId}/status
```

Request body: { "status": "APPROVED" }
Response: { "message": "Partner status updated successfully" }

- Update event status
```http
PUT /api/admin/events/{eventId}/status
```

Request body: { "status": "APPROVED" , "reason": "optional reason" }
Response: { "message": "Event status updated successfully" }

- Partner registration requests
```http
GET /api/admin/partner-requests
```

Query params: status (e.g. PENDING_APPROVAL), page, limit
Response: { requests: [...], total, page, limit }

- Approve/reject partner registration
```http
POST /api/admin/partner-requests/approve
```

Request (PartnerApprovalDto):
```json
{
  "requestId": 123,
  "action": "approve", // or "reject"
  "notes": "optional",
  "rejectionReason": "required when action=reject"
}
```

Response: PartnerRegistrationResponseDto

# Error format
All errors use a standardized JSON format:

```json
{
  "timestamp": "2025-10-08T07:03:54.123",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid or expired OTP",
  "path": "/api/auth/otp/verify"
}
```

Common status codes
- 200 OK — Success
- 201 Created — Resource created
- 400 Bad Request — Validation or client errors
- 401 Unauthorized — Missing/invalid token
- 403 Forbidden — Insufficient permissions
- 404 Not Found — Resource not found
- 429 Too Many Requests — Rate limited
- 500 Internal Server Error — Unexpected errors

# Development notes

- H2 Console (development): http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:copliot`
  - Username: `sa` (empty password)

- Rate limiting: default 100 requests/min, configured via properties
- Passwords are hashed with BCrypt
- JWT secret must be rotated in production and kept secret
- File uploads are validated and can be configured to use local disk or S3 via `AwsS3Config`

# Project structure

`src/main/java/com/spotseeker/copliot/`
- `config/` — configuration classes (S3, security, filters)
- `controller/` — REST controllers (endpoints described above)
- `dto/` — request/response shapes
- `exception/` — centralized exception handling
- `model/` — JPA entities
- `repository/` — Spring Data repositories
- `security/` — JWT and security config
- `service/` — business logic

# Contact & contributing
- See repository `README.md` for contribution guidelines.

# License
MIT
