package turnomedico;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import turnomedico.logica.*;
import turnomedico.modelo.*;
import static org.junit.jupiter.api.Assertions.*;

public class ClinicaTest {
    private Clinica clinica;
    private String url;
    private long medico1, medico2, paciente1, paciente2, consultorio1, consultorio2, obra, atencion1;
    private final LocalDate fecha = LocalDate.of(2026, 10, 15);
    private final LocalTime hora = LocalTime.of(9, 0);

    @BeforeEach
    void preparar() throws Exception {
        url = "jdbc:h2:mem:" + UUID.randomUUID();
        clinica = new Clinica(url);
        clinica.crearAdministrador("admin", "1234", "Ana", "Administradora");
        clinica.iniciarSesion("admin", "1234");
        obra = clinica.guardarObra(new ObraSocial(0, "Obra A"));
        medico1 = clinica.guardarMedico(medico(0, "medico1", "MN1", "100.01", obra), "1234");
        medico2 = clinica.guardarMedico(medico(0, "medico2", "MN2", "200.00", null), "1234");
        paciente1 = clinica.guardarPaciente(paciente(0, "paciente1", "30111222", obra), "1234");
        paciente2 = clinica.guardarPaciente(paciente(0, "paciente2", "30111223", null), "1234");
        consultorio1 = clinica.guardarConsultorio(new Consultorio(0, "Consultorio 1", "Calle 1"));
        consultorio2 = clinica.guardarConsultorio(new Consultorio(0, "Consultorio 2", "Calle 2"));
        atencion1 = clinica.guardarAtencion(0, medico1, consultorio1, fecha, fecha.plusDays(2));
        clinica.guardarAtencion(0, medico2, consultorio1, fecha, fecha);
        clinica.guardarAtencion(0, medico2, consultorio2, fecha.plusDays(1), fecha.plusDays(2));
    }

    @AfterEach
    void cerrar() throws Exception { clinica.close(); }

    private Medico medico(long id, String usuario, String matricula, String monto, Long obraId) {
        return new Medico(id, usuario, "María", "Gómez", matricula, "Clínica", new BigDecimal(monto), obraId);
    }
    private Paciente paciente(long id, String usuario, String dni, Long obraId) {
        return new Paciente(id, usuario, "Juan", "Pérez", dni, "1155551111", obraId);
    }
    private long turno(long medico, long paciente, long consultorio, LocalDate dia, LocalTime tiempo) throws Exception {
        return clinica.guardarTurno(0, medico, paciente, consultorio, dia, tiempo);
    }

    @Test
    void crudMedicosYPacientes() throws Exception {
        long id = clinica.guardarMedico(medico(0, "otro", "MN3", "500.00", null), "clave");
        clinica.guardarMedico(medico(id, "otroeditado", "MN4", "700.00", obra), "");
        Medico modificado = clinica.listarMedicos().stream().filter(m -> m.getId() == id).findFirst().get();
        assertEquals("MN4", modificado.getMatricula());
        assertEquals(new BigDecimal("700.00"), modificado.getHonorario());
        clinica.eliminarUsuario(id);
        assertEquals(2, clinica.listarMedicos().size());
        clinica.guardarPaciente(paciente(paciente2, "pacienteeditado", "40111222", obra), "nueva");
        assertEquals("40111222", clinica.listarPacientes().stream().filter(p -> p.getId() == paciente2).findFirst().get().getDni());
        clinica.eliminarUsuario(paciente2);
        assertEquals(1, clinica.listarPacientes().size());
    }

    @Test
    void rechazaDuplicadosNormalizadosEntreRoles() {
        assertThrows(TurnoMedicoException.class, () -> clinica.guardarPaciente(paciente(0, " MEDICO1 ", "40111222", null), "1234"));
        assertThrows(TurnoMedicoException.class, () -> clinica.guardarMedico(medico(0, "otro", " mn1 ", "10", null), "1234"));
        assertThrows(TurnoMedicoException.class, () -> clinica.guardarPaciente(paciente(0, "otro", "30111222", null), "1234"));
    }

