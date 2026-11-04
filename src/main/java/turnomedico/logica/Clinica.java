package turnomedico.logica;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import turnomedico.modelo.*;
import turnomedico.persistencia.ClinicaDao;

public class Clinica implements AutoCloseable {
    private final ClinicaDao dao;
    private Usuario sesion;

    public Clinica(String url) throws TurnoMedicoException { dao = new ClinicaDao(url); }
    public Usuario getSesion() { return sesion; }
    public boolean necesitaAdministrador() throws TurnoMedicoException { return dao.listarUsuarios().isEmpty(); }

    public void crearAdministrador(String usuario, String clave, String nombre, String apellido) throws TurnoMedicoException {
        exigir(necesitaAdministrador(), "El administrador inicial ya fue creado.");
        Usuario nuevo = new Usuario(0, normalizarUsuario(usuario), texto(nombre, "Nombre", 100), texto(apellido, "Apellido", 100));
        dao.guardarUsuario(nuevo, claveParaGuardar(0, clave));
    }

    public Usuario iniciarSesion(String usuario, String clave) throws TurnoMedicoException {
        sesion = null;
        exigir(usuario != null && clave != null, "Ingrese usuario y contraseña.");
        for (Usuario candidato : dao.listarUsuarios()) {
            if (candidato.getUsuario().equals(usuario.trim().toLowerCase(Locale.ROOT))
                    && Claves.verificar(clave, dao.buscarClave(candidato.getId()))) {
                sesion = candidato;
                return sesion;
            }
        }
        throw new TurnoMedicoException("Usuario o contraseña incorrectos.");
    }

    public void cerrarSesion() { sesion = null; }

    public List<Medico> listarMedicos() throws TurnoMedicoException {
        autenticar();
        List<Medico> lista = new ArrayList<>();
        for (Usuario usuario : dao.listarUsuarios()) { if (usuario instanceof Medico) { lista.add((Medico) usuario); } }
        return lista;
    }

    public List<Paciente> listarPacientes() throws TurnoMedicoException {
        administrador();
        List<Paciente> lista = new ArrayList<>();
        for (Usuario usuario : dao.listarUsuarios()) { if (usuario instanceof Paciente) { lista.add((Paciente) usuario); } }
        return lista;
    }

    public long guardarMedico(Medico datos, String clave) throws TurnoMedicoException {
        administrador();
        exigir(datos != null, "Faltan los datos del médico.");
        validarTipo(datos.getId(), Medico.class);
        String matricula = texto(datos.getMatricula(), "Matrícula", 50).toUpperCase(Locale.ROOT);
        BigDecimal honorario = datos.getHonorario();
        exigir(honorario != null && honorario.signum() > 0 && honorario.scale() <= 2
                && honorario.compareTo(new BigDecimal("9999999999.99")) <= 0,
                "El honorario debe ser positivo y tener como máximo dos decimales.");
        validarObra(datos.getObraSocialId());
        Medico nuevo = new Medico(datos.getId(), normalizarUsuario(datos.getUsuario()),
                texto(datos.getNombre(), "Nombre", 100), texto(datos.getApellido(), "Apellido", 100),
                matricula, texto(datos.getEspecialidad(), "Especialidad", 100), honorario, datos.getObraSocialId());
        validarDuplicados(nuevo);
        return dao.guardarUsuario(nuevo, claveParaGuardar(datos.getId(), clave));
    }

    public long guardarPaciente(Paciente datos, String clave) throws TurnoMedicoException {
        administrador();
        exigir(datos != null, "Faltan los datos del paciente.");
        validarTipo(datos.getId(), Paciente.class);
        String dni = texto(datos.getDni(), "DNI", 8);
        exigir(dni.matches("[0-9]{7,8}"), "El DNI debe tener 7 u 8 números.");
        String telefono = datos.getTelefono() == null ? "" : datos.getTelefono().trim();
        exigir(telefono.length() <= 40, "El teléfono admite hasta 40 caracteres.");
        validarObra(datos.getObraSocialId());
        Paciente nuevo = new Paciente(datos.getId(), normalizarUsuario(datos.getUsuario()),
                texto(datos.getNombre(), "Nombre", 100), texto(datos.getApellido(), "Apellido", 100), dni, telefono, datos.getObraSocialId());
        validarDuplicados(nuevo);
        return dao.guardarUsuario(nuevo, claveParaGuardar(datos.getId(), clave));
    }

