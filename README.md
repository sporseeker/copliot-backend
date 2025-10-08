# Copliot Backend

Spring Boot backend for SpotSeeker Copilot mobile app backend.

## Features

This MVP implementation includes:

- **Authentication**
  - OTP-based authentication (request/verify/resend)
  - Partner login/logout with JWT tokens
  - JWT authentication middleware

- **Partner Management**
  - Partner registration with file uploads (logo)
  - Partner profile GET/PUT endpoints
  - Secure password storage with BCrypt

- **User Management**
  - User profile GET/PUT endpoints
  - Auto-created on OTP verification

- **Security**
  - Rate limiting (100 requests per minute by default)
  - JWT-based authentication
  - Role-based access control (USER, PARTNER)
  - Standardized error handling

- **Database**
  - JPA entities (User, Partner, OTP)
  - H2 in-memory database (development)
  - PostgreSQL ready (production)

- **Testing & CI/CD**
  - Comprehensive unit and integration tests
  - GitHub Actions CI/CD pipeline

## Tech Stack

- Java 17
- Spring Boot 3.2.0
- Spring Security
- Spring Data JPA
- JWT (JJWT 0.12.3)
- H2 Database (dev)
- PostgreSQL (prod ready)
- Maven
- JUnit 5

## Getting Started

### Prerequisites

- JDK 17 or higher
- Maven 3.6+

### Running the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### Running Tests

```bash
mvn test
```

### Building the Application

```bash
mvn clean install
```

## API Documentation

### Authentication Endpoints

#### Request OTP
```http
POST /api/auth/otp/request
Content-Type: application/json

{
  "phoneNumber": "+1234567890"
}
```

#### Verify OTP
```http
POST /api/auth/otp/verify
Content-Type: application/json

{
  "phoneNumber": "+1234567890",
  "code": "123456"
}
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "user": {
    "id": 1,
    "phoneNumber": "+1234567890",
    "name": null,
    "email": null
  }
}
```

#### Resend OTP
```http
POST /api/auth/otp/resend
Content-Type: application/json

{
  "phoneNumber": "+1234567890"
}
```

#### Partner Login
```http
POST /api/auth/partner/login
Content-Type: application/json

{
  "username": "testpartner",
  "password": "password123"
}
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "user": {
    "id": 1,
    "username": "testpartner",
    "businessName": "Test Business",
    "email": "test@example.com"
  }
}
```

### Partner Endpoints

All partner endpoints require authentication with `Authorization: Bearer <token>` header.

#### Register Partner
```http
POST /api/partners/register
Content-Type: multipart/form-data

username=testpartner
password=password123
businessName=Test Business
contactPerson=John Doe
phoneNumber=+1234567890
email=test@example.com
address=123 Main St
logo=<file> (optional)
```

#### Get Partner Profile
```http
GET /api/partners/profile
Authorization: Bearer <token>
```

#### Update Partner Profile
```http
PUT /api/partners/profile
Authorization: Bearer <token>
Content-Type: multipart/form-data

businessName=Updated Business Name
contactPerson=Jane Doe
phoneNumber=+0987654321
email=updated@example.com
address=456 New St
logo=<file> (optional)
```

### User Endpoints

All user endpoints require authentication with `Authorization: Bearer <token>` header.

#### Get User Profile
```http
GET /api/users/profile
Authorization: Bearer <token>
```

#### Update User Profile
```http
PUT /api/users/profile
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com"
}
```

## Configuration

Key configuration properties in `src/main/resources/application.properties`:

```properties
# Server
server.port=8080

# Database
spring.datasource.url=jdbc:h2:mem:copliot
spring.jpa.hibernate.ddl-auto=update

# JWT
jwt.secret=your-secret-key-change-this-in-production
jwt.expiration=86400000

# OTP
otp.expiration=300000
otp.length=6

# Rate Limiting
rate.limit.requests=100
rate.limit.duration=60000

# File Upload
spring.servlet.multipart.max-file-size=10MB
file.upload-dir=uploads
```

## Error Handling

All errors follow a standardized format:

```json
{
  "timestamp": "2025-10-08T07:03:54.123",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid or expired OTP",
  "path": "/api/auth/otp/verify"
}
```

HTTP Status Codes:
- 200: Success
- 201: Created
- 400: Bad Request
- 401: Unauthorized
- 404: Not Found
- 429: Too Many Requests (Rate Limit)
- 500: Internal Server Error

## Security

- All passwords are hashed using BCrypt
- JWT tokens expire after 24 hours (configurable)
- Rate limiting prevents abuse (100 requests/minute default)
- OTPs expire after 5 minutes (configurable)
- File uploads are validated and stored securely
- CORS can be configured for specific origins

## Development

### Database Console

H2 console is available at `http://localhost:8080/h2-console` (development only)

- JDBC URL: `jdbc:h2:mem:copliot`
- Username: `sa`
- Password: (empty)

### Project Structure

```
src/
├── main/
│   ├── java/com/spotseeker/copliot/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # REST controllers
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── exception/       # Custom exceptions
│   │   ├── model/           # JPA entities
│   │   ├── repository/      # JPA repositories
│   │   ├── security/        # Security configuration
│   │   └── service/         # Business logic
│   └── resources/
│       └── application.properties
└── test/
    └── java/com/spotseeker/copliot/
        ├── controller/      # Controller tests
        └── service/         # Service tests
```

## License

MIT License