    @Test
    void validaDatosPersonalesYHonorarios() {
        assertThrows(TurnoMedicoException.class, () -> clinica.guardarPaciente(paciente(0, "otro", "abc", null), "1234"));
        assertThrows(TurnoMedicoException.class, () -> clinica.guardarPaciente(paciente(0, "otro", "30123456", null), "1"));
        assertThrows(TurnoMedicoException.class, () -> clinica.guardarMedico(medico(0, "otro", "MN3", "0", null), "1234"));
        assertThrows(TurnoMedicoException.class, () -> clinica.guardarMedico(medico(0, "otro", "MN3", "-10", null), "1234"));
        assertThrows(TurnoMedicoException.class, () -> clinica.guardarMedico(medico(0, "otro", "MN3", "1.001", null), "1234"));
        assertThrows(TurnoMedicoException.class, () -> clinica.guardarMedico(medico(0, "otro", "MN3", "10000000000", null), "1234"));
        assertThrows(TurnoMedicoException.class, () -> clinica.guardarPaciente(new Paciente(0, "otro", " ", "Pérez", "30123456", "", null), "1234"));
        assertThrows(TurnoMedicoException.class, () -> clinica.guardarPaciente(paciente(0, "otro", "30123456", 999L), "1234"));
    }

    @Test
    void noPermiteCambiarRolesNiModificarUsuariosInexistentes() {
        assertThrows(TurnoMedicoException.class, () -> clinica.guardarMedico(medico(paciente1, "otro", "MN3", "100", null), "1234"));
        assertThrows(TurnoMedicoException.class, () -> clinica.guardarPaciente(paciente(999, "otro", "30123456", null), "1234"));
        assertThrows(TurnoMedicoException.class, () -> clinica.crearAdministrador("admin2", "1234", "Otro", "Admin"));
        assertThrows(TurnoMedicoException.class, () -> clinica.eliminarUsuario(clinica.getSesion().getId()));
    }

    @Test
    void crudCatalogosYProteccionDeReferencias() throws Exception {
        long id = clinica.guardarObra(new ObraSocial(0, "Temporal"));
        clinica.guardarObra(new ObraSocial(id, "Editada"));
        assertTrue(clinica.listarObras().stream().anyMatch(o -> o.getNombre().equals("Editada")));
        clinica.eliminarObra(id);
        assertEquals(1, clinica.listarObras().size());
        assertThrows(TurnoMedicoException.class, () -> clinica.eliminarObra(obra));
        assertThrows(TurnoMedicoException.class, () -> clinica.guardarObra(new ObraSocial(0, " obra a ")));
        long lugar = clinica.guardarConsultorio(new Consultorio(0, "Temporal", "Calle 3"));
        clinica.guardarConsultorio(new Consultorio(lugar, "Editado", "Calle 4"));
        assertTrue(clinica.listarConsultorios().stream().anyMatch(c -> c.getDireccion().equals("Calle 4")));
        clinica.eliminarConsultorio(lugar);
        assertThrows(TurnoMedicoException.class, () -> clinica.eliminarConsultorio(consultorio1));
        assertThrows(TurnoMedicoException.class, () -> clinica.eliminarUsuario(medico1));
        assertThrows(TurnoMedicoException.class, () -> clinica.guardarConsultorio(new Consultorio(0, "CONSULTORIO 1", "Calle")));
    }

    @Test
    void validaPeriodosYLugaresDeAtencion() throws Exception {
        assertThrows(TurnoMedicoException.class, () -> clinica.guardarAtencion(0, medico1, consultorio2, fecha.plusDays(2), fecha.plusDays(3)));
        assertThrows(TurnoMedicoException.class, () -> clinica.guardarAtencion(0, medico1, consultorio1, fecha, fecha.minusDays(1)));
        assertThrows(TurnoMedicoException.class, () -> clinica.guardarAtencion(0, paciente1, consultorio1, fecha, fecha));
        assertThrows(TurnoMedicoException.class, () -> turno(medico1, paciente1, consultorio2, fecha, hora));
        assertThrows(TurnoMedicoException.class, () -> turno(medico1, paciente1, consultorio1, fecha.minusDays(1), hora));
        turno(medico1, paciente1, consultorio1, fecha, hora);
        turno(medico1, paciente1, consultorio1, fecha.plusDays(2), hora);
        assertEquals(2, clinica.listarTurnos(fecha, fecha.plusDays(2)).size());
    }

