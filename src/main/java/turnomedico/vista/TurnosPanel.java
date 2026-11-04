package turnomedico.vista;

import java.awt.BorderLayout;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import turnomedico.logica.*;
import turnomedico.modelo.*;

class TurnosPanel extends PanelDatos {
    private final Tabla tabla = nuevaTabla();
    private final JTextField desde = new JTextField(LocalDate.now().toString(), 10);
    private final JTextField hasta = new JTextField(LocalDate.now().plusMonths(1).toString(), 10);
    private List<Turno> turnos = new ArrayList<>();

    TurnosPanel(Clinica clinica) {
        super(clinica);
        botones.add(new JLabel("Desde (yyyy-MM-dd):")); botones.add(desde);
        botones.add(new JLabel("Hasta:")); botones.add(hasta);
        boton("Consultar", this::cargar);
        if (clinica.esAdministrador()) {
            boton("Agregar", () -> editar(null));
            boton("Modificar", () -> editar(seleccionado()));
            boton("Eliminar", () -> {
                long id = tabla.seleccionado();
                if (Ui.confirmar(this)) { clinica.eliminarTurno(id); cargar(); }
            });
        }
        add(tabla, BorderLayout.CENTER);
        add(new JLabel("Para consultar un día, ingrese la misma fecha en Desde y Hasta."), BorderLayout.SOUTH);
    }

    static Tabla nuevaTabla() {
        return new Tabla("ID", "Fecha", "Hora", "Médico", "Paciente", "Consultorio", "Monto", "Descuento");
    }
    static void llenar(Tabla tabla, List<Turno> turnos) {
        tabla.limpiar();
        for (Turno turno : turnos) {
            tabla.agregar(turno.getId(), turno.getFecha().toString(), turno.getHora().toString(),
                    turno.getMedico().getNombreCompleto(), turno.getPaciente().getNombreCompleto(),
                    turno.getConsultorio().getNombre(), turno.getMonto(), turno.tieneDescuento() ? "50 %" : "No");
        }
    }
    @Override
    public void cargar() throws TurnoMedicoException {
        turnos = clinica.listarTurnos(Ui.fecha(desde), Ui.fecha(hasta));
        llenar(tabla, turnos);
    }
    private Turno seleccionado() throws TurnoMedicoException {
        long id = tabla.seleccionado();
        for (Turno turno : turnos) { if (turno.getId() == id) { return turno; } }
        throw new TurnoMedicoException("El turno ya no existe.");
    }

    private void editar(Turno actual) throws TurnoMedicoException {
        JComboBox<Medico> medicos = new JComboBox<>();
        for (Medico medico : clinica.listarMedicos()) {
            medicos.addItem(medico);
            if (actual != null && actual.getMedico().getId() == medico.getId()) { medicos.setSelectedItem(medico); }
        }
        JComboBox<Paciente> pacientes = new JComboBox<>();
        for (Paciente paciente : clinica.listarPacientes()) {
            pacientes.addItem(paciente);
            if (actual != null && actual.getPaciente().getId() == paciente.getId()) { pacientes.setSelectedItem(paciente); }
        }
        JComboBox<Consultorio> consultorios = new JComboBox<>();
        for (Consultorio consultorio : clinica.listarConsultorios()) {
            consultorios.addItem(consultorio);
            if (actual != null && actual.getConsultorio().getId() == consultorio.getId()) { consultorios.setSelectedItem(consultorio); }
        }
        if (medicos.getItemCount() == 0 || pacientes.getItemCount() == 0 || consultorios.getItemCount() == 0) {
            throw new TurnoMedicoException("Primero agregue médicos, pacientes, consultorios y sus períodos de atención.");
        }
        JTextField fecha = new JTextField(actual == null ? LocalDate.now().toString() : actual.getFecha().toString(), 16);
        JTextField hora = new JTextField(actual == null ? "09:00" : actual.getHora().toString());
        if (Ui.formulario(this, "Turno", Ui.campos("Médico", medicos, "Paciente", pacientes, "Consultorio", consultorios,
                "Fecha (yyyy-MM-dd)", fecha, "Hora (HH:mm)", hora), () -> {
            clinica.guardarTurno(actual == null ? 0 : actual.getId(), ((Medico) medicos.getSelectedItem()).getId(),
                    ((Paciente) pacientes.getSelectedItem()).getId(), ((Consultorio) consultorios.getSelectedItem()).getId(),
                    Ui.fecha(fecha), Ui.hora(hora));
        })) {
            desde.setText(fecha.getText().trim()); hasta.setText(fecha.getText().trim()); cargar();
        }
    }
}
