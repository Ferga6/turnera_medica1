package turnomedico.vista;

import java.awt.BorderLayout;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import turnomedico.logica.*;
import turnomedico.modelo.Turno;

class CalendarioPanel extends PanelDatos {
    private final JTextField mes = new JTextField(YearMonth.now().toString(), 7);
    private final DefaultTableModel modelo = new DefaultTableModel(new String[]{"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"}, 6) {
        @Override
        public boolean isCellEditable(int fila, int columna) { return false; }
    };
    private final JTable calendario = new JTable(modelo);
    private final Tabla detalle = TurnosPanel.nuevaTabla();
    private final JLabel dia = new JLabel("Seleccione un día para ver sus turnos.");
    private final LocalDate[][] dias = new LocalDate[6][7];
    private List<Turno> turnos = new ArrayList<>();

    CalendarioPanel(Clinica clinica) {
        super(clinica);
        boton("Anterior", () -> mover(-1));
        botones.add(new JLabel("Mes (yyyy-MM):")); botones.add(mes);
        boton("Ver mes", this::cargar);
        boton("Siguiente", () -> mover(1));
        calendario.setCellSelectionEnabled(true);
        calendario.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        calendario.setRowHeight(30);
        JPanel inferior = new JPanel(new BorderLayout());
        inferior.add(dia, BorderLayout.NORTH); inferior.add(detalle, BorderLayout.CENTER);
        JSplitPane division = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(calendario), inferior);
        division.setResizeWeight(0.5);
        add(division, BorderLayout.CENTER);
        calendario.getSelectionModel().addListSelectionListener(e -> mostrarDia());
        calendario.getColumnModel().getSelectionModel().addListSelectionListener(e -> mostrarDia());
    }
    private void mover(int meses) throws TurnoMedicoException {
        mes.setText(YearMonth.parse(mes.getText().trim()).plusMonths(meses).toString()); cargar();
    }
    @Override
    public void cargar() throws TurnoMedicoException {
        YearMonth elegido = YearMonth.parse(mes.getText().trim());
        List<Turno> nuevos = clinica.listarTurnos(elegido.atDay(1), elegido.atEndOfMonth());
        calendario.clearSelection();
        turnos = nuevos;
        detalle.limpiar(); dia.setText("Seleccione un día para ver sus turnos.");
        int inicio = elegido.atDay(1).getDayOfWeek().getValue() - 1;
        for (int fila = 0; fila < 6; fila++) {
            for (int columna = 0; columna < 7; columna++) {
                int numero = fila * 7 + columna - inicio + 1;
                dias[fila][columna] = numero >= 1 && numero <= elegido.lengthOfMonth() ? elegido.atDay(numero) : null;
                int cantidad = 0;
                for (Turno turno : turnos) { if (turno.getFecha().equals(dias[fila][columna])) { cantidad++; } }
                modelo.setValueAt(dias[fila][columna] == null ? "" : numero + " (" + cantidad + ")", fila, columna);
            }
        }
    }
    private void mostrarDia() {
        int fila = calendario.getSelectedRow(), columna = calendario.getSelectedColumn();
        detalle.limpiar();
        if (fila < 0 || columna < 0 || dias[fila][columna] == null) { return; }
        LocalDate fecha = dias[fila][columna];
        dia.setText("Turnos del " + fecha);
        List<Turno> elegidos = new ArrayList<>();
        for (Turno turno : turnos) { if (turno.getFecha().equals(fecha)) { elegidos.add(turno); } }
        TurnosPanel.llenar(detalle, elegidos);
    }
}
