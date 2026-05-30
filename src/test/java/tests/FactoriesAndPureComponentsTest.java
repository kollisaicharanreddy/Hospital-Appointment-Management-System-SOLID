package tests;

import factories.BillingFactory;
import factories.DoctorAllocationFactory;
import factories.NotificationFactory;
import factories.ReportFactory;
import implementations.allocation.EmergencyDoctorAllocationStrategy;
import implementations.allocation.LeastBusyDoctorAllocationStrategy;
import implementations.allocation.SpecialtyBasedAllocationStrategy;
import implementations.billing.EmergencyBillingService;
import implementations.billing.FollowUpDiscountBillingService;
import implementations.billing.StandardBillingService;
import implementations.notifications.ConsoleNotificationService;
import implementations.notifications.EmailNotificationService;
import implementations.notifications.SMSNotificationService;
import implementations.notifications.WhatsAppNotificationService;
import implementations.reports.DailyRevenueReportService;
import implementations.reports.DoctorAppointmentReportService;
import implementations.reports.PatientHistoryReportService;
import interfaces.BillingService;
import interfaces.DoctorAllocationStrategy;
import interfaces.NotificationService;
import interfaces.ReportService;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import models.Appointment;
import models.Bill;
import models.Department;
import models.Doctor;
import models.Patient;
import models.TimeSlot;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import services.HospitalStore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FactoriesAndPureComponentsTest {

    private static String captureOutput(Runnable action) {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer));

        try {
            action.run();
        } finally {
            System.setOut(original);
        }

        return buffer.toString().replace("\r\n", "\n");
    }

    @Nested
    class BillingFactoryCases {

        @Test
        void usesStandardBillingForNullAndUnknownTypes() {
            BillingService nullBilling = BillingFactory.create(null);
            BillingService unknownBilling = BillingFactory.create("anything");

            assertInstanceOf(StandardBillingService.class, nullBilling);
            assertInstanceOf(StandardBillingService.class, unknownBilling);
        }

        @Test
        void selectsEmergencyAndFollowUpBillingStrategies() {
            assertInstanceOf(EmergencyBillingService.class, BillingFactory.create("emergency"));
            assertInstanceOf(FollowUpDiscountBillingService.class, BillingFactory.create("followup"));
        }
    }

    @Nested
    class DoctorAllocationFactoryCases {

        @Test
        void fallsBackToLeastBusyStrategyForNullAndUnknownTypes() {
            HospitalStore store = new HospitalStore();

            assertInstanceOf(LeastBusyDoctorAllocationStrategy.class, DoctorAllocationFactory.create(null, store));
            assertInstanceOf(LeastBusyDoctorAllocationStrategy.class, DoctorAllocationFactory.create("unknown", store));
        }

        @Test
        void choosesSpecialtyAndEmergencyStrategies() {
            HospitalStore store = new HospitalStore();

            assertInstanceOf(SpecialtyBasedAllocationStrategy.class, DoctorAllocationFactory.create("specialty", store));
            assertInstanceOf(EmergencyDoctorAllocationStrategy.class, DoctorAllocationFactory.create("emergency", store));
        }

        @Test
        void passesTheStoreIntoTheSelectedStrategy() {
            HospitalStore store = new HospitalStore();
            Doctor first = store.addDoctor("Alex", "cardiology");

            DoctorAllocationStrategy specialtyStrategy = DoctorAllocationFactory.create("specialty", store);
            DoctorAllocationStrategy emergencyStrategy = DoctorAllocationFactory.create("emergency", store);
            DoctorAllocationStrategy leastBusyStrategy = DoctorAllocationFactory.create("anything", store);

            assertEquals(first.id, specialtyStrategy.allocate(10, "cardiology"));
            assertEquals(first.id, emergencyStrategy.allocate(10, "anything"));
            assertEquals(first.id, leastBusyStrategy.allocate(10, "anything"));
        }
    }

    @Nested
    class NotificationFactoryCases {

        @Test
        void fallsBackToConsoleNotificationsForNullAndUnknownTypes() {
            NotificationService nullNotification = NotificationFactory.create(null);
            NotificationService unknownNotification = NotificationFactory.create("whatever");

            assertInstanceOf(ConsoleNotificationService.class, nullNotification);
            assertInstanceOf(ConsoleNotificationService.class, unknownNotification);
        }

        @Test
        void choosesEmailSmsAndWhatsAppNotifications() {
            assertInstanceOf(EmailNotificationService.class, NotificationFactory.create("email"));
            assertInstanceOf(SMSNotificationService.class, NotificationFactory.create("sms"));
            assertInstanceOf(WhatsAppNotificationService.class, NotificationFactory.create("whatsapp"));
        }
    }

    @Nested
    class ReportFactoryCases {

        @Test
        void fallsBackToDailyRevenueReportsForNullAndUnknownTypes() {
            HospitalStore store = new HospitalStore();

            assertInstanceOf(DailyRevenueReportService.class, ReportFactory.create(null, store, 0));
            assertInstanceOf(DailyRevenueReportService.class, ReportFactory.create("anything", store, 0));
        }

        @Test
        void choosesDoctorAndPatientReports() {
            HospitalStore store = new HospitalStore();

            assertInstanceOf(DoctorAppointmentReportService.class, ReportFactory.create("doctor", store, 0));
            assertInstanceOf(PatientHistoryReportService.class, ReportFactory.create("patient", store, 99));
        }

        @Test
        void patientReportsUseTheProvidedPatientId() {
            HospitalStore store = new HospitalStore();
            store.addPatient("Mina", "100");
            store.addDoctor("Dr. Lee", "cardiology");
            store.addAppointment(1, 1, "09:00", "checkup");
            store.addAppointment(2, 1, "10:00", "follow-up");

            ReportService report = ReportFactory.create("patient", store, 1);

            assertEquals("Patient history report: patientId=1, appointments=1", report.generate());
        }
    }

    @Nested
    class AllocationStrategyCases {

        @Test
        void leastBusyStrategyReturnsMinusOneWhenNoDoctorsExist() {
            DoctorAllocationStrategy strategy = new LeastBusyDoctorAllocationStrategy(new HospitalStore());

            assertEquals(-1, strategy.allocate(1, "cardiology"));
        }

        @Test
        void leastBusyStrategyChoosesTheFirstDoctor() {
            HospitalStore store = new HospitalStore();
            Doctor first = store.addDoctor("A", "cardiology");
            store.addDoctor("B", "neurology");

            DoctorAllocationStrategy strategy = new LeastBusyDoctorAllocationStrategy(store);

            assertEquals(first.id, strategy.allocate(1, "anything"));
        }

        @Test
        void specialtyStrategyPrefersMatchingSpecialtyAndFallsBackWhenNeeded() {
            HospitalStore store = new HospitalStore();
            Doctor cardiology = store.addDoctor("Dr. One", "cardiology");
            Doctor first = store.addDoctor("Dr. Two", "general");

            DoctorAllocationStrategy strategy = new SpecialtyBasedAllocationStrategy(store);

            assertEquals(cardiology.id, strategy.allocate(1, "cardiology"));
            assertEquals(cardiology.id, strategy.allocate(1, null));
        }

        @Test
        void specialtyStrategyReturnsMinusOneForEmptyStore() {
            DoctorAllocationStrategy strategy = new SpecialtyBasedAllocationStrategy(new HospitalStore());

            assertEquals(-1, strategy.allocate(1, "cardiology"));
        }

        @Test
        void emergencyStrategyPrefersEmergencyDoctorsAndFallsBackWhenNeeded() {
            HospitalStore store = new HospitalStore();
            Doctor first = store.addDoctor("Dr. One", "cardiology");
            Doctor emergency = store.addDoctor("Dr. Two", "emergency");

            DoctorAllocationStrategy strategy = new EmergencyDoctorAllocationStrategy(store);

            assertEquals(emergency.id, strategy.allocate(1, "cardiology"));
            assertEquals(emergency.id, strategy.allocate(1, null));

            HospitalStore noEmergencyStore = new HospitalStore();
            Doctor onlyDoctor = noEmergencyStore.addDoctor("Dr. Three", "orthopedics");

            assertEquals(onlyDoctor.id, new EmergencyDoctorAllocationStrategy(noEmergencyStore).allocate(1, "cardiology"));
            assertEquals(-1, new EmergencyDoctorAllocationStrategy(new HospitalStore()).allocate(1, "cardiology"));
        }
    }

    @Nested
    class BillingServiceCases {

        @Test
        void standardBillingAlwaysReturnsOneHundred() {
            assertEquals(100.0, new StandardBillingService().generateCharge(1));
        }

        @Test
        void emergencyBillingAlwaysReturnsTwoHundred() {
            assertEquals(200.0, new EmergencyBillingService().generateCharge(1));
        }

        @Test
        void followUpBillingAlwaysReturnsSeventy() {
            assertEquals(70.0, new FollowUpDiscountBillingService().generateCharge(1));
        }
    }

    @Nested
    class NotificationServiceCases {

        @Test
        void consoleNotificationPrintsTheExpectedMessage() {
            String output = captureOutput(() -> new ConsoleNotificationService().notify("1", "done"));

            assertTrue(output.contains("[NOTIFY] to:1 msg:done"));
        }

        @Test
        void emailNotificationPrintsTheExpectedMessage() {
            String output = captureOutput(() -> new EmailNotificationService().notify("2", "sent"));

            assertTrue(output.contains("[EMAIL] to:2 msg:sent"));
        }

        @Test
        void smsNotificationPrintsTheExpectedMessage() {
            String output = captureOutput(() -> new SMSNotificationService().notify("3", "sent"));

            assertTrue(output.contains("[SMS] to:3 msg:sent"));
        }

        @Test
        void whatsappNotificationPrintsTheExpectedMessage() {
            String output = captureOutput(() -> new WhatsAppNotificationService().notify("4", "sent"));

            assertTrue(output.contains("[WHATSAPP] to:4 msg:sent"));
        }
    }

    @Nested
    class ReportServiceCases {

        @Test
        void dailyRevenueReportSummarizesAppointmentsAndBillAmount() {
            HospitalStore store = new HospitalStore();
            store.addPatient("Asha", "111");
            store.addDoctor("Dr. Ray", "general");
            store.addAppointment(1, 1, "09:00", "checkup");
            store.addAppointment(1, 1, "10:00", "follow-up");
            store.addBill(1, 100.0);
            store.addBill(2, 70.0);

            ReportService reportService = new DailyRevenueReportService(store);

            assertEquals("Daily revenue report: appointments=2, revenue=170.0", reportService.generate());
        }

        @Test
        void doctorAppointmentReportSummarizesDoctorAndAppointmentCounts() {
            HospitalStore store = new HospitalStore();
            store.addDoctor("Dr. Ray", "general");
            store.addDoctor("Dr. Kim", "emergency");
            store.addPatient("Asha", "111");
            store.addAppointment(1, 1, "09:00", "checkup");

            ReportService reportService = new DoctorAppointmentReportService(store);

            assertEquals("Doctor appointment report: doctors=2, appointments=1", reportService.generate());
        }

        @Test
        void patientHistoryReportCountsOnlyMatchingAppointments() {
            HospitalStore store = new HospitalStore();
            store.addPatient("Asha", "111");
            store.addPatient("Mina", "222");
            store.addDoctor("Dr. Ray", "general");
            store.addAppointment(1, 1, "09:00", "checkup");
            store.addAppointment(2, 1, "10:00", "follow-up");
            store.addAppointment(1, 1, "11:00", "review");

            ReportService reportService = new PatientHistoryReportService(store, 1);

            assertEquals("Patient history report: patientId=1, appointments=2", reportService.generate());
        }
    }

    @Nested
    class ModelCases {

        @Test
        void patientFieldsAndToStringAreReadable() {
            Patient patient = new Patient(7, "Mina", "999");

            assertEquals(7, patient.id);
            assertEquals("Mina", patient.name);
            assertEquals("999", patient.phone);
            assertEquals("Patient{7;Mina;999}", patient.toString());
        }

        @Test
        void doctorFieldsAndToStringAreReadable() {
            Doctor doctor = new Doctor(8, "Dr. Lee", "cardiology");

            assertEquals(8, doctor.id);
            assertEquals("Dr. Lee", doctor.name);
            assertEquals("cardiology", doctor.specialty);
            assertEquals("Doctor{8;Dr. Lee;cardiology}", doctor.toString());
        }

        @Test
        void appointmentFieldsAndToStringAreReadable() {
            Appointment appointment = new Appointment(9, 1, 2, "12:30", "notes");

            assertEquals(9, appointment.id);
            assertEquals(1, appointment.patientId);
            assertEquals(2, appointment.doctorId);
            assertEquals("12:30", appointment.time);
            assertEquals("notes", appointment.notes);
            assertEquals("Appointment{9 p:1 d:2 t:12:30}", appointment.toString());
        }

        @Test
        void billFieldsAndToStringAreReadable() {
            Bill bill = new Bill(3, 9, 123.45);

            assertEquals(3, bill.id);
            assertEquals(9, bill.appointmentId);
            assertEquals(123.45, bill.amount);
            assertEquals("Bill{3 a:9 amt:123.45}", bill.toString());
        }

        @Test
        void departmentAndTimeslotCarryTheirValues() {
            Department department = new Department(4, "Radiology");
            TimeSlot timeSlot = new TimeSlot("08:00", "09:00");

            assertEquals(4, department.id);
            assertEquals("Radiology", department.name);
            assertEquals("08:00", timeSlot.from);
            assertEquals("09:00", timeSlot.to);
        }
    }
}