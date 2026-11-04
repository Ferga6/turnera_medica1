package turnomedico.modelo;

public class Consultorio {
    private final long id;
    private final String nombre;
    private final String direccion;
    public Consultorio(long id, String nombre, String direccion) {
        this.id = id; this.nombre = nombre; this.direccion = direccion;
    }
    public long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDireccion() { return direccion; }
    @Override
    public String toString() { return nombre + " - " + direccion; }
}