    @Test
    void atencionesNoPuedenDejarTurnosSinLugar() throws Exception {
        clinica.guardarAtencion(atencion1, medico1, consultorio1, fecha, fecha.plusDays(3));
        long id = turno(medico1, paciente1, consultorio1, fecha, hora);
        assertThrows(TurnoMedicoException.class, () -> clinica.eliminarAtencion(atencion1));
        assertThrows(TurnoMedicoException.class, () -> clinica.guardarAtencion(atencion1, medico1, consultorio2, fecha, fecha.plusDays(3)));
        assertThrows(TurnoMedicoException.class, () -> clinica.eliminarUsuario(paciente1));
        clinica.eliminarTurno(id);
        clinica.eliminarAtencion(atencion1);
        clinica.eliminarUsuario(medico1);
        assertEquals(1, clinica.listarMedicos().size());
    }

    @Test
    void impideDobleReservaDeMedico() throws Exception {
        turno(medico1, paciente1, consultorio1, fecha, hora);
        TurnoMedicoException error = assertThrows(TurnoMedicoException.class, () -> turno(medico1, paciente2, consultorio1, fecha, hora));
        assertTrue(error.getMessage().contains("médico"));
        assertEquals(1, clinica.listarTurnos(fecha, fecha).size());
    }

    @Test
    void impideDobleReservaDePaciente() throws Exception {
        LocalDate dia = fecha.plusDays(1);
        turno(medico1, paciente1, consultorio1, dia, hora);
        TurnoMedicoException error = assertThrows(TurnoMedicoException.class, () -> turno(medico2, paciente1, consultorio2, dia, hora));
        assertTrue(error.getMessage().contains("paciente"));
    }

    @Test
    void impideDobleReservaDeConsultorio() throws Exception {
        turno(medico1, paciente1, consultorio1, fecha, hora);
        TurnoMedicoException error = assertThrows(TurnoMedicoException.class, () -> turno(medico2, paciente2, consultorio1, fecha, hora));
        assertTrue(error.getMessage().contains("consultorio"));
    }

    @Test
    void permiteDistintosHorariosYReprogramarElPropioTurno() throws Exception {
        long id = turno(medico1, paciente1, consultorio1, fecha, hora);
        clinica.guardarTurno(id, medico1, paciente1, consultorio1, fecha, hora);
        turno(medico1, paciente2, consultorio1, fecha, hora.plusMinutes(1));
        clinica.guardarTurno(id, medico1, paciente1, consultorio1, fecha.plusDays(1), hora);
        assertEquals(1, clinica.listarTurnos(fecha, fecha).size());
        assertEquals(1, clinica.listarTurnos(fecha.plusDays(1), fecha.plusDays(1)).size());
        clinica.eliminarTurno(id);
        assertEquals(0, clinica.listarTurnos(fecha.plusDays(1), fecha.plusDays(1)).size());
    }

    @Test
    void descuentoSoloConLaMismaObraSocialYRedondeoACentavos() throws Exception {
        turno(medico1, paciente1, consultorio1, fecha, hora);
        turno(medico1, paciente2, consultorio1, fecha, hora.plusHours(1));
        turno(medico2, paciente2, consultorio1, fecha, hora.plusHours(2));
        List<Turno> turnos = clinica.listarTurnos(fecha, fecha);
        assertEquals(new BigDecimal("50.01"), turnos.get(0).getMonto());
        assertTrue(turnos.get(0).tieneDescuento());
        assertEquals(new BigDecimal("100.01"), turnos.get(1).getMonto());
        assertFalse(turnos.get(1).tieneDescuento());
        assertEquals(new BigDecimal("200.00"), turnos.get(2).getMonto());
        assertFalse(turnos.get(2).tieneDescuento());
    }

