package models;

public class Doctor {
    public final int id;
    public final String name;
    public final String specialty;

    public Doctor(int id, String name, String specialty) {
        this.id = id;
        this.name = name;
        this.specialty = specialty;
    }

    public String toString() {
        return "Doctor{" + id + ";" + name + ";" + specialty + "}";
    }
}
