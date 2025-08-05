# SoftwareArchitecturesAndPlatform-Assignment2

This repository contains the implementation for Assignment 2 of the Software Architectures and Platform course. The project demonstrates the application of microservices architecture using Docker and Docker Compose.

## Project Structure

The project is organized into several microservices, each with its own directory:

- `admin-gui` -> admin interface for managing the platform
- `apigateway` -> API Gateway for routing requests to appropriate services
- `ebikes-manager` -> service for managing electric bikes
- `end2end-test` -> end-to-end tests for the platform
- `registry` -> service registry for microservices discovery
- `rides-manager` -> service for managing rides
- `user-gui` -> user interface for interacting with the platform
- `users-manager` -> service for managing users.

## Prerequisites

- Docker
- Docker Compose:contentReference[oaicite:25]{index=25}

## Getting Started

1. Clone the repository:​:contentReference[oaicite:28]{index=28}

   ```bash
   git clone https://github.com/FabioNotaro2001/SoftwareArchitecturesAndPlatform-Assignment2.git
   cd SoftwareArchitecturesAndPlatform-Assignment2

Build and start the services using Docker Compose:
docker-compose up --build

    Access the services:

        Admin GUI: http://localhost:8081

        User GUI: http://localhost:8082

Testing

End-to-end tests are located in the end2end-test directory. Ensure all services are running before executing the tests.
License

This project is licensed under the MIT License - see the LICENSE file for details.
