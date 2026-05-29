package implementations.reports;

import interfaces.ReportService;
import services.HospitalStore;

public class PatientHistoryReportService implements ReportService {
    private final HospitalStore store;
    private final int patientId;

    public PatientHistoryReportService(HospitalStore store, int patientId) {
        this.store = store;
        this.patientId = patientId;
    }

    public String generate() {
        long count = store.listAppointments().stream().filter(appointment -> appointment.patientId == patientId).count();
        return "Patient history report: patientId=" + patientId + ", appointments=" + count;
    }
}
