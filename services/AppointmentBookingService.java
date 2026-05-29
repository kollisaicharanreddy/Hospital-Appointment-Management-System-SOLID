package services;

import interfaces.DoctorAllocationStrategy;
import interfaces.NotificationService;
import models.Appointment;

public class AppointmentBookingService {
    private final HospitalStore store;
    private final DoctorAllocationStrategy allocationStrategy;
    private final NotificationService notificationService;

    public AppointmentBookingService(HospitalStore store, DoctorAllocationStrategy allocationStrategy, NotificationService notificationService) {
        this.store = store;
        this.allocationStrategy = allocationStrategy;
        this.notificationService = notificationService;
    }

    public Appointment book(int patientId, String specialty, String time, String notes) {
        if (store.findPatient(patientId) == null) {
            return null;
        }

        int doctorId = allocationStrategy.allocate(patientId, specialty);
        if (doctorId == -1) {
            return null;
        }

        Appointment appointment = store.addAppointment(patientId, doctorId, time, notes);
        notificationService.notify(String.valueOf(patientId), "Appointment booked with doctor " + doctorId);
        return appointment;
    }
}
