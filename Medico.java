package turnomedico.modelo;

public class Medico extends Usuario {

    private String matricula;
    private String especialidad;
    private double honorario;

    public Medico(String usuario, String contrasena, String nombre, String apellido,
                  String matricula, String especialidad, double honorario) {
        super(usuario, contrasena, nombre, apellido);
        this.matricula = matricula;
        this.especialidad = especialidad;
        this.honorario = honorario;
    }

    @Override
    public String getRol() {
        return "Médico";
    }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }

    public double getHonorario() { return honorario; }
    public void setHonorario(double honorario) { this.honorario = honorario; }

    @Override
    public String toString() {
        return super.toString() + " (" + especialidad + ", $" + honorario + ")";
    }
}
