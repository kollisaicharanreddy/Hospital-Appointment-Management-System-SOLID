**Overview**

This document is a deep-dive explanation of every source file in this workspace and how they connect. Each section covers: purpose, key code elements, dependencies (connected files), and behavior. The **Main.java** section contains a detailed, step-by-step execution flow (line-by-line) showing what happens when you run `java Main`.

References:
- Patients, doctors, appointments, and bills are stored in [services/HospitalStore.java](services/HospitalStore.java)
- Main program entry: [Main.java](Main.java)

**Main.java**

- **Purpose:** Program entrypoint and interactive console UI.
- **Key classes used:** `HospitalStore`, `NotificationService`, `BillingService`, `DoctorAllocationStrategy`, `DoctorManagementService`, `AppointmentBookingService`, `AppointmentCancellationService`, `AppointmentRescheduleService`, `BillingGenerationService`, `ReportGenerationService`.
- **Connected files:** [services/HospitalStore.java](services/HospitalStore.java), [factories/NotificationFactory.java](factories/NotificationFactory.java), [factories/BillingFactory.java](factories/BillingFactory.java), [factories/DoctorAllocationFactory.java](factories/DoctorAllocationFactory.java), services under [services/](services/), models under [models/](models/).

Execution flow (line-by-line, matching `Main.java`):

1. Imports: The file begins by importing factories, interfaces, models, and services required by the application. These imports wire together the small modular components (factories produce implementations; services implement business logic; models are data carriers).

2. `public class Main { public static void main(String[] args) {` — JVM starts here when `java Main` is run.

3. `Scanner sc = new Scanner(System.in);` — creates a `Scanner` to read console input from the user.

4. `HospitalStore store = new HospitalStore();` — instantiates the central in-memory data store. See [services/HospitalStore.java](services/HospitalStore.java) for storage implementation: it keeps Maps for patients, doctors, appointments and a List for bills and manages auto-increment IDs.

5. `NotificationService notificationService = NotificationFactory.create("console");` — uses [factories/NotificationFactory.java](factories/NotificationFactory.java) to create a `NotificationService` implementation. With argument `"console"` the factory returns `implementations.notifications.ConsoleNotificationService` which prints notifications to stdout.

6. `BillingService billingService = BillingFactory.create("standard");` — creates a billing policy via [factories/BillingFactory.java](factories/BillingFactory.java). `"standard"` maps to `implementations.billing.StandardBillingService` which returns a fixed charge (100.0) for `generateCharge`.

7. `DoctorAllocationStrategy doctorAllocationStrategy = DoctorAllocationFactory.create("specialty", store);` — chooses a doctor allocation strategy using [factories/DoctorAllocationFactory.java](factories/DoctorAllocationFactory.java). Passing `"specialty"` returns `implementations.allocation.SpecialtyBasedAllocationStrategy`, which uses the `HospitalStore` to find a doctor matching a requested specialty.

8. Service instances are created next:
- `DoctorManagementService doctorSvc = new DoctorManagementService(store);` — wraps `store.addDoctor(...)` and any doctor-related logic.
- `AppointmentBookingService bookSvc = new AppointmentBookingService(store, doctorAllocationStrategy, notificationService);` — handles booking: checks patient exists, allocates a doctor, creates an appointment in `store`, and notifies the patient.
- `AppointmentCancellationService cancelSvc = new AppointmentCancellationService(store, notificationService);` — removes appointments and notifies when removal succeeds.
- `AppointmentRescheduleService rescheduleSvc = new AppointmentRescheduleService(store, notificationService);` — replaces an appointment with an updated timeslot and notifies the patient.
- `BillingGenerationService billingSvc = new BillingGenerationService(store, billingService);` — finds appointment and delegates to `billingService.generateCharge` to compute amount, then persists a `Bill` via `store.addBill(...)`.
- `ReportGenerationService reportSvc = new ReportGenerationService(store);` — uses `ReportFactory` to construct report generators.

9. The program then enters a `while(true)` interactive loop printing a menu: `1)add patient 2)add doctor 3)book 4)cancel 5)reschedule 6)bill 7)reports 8)list 0)exit` and reads a line from `Scanner`.

10. On input `0` the loop `break`s and program proceeds to `sc.close()` and exits. For other inputs, a `switch` decides the action.