    public void eliminarUsuario(long id) throws TurnoMedicoException {
        administrador();
        Usuario usuario = dao.buscarUsuario(id);
        exigir(usuario instanceof Medico || usuario instanceof Paciente, "No se puede eliminar al administrador.");
        dao.eliminarUsuario(id);
    }

    public List<ObraSocial> listarObras() throws TurnoMedicoException { autenticar(); return dao.listarObras(); }
    public long guardarObra(ObraSocial obra) throws TurnoMedicoException {
        administrador();
        exigir(obra != null, "Faltan los datos de la obra social.");
        String nombre = texto(obra.getNombre(), "Nombre", 100);
        boolean existe = obra.getId() == 0;
        for (ObraSocial otra : dao.listarObras()) {
            if (otra.getId() == obra.getId()) { existe = true; }
            exigir(otra.getId() == obra.getId() || !otra.getNombre().equalsIgnoreCase(nombre), "La obra social ya existe.");
        }
        exigir(existe, "La obra social ya no existe.");
        return dao.guardarObra(new ObraSocial(obra.getId(), nombre));
    }
    public void eliminarObra(long id) throws TurnoMedicoException { administrador(); dao.eliminarObra(id); }

    public List<Consultorio> listarConsultorios() throws TurnoMedicoException { autenticar(); return dao.listarConsultorios(); }
    public long guardarConsultorio(Consultorio datos) throws TurnoMedicoException {
        administrador();
        exigir(datos != null, "Faltan los datos del consultorio.");
        if (datos.getId() != 0) { dao.buscarConsultorio(datos.getId()); }
        String nombre = texto(datos.getNombre(), "Nombre", 100);
        for (Consultorio otro : dao.listarConsultorios()) {
            exigir(otro.getId() == datos.getId() || !otro.getNombre().equalsIgnoreCase(nombre), "El consultorio ya existe.");
        }
        return dao.guardarConsultorio(new Consultorio(datos.getId(), nombre, texto(datos.getDireccion(), "Dirección", 200)));
    }
    public void eliminarConsultorio(long id) throws TurnoMedicoException { administrador(); dao.eliminarConsultorio(id); }

    public List<Atencion> listarAtenciones() throws TurnoMedicoException { administrador(); return dao.listarAtenciones(); }
    public long guardarAtencion(long id, long medicoId, long consultorioId, LocalDate desde, LocalDate hasta) throws TurnoMedicoException {
        administrador();
        validarFechas(desde, hasta);
        Medico medico = medico(medicoId);
        Consultorio consultorio = dao.buscarConsultorio(consultorioId);
        if (id != 0) { validarAtencionSinTurnos(buscarAtencion(id)); }
        for (Atencion otra : dao.listarAtenciones()) {
            if (otra.getId() != id && otra.getMedico().getId() == medicoId) {
                exigir(hasta.isBefore(otra.getDesde()) || desde.isAfter(otra.getHasta()),
                        "El médico ya tiene un consultorio asignado en ese período.");
            }
        }
        return dao.guardarAtencion(new Atencion(id, medico, consultorio, desde, hasta));
    }
    public void eliminarAtencion(long id) throws TurnoMedicoException {
        administrador(); validarAtencionSinTurnos(buscarAtencion(id)); dao.eliminarAtencion(id);
    }

