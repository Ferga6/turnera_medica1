package turnomedico.logica;

import java.util.ArrayList;
import java.util.List;

import turnomedico.modelo.Medico;
import turnomedico.modelo.Paciente;
import turnomedico.modelo.Turno;
import turnomedico.modelo.Usuario;

public class Clinica {

    private List<Usuario> usuarios = new ArrayList<>();
    private List<Turno> turnos = new ArrayList<>();


    public void agregarMedico(Medico medico) throws TurnoMedicoException {
        validarMedico(medico, null);
        usuarios.add(medico);
    }

    public List<Medico> listarMedicos() {
        List<Medico> medicos = new ArrayList<>();
        for (Usuario u : usuarios) {
            if (u instanceof Medico) {
                medicos.add((Medico) u);
            }
        }
        return medicos;
    }

    public Medico buscarMedico(String matricula) throws TurnoMedicoException {
        for (Medico m : listarMedicos()) {
            if (m.getMatricula().equalsIgnoreCase(matricula)) {
                return m;
            }
        }
        throw new TurnoMedicoException("No existe un médico con matrícula " + matricula);
    }

    public void modificarMedico(Medico medico, Medico datosNuevos) throws TurnoMedicoException {
        validarMedico(datosNuevos, medico);
        medico.setUsuario(datosNuevos.getUsuario());
        medico.setContrasena(datosNuevos.getContrasena());
        medico.setNombre(datosNuevos.getNombre());
        medico.setApellido(datosNuevos.getApellido());
        medico.setMatricula(datosNuevos.getMatricula());
        medico.setEspecialidad(datosNuevos.getEspecialidad());
        medico.setHonorario(datosNuevos.getHonorario());
    }

    public void eliminarMedico(Medico medico) throws TurnoMedicoException {
        for (Turno t : turnos) {
            if (t.getMedico() == medico) {
                throw new TurnoMedicoException("No se puede eliminar: el médico tiene turnos asignados");
            }
        }
        usuarios.remove(medico);
    }


    public void agregarPaciente(Paciente paciente) throws TurnoMedicoException {
        validarPaciente(paciente, null);
        usuarios.add(paciente);
    }

    public List<Paciente> listarPacientes() {
        List<Paciente> pacientes = new ArrayList<>();
        for (Usuario u : usuarios) {
            if (u instanceof Paciente) {
                pacientes.add((Paciente) u);
            }
        }
        return pacientes;
    }

    public Paciente buscarPaciente(String dni) throws TurnoMedicoException {
        for (Paciente p : listarPacientes()) {
            if (p.getDni().equals(dni)) {
                return p;
            }
        }
        throw new TurnoMedicoException("No existe un paciente con DNI " + dni);
    }

    public void modificarPaciente(Paciente paciente, Paciente datosNuevos) throws TurnoMedicoException {
        validarPaciente(datosNuevos, paciente);
        paciente.setUsuario(datosNuevos.getUsuario());
        paciente.setContrasena(datosNuevos.getContrasena());
        paciente.setNombre(datosNuevos.getNombre());
        paciente.setApellido(datosNuevos.getApellido());
        paciente.setDni(datosNuevos.getDni());
        paciente.setTelefono(datosNuevos.getTelefono());
    }

    public void eliminarPaciente(Paciente paciente) throws TurnoMedicoException {
        for (Turno t : turnos) {
            if (t.getPaciente() == paciente) {
                throw new TurnoMedicoException("No se puede eliminar: el paciente tiene turnos asignados");
            }
        }
        usuarios.remove(paciente);
    }


    private void validarUsuario(Usuario u, Usuario actual) throws TurnoMedicoException {
        if (estaVacio(u.getNombre()) || estaVacio(u.getApellido())) {
            throw new TurnoMedicoException("Nombre y apellido son obligatorios");
        }
        if (estaVacio(u.getUsuario())) {
            throw new TurnoMedicoException("El nombre de usuario es obligatorio");
        }
        if (estaVacio(u.getContrasena()) || u.getContrasena().length() < 4) {
            throw new TurnoMedicoException("La contraseña debe tener al menos 4 caracteres");
        }
        for (Usuario otro : usuarios) {
            if (otro != actual && otro.getUsuario().equalsIgnoreCase(u.getUsuario())) {
                throw new TurnoMedicoException("El usuario " + u.getUsuario() + " ya existe");
            }
        }
    }

    private void validarMedico(Medico m, Medico actual) throws TurnoMedicoException {
        validarUsuario(m, actual);
        if (estaVacio(m.getMatricula()) || estaVacio(m.getEspecialidad())) {
            throw new TurnoMedicoException("Matrícula y especialidad son obligatorias");
        }
        if (m.getHonorario() <= 0) {
            throw new TurnoMedicoException("El honorario debe ser mayor a cero");
        }
        for (Medico otro : listarMedicos()) {
            if (otro != actual && otro.getMatricula().equalsIgnoreCase(m.getMatricula())) {
                throw new TurnoMedicoException("Ya existe un médico con matrícula " + m.getMatricula());
            }
        }
    }

    private void validarPaciente(Paciente p, Paciente actual) throws TurnoMedicoException {
        validarUsuario(p, actual);
        if (estaVacio(p.getDni()) || !p.getDni().matches("\\d{7,8}")) {
            throw new TurnoMedicoException("El DNI debe tener 7 u 8 números");
        }
        for (Paciente otro : listarPacientes()) {
            if (otro != actual && otro.getDni().equals(p.getDni())) {
                throw new TurnoMedicoException("Ya existe un paciente con DNI " + p.getDni());
            }
        }
    }

    private boolean estaVacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }
}
