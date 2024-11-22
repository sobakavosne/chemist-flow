A distributed system designed to manage and execute parallel requests to the Chemist service.

Run the project:

  $> docker compose -f docker-compose.yml up --build

Project Structure

  - `api`: Handles HTTP endpoints and routing logic for the service.
  - `app`: Manages application-level resources and initialisation.
  - `core`: Implements domain logic, repositories, and services.
  - `infrastructure`: Provides low-level utilities, like HTTP clients.
  - `resource`: Stores configuration files (`application.conf`, `logback.xml`).
  - `test`: Contains unit and integration tests for all components.

Flow vs Preprocessor

  - Flow: Represents operations involving distributed or parallel processing of reactions and mechanisms
  - Preprocessor: Handles tasks to prepare and clean up input data for use in the flow or other services.
  It obtains reactions and mechanisms from the Preprocessor service converts to the corresponding JSON 
  representation

Contribution Guidelines

- Follow the project structure when adding new features:
  - Use `core/domain` for domain classes.
  - Place new APIs under `api/endpoints`.
  - Keep tests in `test` mirroring the `main` folder structure.

Git Hooks Setup

  To enable custom Git hooks for this repository, configure Git to use the `.githooks` directory:

  $> chmod +x .githooks/pre-push
  $> git config --local core.hooksPath .githooks
