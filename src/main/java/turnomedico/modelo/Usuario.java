package turnomedico.modelo;

public class Usuario {
    private final long id;
    private final String usuario;
    private final String nombre;
    private final String apellido;

    public Usuario(long id, String usuario, String nombre, String apellido) {
        this.id = id;
        this.usuario = usuario;
        this.nombre = nombre;
        this.apellido = apellido;
    }

    public long getId() { return id; }
    public String getUsuario() { return usuario; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getNombreCompleto() { return apellido + ", " + nombre; }
    public String getRol() { return "Administrador"; }

    @Override
    public String toString() { return getNombreCompleto() + " (" + usuario + ")"; }
}
