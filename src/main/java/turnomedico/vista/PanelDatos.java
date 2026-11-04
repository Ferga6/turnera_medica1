package turnomedico.vista;

import java.awt.BorderLayout;
import javax.swing.*;
import turnomedico.logica.Clinica;
import turnomedico.logica.TurnoMedicoException;

abstract class PanelDatos extends JPanel {
    protected final Clinica clinica;
    protected final JPanel botones = new JPanel();
    PanelDatos(Clinica clinica) {
        super(new BorderLayout(5, 5));
        this.clinica = clinica;
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        add(botones, BorderLayout.NORTH);
    }
    protected void boton(String texto, Ui.Operacion operacion) {
        JButton boton = new JButton(texto);
        boton.addActionListener(e -> Ui.ejecutar(this, operacion));
        botones.add(boton);
    }
    public abstract void cargar() throws TurnoMedicoException;
}
