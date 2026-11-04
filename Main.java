package turnomedico;

import turnomedico.logica.Clinica;
import turnomedico.logica.TurnoMedicoException;
import turnomedico.modelo.Medico;
import turnomedico.modelo.Paciente;

public class Main {

    public static void main(String[] args) {
        Clinica clinica = new Clinica();

        try {
            clinica.agregarMedico(new Medico("mgomez", "1234", "María", "Gómez", "MN11223", "Clínica médica", 20000));
            clinica.agregarMedico(new Medico("lfernandez", "1234", "Luis", "Fernández", "MN44556", "Cardiología", 35000));
            clinica.agregarPaciente(new Paciente("jperez", "1234", "Juan", "Pérez", "30111222", "1155551111"));
            clinica.agregarPaciente(new Paciente("alopez", "1234", "Ana", "López", "28999888", "1155552222"));
        } catch (TurnoMedicoException e) {
            System.out.println("Error: " + e.getMessage());
        }

        System.out.println("== Médicos ==");
        for (Medico m : clinica.listarMedicos()) {
            System.out.println(m);
        }
        System.out.println("== Pacientes ==");
        for (Paciente p : clinica.listarPacientes()) {
            System.out.println(p);
        }

        try {
            Medico gomez = clinica.buscarMedico("MN11223");
            clinica.modificarMedico(gomez, new Medico("mgomez", "1234", "María", "Gómez", "MN11223", "Clínica médica", 25000));
            System.out.println("\nModificado: " + gomez);

            Paciente lopez = clinica.buscarPaciente("28999888");
            clinica.eliminarPaciente(lopez);
            System.out.println("Eliminado: " + lopez);
        } catch (TurnoMedicoException e) {
            System.out.println("Error: " + e.getMessage());
        }

        try {
            clinica.agregarPaciente(new Paciente("jperez", "1234", "Otro", "Paciente", "12345678", ""));
        } catch (TurnoMedicoException e) {
            System.out.println("\nError esperado: " + e.getMessage());
        }

        System.out.println("\nPacientes: " + clinica.listarPacientes());
    }
}
