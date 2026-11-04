package turnomedico.modelo;

import java.math.BigDecimal;
import java.util.List;

public class ReporteMedico {
    private final Medico medico;
    private final int consultas;
    private final BigDecimal total;
    public ReporteMedico(Medico medico, List<Turno> turnos) {
        this.medico = medico;
        int cantidad = 0;
        BigDecimal suma = BigDecimal.ZERO.setScale(2);
        for (Turno turno : turnos) {
            if (turno.getMedico().getId() == medico.getId()) {
                cantidad++;
                suma = suma.add(turno.getMonto());
            }
        }
        consultas = cantidad;
        total = suma;
    }
    public Medico getMedico() { return medico; }
    public int getConsultas() { return consultas; }
    public BigDecimal getTotal() { return total; }
}
