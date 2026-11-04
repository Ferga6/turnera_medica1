package turnomedico.modelo;

public class Paciente extends Usuario {
    private final String dni;
    private final String telefono;
    private final Long obraSocialId;

    public Paciente(long id, String usuario, String nombre, String apellido,
                    String dni, String telefono, Long obraSocialId) {
        super(id, usuario, nombre, apellido);
        this.dni = dni;
        this.telefono = telefono;
        this.obraSocialId = obraSocialId;
    }

    @Override
    public String getRol() { return "Paciente"; }
    public String getDni() { return dni; }
    public String getTelefono() { return telefono; }
    public Long getObraSocialId() { return obraSocialId; }
}
