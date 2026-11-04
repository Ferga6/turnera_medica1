package turnomedico.modelo;

import java.math.BigDecimal;

public class Medico extends Usuario {
    private final String matricula;
    private final String especialidad;
    private final BigDecimal honorario;
    private final Long obraSocialId;

    public Medico(long id, String usuario, String nombre, String apellido,
                  String matricula, String especialidad, BigDecimal honorario, Long obraSocialId) {
        super(id, usuario, nombre, apellido);
        this.matricula = matricula;
        this.especialidad = especialidad;
        this.honorario = honorario;
        this.obraSocialId = obraSocialId;
    }

    @Override
    public String getRol() { return "Médico"; }
    public String getMatricula() { return matricula; }
    public String getEspecialidad() { return especialidad; }
    public BigDecimal getHonorario() { return honorario; }
    public Long getObraSocialId() { return obraSocialId; }
}