11. Case `1` (add patient): reads `name` and `phone` from console, calls `store.addPatient(name, phone)` which returns a `Patient` (see [models/Patient.java](models/Patient.java) and [services/HospitalStore.java](services/HospitalStore.java)), then prints confirmation.

12. Case `2` (add doctor): reads `name` and `specialty`, calls `doctorSvc.add(dname, spec)` which uses `HospitalStore.addDoctor` to create and store a `Doctor` ([models/Doctor.java](models/Doctor.java)). Prints confirmation.

13. Case `3` (book): reads `patient id`, `specialty`, `time`; calls `bookSvc.book(pid, specialty, time, "")`. Internally:
- `bookSvc.book` checks `store.findPatient(pid)`; if null returns `null`.
- Allocates doctor id via the configured `DoctorAllocationStrategy` (here `SpecialtyBasedAllocationStrategy`), which iterates `store.listDoctors()` and finds one where `doctor.specialty.equalsIgnoreCase(requestedSpecialty)`.
- If a doctor is found it calls `store.addAppointment(patientId, doctorId, time, notes)` to persist an `Appointment` ([models/Appointment.java](models/Appointment.java)), then calls `notificationService.notify(String.valueOf(patientId), "Appointment booked with doctor " + doctorId);` and returns the created `Appointment`.

14. Case `4` (cancel): reads `appointment id`, calls `cancelSvc.cancel(cancelId)` which calls `store.removeAppointment(appointmentId)` and notifies on success.

15. Case `5` (reschedule): reads `appointment id` and `new time`, calls `rescheduleSvc.reschedule(rescheduleId, newTime)` which finds the appointment, constructs a new `Appointment` with updated `time`, calls `store.replaceAppointment(updated)`, notifies patient and returns the updated object.

16. Case `6` (bill): reads `appointment id` and calls `billingSvc.generate(billAppointmentId)`. Internally: `BillingGenerationService.generate` finds the appointment; if found, calls `billingService.generateCharge(appointmentId)` (policy from `BillingFactory`) and then `store.addBill(appointmentId, amount)` to create and persist a `Bill` object.

17. Case `7` (reports): prints `reportSvc.generateDailyRevenueReport()` and `reportSvc.generateDoctorReport()`; then asks for a `patient id for history` and prints `reportSvc.generatePatientHistoryReport(patientId)`. `ReportGenerationService` uses [factories/ReportFactory.java](factories/ReportFactory.java) to obtain a `ReportService` implementation (`DailyRevenueReportService`, `DoctorAppointmentReportService`, or `PatientHistoryReportService`) and calls `generate()`.

18. Case `8` (list): prints the contents of `store.listPatients()`, `store.listDoctors()`, `store.listAppointments()`, and `store.listBills()` by iterating and printing `toString()` for each model.

19. Default: prints `unknown` for unrecognized input.

20. End of loop: when `0` selected `sc.close()` is called which closes the `Scanner` and program exits.

Notes about runtime control flow:
- Factories (`factories/*`) centralize creation of concrete implementations (notification, billing, allocation, report). This decouples `Main` and services from concrete classes and makes the application configurable by strings.
- `HospitalStore` is the single source of truth for persisted domain objects in-memory. Nearly every service interacts with it for reads/writes.

**services/HospitalStore.java**

- **Purpose:** In-memory repository for `Patient`, `Doctor`, `Appointment`, and `Bill` objects. Implements simple auto-increment ids and provides add/find/list/remove operations.
- **Key fields:** `Map<Integer, Patient> patients`, `Map<Integer, Doctor> doctors`, `Map<Integer, Appointment> appointments`, `List<Bill> bills`, and `next*Id` counters.
- **Key methods and behaviors:**
  - `addPatient(String name, String phone)`: constructs a new `Patient` with `nextPatientId++`, stores it and returns it.
  - `addDoctor(String name, String specialty)`: constructs and stores a `Doctor` with `nextDoctorId++`.
  - `addAppointment(int patientId, int doctorId, String time, String notes)`: constructs and stores an `Appointment` with `nextAppointmentId++`.
  - `addBill(int appointmentId, double amount)`: constructs and appends a `Bill` with `nextBillId++`.
  - `findPatient`, `findDoctor`, `findAppointment`: map lookups.
  - `removeAppointment(int id)`: removes appointment mapping and returns whether existing mapping was removed.
  - `replaceAppointment(Appointment appointment)`: puts new appointment (overwrites) using `appointment.id`.
  - `list*()` methods return copies (new ArrayList) of collections — this avoids exposing internal maps directly.

