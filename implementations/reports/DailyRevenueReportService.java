package implementations.reports;

import interfaces.ReportService;
import services.HospitalStore;

public class DailyRevenueReportService implements ReportService {
    private final HospitalStore store;

    public DailyRevenueReportService(HospitalStore store) {
        this.store = store;
    }

    public String generate() {
        return "Daily revenue report: appointments=" + store.listAppointments().size() + ", revenue=" + store.listBills().stream().mapToDouble(bill -> bill.amount).sum();
    }
}
