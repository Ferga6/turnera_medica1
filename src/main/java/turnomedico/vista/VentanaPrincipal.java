package turnomedico.vista;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;
import turnomedico.logica.Clinica;
import turnomedico.modelo.Paciente;

public class VentanaPrincipal extends JFrame {
    private final JTabbedPane pestanas = new JTabbedPane();
    public VentanaPrincipal(Clinica clinica, Runnable salir, Runnable cerrarSesion) {
        super("Turnera médica - " + clinica.getSesion().getNombreCompleto() + " (" + clinica.getSesion().getRol() + ")");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout());
        if (clinica.esAdministrador()) {
            pestanas.addTab("Médicos", new UsuariosPanel(clinica, true));
            pestanas.addTab("Pacientes", new UsuariosPanel(clinica, false));
            pestanas.addTab("Obras sociales", new CatalogosPanel(clinica, true));
            pestanas.addTab("Consultorios", new CatalogosPanel(clinica, false));
            pestanas.addTab("Atenciones", new AtencionesPanel(clinica));
        }
        pestanas.addTab(clinica.esAdministrador() ? "Turnos" : "Mis turnos", new TurnosPanel(clinica));
        pestanas.addTab("Calendario", new CalendarioPanel(clinica));
        if (!(clinica.getSesion() instanceof Paciente)) { pestanas.addTab("Reportes", new ReportesPanel(clinica)); }
        add(pestanas, BorderLayout.CENTER);
        JButton cerrar = new JButton("Cerrar sesión");
        cerrar.addActionListener(e -> { dispose(); clinica.cerrarSesion(); cerrarSesion.run(); });
        JPanel inferior = new JPanel(); inferior.add(cerrar); add(inferior, BorderLayout.SOUTH);
        pestanas.addChangeListener(e -> cargarActual());
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evento) { dispose(); salir.run(); }
        });
        setSize(1100, 650);
        setMinimumSize(new java.awt.Dimension(1000, 550));
        setLocationRelativeTo(null);
        cargarActual();
    }
    private void cargarActual() {
        PanelDatos panel = (PanelDatos) pestanas.getSelectedComponent();
        Ui.ejecutar(this, panel::cargar);
    }
}