    @Test
    void cambiosDeTarifaNoCambianElPrecioGuardado() throws Exception {
        long id = turno(medico1, paciente1, consultorio1, fecha, hora);
        clinica.guardarMedico(medico(medico1, "medico1", "MN1", "800.00", obra), "");
        clinica.guardarTurno(id, medico1, paciente1, consultorio1, fecha.plusDays(1), hora);
        assertEquals(new BigDecimal("50.01"), clinica.listarTurnos(fecha, fecha.plusDays(2)).get(0).getMonto());
        clinica.guardarTurno(id, medico1, paciente2, consultorio1, fecha.plusDays(1), hora);
        assertEquals(new BigDecimal("800.00"), clinica.listarTurnos(fecha, fecha.plusDays(2)).get(0).getMonto());
    }

    @Test
    void reportesIncluyenExtremosCantidadYMedicosSinConsultas() throws Exception {
        turno(medico1, paciente1, consultorio1, fecha, hora);
        turno(medico1, paciente2, consultorio1, fecha.plusDays(2), hora);
        turno(medico1, paciente1, consultorio1, fecha.plusDays(1), hora);
        List<ReporteMedico> reportes = clinica.reporteMedicos(fecha, fecha.plusDays(2));
        ReporteMedico primero = reportes.stream().filter(r -> r.getMedico().getId() == medico1).findFirst().get();
        assertEquals(3, primero.getConsultas());
        assertEquals(new BigDecimal("200.03"), primero.getTotal());
        assertEquals(0, reportes.stream().filter(r -> r.getMedico().getId() == medico2).findFirst().get().getConsultas());
        assertEquals(1, clinica.reporteMedicos(fecha.plusDays(1), fecha.plusDays(1)).get(0).getConsultas());
    }

    @Test
    void validaFechasHorasEIdentificadores() {
        assertThrows(TurnoMedicoException.class, () -> clinica.listarTurnos(fecha, fecha.minusDays(1)));
        assertThrows(TurnoMedicoException.class, () -> clinica.listarTurnos(null, fecha));
        assertThrows(TurnoMedicoException.class, () -> clinica.listarTurnos(LocalDate.of(1800, 1, 1), fecha));
        assertThrows(TurnoMedicoException.class, () -> turno(medico1, paciente1, consultorio1, fecha, hora.plusSeconds(1)));
        assertThrows(TurnoMedicoException.class, () -> turno(paciente1, paciente2, consultorio1, fecha, hora));
        assertThrows(TurnoMedicoException.class, () -> turno(medico1, medico2, consultorio1, fecha, hora));
        assertThrows(TurnoMedicoException.class, () -> clinica.guardarTurno(999, medico1, paciente1, consultorio1, fecha, hora));
    }

    @Test
    void permisosDeMedicoSeAplicanEnLaLogica() throws Exception {
        turno(medico1, paciente1, consultorio1, fecha, hora);
        turno(medico2, paciente2, consultorio1, fecha, hora.plusHours(1));
        clinica.iniciarSesion("MEDICO1", "1234");
        assertEquals(1, clinica.listarTurnos(fecha, fecha).size());
        assertEquals(medico1, clinica.listarTurnos(fecha, fecha).get(0).getMedico().getId());
        assertEquals(1, clinica.reporteMedicos(fecha, fecha).size());
        assertThrows(TurnoMedicoException.class, () -> clinica.listarPacientes());
        assertThrows(TurnoMedicoException.class, () -> clinica.eliminarTurno(1));
        assertThrows(TurnoMedicoException.class, () -> clinica.guardarObra(new ObraSocial(0, "Otra")));
        assertThrows(TurnoMedicoException.class, () -> turno(medico1, paciente1, consultorio1, fecha, hora.plusHours(2)));
    }

