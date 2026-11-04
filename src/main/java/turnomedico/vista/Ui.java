package turnomedico.vista;

import java.awt.Component;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.LocalTime;
import javax.swing.*;
import turnomedico.logica.TurnoMedicoException;
import turnomedico.modelo.ObraSocial;

final class Ui {
    private Ui() { }
    interface Operacion { void ejecutar() throws TurnoMedicoException; }

    static void ejecutar(Component padre, Operacion operacion) {
        try { operacion.ejecutar(); }
        catch (TurnoMedicoException e) { error(padre, e.getMessage()); }
        catch (IllegalArgumentException | java.time.DateTimeException e) {
            error(padre, "Revise los números y las fechas. Use yyyy-MM-dd para fechas, HH:mm para horas y hasta dos decimales para importes.");
        }
    }

    static void error(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "No se pudo completar", JOptionPane.ERROR_MESSAGE);
    }

    static JPanel campos(Object... pares) {
        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        for (int i = 0; i < pares.length; i += 2) {
            panel.add(new JLabel((String) pares[i])); panel.add((Component) pares[i + 1]);
        }
        return panel;
    }

    static boolean formulario(Component padre, String titulo, JPanel campos, Operacion guardar) {
        while (JOptionPane.showConfirmDialog(padre, campos, titulo, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            try { guardar.ejecutar(); return true; }
            catch (TurnoMedicoException e) { error(padre, e.getMessage()); }
            catch (IllegalArgumentException | java.time.DateTimeException e) {
                error(padre, "Revise el formato: fecha yyyy-MM-dd, hora HH:mm e importes sin separador de miles.");
            }
        }
        return false;
    }

    static boolean confirmar(Component padre) {
        return JOptionPane.showConfirmDialog(padre, "¿Eliminar el registro seleccionado?", "Eliminar",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    static LocalDate fecha(JTextField campo) {
        String valor = campo.getText().trim();
        if (!valor.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) { throw new IllegalArgumentException(); }
        return LocalDate.parse(valor);
    }

    static LocalTime hora(JTextField campo) {
        String valor = campo.getText().trim();
        if (!valor.matches("[0-9]{2}:[0-9]{2}")) { throw new IllegalArgumentException(); }
        return LocalTime.parse(valor);
    }

    static Long obraId(JComboBox<ObraSocial> combo) {
        ObraSocial obra = (ObraSocial) combo.getSelectedItem();
        return obra == null || obra.getId() == 0 ? null : obra.getId();
    }

    static void seleccionarObra(JComboBox<ObraSocial> combo, Long id) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (id != null && combo.getItemAt(i).getId() == id) { combo.setSelectedIndex(i); }
        }
    }
}
