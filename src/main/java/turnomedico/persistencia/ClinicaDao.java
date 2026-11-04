package turnomedico.persistencia;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.h2.tools.RunScript;
import turnomedico.logica.TurnoMedicoException;
import turnomedico.modelo.*;

public class ClinicaDao implements AutoCloseable {
    private final Connection conexion;

    public ClinicaDao(String url) throws TurnoMedicoException {
        Connection abierta = null;
        try {
            abierta = DriverManager.getConnection(url, "sa", "");
            try (InputStream archivo = ClinicaDao.class.getResourceAsStream("/esquema.sql")) {
                if (archivo == null) { throw new IllegalStateException("Falta esquema.sql"); }
                RunScript.execute(abierta, new InputStreamReader(archivo, StandardCharsets.UTF_8));
            }
            conexion = abierta;
        } catch (Exception e) {
            if (abierta != null) { try { abierta.close(); } catch (SQLException cierre) { e.addSuppressed(cierre); } }
            throw new TurnoMedicoException("No se pudo abrir la base de datos. Verifique la carpeta y que no haya otra instancia abierta.", e);
        }
    }

    public List<Usuario> listarUsuarios() throws TurnoMedicoException {
        List<Usuario> lista = new ArrayList<>();
        try (PreparedStatement consulta = conexion.prepareStatement("SELECT * FROM usuarios ORDER BY apellido, nombre");
             ResultSet filas = consulta.executeQuery()) {
            while (filas.next()) { lista.add(leerUsuario(filas)); }
            return lista;
        } catch (SQLException e) { throw error(e); }
    }

    public Usuario buscarUsuario(long id) throws TurnoMedicoException {
        try (PreparedStatement consulta = conexion.prepareStatement("SELECT * FROM usuarios WHERE id=?")) {
            consulta.setLong(1, id);
            try (ResultSet filas = consulta.executeQuery()) {
                if (filas.next()) { return leerUsuario(filas); }
                throw new TurnoMedicoException("El usuario ya no existe.");
            }
        } catch (SQLException e) { throw error(e); }
    }

    public String buscarClave(long id) throws TurnoMedicoException {
        try (PreparedStatement consulta = conexion.prepareStatement("SELECT clave FROM usuarios WHERE id=?")) {
            consulta.setLong(1, id);
            try (ResultSet filas = consulta.executeQuery()) {
                if (filas.next()) { return filas.getString(1); }
                throw new TurnoMedicoException("El usuario ya no existe.");
            }
        } catch (SQLException e) { throw error(e); }
    }

    private Usuario leerUsuario(ResultSet filas) throws SQLException {
        long id = filas.getLong("id");
        String usuario = filas.getString("usuario");
        String nombre = filas.getString("nombre");
        String apellido = filas.getString("apellido");
        long obra = filas.getLong("obra_social_id");
        Long obraId = filas.wasNull() ? null : obra;
        if ("Médico".equals(filas.getString("rol"))) {
            return new Medico(id, usuario, nombre, apellido, filas.getString("matricula"),
                    filas.getString("especialidad"), filas.getBigDecimal("honorario"), obraId);
        }
        if ("Paciente".equals(filas.getString("rol"))) {
            return new Paciente(id, usuario, nombre, apellido, filas.getString("dni"), filas.getString("telefono"), obraId);
        }
        return new Usuario(id, usuario, nombre, apellido);
    }

    public long guardarUsuario(Usuario usuario, String claveHash) throws TurnoMedicoException {
        String matricula = null, especialidad = null, dni = null, telefono = null;
        BigDecimal honorario = null;
        Long obra = null;
        if (usuario instanceof Medico) {
            Medico medico = (Medico) usuario;
            matricula = medico.getMatricula(); especialidad = medico.getEspecialidad();
            honorario = medico.getHonorario(); obra = medico.getObraSocialId();
        } else if (usuario instanceof Paciente) {
            Paciente paciente = (Paciente) usuario;
            dni = paciente.getDni(); telefono = paciente.getTelefono(); obra = paciente.getObraSocialId();
        }
        Object[] valores = {usuario.getUsuario(), claveHash, usuario.getNombre(), usuario.getApellido(),
                usuario.getRol(), matricula, especialidad, honorario, dni, telefono, obra};
        if (usuario.getId() == 0) {
            return insertar("INSERT INTO usuarios(usuario,clave,nombre,apellido,rol,matricula,especialidad,honorario,dni,telefono,obra_social_id) VALUES(?,?,?,?,?,?,?,?,?,?,?)", valores);
        }
        Object[] cambios = java.util.Arrays.copyOf(valores, 12);
        cambios[11] = usuario.getId();
        ejecutar("UPDATE usuarios SET usuario=?,clave=?,nombre=?,apellido=?,rol=?,matricula=?,especialidad=?,honorario=?,dni=?,telefono=?,obra_social_id=? WHERE id=?", cambios);
        return usuario.getId();
    }