    @Test
    void permisosDePacienteYCierreDeSesion() throws Exception {
        turno(medico1, paciente1, consultorio1, fecha, hora);
        turno(medico2, paciente2, consultorio1, fecha, hora.plusHours(1));
        clinica.iniciarSesion("paciente1", "1234");
        assertEquals(1, clinica.listarTurnos(fecha, fecha).size());
        assertEquals(paciente1, clinica.listarTurnos(fecha, fecha).get(0).getPaciente().getId());
        assertThrows(TurnoMedicoException.class, () -> clinica.reporteMedicos(fecha, fecha));
        assertThrows(TurnoMedicoException.class, () -> clinica.eliminarUsuario(medico1));
        assertThrows(TurnoMedicoException.class, () -> clinica.listarAtenciones());
        clinica.cerrarSesion();
        assertNull(clinica.getSesion());
        assertThrows(TurnoMedicoException.class, () -> clinica.listarTurnos(fecha, fecha));
        assertThrows(TurnoMedicoException.class, () -> clinica.listarMedicos());
    }

    @Test
    void clavesProtegidasYCambiosDeClave() throws Exception {
        try (Connection conexion = DriverManager.getConnection(url, "sa", "");
             Statement consulta = conexion.createStatement();
             ResultSet filas = consulta.executeQuery("SELECT clave FROM usuarios")) {
            filas.next(); String primera = filas.getString(1);
            filas.next(); String segunda = filas.getString(1);
            assertNotEquals("1234", primera);
            assertNotEquals(primera, segunda);
        }
        clinica.guardarPaciente(paciente(paciente1, "paciente1", "30111222", obra), "nueva");
        assertThrows(TurnoMedicoException.class, () -> clinica.iniciarSesion("paciente1", "1234"));
        assertNull(clinica.getSesion());
        assertEquals(paciente1, clinica.iniciarSesion("paciente1", "nueva").getId());
        assertThrows(TurnoMedicoException.class, () -> clinica.iniciarSesion("admin' OR '1'='1", "1234"));
    }

    @Test
    void baseDeDatosTambienImpideDuplicarTurnos() throws Exception {
        turno(medico1, paciente1, consultorio1, fecha, hora);
        try (Connection conexion = DriverManager.getConnection(url, "sa", "");
             Statement consulta = conexion.createStatement()) {
            SQLException error = assertThrows(SQLException.class, () -> consulta.executeUpdate(
                    "INSERT INTO turnos(medico_id,paciente_id,consultorio_id,fecha,hora,monto,descuento) SELECT medico_id,paciente_id,consultorio_id,fecha,hora,monto,descuento FROM turnos"));
            assertEquals("23505", error.getSQLState());
        }
    }

    @Test
    void persisteTodoAlCerrarYVolverAAbrir(@TempDir Path carpeta) throws Exception {
        String archivo = "jdbc:h2:file:" + carpeta.resolve("clinica").toString().replace('\\', '/');
        long turnoId;
        try (Clinica otra = new Clinica(archivo)) {
            otra.crearAdministrador("admin", "1234", "Ana", "Admin");
            otra.iniciarSesion("admin", "1234");
            long os = otra.guardarObra(new ObraSocial(0, "Obra A"));
            long m = otra.guardarMedico(medico(0, "medico1", "MN1", "100.01", os), "1234");
            long p = otra.guardarPaciente(paciente(0, "paciente1", "30111222", os), "1234");
            long c = otra.guardarConsultorio(new Consultorio(0, "Sala 1", "Calle 1"));
            otra.guardarAtencion(0, m, c, fecha, fecha.plusDays(1));
            turnoId = otra.guardarTurno(0, m, p, c, fecha, hora);
        }
        try (Clinica otra = new Clinica(archivo)) {
            assertFalse(otra.necesitaAdministrador());
            otra.iniciarSesion("admin", "1234");
            assertEquals(1, otra.listarMedicos().size());
            assertEquals(1, otra.listarPacientes().size());
            assertEquals(1, otra.listarObras().size());
            assertEquals(1, otra.listarConsultorios().size());
            assertEquals(1, otra.listarAtenciones().size());
            Turno guardado = otra.listarTurnos(fecha, fecha).get(0);
            assertEquals(turnoId, guardado.getId());
            assertEquals(new BigDecimal("50.01"), guardado.getMonto());
            assertTrue(guardado.tieneDescuento());
            assertEquals(1, otra.reporteMedicos(fecha, fecha).get(0).getConsultas());
            otra.iniciarSesion("paciente1", "1234");
            assertEquals(1, otra.listarTurnos(fecha, fecha).size());
        }
    }
}
