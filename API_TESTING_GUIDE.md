# API Testing Guide

This guide shows how to test the Copliot Backend API using curl commands.

## Setup

Make sure the application is running:
```bash
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`

## User Flow (OTP Authentication)

### 1. Request OTP

```bash
curl -X POST http://localhost:8080/api/auth/otp/request \
  -H "Content-Type: application/json" \
  -d '{"phoneNumber": "+1234567890"}'
```

Response:
```json
{
  "message": "OTP sent successfully"
}
```

**Note:** In development, the OTP code is printed to the console. Check the application logs to see the code.

### 2. Verify OTP

```bash
curl -X POST http://localhost:8080/api/auth/otp/verify \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "+1234567890",
    "code": "123456"
  }'
```

Replace `123456` with the actual code from the logs.

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "user": {
    "id": 1,
    "phoneNumber": "+1234567890",
    "name": null,
    "email": null,
    "createdAt": "2025-10-08T07:03:54.123",
    "updatedAt": "2025-10-08T07:03:54.123"
  }
}
```

Save the token for subsequent requests.

### 3. Get User Profile

```bash
export USER_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

curl -X GET http://localhost:8080/api/users/profile \
  -H "Authorization: Bearer $USER_TOKEN"
```

### 4. Update User Profile

```bash
curl -X PUT http://localhost:8080/api/users/profile \
  -H "Authorization: Bearer $USER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com"
  }'
```

## Partner Flow

### 1. Register Partner

```bash
curl -X POST http://localhost:8080/api/partners/register \
  -F "username=testpartner" \
  -F "password=password123" \
  -F "businessName=Test Business" \
  -F "contactPerson=John Doe" \
  -F "phoneNumber=+1234567890" \
  -F "email=test@example.com" \
  -F "address=123 Main St"
```

With logo:
```bash
curl -X POST http://localhost:8080/api/partners/register \
  -F "username=testpartner" \
  -F "password=password123" \
  -F "businessName=Test Business" \
  -F "logo=@/path/to/logo.png"
```

### 2. Partner Login

```bash
curl -X POST http://localhost:8080/api/auth/partner/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testpartner",
    "password": "password123"
  }'
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

### 3. Get Partner Profile

```bash
export PARTNER_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

curl -X GET http://localhost:8080/api/partners/profile \
  -H "Authorization: Bearer $PARTNER_TOKEN"
```

### 4. Update Partner Profile

```bash
curl -X PUT http://localhost:8080/api/partners/profile \
  -H "Authorization: Bearer $PARTNER_TOKEN" \
  -F "businessName=Updated Business" \
  -F "email=updated@example.com"
```

## Error Examples

### Invalid Phone Number
```bash
curl -X POST http://localhost:8080/api/auth/otp/request \
  -H "Content-Type: application/json" \
  -d '{"phoneNumber": "invalid"}'
```

Response (400):
```json
{
  "timestamp": "2025-10-08T07:03:54.123",
  "status": 400,
  "error": "Validation Failed",
  "errors": {
    "phoneNumber": "Invalid phone number format"
  },
  "path": "/api/auth/otp/request"
}
```

### Invalid OTP
```bash
curl -X POST http://localhost:8080/api/auth/otp/verify \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "+1234567890",
    "code": "999999"
  }'
```

Response (400):
```json
{
  "timestamp": "2025-10-08T07:03:54.123",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid or expired OTP",
  "path": "/api/auth/otp/verify"
}
```

### Unauthorized Access
```bash
curl -X GET http://localhost:8080/api/users/profile
```

Response (401):
```json
{
  "timestamp": "2025-10-08T07:03:54.123",
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "path": "/api/users/profile"
}
```

### Wrong Role
```bash
# Using user token to access partner endpoint
curl -X GET http://localhost:8080/api/partners/profile \
  -H "Authorization: Bearer $USER_TOKEN"
```

Response (403):
```json
{
  "timestamp": "2025-10-08T07:03:54.123",
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied",
  "path": "/api/partners/profile"
}
```

## Rate Limiting

The API has rate limiting enabled (100 requests per minute by default).

```bash
# Make 101 requests quickly
for i in {1..101}; do
  curl -X POST http://localhost:8080/api/auth/otp/request \
    -H "Content-Type: application/json" \
    -d '{"phoneNumber": "+1234567890"}'
done
```

After exceeding the limit, you'll get:
```json
{
  "timestamp": "2025-10-08T07:03:54.123",
  "status": 429,
  "error": "Too Many Requests",
  "message": "Rate limit exceeded. Please try again later.",
  "path": "/api/auth/otp/request"
}
```

## Testing Complete Flow

Here's a complete test script:

```bash
#!/bin/bash

# 1. Request OTP
echo "1. Requesting OTP..."
curl -X POST http://localhost:8080/api/auth/otp/request \
  -H "Content-Type: application/json" \
  -d '{"phoneNumber": "+1234567890"}'
echo -e "\n"

# Check console for OTP code, then:
read -p "Enter OTP code from console: " OTP_CODE

# 2. Verify OTP
echo "2. Verifying OTP..."
RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/otp/verify \
  -H "Content-Type: application/json" \
  -d "{\"phoneNumber\": \"+1234567890\", \"code\": \"$OTP_CODE\"}")
echo $RESPONSE | jq .
USER_TOKEN=$(echo $RESPONSE | jq -r .token)
echo -e "\n"

# 3. Get user profile
echo "3. Getting user profile..."
curl -X GET http://localhost:8080/api/users/profile \
  -H "Authorization: Bearer $USER_TOKEN" | jq .
echo -e "\n"

# 4. Update user profile
echo "4. Updating user profile..."
curl -X PUT http://localhost:8080/api/users/profile \
  -H "Authorization: Bearer $USER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Test User", "email": "test@example.com"}' | jq .
echo -e "\n"

# 5. Register partner
echo "5. Registering partner..."
curl -X POST http://localhost:8080/api/partners/register \
  -F "username=testpartner$(date +%s)" \
  -F "password=password123" \
  -F "businessName=Test Business" | jq .
echo -e "\n"

echo "Test completed!"
```

Save this as `test_api.sh` and run:
```bash
chmod +x test_api.sh
./test_api.sh
```
