package turnomedico.modelo;

public class Usuario {

    private String usuario;
    private String contrasena;
    private String nombre;
    private String apellido;

    public Usuario(String usuario, String contrasena, String nombre, String apellido) {
        this.usuario = usuario;
        this.contrasena = contrasena;
        this.nombre = nombre;
        this.apellido = apellido;
    }

    public String getRol() {
        return "Administrador";
    }

    public String getNombreCompleto() {
        return apellido + ", " + nombre;
    }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    @Override
    public String toString() {
        return getRol() + ": " + getNombreCompleto();
    }
}
