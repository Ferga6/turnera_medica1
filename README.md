# Turnera médica

Trabajo práctico de Java con Swing y H2. La consigna está en [consigna_turnera_medica.jpeg](consigna_turnera_medica.jpeg).

## Ejecutar

Se necesita un JDK 8 o superior. Maven se descarga con el wrapper la primera vez, por lo que esa compilación requiere Internet.

En Windows, desde la carpeta del proyecto:

```powershell
.\mvnw.cmd clean package
java -jar target/turnera-medica.jar
```

En Linux o macOS:

```sh
sh mvnw clean package
java -jar target/turnera-medica.jar
```

Si `JAVA_HOME` apunta a un JRE, hay que cambiarlo por la carpeta del JDK. En IntelliJ se abre `pom.xml` como proyecto Maven y se ejecuta `turnomedico.Main`.

La primera ejecución pide crear un administrador. No hay una contraseña predeterminada. Luego se ingresa con ese usuario. El administrador crea las cuentas de médicos y pacientes desde sus formularios. Al modificar una cuenta, una contraseña vacía conserva la anterior.

Los datos se guardan en `datos/clinica.mv.db`, dentro de la carpeta desde la que se ejecuta el programa. No hay que abrir dos instancias sobre la misma base. Para usar otra carpeta:

```powershell
java "-Dturnera.datos=C:/Turnera/datos" -jar target/turnera-medica.jar
```

Para hacer una copia de la base, cerrar primero la aplicación y copiar la carpeta `datos`.

## Uso

1. Agregar las obras sociales y los consultorios.
2. Agregar médicos con matrícula, especialidad y honorario, y pacientes con DNI y teléfono opcional. Ambos pueden tener una obra social o atenderse como particulares.
3. En **Atenciones**, asignar a cada médico un consultorio entre dos fechas.
4. En **Turnos**, elegir médico, paciente y consultorio de las listas, e ingresar fecha y hora.
5. En **Reportes**, ingresar el período. La tabla superior muestra cantidad de consultas e importe por médico; al seleccionar uno, se ven sus turnos en la tabla inferior.

Los médicos ven sus turnos, calendario y recaudación. Los pacientes ven solamente sus turnos y calendario. El administrador tiene los CRUD y todos los reportes. **Cerrar sesión** permite ingresar con otra cuenta.

Las fechas se escriben como `2026-11-04` y las horas como `09:30`. Para consultar los turnos de un solo día se pone la misma fecha en ambos extremos. En el calendario, cada celda muestra el día y la cantidad de turnos; al seleccionarla aparece el detalle.

## Criterios del trabajo

- Los períodos incluyen ambas fechas. Un médico tiene un consultorio por período; no puede tener períodos superpuestos. Varios médicos pueden compartir consultorio, pero no un turno a la misma hora.
- Los turnos se comparan por fecha, hora y minuto. La consigna no establece duración de consultas, horarios de apertura ni días de la semana.
- Si médico y paciente tienen la misma obra social, se aplica el 50 % de descuento. Dos particulares pagan el honorario completo. Los importes se manejan con `BigDecimal` y se redondean a centavos.
- El turno guarda su importe al crearse. Cambiar honorarios u obra social después no cambia ese importe. Reprogramar fecha, hora o consultorio conserva el precio; cambiar médico o paciente lo recalcula.
- La recaudación es la suma de los importes de los turnos registrados en el período, como plantea la consigna. No se lleva un registro separado de pagos o de asistencia. Se pueden ingresar fechas pasadas.
- Eliminar un turno lo quita también del reporte y libera el horario. No se eliminan médicos, pacientes, obras sociales o consultorios que estén en uso. Una atención con turnos tampoco se puede modificar o eliminar hasta reprogramarlos o quitarlos.
- Los nombres de usuario, matrículas y DNI no se repiten. Las contraseñas se guardan como huellas con sal en `Claves`, separadas de los datos que muestran las pantallas.

## Código y pruebas

El proyecto usa Java, Swing y JDBC; no usa frameworks de interfaz ni de persistencia.

- `modelo`: clases de las entidades. `Medico` y `Paciente` heredan de `Usuario`.
- `persistencia/ClinicaDao`: conexión H2 y sentencias SQL con parámetros.
- `logica/Clinica`: validaciones, sesión, permisos y operaciones.
- `vista`: formularios y tablas estándar de Swing.
- `src/main/resources/esquema.sql`: tablas y restricciones.
- `src/test/java`: pruebas de lógica, persistencia y componentes Swing.

Para ejecutar solamente las pruebas:

```powershell
.\mvnw.cmd test
```

La correspondencia con cada punto de la consigna está en [docs/consigna.md](docs/consigna.md). `avance1.txt` y `Robles_Facundo_TP3.zip` se conservan como antecedentes del primer avance; el código actual está en `src`.
