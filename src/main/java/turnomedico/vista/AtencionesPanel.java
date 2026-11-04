package turnomedico.vista;

import java.awt.BorderLayout;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import turnomedico.logica.*;
import turnomedico.modelo.*;

class AtencionesPanel extends PanelDatos {
    private final Tabla tabla = new Tabla("ID", "Médico", "Consultorio", "Desde", "Hasta");
    private List<Atencion> atenciones = new ArrayList<>();
    AtencionesPanel(Clinica clinica) {
        super(clinica);
        boton("Agregar", () -> editar(null));
        boton("Modificar", () -> editar(seleccionado()));
        boton("Eliminar", () -> {
            long id = tabla.seleccionado();
            if (Ui.confirmar(this)) { clinica.eliminarAtencion(id); cargar(); }
        });
        add(tabla, BorderLayout.CENTER);
        add(new JLabel("Un médico tiene un consultorio por período. Las fechas inicial y final están incluidas."), BorderLayout.SOUTH);
    }
    @Override
    public void cargar() throws TurnoMedicoException {
        atenciones = clinica.listarAtenciones();
        tabla.limpiar();
        for (Atencion atencion : atenciones) {
            tabla.agregar(atencion.getId(), atencion.getMedico(), atencion.getConsultorio(), atencion.getDesde().toString(), atencion.getHasta().toString());
        }
    }
    private Atencion seleccionado() throws TurnoMedicoException {
        long id = tabla.seleccionado();
        for (Atencion atencion : atenciones) { if (atencion.getId() == id) { return atencion; } }
        throw new TurnoMedicoException("La atención ya no existe.");
    }
    private void editar(Atencion actual) throws TurnoMedicoException {
        JComboBox<Medico> medicos = new JComboBox<>();
        for (Medico medico : clinica.listarMedicos()) {
            medicos.addItem(medico);
            if (actual != null && medico.getId() == actual.getMedico().getId()) { medicos.setSelectedItem(medico); }
        }
        JComboBox<Consultorio> consultorios = new JComboBox<>();
        for (Consultorio consultorio : clinica.listarConsultorios()) {
            consultorios.addItem(consultorio);
            if (actual != null && consultorio.getId() == actual.getConsultorio().getId()) { consultorios.setSelectedItem(consultorio); }
        }
        if (medicos.getItemCount() == 0 || consultorios.getItemCount() == 0) {
            throw new TurnoMedicoException("Primero agregue un médico y un consultorio.");
        }
        JTextField desde = new JTextField(actual == null ? LocalDate.now().toString() : actual.getDesde().toString(), 16);
        JTextField hasta = new JTextField(actual == null ? LocalDate.now().plusMonths(1).toString() : actual.getHasta().toString());
        if (Ui.formulario(this, "Atención", Ui.campos("Médico", medicos, "Consultorio", consultorios,
                "Desde (yyyy-MM-dd)", desde, "Hasta (yyyy-MM-dd)", hasta), () -> {
            Medico medico = (Medico) medicos.getSelectedItem();
            Consultorio consultorio = (Consultorio) consultorios.getSelectedItem();
            clinica.guardarAtencion(actual == null ? 0 : actual.getId(), medico.getId(), consultorio.getId(), Ui.fecha(desde), Ui.fecha(hasta));
        })) { cargar(); }
    }
}
