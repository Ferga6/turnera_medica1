package turnomedico.vista;

import java.awt.Component;
import java.awt.Container;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.swing.*;
import org.junit.jupiter.api.*;
import turnomedico.logica.Clinica;
import turnomedico.modelo.*;
import static org.junit.jupiter.api.Assertions.*;

public class VistaTest {
    private Clinica clinica;
    private final LocalDate fecha = LocalDate.of(2024, 2, 29);

    @BeforeEach
    void preparar() throws Exception {
        clinica = datos();
    }
    @AfterEach
    void cerrar() throws Exception { clinica.close(); }

    private static Clinica datos() throws Exception {
        Clinica clinica = new Clinica("jdbc:h2:mem:" + UUID.randomUUID());
        clinica.crearAdministrador("admin", "1234", "Ana", "Admin");
        clinica.iniciarSesion("admin", "1234");
        long obra = clinica.guardarObra(new ObraSocial(0, "Obra A"));
        long medico = clinica.guardarMedico(new Medico(0, "medico1", "María", "Gómez", "MN1", "Clínica", new BigDecimal("20000"), obra), "1234");
        long paciente = clinica.guardarPaciente(new Paciente(0, "paciente1", "Juan", "Pérez", "30111222", "1155551111", obra), "1234");
        long lugar = clinica.guardarConsultorio(new Consultorio(0, "Sala 1", "Calle 123"));
        clinica.guardarAtencion(0, medico, lugar, LocalDate.of(2024, 1, 1), LocalDate.now().plusYears(1));
        clinica.guardarTurno(0, medico, paciente, lugar, LocalDate.of(2024, 2, 29), LocalTime.of(9, 0));
        clinica.guardarTurno(0, medico, paciente, lugar, LocalDate.now(), LocalTime.of(10, 0));
        return clinica;
    }

    @Test
    void panelesAdministrativosCarganLosDatos() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                PanelDatos[] paneles = {new UsuariosPanel(clinica, true), new UsuariosPanel(clinica, false),
                        new CatalogosPanel(clinica, true), new CatalogosPanel(clinica, false), new AtencionesPanel(clinica)};
                for (PanelDatos panel : paneles) {
                    panel.cargar();
                    assertEquals(1, buscar(panel, JTable.class).get(0).getRowCount());
                }
            } catch (Exception e) { throw new RuntimeException(e); }
        });
    }

    @Test
    void agendaConsultaUnaFechaYSoloElAdministradorVeLosBotonesDeEdicion() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                TurnosPanel panel = new TurnosPanel(clinica);
                for (JTextField campo : buscar(panel, JTextField.class)) { campo.setText(fecha.toString()); }
                panel.cargar();
                assertEquals(1, buscar(panel, JTable.class).get(0).getRowCount());
                assertEquals(4, buscar(panel.botones, JButton.class).size());
                clinica.iniciarSesion("medico1", "1234");
                assertEquals(1, buscar(new TurnosPanel(clinica).botones, JButton.class).size());
                clinica.iniciarSesion("paciente1", "1234");
                assertEquals(1, buscar(new TurnosPanel(clinica).botones, JButton.class).size());
            } catch (Exception e) { throw new RuntimeException(e); }
        });
    }

    @Test
    void calendarioUbicaElDiaBisiestoYMuestraSuDetalle() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                CalendarioPanel panel = new CalendarioPanel(clinica);
                buscar(panel, JTextField.class).get(0).setText("2024-02");
                panel.cargar();
                JTable calendario = buscar(panel, JTable.class).get(0);
                JTable detalle = buscar(panel, JTable.class).get(1);
                assertEquals("", calendario.getValueAt(0, 0));
                assertEquals("1 (0)", calendario.getValueAt(0, 3));
                assertEquals("29 (1)", calendario.getValueAt(4, 3));
                calendario.changeSelection(4, 3, false, false);
                assertEquals(1, detalle.getRowCount());
                assertEquals("2024-02-29", detalle.getValueAt(0, 1));
                buscar(panel, JTextField.class).get(0).setText("2024-03");
                panel.cargar();
                assertEquals("1 (0)", calendario.getValueAt(0, 4));
                assertEquals(0, detalle.getRowCount());
            } catch (Exception e) { throw new RuntimeException(e); }
        });
    }

    @Test
    void reporteMuestraCantidadMontoYDetalleDelMedicoSeleccionado() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                ReportesPanel panel = new ReportesPanel(clinica);
                for (JTextField campo : buscar(panel, JTextField.class)) { campo.setText(fecha.toString()); }
                panel.cargar();
                List<JTable> tablas = buscar(panel, JTable.class);
                assertEquals(1, tablas.get(0).getValueAt(0, 2));
                assertEquals(new BigDecimal("10000.00"), tablas.get(0).getValueAt(0, 3));
                tablas.get(0).setRowSelectionInterval(0, 0);
                assertEquals(1, tablas.get(1).getRowCount());
            } catch (Exception e) { throw new RuntimeException(e); }
        });
    }

    @Test
    void seleccionDeTablaOrdenadaMantieneElIdCorrecto() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                Tabla tabla = new Tabla("ID", "Nombre");
                tabla.agregar(10L, "A"); tabla.agregar(2L, "Z");
                tabla.getTabla().getRowSorter().toggleSortOrder(0);
                tabla.getTabla().setRowSelectionInterval(0, 0);
                assertEquals(2, tabla.seleccionado());
                assertFalse(tabla.getTabla().isCellEditable(0, 0));
            } catch (Exception e) { throw new RuntimeException(e); }
        });
    }

    @Test
    void formatosDeFechaYHoraSonEstrictos() {
        assertThrows(IllegalArgumentException.class, () -> Ui.fecha(new JTextField("29/02/2024")));
        assertThrows(java.time.DateTimeException.class, () -> Ui.fecha(new JTextField("2024-02-30")));
        assertThrows(IllegalArgumentException.class, () -> Ui.hora(new JTextField("9:00")));
        assertThrows(java.time.DateTimeException.class, () -> Ui.hora(new JTextField("25:00")));
        assertEquals(LocalTime.of(9, 0), Ui.hora(new JTextField("09:00")));
    }

    private static <T> List<T> buscar(Container padre, Class<T> tipo) {
        List<T> lista = new ArrayList<>();
        for (Component hijo : padre.getComponents()) {
            if (tipo.isInstance(hijo)) { lista.add(tipo.cast(hijo)); }
            if (hijo instanceof Container) { lista.addAll(buscar((Container) hijo, tipo)); }
        }
        return lista;
    }

    public static void main(String[] args) throws Exception {
        Clinica clinica = datos();
        if (args.length > 0) { clinica.iniciarSesion(args[0], "1234"); }
        SwingUtilities.invokeLater(() -> new VentanaPrincipal(clinica, () -> {
            try { clinica.close(); } catch (Exception e) { throw new RuntimeException(e); }
        }, () -> {
            try { clinica.close(); } catch (Exception e) { throw new RuntimeException(e); }
        }).setVisible(true));
    }
}
