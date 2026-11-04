# Revisión de la consigna

| Punto | Implementación | Verificación |
| --- | --- | --- |
| Administrar médicos; cada médico consulta sus turnos de una fecha | CRUD en Médicos, ingreso con su cuenta y consulta Desde/Hasta | `crudMedicosYPacientes`, `permisosDeMedicoSeAplicanEnLaLogica` |
| Honorario por consulta | Formulario de médico e importe guardado en cada turno | `validaDatosPersonalesYHonorarios`, `cambiosDeTarifaNoCambianElPrecioGuardado` |
| Administrar pacientes | CRUD en Pacientes, búsqueda por nombre, usuario o DNI | `crudMedicosYPacientes`, `rechazaDuplicadosNormalizadosEntreRoles` |
| Administrar turnos con fecha y hora; elegir médico y paciente de listas | Formulario de Turnos con `JComboBox`, alta, modificación y eliminación | `permiteDistintosHorariosYReprogramarElPropioTurno`, `agendaConsultaUnaFechaYSoloElAdministradorVeLosBotonesDeEdicion` |
| Un médico no puede tener dos turnos al mismo tiempo | Validación en Clinica y restricción UNIQUE en H2 | `impideDobleReservaDeMedico`, `baseDeDatosTambienImpideDuplicarTurnos` |
| Reportar cuánto cobró un médico y por cuántas consultas entre fechas | Reportes, fila del médico y detalle inferior | `reportesIncluyenExtremosCantidadYMedicosSinConsultas`, `reporteMuestraCantidadMontoYDetalleDelMedicoSeleccionado` |
| Pacientes como usuarios que consultan sus próximas consultas | Ingreso del paciente, Mis turnos y Calendario | `permisosDePacienteYCierreDeSesion` |
| Administrar consultorios y lugar de atención del médico entre fechas | CRUD de Consultorios y Atenciones; validación del lugar al dar un turno | `crudCatalogosYProteccionDeReferencias`, `validaPeriodosYLugaresDeAtencion`, `atencionesNoPuedenDejarTurnosSinLugar` |
| Listar todos los médicos y su recaudación entre fechas | Tabla superior de Reportes para el administrador | `reportesIncluyenExtremosCantidadYMedicosSinConsultas` |
| Obras sociales y descuento del 50 % cuando coinciden | CRUD de Obras sociales y selección en médico/paciente | `descuentoSoloConLaMismaObraSocialYRedondeoACentavos` |
| Grilla mensual o semanal de turnos | Calendario mensual y detalle del día seleccionado | `calendarioUbicaElDiaBisiestoYMuestraSuDetalle` |
| Swing y persistencia H2 con JDBC, pendientes del primer avance | Pantallas estándar, esquema SQL y DAO | `panelesAdministrativosCarganLosDatos`, `persisteTodoAlCerrarYVolverAAbrir` |

## Recorrido manual

1. Iniciar con una base vacía y crear el administrador.
2. Crear una obra social, dos médicos, dos pacientes y dos consultorios.
3. Asignar al primer médico un consultorio para el mes actual. Intentar superponer otra atención del mismo médico.
4. Crear un turno dentro del período y probar otro turno del mismo médico a la misma hora. Probar también una fecha o un consultorio sin atención asignada.
5. Asignar la misma obra social al médico y al paciente. Crear otro turno y comprobar el descuento; comprobar que dos particulares pagan el total.
6. Cambiar el honorario del médico. Confirmar que los turnos ya creados conservan el importe.
7. Consultar un período en Reportes y seleccionar un médico para ver el detalle. Revisar el día correspondiente en Calendario.
8. Cerrar y volver a abrir el programa para comprobar que se conservan los datos.
9. Entrar como médico y como paciente. Comprobar que cada cuenta ve sus turnos y que no tiene botones para modificar datos.
10. Volver al administrador, modificar un turno y eliminarlo. Comprobar que el horario queda libre y que los reportes se actualizan.
