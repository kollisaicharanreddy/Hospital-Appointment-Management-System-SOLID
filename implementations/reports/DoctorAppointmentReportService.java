package implementations.reports;

import interfaces.ReportService;
import services.HospitalStore;

public class DoctorAppointmentReportService implements ReportService {
    private final HospitalStore store;

    public DoctorAppointmentReportService(HospitalStore store) {
        this.store = store;
    }

    public String generate() {
        return "Doctor appointment report: doctors=" + store.listDoctors().size() + ", appointments=" + store.listAppointments().size();
    }
}
