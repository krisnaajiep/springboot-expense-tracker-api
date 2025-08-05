# Spring Boot Expense Tracker API

![Current Version](https://img.shields.io/badge/version-1.4.0-green)
[![Framework](https://img.shields.io/badge/framework-Spring_Boot-6DB33F?logo=springboot&logoColor=white)](https://spring.io/)
[![Redis](https://img.shields.io/badge/cache-Redis-DC382D?logo=redis&logoColor=white)](https://redis.io/)

> Simple Expense Tracker RESTful API built with Spring Boot to allow users to create, read, update, and delete expenses.

## Table of Contents

- [General Info](#general-information)
- [Technologies Used](#technologies-used)
- [Features](#features)
- [Setup](#setup)
- [Usage](#usage)
- [Authentication](#authentication)
- [Caching Strategy](#caching-strategy)
- [Rate and Usage Limits](#rate-and-usage-limits)
- [HTTP Response Codes](#http-response-codes)
- [Project Status](#project-status)
- [Acknowledgements](#acknowledgements)
- [License](#license)

## General Information

Spring Boot Expense Tracker API is a simple RESTful API that allows users to manage their expenses.
It supports pagination, sorting, and filtering by date range.
This API uses [JWT](https://jwt.io/) for authentication and [Redis](https://redis.io/) for caching.
This project is designed to explore and practice working with the Java programming language, data modeling,
and user authentication in Spring Boot.

## Technologies Used

- Java 21.0.6 LTS
- Maven 3.9.9
- H2 Database 2.3.232
- Microsoft SQL Server 2022
- Spring Boot 3.4.7
- Lombok 1.18.38
- [JJWT](https://github.com/jwtk/jjwt) 0.12.6
- Redis 8.0.3

## Features

- **User Registration**: Register a new user using the `POST` method.
- **User Login**: Authenticate the user using the `POST` method.
- **Create a new expense**: Create a new expense using the `POST` method.
- **Update an existing expense**: Update an existing expense using the `PUT` method.
- **Delete an existing expense**: Delete an existing expense using the `DELETE` method.
- **List and filter all past expenses**: Get the list of expenses with pagination and filtering by date range using the
  `GET` method.
- **Refresh Token**: Get a new access token using the `POST` method.
- **Revoke Tokens**: Invalidates all refresh tokens for the authenticated user using the `POST` method.
- **Expenses Caching**: Speeds up repeated requests for filtered/paginated expenses using Redis with 60-minute TTL.

## Setup

To run this API, you’ll need:

* **Java**: Version 21 or higher
* **Microsoft SQL Server** 2022 or higher
* **Redis**: Version 8 or higher

How to install:

1. Clone the repository

   ```bash
   git clone https://github.com/krisnaajiep/springboot-expense-tracker-api.git
   ```

2. Change the current working directory

   ```bash
   cd springboot-expense-tracker-api
   ```

3. Create the database

   ```bash
   sqlcmd -S <host> -U <your_database_username> -P <your_database_password> -No -Q "CREATE DATABASE ExpenseTrackerAPI"
   ```

4. Copy and rename `.env.example`

   ```bash
   cp .env.example .env
   ```

5. Set environment variables in `.env` for database and JWT secret configuration

   ```dotenv
   SPRING_DATASOURCE_URL=jdbc:sqlserver://localhost:1433;databaseName=ExpenseTrackerAPI;encrypt=true;trustServerCertificate=true
   SPRING_DATASOURCE_USERNAME=<your_database_username>
   SPRING_DATASOURCE_PASSWORD=<your_database_password>

   JWT_SECRET=<your_strong_secret>

   SPRING_DATA_REDIS_HOST=localhost
   SPRING_DATA_REDIS_PORT=6379
   SPRING_DATA_REDIS_USERNAME=
   SPRING_DATA_REDIS_PASSWORD=
   ```

6. Build the project

   ```bash
   ./mvnw clean package
   ```

   If you want to skip the test:
   ```bash
   ./mvnw clean package -DskipTests
   ```

7. Run the JAR file

   ```bash
   java -jar target/expense-tracker-api-1.4.0.jar
   ```

## Usage

### Example Request

1. **Register**

   ```http
   POST http://localhost:8080/register
   Content-Type: application/json
   
   {
     "name": "John Doe",
     "email": "john@doe.com",
     "password": "<your_secret_password>"
   }
   ```

2. **Create a new expense**

   ```http
   POST http://localhost:8080/expenses
   Content-Type: application/json
   Authorization: Bearer <your_access_token>
   
   {
       "description": "Purchase of new computer",
       "amount": "800",
       "date": "2025-06-30",
       "category": "ELECTRONICS"
   }
   ```

### API Documentation

* [**Swagger UI**](https://krisnaajiep.github.io/springboot-expense-tracker-api/)
* [**OpenAPI Document**](https://github.com/krisnaajiep/springboot-expense-tracker-api/blob/dev/docs/openapi.yaml)

## Authentication

This API uses Bearer Token for authentication. You can generate an access token by registering a new user or login.

You must include an access token in each request to the API with the Authorization request header.

### Authentication error response

If an API key is missing, malformed, or invalid, you will receive an HTTP 401 Unauthorized response code.

```json
{
  "message": "Unauthorized"
}
```

## Caching Strategy

This API uses a **Cache-Aside strategy** to cache expense listing responses.

- Responses to `GET /expenses` are cached in Redis using a combination of:
   - User ID
   - Date filter (e.g. `PAST_MONTH`)
   - Page number, page size, sort order
- Cache is stored for **60 minutes** (TTL).
- Whenever an expense is created, updated, or deleted, all related cache entries are automatically evicted.
- Cache keys follow the pattern: `expenses::userId=<long>:filter=<ExpenseFilter>&from=<LocalDate>&to=<LocalDate>&page=<int>&size=<int>&sort=<Sort>`

See: [Cache-Aside pattern](https://learn.microsoft.com/en-us/azure/architecture/patterns/cache-aside)

## Rate and Usage Limits

API access rate limits apply on a per-IP address basis in unit time.
The limit is 10 requests per 10 seconds.
If you exceed either limit, your request will return an HTTP 429 `Too Many Requests` status code.

Each API response returns the following set of headers to help you identify your use status:

| Header                  | Description                                                                       |
|-------------------------|-----------------------------------------------------------------------------------|
| `X-RateLimit-Limit`     | The maximum number of requests that the consumer is permitted to make per minute. |
| `X-RateLimit-Remaining` | The number of requests remaining in the current rate limit window.                |
| `X-RateLimit-Reset`     | The time at which the current rate limit window resets in UTC epoch seconds.      |

## HTTP Response Codes

The API returns the following status codes depending on the success or failure of the request.

| Status Code                | Description                                                                                     |
|----------------------------|-------------------------------------------------------------------------------------------------|
| 200 OK                     | The request was processed successfully.                                                         |
| 201 Created                | The new resource was created successfully.                                                      |
| 400 Bad Request            | The server could not understand the request due to invalid syntax.                              |
| 401 Unauthorized           | Authentication is required or the access token is invalid.                                      |
| 403 Forbidden              | Access to the requested resource is forbidden.                                                  |
| 404 Not Found              | The requested resource was not found.                                                           |
| 405 Method Not Allowed     | The HTTP method is not supported for the requested resource.                                    |
| 409 Conflict               | Indicates a conflict between the request and the current state of a resource on a web server.   |
| 415 Unsupported Media Type | The media format of the requested data is not supported by the server.                          |
| 429 Too Many Request       | The client has sent too many requests in a given amount of time (rate limiting).                |
| 500 Internal Server Error  | An unexpected server error occurred.                                                            |
| 503 Service Unavailable    | The server is temporarily unable to handle the request, usually due to maintenance or overload. |

## Project Status

Project is: _complete_.

[![CI](https://github.com/krisnaajiep/springboot-expense-tracker-api/actions/workflows/maven.yml/badge.svg)](https://github.com/krisnaajiep/springboot-expense-tracker-api/actions/workflows/maven.yml)   
[![CD](https://github.com/krisnaajiep/springboot-expense-tracker-api/actions/workflows/azure-webapps-java-jar.yml/badge.svg)](https://github.com/krisnaajiep/springboot-expense-tracker-api/actions/workflows/azure-webapps-java-jar.yml)

## Acknowledgements

This project was inspired by [roadmap.sh](https://roadmap.sh/projects/expense-tracker-api).

## License

This project is licensed under the MIT License—see the [LICENSE](/LICENSE) file for details.