    public List<ObraSocial> listarObras() throws TurnoMedicoException {
        List<ObraSocial> lista = new ArrayList<>();
        try (PreparedStatement consulta = conexion.prepareStatement("SELECT * FROM obras_sociales ORDER BY nombre");
             ResultSet filas = consulta.executeQuery()) {
            while (filas.next()) { lista.add(new ObraSocial(filas.getLong("id"), filas.getString("nombre"))); }
            return lista;
        } catch (SQLException e) { throw error(e); }
    }

    public long guardarObra(ObraSocial obra) throws TurnoMedicoException {
        if (obra.getId() == 0) { return insertar("INSERT INTO obras_sociales(nombre) VALUES(?)", obra.getNombre()); }
        ejecutar("UPDATE obras_sociales SET nombre=? WHERE id=?", obra.getNombre(), obra.getId());
        return obra.getId();
    }

    public List<Consultorio> listarConsultorios() throws TurnoMedicoException {
        List<Consultorio> lista = new ArrayList<>();
        try (PreparedStatement consulta = conexion.prepareStatement("SELECT * FROM consultorios ORDER BY nombre");
             ResultSet filas = consulta.executeQuery()) {
            while (filas.next()) {
                lista.add(new Consultorio(filas.getLong("id"), filas.getString("nombre"), filas.getString("direccion")));
            }
            return lista;
        } catch (SQLException e) { throw error(e); }
    }

    public Consultorio buscarConsultorio(long id) throws TurnoMedicoException {
        for (Consultorio consultorio : listarConsultorios()) { if (consultorio.getId() == id) { return consultorio; } }
        throw new TurnoMedicoException("El consultorio ya no existe.");
    }

    public long guardarConsultorio(Consultorio consultorio) throws TurnoMedicoException {
        if (consultorio.getId() == 0) {
            return insertar("INSERT INTO consultorios(nombre,direccion) VALUES(?,?)", consultorio.getNombre(), consultorio.getDireccion());
        }
        ejecutar("UPDATE consultorios SET nombre=?,direccion=? WHERE id=?", consultorio.getNombre(), consultorio.getDireccion(), consultorio.getId());
        return consultorio.getId();
    }

    public List<Atencion> listarAtenciones() throws TurnoMedicoException {
        List<Atencion> lista = new ArrayList<>();
        try (PreparedStatement consulta = conexion.prepareStatement("SELECT * FROM atenciones ORDER BY desde, id");
             ResultSet filas = consulta.executeQuery()) {
            while (filas.next()) {
                lista.add(new Atencion(filas.getLong("id"), (Medico) buscarUsuario(filas.getLong("medico_id")),
                        buscarConsultorio(filas.getLong("consultorio_id")), filas.getDate("desde").toLocalDate(),
                        filas.getDate("hasta").toLocalDate()));
            }
            return lista;
        } catch (SQLException e) { throw error(e); }
    }

    public long guardarAtencion(Atencion atencion) throws TurnoMedicoException {
        Object[] valores = {atencion.getMedico().getId(), atencion.getConsultorio().getId(),
                Date.valueOf(atencion.getDesde()), Date.valueOf(atencion.getHasta())};
        if (atencion.getId() == 0) {
            return insertar("INSERT INTO atenciones(medico_id,consultorio_id,desde,hasta) VALUES(?,?,?,?)", valores);
        }
        ejecutar("UPDATE atenciones SET medico_id=?,consultorio_id=?,desde=?,hasta=? WHERE id=?",
                valores[0], valores[1], valores[2], valores[3], atencion.getId());
        return atencion.getId();
    }

