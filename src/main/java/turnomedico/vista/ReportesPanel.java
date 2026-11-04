package turnomedico.vista;

import java.awt.BorderLayout;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import turnomedico.logica.*;
import turnomedico.modelo.*;

class ReportesPanel extends PanelDatos {
    private final JTextField desde = new JTextField(LocalDate.now().withDayOfMonth(1).toString(), 10);
    private final JTextField hasta = new JTextField(LocalDate.now().toString(), 10);
    private final Tabla resumen = new Tabla("ID", "Médico", "Consultas", "Recaudación");
    private final Tabla detalle = TurnosPanel.nuevaTabla();
    private final JLabel total = new JLabel();
    private List<Turno> turnos = new ArrayList<>();

    ReportesPanel(Clinica clinica) {
        super(clinica);
        botones.add(new JLabel("Desde (yyyy-MM-dd):")); botones.add(desde);
        botones.add(new JLabel("Hasta:")); botones.add(hasta);
        boton("Consultar", this::cargar);
        JSplitPane division = new JSplitPane(JSplitPane.VERTICAL_SPLIT, resumen, detalle);
        division.setResizeWeight(0.5);
        add(division, BorderLayout.CENTER);
        add(total, BorderLayout.SOUTH);
        resumen.getTabla().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && resumen.getTabla().getSelectedRow() >= 0) {
                Ui.ejecutar(this, () -> {
                    long id = resumen.seleccionado();
                    List<Turno> elegidos = new ArrayList<>();
                    for (Turno turno : turnos) { if (turno.getMedico().getId() == id) { elegidos.add(turno); } }
                    TurnosPanel.llenar(detalle, elegidos);
                });
            }
        });
    }
    @Override
    public void cargar() throws TurnoMedicoException {
        LocalDate inicio = Ui.fecha(desde), fin = Ui.fecha(hasta);
        List<ReporteMedico> reportes = clinica.reporteMedicos(inicio, fin);
        turnos = clinica.listarTurnos(inicio, fin);
        resumen.limpiar(); detalle.limpiar();
        BigDecimal suma = BigDecimal.ZERO.setScale(2);
        int cantidad = 0;
        for (ReporteMedico reporte : reportes) {
            resumen.agregar(reporte.getMedico().getId(), reporte.getMedico().getNombreCompleto(), reporte.getConsultas(), reporte.getTotal());
            suma = suma.add(reporte.getTotal()); cantidad += reporte.getConsultas();
        }
        total.setText("Consultas: " + cantidad + "    Total: $ " + suma.toPlainString() + "    Seleccione un médico para ver sus turnos.");
    }
}