    public long guardarTurno(long id, long medicoId, long pacienteId, long consultorioId,
                             LocalDate fecha, LocalTime hora) throws TurnoMedicoException {
        administrador();
        validarFechas(fecha, fecha);
        exigir(hora != null && hora.getSecond() == 0 && hora.getNano() == 0, "La hora debe expresarse en horas y minutos (HH:mm).");
        Medico medico = medico(medicoId);
        Usuario usuario = dao.buscarUsuario(pacienteId);
        exigir(usuario instanceof Paciente, "Seleccione un paciente válido.");
        Paciente paciente = (Paciente) usuario;
        Consultorio consultorio = dao.buscarConsultorio(consultorioId);
        boolean atiende = false;
        for (Atencion atencion : dao.listarAtenciones()) {
            if (atencion.getMedico().getId() == medicoId && atencion.getConsultorio().getId() == consultorioId
                    && !fecha.isBefore(atencion.getDesde()) && !fecha.isAfter(atencion.getHasta())) { atiende = true; }
        }
        exigir(atiende, "El médico no atiende en ese consultorio en la fecha elegida. Revise Atenciones.");
        Turno anterior = id == 0 ? null : buscarTurno(id);
        for (Turno otro : dao.listarTurnos(fecha, fecha)) {
            if (otro.getId() != id && otro.getHora().equals(hora)) {
                exigir(otro.getMedico().getId() != medicoId, "El médico ya tiene un turno a esa hora.");
                exigir(otro.getPaciente().getId() != pacienteId, "El paciente ya tiene un turno a esa hora.");
                exigir(otro.getConsultorio().getId() != consultorioId, "El consultorio ya está ocupado a esa hora.");
            }
        }
        boolean descuento = medico.getObraSocialId() != null && medico.getObraSocialId().equals(paciente.getObraSocialId());
        BigDecimal monto = medico.getHonorario();
        if (descuento) { monto = monto.multiply(new BigDecimal("0.50")).setScale(2, RoundingMode.HALF_UP); }
        if (anterior != null && anterior.getMedico().getId() == medicoId && anterior.getPaciente().getId() == pacienteId) {
            monto = anterior.getMonto(); descuento = anterior.tieneDescuento();
        }
        return dao.guardarTurno(new Turno(id, medico, paciente, consultorio, fecha, hora, monto, descuento));
    }

    public void eliminarTurno(long id) throws TurnoMedicoException { administrador(); dao.eliminarTurno(id); }

    public List<Turno> listarTurnos(LocalDate desde, LocalDate hasta) throws TurnoMedicoException {
        autenticar(); validarFechas(desde, hasta);
        List<Turno> visibles = new ArrayList<>();
        for (Turno turno : dao.listarTurnos(desde, hasta)) {
            if (esAdministrador() || (sesion instanceof Medico && turno.getMedico().getId() == sesion.getId())
                    || (sesion instanceof Paciente && turno.getPaciente().getId() == sesion.getId())) { visibles.add(turno); }
        }
        return visibles;
    }

    public List<ReporteMedico> reporteMedicos(LocalDate desde, LocalDate hasta) throws TurnoMedicoException {
        autenticar();
        exigir(!(sesion instanceof Paciente), "El paciente no tiene acceso a reportes de recaudación.");
        List<Turno> turnos = listarTurnos(desde, hasta);
        List<ReporteMedico> reportes = new ArrayList<>();
        for (Medico medico : listarMedicos()) {
            if (esAdministrador() || medico.getId() == sesion.getId()) { reportes.add(new ReporteMedico(medico, turnos)); }
        }
        return reportes;
    }

