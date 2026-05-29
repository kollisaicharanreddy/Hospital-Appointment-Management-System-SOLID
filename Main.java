import factories.BillingFactory;
import factories.DoctorAllocationFactory;
import factories.NotificationFactory;
import interfaces.BillingService;
import interfaces.DoctorAllocationStrategy;
import interfaces.NotificationService;
import java.util.Scanner;
import models.Appointment;
import models.Bill;
import models.Doctor;
import models.Patient;
import services.AppointmentBookingService;
import services.AppointmentCancellationService;
import services.AppointmentRescheduleService;
import services.BillingGenerationService;
import services.DoctorManagementService;
import services.HospitalStore;
import services.ReportGenerationService;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        HospitalStore store = new HospitalStore();
        NotificationService notificationService = NotificationFactory.create("console");
        BillingService billingService = BillingFactory.create("standard");
        DoctorAllocationStrategy doctorAllocationStrategy = DoctorAllocationFactory.create("specialty", store);
        DoctorManagementService doctorSvc = new DoctorManagementService(store);
        AppointmentBookingService bookSvc = new AppointmentBookingService(store, doctorAllocationStrategy, notificationService);
        AppointmentCancellationService cancelSvc = new AppointmentCancellationService(store, notificationService);
        AppointmentRescheduleService rescheduleSvc = new AppointmentRescheduleService(store, notificationService);
        BillingGenerationService billingSvc = new BillingGenerationService(store, billingService);
        ReportGenerationService reportSvc = new ReportGenerationService(store);

        while (true) {
            System.out.println("1)add patient 2)add doctor 3)book 4)cancel 5)reschedule 6)bill 7)reports 8)list 0)exit");
            String line = sc.nextLine();

            if (line.equals("0")) {
                break;
            }

            switch (line) {
                case "1":
                    System.out.print("name:");
                    String name = sc.nextLine();
                    System.out.print("phone:");
                    String phone = sc.nextLine();
                    Patient p = store.addPatient(name, phone);
                    System.out.println("added:" + p);
                    break;

                case "2":
                    System.out.print("name:");
                    String dname = sc.nextLine();
                    System.out.print("specialty:");
                    String spec = sc.nextLine();
                    Doctor d = doctorSvc.add(dname, spec);
                    System.out.println("added:" + d);
                    break;

                case "3":
                    System.out.print("patient id:");
                    int pid = Integer.parseInt(sc.nextLine());
                    System.out.print("specialty:");
                    String specialty = sc.nextLine();
                    System.out.print("time:");
                    String time = sc.nextLine();
                    Appointment a = bookSvc.book(pid, specialty, time, "");
                    System.out.println(a == null ? "booking failed" : "booked:" + a);
                    break;

                case "4":
                    System.out.print("appointment id:");
                    int cancelId = Integer.parseInt(sc.nextLine());
                    System.out.println(cancelSvc.cancel(cancelId) ? "cancelled" : "not found");
                    break;

                case "5":
                    System.out.print("appointment id:");
                    int rescheduleId = Integer.parseInt(sc.nextLine());
                    System.out.print("new time:");
                    String newTime = sc.nextLine();
                    Appointment updated = rescheduleSvc.reschedule(rescheduleId, newTime);
                    System.out.println(updated == null ? "not found" : "rescheduled:" + updated);
                    break;

                case "6":
                    System.out.print("appointment id:");
                    int billAppointmentId = Integer.parseInt(sc.nextLine());
                    Bill bill = billingSvc.generate(billAppointmentId);
                    System.out.println(bill == null ? "not found" : "bill:" + bill);
                    break;

                case "7":
                    System.out.println(reportSvc.generateDailyRevenueReport());
                    System.out.println(reportSvc.generateDoctorReport());
                    System.out.print("patient id for history:");
                    int historyPatientId = Integer.parseInt(sc.nextLine());
                    System.out.println(reportSvc.generatePatientHistoryReport(historyPatientId));
                    break;

                case "8":
                    System.out.println("Patients:");
                    for (Patient patient : store.listPatients()) {
                        System.out.println(patient);
                    }

                    System.out.println("Doctors:");
                    for (Doctor doctor : store.listDoctors()) {
                        System.out.println(doctor);
                    }

                    System.out.println("Appointments:");
                    for (Appointment appointment : store.listAppointments()) {
                        System.out.println(appointment);
                    }

                    System.out.println("Bills:");
                    for (Bill item : store.listBills()) {
                        System.out.println(item);
                    }
                    break;

                default:
                    System.out.println("unknown");
            }
        }

        sc.close();
    }
}
