package turnomedico.vista;

import java.awt.BorderLayout;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import turnomedico.logica.TurnoMedicoException;

class Tabla extends JPanel {
    private final DefaultTableModel modelo;
    private final JTable tabla;
    Tabla(String... columnas) {
        super(new BorderLayout());
        modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) { return false; }
            @Override
            public Class<?> getColumnClass(int columna) {
                return getRowCount() == 0 || getValueAt(0, columna) == null ? Object.class : getValueAt(0, columna).getClass();
            }
        };
        tabla = new JTable(modelo);
        tabla.setAutoCreateRowSorter(true);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
    }
    void limpiar() { modelo.setRowCount(0); }
    void agregar(Object... valores) { modelo.addRow(valores); }
    JTable getTabla() { return tabla; }
    long seleccionado() throws TurnoMedicoException {
        int fila = tabla.getSelectedRow();
        if (fila < 0) { throw new TurnoMedicoException("Seleccione una fila de la lista."); }
        return ((Number) modelo.getValueAt(tabla.convertRowIndexToModel(fila), 0)).longValue();
    }
}
