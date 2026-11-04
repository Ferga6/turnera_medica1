package turnomedico.modelo;

public class ObraSocial {
    private final long id;
    private final String nombre;
    public ObraSocial(long id, String nombre) { this.id = id; this.nombre = nombre; }
    public long getId() { return id; }
    public String getNombre() { return nombre; }
    @Override
    public String toString() { return nombre; }
}
