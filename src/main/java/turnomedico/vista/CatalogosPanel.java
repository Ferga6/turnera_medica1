package turnomedico.vista;

import java.awt.BorderLayout;
import javax.swing.*;
import turnomedico.logica.*;
import turnomedico.modelo.*;

class CatalogosPanel extends PanelDatos {
    private final boolean obras;
    private final Tabla tabla;

    CatalogosPanel(Clinica clinica, boolean obras) {
        super(clinica);
        this.obras = obras;
        tabla = obras ? new Tabla("ID", "Nombre") : new Tabla("ID", "Nombre", "Dirección");
        boton("Agregar", () -> editar(0));
        boton("Modificar", () -> editar(tabla.seleccionado()));
        boton("Eliminar", () -> {
            long id = tabla.seleccionado();
            if (Ui.confirmar(this)) {
                if (obras) { clinica.eliminarObra(id); } else { clinica.eliminarConsultorio(id); }
                cargar();
            }
        });
        add(tabla, BorderLayout.CENTER);
    }
    @Override
    public void cargar() throws TurnoMedicoException {
        tabla.limpiar();
        if (obras) {
            for (ObraSocial obra : clinica.listarObras()) { tabla.agregar(obra.getId(), obra.getNombre()); }
        } else {
            for (Consultorio consultorio : clinica.listarConsultorios()) {
                tabla.agregar(consultorio.getId(), consultorio.getNombre(), consultorio.getDireccion());
            }
        }
    }
    private void editar(long id) throws TurnoMedicoException {
        JTextField nombre = new JTextField(24);
        JTextField direccion = new JTextField(24);
        if (id != 0) {
            if (obras) {
                for (ObraSocial obra : clinica.listarObras()) { if (obra.getId() == id) { nombre.setText(obra.getNombre()); } }
            } else {
                for (Consultorio consultorio : clinica.listarConsultorios()) {
                    if (consultorio.getId() == id) { nombre.setText(consultorio.getNombre()); direccion.setText(consultorio.getDireccion()); }
                }
            }
        }
        JPanel campos = obras ? Ui.campos("Nombre", nombre) : Ui.campos("Nombre", nombre, "Dirección", direccion);
        if (Ui.formulario(this, obras ? "Obra social" : "Consultorio", campos, () -> {
            if (obras) { clinica.guardarObra(new ObraSocial(id, nombre.getText())); }
            else { clinica.guardarConsultorio(new Consultorio(id, nombre.getText(), direccion.getText())); }
        })) { cargar(); }
    }
}
