package models;

public class Bill {
    public final int id;
    public final int appointmentId;
    public final double amount;

    public Bill(int id, int appointmentId, double amount) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.amount = amount;
    }

    public String toString() {
        return "Bill{" + id + " a:" + appointmentId + " amt:" + amount + "}";
    }
}
