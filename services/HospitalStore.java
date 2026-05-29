package services;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import models.Appointment;
import models.Bill;
import models.Doctor;
import models.Patient;

public class HospitalStore {
    private final Map<Integer, Patient> patients = new LinkedHashMap<>();
    private final Map<Integer, Doctor> doctors = new LinkedHashMap<>();
    private final Map<Integer, Appointment> appointments = new LinkedHashMap<>();
    private final List<Bill> bills = new ArrayList<>();
    private int nextPatientId = 1;
    private int nextDoctorId = 1;
    private int nextAppointmentId = 1;
    private int nextBillId = 1;

    public Patient addPatient(String name, String phone) {
        Patient patient = new Patient(nextPatientId++, name, phone);
        patients.put(patient.id, patient);
        return patient;
    }

    public Doctor addDoctor(String name, String specialty) {
        Doctor doctor = new Doctor(nextDoctorId++, name, specialty);
        doctors.put(doctor.id, doctor);
        return doctor;
    }

    public Appointment addAppointment(int patientId, int doctorId, String time, String notes) {
        Appointment appointment = new Appointment(nextAppointmentId++, patientId, doctorId, time, notes);
        appointments.put(appointment.id, appointment);
        return appointment;
    }

    public Bill addBill(int appointmentId, double amount) {
        Bill bill = new Bill(nextBillId++, appointmentId, amount);
        bills.add(bill);
        return bill;
    }

    public Patient findPatient(int id) {
        return patients.get(id);
    }

    public Doctor findDoctor(int id) {
        return doctors.get(id);
    }

    public Appointment findAppointment(int id) {
        return appointments.get(id);
    }

    public boolean removeAppointment(int id) {
        return appointments.remove(id) != null;
    }

    public void replaceAppointment(Appointment appointment) {
        appointments.put(appointment.id, appointment);
    }

    public List<Patient> listPatients() {
        return new ArrayList<>(patients.values());
    }

    public List<Doctor> listDoctors() {
        return new ArrayList<>(doctors.values());
    }

    public List<Appointment> listAppointments() {
        return new ArrayList<>(appointments.values());
    }

    public List<Bill> listBills() {
        return new ArrayList<>(bills);
    }
}