    public List<Turno> listarTurnos(LocalDate desde, LocalDate hasta) throws TurnoMedicoException {
        List<Turno> lista = new ArrayList<>();
        try (PreparedStatement consulta = conexion.prepareStatement("SELECT * FROM turnos WHERE fecha BETWEEN ? AND ? ORDER BY fecha,hora,id")) {
            consulta.setDate(1, Date.valueOf(desde)); consulta.setDate(2, Date.valueOf(hasta));
            try (ResultSet filas = consulta.executeQuery()) {
                while (filas.next()) {
                    lista.add(new Turno(filas.getLong("id"), (Medico) buscarUsuario(filas.getLong("medico_id")),
                            (Paciente) buscarUsuario(filas.getLong("paciente_id")), buscarConsultorio(filas.getLong("consultorio_id")),
                            filas.getDate("fecha").toLocalDate(), filas.getTime("hora").toLocalTime(),
                            filas.getBigDecimal("monto"), filas.getBoolean("descuento")));
                }
            }
            return lista;
        } catch (SQLException e) { throw error(e); }
    }

    public long guardarTurno(Turno turno) throws TurnoMedicoException {
        Object[] valores = {turno.getMedico().getId(), turno.getPaciente().getId(), turno.getConsultorio().getId(),
                Date.valueOf(turno.getFecha()), Time.valueOf(turno.getHora()), turno.getMonto(), turno.tieneDescuento()};
        if (turno.getId() == 0) {
            return insertar("INSERT INTO turnos(medico_id,paciente_id,consultorio_id,fecha,hora,monto,descuento) VALUES(?,?,?,?,?,?,?)", valores);
        }
        ejecutar("UPDATE turnos SET medico_id=?,paciente_id=?,consultorio_id=?,fecha=?,hora=?,monto=?,descuento=? WHERE id=?",
                valores[0], valores[1], valores[2], valores[3], valores[4], valores[5], valores[6], turno.getId());
        return turno.getId();
    }

    public void eliminarUsuario(long id) throws TurnoMedicoException { ejecutar("DELETE FROM usuarios WHERE id=?", id); }
    public void eliminarObra(long id) throws TurnoMedicoException { ejecutar("DELETE FROM obras_sociales WHERE id=?", id); }
    public void eliminarConsultorio(long id) throws TurnoMedicoException { ejecutar("DELETE FROM consultorios WHERE id=?", id); }
    public void eliminarAtencion(long id) throws TurnoMedicoException { ejecutar("DELETE FROM atenciones WHERE id=?", id); }
    public void eliminarTurno(long id) throws TurnoMedicoException { ejecutar("DELETE FROM turnos WHERE id=?", id); }

    private long insertar(String sql, Object... valores) throws TurnoMedicoException {
        try (PreparedStatement consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            completar(consulta, valores);
            consulta.executeUpdate();
            try (ResultSet claves = consulta.getGeneratedKeys()) {
                claves.next(); return claves.getLong(1);
            }
        } catch (SQLException e) { throw error(e); }
    }

    private void ejecutar(String sql, Object... valores) throws TurnoMedicoException {
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            completar(consulta, valores);
            if (consulta.executeUpdate() == 0) { throw new TurnoMedicoException("El registro ya no existe."); }
        } catch (SQLException e) { throw error(e); }
    }

    private void completar(PreparedStatement consulta, Object[] valores) throws SQLException {
        for (int i = 0; i < valores.length; i++) { consulta.setObject(i + 1, valores[i]); }
    }

    private TurnoMedicoException error(SQLException e) {
        if ("23505".equals(e.getSQLState())) {
            return new TurnoMedicoException("Ya existe un registro con esos datos o un turno en ese horario.", e);
        }
        if ("23503".equals(e.getSQLState()) || "23506".equals(e.getSQLState())) {
            return new TurnoMedicoException("El registro está en uso o hace referencia a un dato que ya no existe.", e);
        }
        return new TurnoMedicoException("No se pudo completar la operación en la base de datos.", e);
    }

    @Override
    public void close() throws TurnoMedicoException {
        try { conexion.close(); } catch (SQLException e) { throw error(e); }
    }
}
