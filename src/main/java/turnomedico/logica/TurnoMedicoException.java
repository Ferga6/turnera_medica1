package turnomedico.logica;

public class TurnoMedicoException extends Exception {
    private static final long serialVersionUID = 1L;
    public TurnoMedicoException(String mensaje) { super(mensaje); }
    public TurnoMedicoException(String mensaje, Throwable causa) { super(mensaje, causa); }
}
