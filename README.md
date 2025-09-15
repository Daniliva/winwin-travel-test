# Mini Test: Two Spring Boot Applications with Postgres and Docker

Two Spring Boot applications (`auth-api` and `data-api`) with PostgreSQL, running via Docker Compose. Uses Java 21 and Spring Boot 3.2.0. Compilation is performed inside Docker.

## Requirements
- Docker and Docker Compose
- (No local Java or Maven installation required)

## Structure
- `/auth-api`: Service A (authentication, JPA, calls Service B)
- `/data-api`: Service B (text transformation)
- `docker-compose.yml`: Docker configuration
- `init.sql`: Database table initialization

## Setup
1. Run Docker Compose:
   ```bash
   docker compose up -d --build
   ```

## Testing via cURL
1. **Registration**:
   ```bash
   curl -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" -d "{\"email\":\"newuser@a.com\",\"password\":\"pass\"}" -w "%{http_code}"
   ```
   - **Expected Response**: `201` (Created).
   - **Error**: `400` and `{"error":"Email exists"}` if email is already registered.

2. **Login**:
   ```bash
   curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d "{\"email\":\"newuser@a.com\",\"password\":\"pass\"}" -w "%{http_code}"
   ```
   - **Expected Response**: `200` and `{"token":"..."}`. Save the token for the next request.
   - **Error**: `400` and `{"error":"Invalid credentials"}` for incorrect credentials.

3. **Text Processing**:
   ```bash
   curl -X POST http://localhost:8080/api/process -H "Authorization: Bearer <token>" -H "Content-Type: application/json" -d "{\"text\":\"hello\"}" -w "%{http_code}"
   ```
   - **Expected Response**: `200` and `{"result":"OLLEH"}`.
   - **Errors**:
     - `401` — Invalid or expired token.
     - `503` — `data-api` unavailable.

4. **Test data-api Denial**:
   ```bash
   curl -X POST http://localhost:8081/api/transform -H "Content-Type: application/json" -d "{\"text\":\"test\"}" -w "%{http_code}"
   ```
   - **Expected Response**: `403` (Forbidden).

## Testing via Swagger
- Access Swagger UI after starting the application:
  ```
  http://localhost:8080/swagger-ui.html
  ```
- Use the interface to test `/api/auth/register`, `/api/auth/login`, and `/api/process`.
- For `/api/process`, add the `Authorization: Bearer <token>` header in the "Authorize" section.

## Verification
- **Database Logs**:
  ```bash
  docker exec -it <postgres-container-id> psql -U postgres -d mini_test -c "SELECT * FROM processing_log;"
  ```
  Checks records in the `processing_log` table.

## Shutdown
```bash
docker compose down -v
```

## Troubleshooting
- **401 Unauthorized**: Token is expired or invalid. Generate a new token via `/api/auth/login`.
- **403 Forbidden from data-api**: Verify `INTERNAL_TOKEN` matches in `docker-compose.yml`:
  ```yaml
  auth-api:
    environment:
      INTERNAL_TOKEN: internalSecretToken
  data-api:
    environment:
      INTERNAL_TOKEN: internalSecretToken
  ```
- **503 Service Unavailable**: Ensure `data-api` is running:
  ```bash
  docker compose logs data-api
  docker network inspect mini-test_app-network
  ```
- **400 Bad Request (JSON parse error)**: Use double quotes in JSON:
  ```json
  {"email":"newuser@a.com","password":"pass"}
  ```
- **WeakKeyException**: Ensure `JWT_SECRET` is ≥ 32 characters:
  ```yaml
  JWT_SECRET: mySuperSecretKeyForJWTWhichIsLongEnough1234567890
  ```
- **Clear Docker cache**:
  ```bash
  docker compose down -v
  docker system prune -a --volumes
  docker compose build --no-cache
  docker compose up -d
  ```

## Notes
- Passwords are hashed using BCrypt.
- Service A uses JWT for authentication.
- For production, use a secure `JWT_SECRET` (≥ 32 characters) and `INTERNAL_TOKEN`.
- Swagger UI is available at `http://localhost:8080/swagger-ui.html` for easy API testing.
