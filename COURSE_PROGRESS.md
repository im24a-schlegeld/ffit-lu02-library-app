# FFIT Java Course Progress

Status values: TODO, IN PROGRESS, COMPLETE, BLOCKED, MANUAL TASK, NO CODE REQUIRED.

## Requirements and source material

- COMPLETE: Inspect the outer and nested Git repositories, remotes, branches, and history.
- COMPLETE: Inspect source, tests, Gradle files, CI, resources, and configuration.
- BLOCKED: `moodle_tasks.md` is not present anywhere under the workspace root.
- BLOCKED: The LU01 palindrome repository is not present anywhere under the workspace root.
- COMPLETE: Official BZZ learning-unit pages were inspected through LU05 as of 15 September 2026.
- COMPLETE: Referenced commits were inspected with `git show`; existing work was not blindly cherry-picked.

## LU01

- BLOCKED: Complete palindrome assignment; the repository and `moodle_tasks.md` are missing.
- MANUAL TASK: Git branching exercise; it cannot be completed as Java code.
- COMPLETE: No LU01 code could be changed without the missing repository and exact assignment tests.

## LU02

- COMPLETE: LU02a console input and command loop.
- COMPLETE: LU02a `help`, `quit`, and unknown-command handling.
- COMPLETE: LU02b `Book` functionality and `listBooks`.
- COMPLETE: LU02b PostgreSQL/JDBC persistence and ordered book retrieval.
- COMPLETE: LU02c `config.properties` loading without committed credentials.
- COMPLETE: LU02c TSV import and database persistence with ID replacement.

## LU03

- COMPLETE: LU03a DRY/SRP refactoring requirements applied while preserving behavior.
- COMPLETE: LU03b SLF4J/Logback logging and robust invalid-input/file handling.
- COMPLETE: LU03b optional `listBooks` limit.
- COMPLETE: LU03c JPA/Hibernate entities, persistence configuration, and EntityManager persistence.

## LU04

- COMPLETE: LU04a Javalin API on port 7070 with `GET /books`, JSON, and optional limit.
- COMPLETE: LU04b `User` entity and `createUser` command.
- COMPLETE: LU04b salted SHA-256 password hashing; plaintext passwords are not persisted.

## LU05

- NO CODE REQUIRED: LU05a inheritance/interfaces/abstract classes page contains theory only.
- NO CODE REQUIRED: LU05b LSP/ISP page contains theory only.
- COMPLETE: LU05c generic types; persistence is centralized in `AbstractPersistor<T>` and specialized for books/users.

## Validation

- BLOCKED: Outer `./gradlew test` currently fails during test compilation because `JavalinMainTest` uses Java 22 unnamed-resource syntax and the local compiler is older. CI is configured for JDK 22.
- COMPLETE: Nested clean template clone `./gradlew test` passed.
- COMPLETE: Complete outer test suite and `./gradlew build` pass with JDK 22.

## Security and manual work

- COMPLETE: No credentials were added to tracked files; `config.properties` remains local/ignored.
- MANUAL TASK: Configure local PostgreSQL credentials/database if not already available.
- MANUAL TASK: Perform interactive Git branching exercise and manual Postman/API checks.