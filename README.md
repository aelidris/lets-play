📋 Running and Testing Instructions

    Prerequisites: Ensure you have Java 17+, Maven, and MongoDB running locally (mongodb://localhost:27017).

    Build and Run:
    Bash

mvn clean spring-boot:run

API Testing with Postman:

    Register User: POST http://localhost:8080/api/auth/register
    JSON

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "role": "USER"
}

Login: POST http://localhost:8080/api/auth/login (Returns the JWT token).

Get Products (Public): GET http://localhost:8080/products

Create Product (Authenticated): POST http://localhost:8080/products with Header Authorization: Bearer <TOKEN>
JSON

        {
          "name": "Gaming Console",
          "description": "Next-gen console",
          "price": 499.99
        }