    public boolean esAdministrador() { return sesion != null && !(sesion instanceof Medico) && !(sesion instanceof Paciente); }
    private void autenticar() throws TurnoMedicoException { exigir(sesion != null, "Debe iniciar sesión."); }
    private void administrador() throws TurnoMedicoException { autenticar(); exigir(esAdministrador(), "Esta operación requiere un administrador."); }
    private Medico medico(long id) throws TurnoMedicoException {
        Usuario usuario = dao.buscarUsuario(id);
        exigir(usuario instanceof Medico, "Seleccione un médico válido."); return (Medico) usuario;
    }
    private void validarTipo(long id, Class<?> tipo) throws TurnoMedicoException {
        exigir(id >= 0, "Identificador inválido.");
        if (id != 0) { exigir(tipo.isInstance(dao.buscarUsuario(id)), "No se puede cambiar el rol del usuario."); }
    }
    private void validarObra(Long id) throws TurnoMedicoException {
        if (id == null) { return; }
        for (ObraSocial obra : dao.listarObras()) { if (obra.getId() == id) { return; } }
        throw new TurnoMedicoException("Seleccione una obra social válida.");
    }
    private void validarDuplicados(Usuario datos) throws TurnoMedicoException {
        for (Usuario otro : dao.listarUsuarios()) {
            if (otro.getId() == datos.getId()) { continue; }
            exigir(!otro.getUsuario().equalsIgnoreCase(datos.getUsuario()), "El nombre de usuario ya existe.");
            if (datos instanceof Medico && otro instanceof Medico) {
                exigir(!((Medico) datos).getMatricula().equalsIgnoreCase(((Medico) otro).getMatricula()), "La matrícula ya existe.");
            }
            if (datos instanceof Paciente && otro instanceof Paciente) {
                exigir(!((Paciente) datos).getDni().equals(((Paciente) otro).getDni()), "El DNI ya existe.");
            }
        }
    }
    private String claveParaGuardar(long id, String clave) throws TurnoMedicoException {
        if (id != 0 && (clave == null || clave.isEmpty())) { return dao.buscarClave(id); }
        exigir(clave != null && clave.trim().length() >= 4 && clave.length() <= 100, "La contraseña debe tener entre 4 y 100 caracteres.");
        return Claves.crear(clave);
    }
    private String normalizarUsuario(String usuario) throws TurnoMedicoException {
        String valor = texto(usuario, "Usuario", 50).toLowerCase(Locale.ROOT);
        exigir(valor.matches("[a-z0-9._-]{3,50}"), "El usuario requiere 3 a 50 letras sin acentos, números, puntos, guiones o guiones bajos.");
        return valor;
    }
    private String texto(String valor, String campo, int maximo) throws TurnoMedicoException {
        exigir(valor != null && !valor.trim().isEmpty(), campo + " es obligatorio.");
        exigir(valor.trim().length() <= maximo, campo + " admite hasta " + maximo + " caracteres.");
        return valor.trim();
    }
    private void validarFechas(LocalDate desde, LocalDate hasta) throws TurnoMedicoException {
        exigir(desde != null && hasta != null, "Ingrese ambas fechas.");
        exigir(desde.getYear() >= 1900 && hasta.getYear() <= 9999 && !desde.isAfter(hasta),
                "El período debe estar ordenado, con años entre 1900 y 9999.");
    }
    private Atencion buscarAtencion(long id) throws TurnoMedicoException {
        for (Atencion atencion : dao.listarAtenciones()) { if (atencion.getId() == id) { return atencion; } }
        throw new TurnoMedicoException("La atención ya no existe.");
    }
    private void validarAtencionSinTurnos(Atencion atencion) throws TurnoMedicoException {
        for (Turno turno : dao.listarTurnos(atencion.getDesde(), atencion.getHasta())) {
            exigir(turno.getMedico().getId() != atencion.getMedico().getId()
                    || turno.getConsultorio().getId() != atencion.getConsultorio().getId(),
                    "La atención tiene turnos. Reprograme o elimine esos turnos antes de cambiarla.");
        }
    }
    private Turno buscarTurno(long id) throws TurnoMedicoException {
        for (Turno turno : dao.listarTurnos(LocalDate.of(1900, 1, 1), LocalDate.of(9999, 12, 31))) {
            if (turno.getId() == id) { return turno; }
        }
        throw new TurnoMedicoException("El turno ya no existe.");
    }
    private void exigir(boolean condicion, String mensaje) throws TurnoMedicoException {
        if (!condicion) { throw new TurnoMedicoException(mensaje); }
    }
    @Override
    public void close() throws TurnoMedicoException { sesion = null; dao.close(); }
}
