package models;

public class Patient {
    public final int id;
    public final String name;
    public final String phone;

    public Patient(int id, String name, String phone) {
        this.id = id;
        this.name = name;
        this.phone = phone;
    }

    public String toString() {
        return "Patient{" + id + ";" + name + ";" + phone + "}";
    }
}
