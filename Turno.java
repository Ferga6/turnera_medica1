package turnomedico.modelo;

import java.time.LocalDate;
import java.time.LocalTime;

public class Turno {

    private Medico medico;
    private Paciente paciente;
    private LocalDate fecha;
    private LocalTime hora;
    private double monto;

    public Turno(Medico medico, Paciente paciente, LocalDate fecha, LocalTime hora) {
        this.medico = medico;
        this.paciente = paciente;
        this.fecha = fecha;
        this.hora = hora;
        this.monto = medico.getHonorario();
    }

    public Medico getMedico() { return medico; }
    public Paciente getPaciente() { return paciente; }
    public LocalDate getFecha() { return fecha; }
    public LocalTime getHora() { return hora; }
    public double getMonto() { return monto; }

    @Override
    public String toString() {
        return fecha + " " + hora + " - " + paciente.getNombreCompleto()
                + " con " + medico.getNombreCompleto() + " ($" + monto + ")";
    }
}
