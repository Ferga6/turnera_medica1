package turnomedico.vista;

import java.awt.BorderLayout;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import turnomedico.logica.*;
import turnomedico.modelo.*;

class UsuariosPanel extends PanelDatos {
    private final boolean medicos;
    private final Tabla tabla;
    private List<Usuario> usuarios = new ArrayList<>();
    private final JTextField buscar = new JTextField(18);

    UsuariosPanel(Clinica clinica, boolean medicos) {
        super(clinica);
        this.medicos = medicos;
        tabla = medicos ? new Tabla("ID", "Usuario", "Nombre", "Apellido", "Matrícula", "Especialidad", "Honorario", "Obra social")
                : new Tabla("ID", "Usuario", "Nombre", "Apellido", "DNI", "Teléfono", "Obra social");
        botones.add(new JLabel("Buscar:")); botones.add(buscar);
        boton("Buscar", this::cargar);
        buscar.addActionListener(e -> Ui.ejecutar(this, this::cargar));
        boton("Agregar", () -> editar(null));
        boton("Modificar", () -> editar(seleccionado()));
        boton("Eliminar", () -> {
            long id = tabla.seleccionado();
            if (Ui.confirmar(this)) { clinica.eliminarUsuario(id); cargar(); }
        });
        add(tabla, BorderLayout.CENTER);
    }

    @Override
    public void cargar() throws TurnoMedicoException {
        usuarios = new ArrayList<>();
        if (medicos) { usuarios.addAll(clinica.listarMedicos()); } else { usuarios.addAll(clinica.listarPacientes()); }
        List<ObraSocial> obras = clinica.listarObras();
        tabla.limpiar();
        String filtro = buscar.getText().trim().toLowerCase(java.util.Locale.ROOT);
        for (Usuario usuario : usuarios) {
            String numero = medicos ? ((Medico) usuario).getMatricula() : ((Paciente) usuario).getDni();
            if (!(usuario.toString() + " " + numero).toLowerCase(java.util.Locale.ROOT).contains(filtro)) { continue; }
            Long obraId = medicos ? ((Medico) usuario).getObraSocialId() : ((Paciente) usuario).getObraSocialId();
            String obraNombre = "Particular";
            for (ObraSocial obra : obras) { if (obraId != null && obra.getId() == obraId) { obraNombre = obra.getNombre(); } }
            if (medicos) {
                Medico medico = (Medico) usuario;
                tabla.agregar(medico.getId(), medico.getUsuario(), medico.getNombre(), medico.getApellido(),
                        medico.getMatricula(), medico.getEspecialidad(), medico.getHonorario(), obraNombre);
            } else {
                Paciente paciente = (Paciente) usuario;
                tabla.agregar(paciente.getId(), paciente.getUsuario(), paciente.getNombre(), paciente.getApellido(),
                        paciente.getDni(), paciente.getTelefono(), obraNombre);
            }
        }
    }

    private Usuario seleccionado() throws TurnoMedicoException {
        long id = tabla.seleccionado();
        for (Usuario usuario : usuarios) { if (usuario.getId() == id) { return usuario; } }
        throw new TurnoMedicoException("El usuario ya no existe.");
    }

    private void editar(Usuario actual) throws TurnoMedicoException {
        JTextField usuario = new JTextField(actual == null ? "" : actual.getUsuario(), 22);
        JPasswordField clave = new JPasswordField(22);
        JTextField nombre = new JTextField(actual == null ? "" : actual.getNombre());
        JTextField apellido = new JTextField(actual == null ? "" : actual.getApellido());
        JTextField numero = new JTextField();
        JTextField extra = new JTextField();
        JTextField honorario = new JTextField();
        JComboBox<ObraSocial> obra = new JComboBox<>();
        obra.addItem(new ObraSocial(0, "Particular / sin obra social"));
        for (ObraSocial item : clinica.listarObras()) { obra.addItem(item); }
        if (actual instanceof Medico) {
            Medico medico = (Medico) actual;
            numero.setText(medico.getMatricula()); extra.setText(medico.getEspecialidad());
            honorario.setText(medico.getHonorario().toPlainString()); Ui.seleccionarObra(obra, medico.getObraSocialId());
        } else if (actual instanceof Paciente) {
            Paciente paciente = (Paciente) actual;
            numero.setText(paciente.getDni()); extra.setText(paciente.getTelefono()); Ui.seleccionarObra(obra, paciente.getObraSocialId());
        }
        JPanel campos = Ui.campos("Usuario", usuario, actual == null ? "Contraseña (mínimo 4)" : "Nueva contraseña (vacía: conservar)", clave,
                "Nombre", nombre, "Apellido", apellido, medicos ? "Matrícula" : "DNI", numero,
                medicos ? "Especialidad" : "Teléfono (opcional)", extra, "Obra social", obra);
        if (medicos) { campos.add(new JLabel("Honorario")); campos.add(honorario); }
        boolean guardado = Ui.formulario(this, medicos ? "Médico" : "Paciente", campos, () -> {
            long id = actual == null ? 0 : actual.getId();
            String contrasena = new String(clave.getPassword());
            if (medicos) {
                clinica.guardarMedico(new Medico(id, usuario.getText(), nombre.getText(), apellido.getText(),
                        numero.getText(), extra.getText(), new BigDecimal(honorario.getText().trim().replace(',', '.')),
                        Ui.obraId(obra)), contrasena);
            } else {
                clinica.guardarPaciente(new Paciente(id, usuario.getText(), nombre.getText(), apellido.getText(),
                        numero.getText(), extra.getText(), Ui.obraId(obra)), contrasena);
            }
        });
        clave.setText("");
        if (guardado) { cargar(); }
    }
}
