package services;

import models.Doctor;

public class DoctorManagementService {
    private final HospitalStore store;

    public DoctorManagementService(HospitalStore store) {
        this.store = store;
    }

    public Doctor add(String name, String specialty) {
        return store.addDoctor(name, specialty);
    }
}
