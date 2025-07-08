# Spring Boot Expense Tracker API

> Simple Expense Tracker RESTful API built with Spring Boot to allow users to create, read, update, and delete expenses.

## Table of Contents

- [General Info](#general-information)
- [Technologies Used](#technologies-used)
- [Features](#features)
- [Setup](#setup)
- [Usage](#usage)
- [HTTP Response Codes](#http-response-codes)
- [Project Status](#project-status)
- [Acknowledgements](#acknowledgements)
- [License](#license)

## General Information

Spring Boot Expense Tracker API is a simple RESTful API that allows users to manage their expenses.
It supports pagination, sorting, and filtering by date range.
This API uses [JWT](https://jwt.io/) for authentication.
This project is designed to explore and practice working with the Java programming language, data modeling,
and user authentication in Spring Boot.

## Technologies Used

- Java 21.0.6 LTS
- Maven 3.9.9
- Microsoft SQL Server 2022
- Spring Boot 3.4.5
- Lombok 1.18.38
- [JJWT](https://github.com/jwtk/jjwt) 0.12.6

## Features

- **User Registration**: Register a new user using the `POST` method.
- **User Login**: Authenticate the user using the `POST` method.
- **Create a new expense**: Create a new expense using the `POST` method.
- **Update an existing expense**: Update an existing expense using the `PUT` method.
- **Delete an existing expense**: Delete an existing expense using the `DELETE` method.
- **List and filter all past expenses**: Get the list of expenses with pagination and filtering by date range using the
  `GET` method.
- **Refresh Token**: Get a new access token using the `POST` method.

## Setup

To run this API, you’ll need:

* **Java**: Version 21 or higher
* **Maven**: Version 3.x
* **Microsoft SQL Server** 2022 or higher

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

5. Set environment variables in `.env` for databases and JWT secret configuration

   ```dotenv
    DB_HOST=your_database_host
    DB_PORT=your_database_port
    DB_NAME=ExpenseTrackerAPI
    DB_USERNAME=your_database_username
    DB_PASSWORD=your_database_password
    JWT_SECRET=your_jwt_secret
   ```

6. Build the project

   ```bash
   mvn clean package -DskipTests
   ```

7. Run the JAR file

   ```bash
   java -jar target/expense-tracker-api-1.2.1.jar
   ```

## Usage

Example API Endpoints:

1. **User Registration**

    - Method: `POST`
    - Endpoint: `/register`
    - Request Header:

        - `Content-Type` (string)—The content type of request body (must be `application/json`).

    - Request Body:

        - `name` (string)—The name of the user.
        - `email` (string)—The email address of the user.
        - `password` (string)—The password of the user account.

    - Example Request:

      ```http
      POST /register
      Content-Type: application/json
      
      {
        "name": "John Doe",
        "email": "john@doe.com",
        "password": "example_password",
      }
      ```

    - Response:

        - Status: `201 Created`
        - Content-Type: `application/json`

    - Example Response:

      ```json
      {
        "access-token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9_access",
        "refresh-token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9_refresh"
      }
      ```

2. **User Login**

    - Method: `POST`
    - Endpoint: `/login`
    - Request Header:

        - `Content-Type` (string)—The content type of request body (must be `application/json`).

    - Request Body:

        - `email` (string)—The email address of the user.
        - `password` (string)—The password of the user account.

    - Example Request:

      ```http
      POST /login
      Content-Type: application/json
      
      {
        "email": "john@doe.com",
        "password": "example_password",
      }
      ```

    - Response:

        - Status: `200 OK`
        - Content-Type: `application/json`

    - Example Response:

      ```json
      {
        "access-token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9_access",
        "refresh-token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9_refresh"
      }
      ```

3. **Refresh Token**

    - Method: `POST`
    - Endpoint: `/refresh`
    - Request Header:

        - `Content-Type` (string)—The content type of request body (must be `application/json`).

    - Request Body:

        - `refresh-token` (string)—The refresh token.

    - Example Request:

      ```http
      POST /refresh
      Content-Type: application/json
      
      {
        "refresh-token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9_refresh"
      }
      ```

    - Response:

        - Status: `200 OK`
        - Content-Type: `application/json`

    - Example Response:

      ```json
      {
        "access-token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9_new_access",
        "refresh-token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9_new_refresh"
      }
      ```

4. **Create an expense**

    - Method: `POST`
    - Endpoint: `/expenses`
    - Request Header:

        - `Content-Type` (string)—The content type of request body (must be `application/json`).
        - `Authorization` (string)—The access token with `Bearer` type.

    - Request Body:

        - `description` (string)—The description of the expense.
        - `amount` (number)—The amount spent. Must be positive.
        - `date` (string)—The date of the expense in yyyy-MM-dd format.
        - `category` (string)—The category of the expense. Must be one of the allowed values:
            - `Groceries`
            - `Leisure`
            - `Electronics`
            - `Utilities`
            - `Clothing`
            - `Health`
            - `Others`

    - Example Request:

      ```http
      POST /expenses
      Content-Type: application/json
      Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9_access
      
      {
        "description": "Buy milk, eggs, and bread",
        "amount": 60.00,
        "date": "2023-10-02",
        "category": "Groceries"
      }
      ```

    - Response:

        - Status: `201 Created`
        - Content-Type: `application/json`

    - Example Response:

      ```json
      {
        "id": 1,
        "description": "Buy milk, eggs, and bread",
        "amount": 60.00,
        "date": "2023-10-02",
        "category": "Groceries"
      }
      ```

5. **Update an existing expense**

    - Method: `PUT`
    - Endpoint: `/expenses/{id}`
    - Request Header:

        - `Content-Type` (string)—The content type of request body (must be `application/json`).
        - `Authorization` (string)—The access token with `Bearer` type.

    - Request Body:

        - `description` (string)—The description of the expense.
        - `amount` (number)—The amount spent. Must be positive.
        - `date` (string)—The date of the expense in yyyy-MM-dd format.
        - `category` (string)—The category of the expense. Must be one of the allowed values:
            - `Groceries`
            - `Leisure`
            - `Electronics`
            - `Utilities`
            - `Clothing`
            - `Health`
            - `Others`

    - Example Request:

      ```http
      POST /expenses/1
      Content-Type: application/json
      Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9_access
      
      {
       "description": "Buy milk, eggs, bread, and cheese"
       "amount": 60.00,
       "date": "2023-10-02",
       "category": "Groceries"
      }
      ```

    - Response:

        - Status: `200 OK`
        - Content-Type: `application/json`

    - Example Response:

      ```json
      {
        "id": 1,
        "description": "Buy milk, eggs, bread, and cheese",
        "amount": 60.00,
        "date": "2023-10-02",
        "category": "Groceries"
      }
      ```

6. **Delete an existing expense**

    - Method: `DELETE`
    - Endpoint: `/expenses/{id}`
    - Request Header:

        - `Authorization` (string)—The access token with `Bearer` type.

    - Response:

        - Status: `204 No Content`

7. **List and filter expenses**

    - Method: `GET`
    - Endpoint: `/expenses`
    - Request Header:

        - `Content-Type` (string)—The content type of request body (must be `application/json`).
        - `Authorization` (string)—The access token with `Bearer` type.

    - Response:

        - Status: `200 OK`
        - Content-Type: `application/json`

    - Example Response:

      ```json
      {
         "content": [
           {
             "id": 1,
             "description": "Buy milk, eggs, bread, and cheese",
             "amount": 60.00,
             "date": "2023-10-02",
             "category": "Groceries"
           },
           {
             "id": 2,
             "description": "Pay electricity and water bills",
             "amount": 120.00,
             "date": "2023-10-03",
             "category": "Utilities"
           }
         ],
         "page": {
            "size": 10,
            "number": 0,
            "totalElements": 2,
            "totalPages": 1
         }
      }
      ```

        - Params:

            - `page` - (integer, optional)—The page number to retrieve (default is `0`).
            - `size` - (integer, optional)—The number of expenses per page (default is `20`).
            - `sort` - (string, optional)—The sorting criteria in the format `property,asc|desc`.
            - `filter` - (string, optional)—The filter type to apply based on the expense date. Valid values are:
                - `past_week` - Expenses from the past week.
                - `past_month` - Expenses from the past month.
                - `last_3_months` - Expenses from the last 3 months.
            - `from` - (string, optional)—The start date for custom expense filtering in the format `yyyy-MM-dd`.
            - `to` - (string, optional)—The end date for custom expense filtering in the format `yyyy-MM-dd`.

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

## Rate and Usage Limits

API access rate limits apply on a per-IP address basis in unit time.
The limit is 60 requests per minute.
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

## Acknowledgements

This project was inspired by [roadmap.sh](https://roadmap.sh/projects/expense-tracker-api).

## License

This project is licensed under the MIT License—see the [LICENSE](./LICENSE) file for details.
