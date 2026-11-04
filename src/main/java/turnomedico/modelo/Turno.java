package turnomedico.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class Turno {
    private final long id;
    private final Medico medico;
    private final Paciente paciente;
    private final Consultorio consultorio;
    private final LocalDate fecha;
    private final LocalTime hora;
    private final BigDecimal monto;
    private final boolean descuento;

    public Turno(long id, Medico medico, Paciente paciente, Consultorio consultorio,
                 LocalDate fecha, LocalTime hora, BigDecimal monto, boolean descuento) {
        this.id = id; this.medico = medico; this.paciente = paciente; this.consultorio = consultorio;
        this.fecha = fecha; this.hora = hora; this.monto = monto; this.descuento = descuento;
    }
    public long getId() { return id; }
    public Medico getMedico() { return medico; }
    public Paciente getPaciente() { return paciente; }
    public Consultorio getConsultorio() { return consultorio; }
    public LocalDate getFecha() { return fecha; }
    public LocalTime getHora() { return hora; }
    public BigDecimal getMonto() { return monto; }
    public boolean tieneDescuento() { return descuento; }
}
