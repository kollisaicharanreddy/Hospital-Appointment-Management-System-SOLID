# Hospital Appointment Management System

This workspace contains a simple console-based Java version of a Hospital Appointment Management System. It is designed as a low-level design practice project that shows how to structure a small application using Java interfaces, factories, services, and in-memory storage.

## What this project does

The application lets a user manage basic hospital workflows from the terminal:

- add patients
- add doctors
- book appointments
- cancel appointments
- reschedule appointments
- generate bills
- print summary reports

The code is intentionally modular. Each feature is separated into small classes so the behavior is easy to trace and explain in [DeepDive.md](DeepDive.md).

## How it is organized

- `models/` holds the data objects like patients, doctors, appointments, and bills.
- `interfaces/` defines small contracts for billing, notifications, reporting, and doctor allocation.
- `implementations/` contains the concrete strategies and services for those interfaces.
- `factories/` creates the correct implementation based on a string input.
- `services/` contains the business logic and the in-memory `HospitalStore`.
- `Main.java` is the console entry point that wires everything together.

## Why this project is useful

This project is a good reference for learning:

- object-oriented design in Java
- dependency injection through constructors
- factory pattern usage
- interface-driven architecture
- basic SOLID principles in a small codebase
- how to keep application state in memory with simple collections

Compile and run:

```powershell
javac models\*.java interfaces\*.java factories\*.java implementations\notifications\*.java implementations\billing\*.java implementations\reports\*.java implementations\allocation\*.java services\*.java Main.java
java Main
```

Use the menu in `Main` to add patients, add doctors, book appointments, cancel appointments, reschedule appointments, generate bills, and print reports.

For a detailed explanation of every file and the full runtime flow, see [DeepDive.md](DeepDive.md).

## Testing & Coverage (2026-05-30)

- **Run unit tests:** execute `mvn test` from the repository root. Test reports (text and XML) are written to `target/surefire-reports/`.
- **Generate coverage report (JaCoCo):** run `mvn test jacoco:report` (or `mvn jacoco:report` after tests complete). The HTML coverage report is available at `target/site/jacoco/index.html` and the binary execution data is `target/jacoco.exec`.
- **Quick checks:** open `target/surefire-reports/` for test outputs and `target/site/jacoco/index.html` in a browser to inspect line-level coverage.

If you'd like, I can run the tests locally and embed a short summary (pass/fail counts and coverage %) into these docs.
