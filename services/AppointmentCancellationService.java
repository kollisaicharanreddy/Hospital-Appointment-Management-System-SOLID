package services;

import interfaces.NotificationService;

public class AppointmentCancellationService {
    private final HospitalStore store;
    private final NotificationService notificationService;

    public AppointmentCancellationService(HospitalStore store, NotificationService notificationService) {
        this.store = store;
        this.notificationService = notificationService;
    }

    public boolean cancel(int appointmentId) {
        boolean removed = store.removeAppointment(appointmentId);
        if (removed) {
            notificationService.notify("patient", "Appointment " + appointmentId + " cancelled");
        }
        return removed;
    }
}
