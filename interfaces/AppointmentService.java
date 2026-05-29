package interfaces;

import models.Appointment;

public interface AppointmentService {
	Appointment book(int patientId, int doctorId, String time, String notes);
}
