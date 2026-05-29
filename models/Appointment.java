package models;

public class Appointment {
    public final int id;
    public final int patientId;
    public final int doctorId;
    public final String time;
    public final String notes;

    public Appointment(int id, int patientId, int doctorId, String time, String notes) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.time = time;
        this.notes = notes;
    }

    public String toString() {
        return "Appointment{" + id + " p:" + patientId + " d:" + doctorId + " t:" + time + "}";
    }
}
