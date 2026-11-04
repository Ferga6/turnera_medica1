package turnomedico.modelo;

public class Paciente extends Usuario {

    private String dni;
    private String telefono;

    public Paciente(String usuario, String contrasena, String nombre, String apellido,
                    String dni, String telefono) {
        super(usuario, contrasena, nombre, apellido);
        this.dni = dni;
        this.telefono = telefono;
    }

    @Override
    public String getRol() {
        return "Paciente";
    }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    @Override
    public String toString() {
        return super.toString() + " (DNI " + dni + ")";
    }
}
