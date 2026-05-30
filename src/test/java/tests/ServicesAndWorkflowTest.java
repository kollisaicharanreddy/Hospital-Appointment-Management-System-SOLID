package tests;

import interfaces.BillingService;
import interfaces.DoctorAllocationStrategy;
import interfaces.NotificationService;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import models.Appointment;
import models.Bill;
import models.Doctor;
import models.Patient;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import services.AppointmentBookingService;
import services.AppointmentCancellationService;
import services.AppointmentRescheduleService;
import services.BillingGenerationService;
import services.DoctorManagementService;
import services.HospitalStore;
import services.ReportGenerationService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServicesAndWorkflowTest {

    private static final class RecordingNotificationService implements NotificationService {
        int callCount;
        String lastRecipient;
        String lastMessage;

        @Override
        public void notify(String recipient, String message) {
            callCount++;
            lastRecipient = recipient;
            lastMessage = message;
        }
    }

    private static final class RecordingAllocationStrategy implements DoctorAllocationStrategy {
        int callCount;
        int lastPatientId;
        String lastSpecialty;
        int doctorIdToReturn;

        @Override
        public int allocate(int patientId, String specialty) {
            callCount++;
            lastPatientId = patientId;
            lastSpecialty = specialty;
            return doctorIdToReturn;
        }
    }

    private static final class RecordingBillingService implements BillingService {
        int callCount;
        int lastAppointmentId;
        double amountToReturn;

        @Override
        public double generateCharge(int appointmentId) {
            callCount++;
            lastAppointmentId = appointmentId;
            return amountToReturn;
        }
    }

    @Nested
    class HospitalStoreCases {

        @Test
        void storeAssignsIdsAndKeepsIndependentCopiesForLists() {
            HospitalStore store = new HospitalStore();

            Patient patient = store.addPatient("Mina", "100");
            Doctor doctor = store.addDoctor("Dr. Ray", "general");
            Appointment appointment = store.addAppointment(patient.id, doctor.id, "09:00", "checkup");
            Bill bill = store.addBill(appointment.id, 100.0);

            assertEquals(1, patient.id);
            assertEquals(1, doctor.id);
            assertEquals(1, appointment.id);
            assertEquals(1, bill.id);
            assertEquals(patient, store.findPatient(1));
            assertEquals(doctor, store.findDoctor(1));
            assertEquals(appointment, store.findAppointment(1));
            assertEquals(bill, store.listBills().get(0));

            store.listPatients().clear();
            store.listDoctors().clear();
            store.listAppointments().clear();
            store.listBills().clear();

            assertEquals(1, store.listPatients().size());
            assertEquals(1, store.listDoctors().size());
            assertEquals(1, store.listAppointments().size());
            assertEquals(1, store.listBills().size());
        }

        @Test
        void storeCanRemoveAndReplaceAppointments() {
            HospitalStore store = new HospitalStore();
            store.addPatient("Mina", "100");
            store.addDoctor("Dr. Ray", "general");
            Appointment appointment = store.addAppointment(1, 1, "09:00", "checkup");

            assertTrue(store.removeAppointment(appointment.id));
            assertFalse(store.removeAppointment(appointment.id));

            Appointment replacement = new Appointment(appointment.id, 1, 1, "10:00", "follow-up");
            store.replaceAppointment(replacement);

            assertEquals(replacement, store.findAppointment(appointment.id));
        }
    }

    @Nested
    class BookingCases {

        @Test
        void bookingFailsWhenPatientDoesNotExist() {
            HospitalStore store = new HospitalStore();
            RecordingAllocationStrategy allocationStrategy = new RecordingAllocationStrategy();
            RecordingNotificationService notificationService = new RecordingNotificationService();
            AppointmentBookingService service = new AppointmentBookingService(store, allocationStrategy, notificationService);

            assertNull(service.book(1, "cardiology", "09:00", "checkup"));
            assertEquals(0, allocationStrategy.callCount);
            assertEquals(0, notificationService.callCount);
        }

        @Test
        void bookingFailsWhenAllocationReturnsMinusOne() {
            HospitalStore store = new HospitalStore();
            store.addPatient("Mina", "100");
            RecordingAllocationStrategy allocationStrategy = new RecordingAllocationStrategy();
            allocationStrategy.doctorIdToReturn = -1;
            RecordingNotificationService notificationService = new RecordingNotificationService();
            AppointmentBookingService service = new AppointmentBookingService(store, allocationStrategy, notificationService);

            assertNull(service.book(1, "cardiology", "09:00", "checkup"));
            assertEquals(1, allocationStrategy.callCount);
            assertEquals(0, notificationService.callCount);
            assertEquals(1, allocationStrategy.lastPatientId);
            assertEquals("cardiology", allocationStrategy.lastSpecialty);
        }

        @Test
        void bookingCreatesAppointmentAndSendsNotification() {
            HospitalStore store = new HospitalStore();
            store.addPatient("Mina", "100");
            RecordingAllocationStrategy allocationStrategy = new RecordingAllocationStrategy();
            allocationStrategy.doctorIdToReturn = 7;
            RecordingNotificationService notificationService = new RecordingNotificationService();
            AppointmentBookingService service = new AppointmentBookingService(store, allocationStrategy, notificationService);

            Appointment appointment = service.book(1, "cardiology", "09:00", "checkup");

            assertNotNull(appointment);
            assertEquals(1, appointment.id);
            assertEquals(1, appointment.patientId);
            assertEquals(7, appointment.doctorId);
            assertEquals("09:00", appointment.time);
            assertEquals("checkup", appointment.notes);
            assertEquals(1, allocationStrategy.callCount);
            assertEquals(1, notificationService.callCount);
            assertEquals("1", notificationService.lastRecipient);
            assertEquals("Appointment booked with doctor 7", notificationService.lastMessage);
            assertEquals(appointment, store.findAppointment(1));
        }
    }

    @Nested
    class CancellationCases {

        @Test
        void cancellationReturnsFalseForMissingAppointment() {
            HospitalStore store = new HospitalStore();
            RecordingNotificationService notificationService = new RecordingNotificationService();
            AppointmentCancellationService service = new AppointmentCancellationService(store, notificationService);

            assertFalse(service.cancel(1));
            assertEquals(0, notificationService.callCount);
        }

        @Test
        void cancellationRemovesAppointmentAndNotifiesPatientPlaceholder() {
            HospitalStore store = new HospitalStore();
            store.addPatient("Mina", "100");
            store.addDoctor("Dr. Ray", "general");
            Appointment appointment = store.addAppointment(1, 1, "09:00", "checkup");
            RecordingNotificationService notificationService = new RecordingNotificationService();
            AppointmentCancellationService service = new AppointmentCancellationService(store, notificationService);

            assertTrue(service.cancel(appointment.id));
            assertNull(store.findAppointment(appointment.id));
            assertEquals(1, notificationService.callCount);
            assertEquals("patient", notificationService.lastRecipient);
            assertEquals("Appointment 1 cancelled", notificationService.lastMessage);
        }
    }

    @Nested
    class RescheduleCases {

        @Test
        void rescheduleReturnsNullForMissingAppointment() {
            HospitalStore store = new HospitalStore();
            RecordingNotificationService notificationService = new RecordingNotificationService();
            AppointmentRescheduleService service = new AppointmentRescheduleService(store, notificationService);

            assertNull(service.reschedule(1, "10:00"));
            assertEquals(0, notificationService.callCount);
        }

        @Test
        void rescheduleReplacesOnlyTheTimeAndSendsNotification() {
            HospitalStore store = new HospitalStore();
            store.addPatient("Mina", "100");
            store.addDoctor("Dr. Ray", "general");
            Appointment appointment = store.addAppointment(1, 1, "09:00", "checkup");
            RecordingNotificationService notificationService = new RecordingNotificationService();
            AppointmentRescheduleService service = new AppointmentRescheduleService(store, notificationService);

            Appointment updated = service.reschedule(appointment.id, "11:00");

            assertNotNull(updated);
            assertEquals(appointment.id, updated.id);
            assertEquals(appointment.patientId, updated.patientId);
            assertEquals(appointment.doctorId, updated.doctorId);
            assertEquals("11:00", updated.time);
            assertEquals(appointment.notes, updated.notes);
            assertEquals(updated, store.findAppointment(appointment.id));
            assertEquals(1, notificationService.callCount);
            assertEquals("1", notificationService.lastRecipient);
            assertEquals("Appointment 1 rescheduled", notificationService.lastMessage);
        }
    }

    @Nested
    class BillingGenerationCases {

        @Test
        void billingGenerationReturnsNullWhenAppointmentDoesNotExist() {
            HospitalStore store = new HospitalStore();
            RecordingBillingService billingService = new RecordingBillingService();
            BillingGenerationService service = new BillingGenerationService(store, billingService);

            assertNull(service.generate(1));
            assertEquals(0, billingService.callCount);
        }

        @Test
        void billingGenerationCreatesBillUsingTheInjectedCharge() {
            HospitalStore store = new HospitalStore();
            store.addPatient("Mina", "100");
            store.addDoctor("Dr. Ray", "general");
            Appointment appointment = store.addAppointment(1, 1, "09:00", "checkup");
            RecordingBillingService billingService = new RecordingBillingService();
            billingService.amountToReturn = 123.45;
            BillingGenerationService service = new BillingGenerationService(store, billingService);

            Bill bill = service.generate(appointment.id);

            assertNotNull(bill);
            assertEquals(1, bill.id);
            assertEquals(appointment.id, bill.appointmentId);
            assertEquals(123.45, bill.amount);
            assertEquals(1, billingService.callCount);
            assertEquals(appointment.id, billingService.lastAppointmentId);
            assertEquals(bill, store.listBills().get(0));
        }
    }

    @Nested
    class DoctorManagementCases {

        @Test
        void doctorManagementAddsDoctorsToTheStore() {
            HospitalStore store = new HospitalStore();
            DoctorManagementService service = new DoctorManagementService(store);

            Doctor doctor = service.add("Dr. Ray", "general");

            assertEquals(1, doctor.id);
            assertEquals("Dr. Ray", doctor.name);
            assertEquals("general", doctor.specialty);
            assertEquals(doctor, store.findDoctor(doctor.id));
        }
    }

    @Nested
    class ReportGenerationCases {

        @Test
        void reportGenerationDelegatesToTheRightReportTypes() {
            HospitalStore store = new HospitalStore();
            store.addPatient("Mina", "100");
            store.addDoctor("Dr. Ray", "general");
            store.addAppointment(1, 1, "09:00", "checkup");
            store.addBill(1, 100.0);

            ReportGenerationService service = new ReportGenerationService(store);

            assertEquals("Daily revenue report: appointments=1, revenue=100.0", service.generateDailyRevenueReport());
            assertEquals("Doctor appointment report: doctors=1, appointments=1", service.generateDoctorReport());
            assertEquals("Patient history report: patientId=1, appointments=1", service.generatePatientHistoryReport(1));
        }
    }
}