package turnomedico.modelo;

import java.time.LocalDate;

public class Atencion {
    private final long id;
    private final Medico medico;
    private final Consultorio consultorio;
    private final LocalDate desde;
    private final LocalDate hasta;
    public Atencion(long id, Medico medico, Consultorio consultorio, LocalDate desde, LocalDate hasta) {
        this.id = id; this.medico = medico; this.consultorio = consultorio;
        this.desde = desde; this.hasta = hasta;
    }
    public long getId() { return id; }
    public Medico getMedico() { return medico; }
    public Consultorio getConsultorio() { return consultorio; }
    public LocalDate getDesde() { return desde; }
    public LocalDate getHasta() { return hasta; }
}