**models/**

All model classes are simple immutable data carriers with public final fields and a minimal `toString()` implementation.

- [models/Patient.java](models/Patient.java): holds `id`, `name`, `phone`. Created by `HospitalStore.addPatient` and returned/printed by `Main` and `list` operations.
- [models/Doctor.java](models/Doctor.java): holds `id`, `name`, `specialty`. Created by `HospitalStore.addDoctor` and used by allocation strategies.
- [models/Appointment.java](models/Appointment.java): holds `id`, `patientId`, `doctorId`, `time`, `notes`. Instances are created by `HospitalStore.addAppointment` and recreated during reschedules.
- [models/Bill.java](models/Bill.java): holds `id`, `appointmentId`, `amount`. Created by `HospitalStore.addBill` and printed by `Main`.
- [models/Department.java](models/Department.java): small class with `id` and `name` but not actively used in provided services.
- [models/TimeSlot.java](models/TimeSlot.java): carries `from` and `to` strings; not heavily used in the current code but present for future extension.

**interfaces/**

Interfaces define small contracts used by services and factories.

- [interfaces/AppointmentService.java](interfaces/AppointmentService.java): declares `Appointment book(int patientId, int doctorId, String time, String notes);` — not used directly by current services but expresses the pattern for an appointment creator.
- [interfaces/BillingService.java](interfaces/BillingService.java): declares `double generateCharge(int appointmentId);` implemented by `StandardBillingService`, `EmergencyBillingService`, and `FollowUpDiscountBillingService`.
- [interfaces/DoctorAllocationStrategy.java](interfaces/DoctorAllocationStrategy.java): declares `int allocate(int patientId, String specialty);` implemented by `LeastBusyDoctorAllocationStrategy`, `SpecialtyBasedAllocationStrategy`, and `EmergencyDoctorAllocationStrategy`.
- [interfaces/NotificationService.java](interfaces/NotificationService.java): declares `void notify(String recipient, String message);` implemented by `ConsoleNotificationService`, `EmailNotificationService`, `SMSNotificationService`, `WhatsAppNotificationService`.
- [interfaces/ReportService.java](interfaces/ReportService.java): declares `String generate();` implemented by the three report services.

**factories/**

Purpose: Centralize construction of concrete implementations based on a string key.

- [factories/NotificationFactory.java](factories/NotificationFactory.java): `create(String type)` returns `ConsoleNotificationService` when `null` or unknown, `EmailNotificationService` for `"email"`, `SMSNotificationService` for `"sms"`, `WhatsAppNotificationService` for `"whatsapp"`.
- [factories/BillingFactory.java](factories/BillingFactory.java): `create(String type)` returns `StandardBillingService` when `null` or unknown, `EmergencyBillingService` for `"emergency"`, `FollowUpDiscountBillingService` for `"followup"`.
- [factories/DoctorAllocationFactory.java](factories/DoctorAllocationFactory.java): `create(String type, HospitalStore store)` returns `LeastBusyDoctorAllocationStrategy` default, `SpecialtyBasedAllocationStrategy` for `"specialty"`, `EmergencyDoctorAllocationStrategy` for `"emergency"`.
- [factories/ReportFactory.java](factories/ReportFactory.java): `create(String type, HospitalStore store, int patientId)` selects the desired `ReportService` implementation. `"doctor"` -> `DoctorAppointmentReportService`; `"patient"` -> `PatientHistoryReportService`; default -> `DailyRevenueReportService`.

**implementations/notifications/**

Each implementation prints a different prefix to stdout to simulate sending notifications.

- `ConsoleNotificationService`: prints `[NOTIFY] to:... msg:...`.
- `EmailNotificationService`: prints `[EMAIL] to:... msg:...`.
- `SMSNotificationService`: prints `[SMS] to:... msg:...`.
- `WhatsAppNotificationService`: prints `[WHATSAPP] to:... msg:...`.

Connected files: `Main.java` (which configures the notification type), `AppointmentBookingService`, `AppointmentCancellationService`, and `AppointmentRescheduleService` (which call `notify(...)`).

**implementations/billing/**

- `StandardBillingService`: `generateCharge(...)` returns `100.0`.
- `EmergencyBillingService`: returns `200.0`.
- `FollowUpDiscountBillingService`: returns `70.0`.

These are intentionally simple placeholder strategies to show how billing policy can be swapped via `BillingFactory`.

**implementations/allocation/**

- `LeastBusyDoctorAllocationStrategy`: returns the first doctor in `store.listDoctors()` or `-1` if none. In a real system this would count appointments per doctor.
- `SpecialtyBasedAllocationStrategy`: iterates doctors and returns the `id` of the first doctor whose `specialty` matches the requested `specialty`. If none found, returns first doctor's id or `-1` if no doctors exist.
- `EmergencyDoctorAllocationStrategy`: prefers a doctor whose `specialty` equals `"emergency"`, otherwise falls back to the first doctor.

Connected files: `Main.java` uses `DoctorAllocationFactory` to pick a strategy; `AppointmentBookingService` calls `allocate(...)`.

**implementations/reports/**

- `DailyRevenueReportService`: uses `store.listAppointments().size()` and sums `store.listBills()` amounts to produce a revenue summary string.
- `DoctorAppointmentReportService`: reports basic counts of doctors and appointments.
- `PatientHistoryReportService`: filters `store.listAppointments()` by `appointment.patientId == patientId` and counts them.

All reports implement `ReportService.generate()` and are constructed by `ReportFactory` when `ReportGenerationService` requests them.

**services/**

- `AppointmentBookingService`: coordinates booking — verifies patient exists, asks `DoctorAllocationStrategy` for a `doctorId`, persists an `Appointment` via `HospitalStore.addAppointment(...)`, notifies the patient using `NotificationService`.
- `AppointmentCancellationService`: removes the appointment from `HospitalStore` and notifies.
- `AppointmentRescheduleService`: finds the current appointment, creates a new `Appointment` object with updated time, calls `HospitalStore.replaceAppointment(updated)`, and notifies the patient.
- `BillingGenerationService`: finds the appointment and delegates charge computation to a `BillingService`, then persists a `Bill` in `HospitalStore`.
- `DoctorManagementService`: thin wrapper over `HospitalStore.addDoctor(...)`.
- `ReportGenerationService`: uses `ReportFactory` to obtain `ReportService` implementation and calls `generate()`.

**Key Java features and standard library usage used across the project**

- `java.util` collections: `Map`, `List`, `ArrayList`, `LinkedHashMap` — used for in-memory storage with predictable iteration order.
- `java.util.Scanner` — used to read console input in `Main`.
- Streams and lambdas: used in report implementations (`store.listBills().stream().mapToDouble(...).sum()`) and in `PatientHistoryReportService` (`stream().filter(...).count()`). This requires Java 8+.
- `final` fields in models: immutability pattern for simple DTOs.
- Interfaces and polymorphism: the project uses interfaces (`BillingService`, `NotificationService`, `DoctorAllocationStrategy`, `ReportService`, `AppointmentService`) and factories to decouple implementation selection from business logic.
- Factory pattern: `factories/*` centralize object creation based on a string key.

**SOLID principles in this project**

- **S - Single Responsibility Principle**
  - `services/AppointmentBookingService.java`, `services/AppointmentCancellationService.java`, `services/AppointmentRescheduleService.java`, `services/BillingGenerationService.java`, and `services/ReportGenerationService.java` each do one business job instead of combining booking, billing, reporting, and notification logic in one class.
  - `factories/NotificationFactory.java`, `factories/BillingFactory.java`, `factories/DoctorAllocationFactory.java`, and `factories/ReportFactory.java` only create objects and do not contain business rules.
  - `services/HospitalStore.java` only manages in-memory data storage and lookup. It does not decide business rules such as how to allocate doctors or how to calculate billing.
  - Why it matters: the code is easier to understand, test, and change because one class has one main reason to change.

- **O - Open/Closed Principle**
  - `interfaces/BillingService.java`, `interfaces/DoctorAllocationStrategy.java`, `interfaces/NotificationService.java`, and `interfaces/ReportService.java` define extension points.
  - New billing or allocation behavior can be added by creating a new class in `implementations/billing/` or `implementations/allocation/` and wiring it through the relevant factory, without changing the caller logic in `Main.java` or the service classes.
  - Example: `implementations/billing/EmergencyBillingService.java` and `implementations/billing/FollowUpDiscountBillingService.java` extend billing behavior without modifying `services/BillingGenerationService.java`.
  - Why it matters: the system is open for extension but closed for direct modification in the core flow.

- **L - Liskov Substitution Principle**
  - Any class that implements `interfaces/BillingService.java` can be used where a `BillingService` is expected, such as `StandardBillingService`, `EmergencyBillingService`, or `FollowUpDiscountBillingService`.
  - Any class that implements `interfaces/NotificationService.java` can be swapped into `AppointmentBookingService`, `AppointmentCancellationService`, or `AppointmentRescheduleService` without changing those services.
  - Any `DoctorAllocationStrategy` implementation can be used by `AppointmentBookingService` because it only relies on the `allocate(...)` contract.
  - Why it matters: the services remain stable even when the concrete implementations change, as long as the contract is honored.

- **I - Interface Segregation Principle**
  - The project uses small focused interfaces instead of one large interface. For example, billing, notification, doctor allocation, and reporting each have their own contract.
  - `interfaces/AppointmentService.java` is also small and focused on appointment booking behavior only.
  - Why it matters: classes are not forced to implement methods they do not need, which keeps the design simpler and cleaner.

- **D - Dependency Inversion Principle**
  - High-level services such as `services/AppointmentBookingService.java` and `services/BillingGenerationService.java` depend on abstractions (`DoctorAllocationStrategy`, `NotificationService`, `BillingService`) rather than concrete classes.
  - `Main.java` creates the concrete implementations through factories and passes them into the services. This moves object creation away from business logic.
  - `services/ReportGenerationService.java` depends on `interfaces/ReportService` through `ReportFactory`, not on specific report classes directly.
  - Why it matters: the design is easier to test, swap, and extend because dependencies are inverted toward interfaces instead of hard-coded implementations.

Short conclusion on SOLID:
- The project shows SOLID in a practical but lightweight way, especially through small services, interfaces, and factories.
- Some model and store classes are intentionally simple because this is a console-based LLD project, but the core service design still follows the principles well.

**How files connect (high-level call graph)**

- `Main.java` creates a `HospitalStore` and configures implementations via factories. It constructs all service objects and drives the CLI loop.
- `AppointmentBookingService`, `AppointmentCancellationService`, `AppointmentRescheduleService`, `BillingGenerationService`, `DoctorManagementService`, `ReportGenerationService` all depend on `HospitalStore`.
- `AppointmentBookingService` depends on `DoctorAllocationStrategy` (provided by `DoctorAllocationFactory`) and `NotificationService` (provided by `NotificationFactory`).
- `BillingGenerationService` depends on `BillingService` (provided by `BillingFactory`).
- `ReportGenerationService` depends on `ReportFactory` which selects a `ReportService` implementation.

**Notes, limitations, and suggestions for extension**

- The allocation strategies use simple heuristics; they can be replaced with more realistic data-driven strategies (e.g., counting current appointments per doctor, availability calendars, or priority queues).
- `HospitalStore` is in-memory and single-process. To persist data between restarts, add a persistence layer (JDBC/ORM, file storage, or a lightweight embedded DB).
- Notification implementations are stubs that `System.out.println` messages — replace with real integrations for email/SMS/WhatsApp to deliver notifications.
- Consider adding validation and error handling around `Integer.parseInt` calls in `Main` to avoid runtime exceptions on invalid input.
- Add unit tests for each service and strategy. The current structure—with small classes and dependency injection—makes this straightforward.

---

If you want, I can:
- generate separate `DeepDive_<filename>.md` files per source file instead of this single document, or
- produce a condensed diagram (Mermaid) showing the call graph and dependencies, or
- run a static analysis or add Javadoc comments directly into the source files.

Tell me which follow-up you'd like next.

**Testing & Coverage (2026-05-30)**

- To run the project's tests use Maven from the project root:

```powershell
mvn test
```

- Test reports (plain text and XML) are produced under `target/surefire-reports/` (useful for CI or local inspection).
- To produce an HTML coverage report with JaCoCo run:

```powershell
mvn test jacoco:report
# or if tests already ran: mvn jacoco:report
```

- The coverage site is available at `target/site/jacoco/index.html`. The JaCoCo execution data file is `target/jacoco.exec`.
- If you want a short summary added here (number of tests, failures, and overall coverage %), I can run the tests now and append the results to this document.
