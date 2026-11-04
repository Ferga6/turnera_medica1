package turnomedico;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.*;
import turnomedico.logica.*;
import turnomedico.vista.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                Path carpeta = Paths.get(System.getProperty("turnera.datos", "datos")).toAbsolutePath().normalize();
                Files.createDirectories(carpeta);
                Clinica clinica = new Clinica("jdbc:h2:file:" + carpeta.resolve("clinica").toString().replace('\\', '/'));
                ingresar(clinica);
            } catch (Exception e) { error(e); }
        });
    }
    private static void ingresar(Clinica clinica) {
        try {
            if (Login.mostrar(clinica)) {
                new VentanaPrincipal(clinica, () -> cerrar(clinica), () -> ingresar(clinica)).setVisible(true);
            } else { cerrar(clinica); }
        } catch (TurnoMedicoException e) { error(e); cerrar(clinica); }
    }
    private static void cerrar(Clinica clinica) {
        try { clinica.close(); } catch (TurnoMedicoException e) { error(e); }
    }
    private static void error(Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}
