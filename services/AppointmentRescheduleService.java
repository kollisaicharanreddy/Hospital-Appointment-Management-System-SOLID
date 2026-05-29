package services;

import interfaces.NotificationService;
import models.Appointment;

public class AppointmentRescheduleService {
    private final HospitalStore store;
    private final NotificationService notificationService;

    public AppointmentRescheduleService(HospitalStore store, NotificationService notificationService) {
        this.store = store;
        this.notificationService = notificationService;
    }

    public Appointment reschedule(int appointmentId, String newTime) {
        Appointment appointment = store.findAppointment(appointmentId);
        if (appointment == null) {
            return null;
        }

        Appointment updated = new Appointment(appointment.id, appointment.patientId, appointment.doctorId, newTime, appointment.notes);
        store.replaceAppointment(updated);
        notificationService.notify(String.valueOf(updated.patientId), "Appointment " + appointmentId + " rescheduled");
        return updated;
    }
}
