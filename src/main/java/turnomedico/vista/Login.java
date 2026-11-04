package turnomedico.vista;

import javax.swing.*;
import turnomedico.logica.*;

public final class Login {
    private Login() { }
    public static boolean mostrar(Clinica clinica) throws TurnoMedicoException {
        if (clinica.necesitaAdministrador()) {
            JTextField usuario = new JTextField(22);
            JPasswordField clave = new JPasswordField(22);
            JTextField nombre = new JTextField();
            JTextField apellido = new JTextField();
            boolean creado = Ui.formulario(null, "Crear administrador inicial", Ui.campos("Usuario", usuario,
                    "Contraseña (mínimo 4)", clave, "Nombre", nombre, "Apellido", apellido), () ->
                    clinica.crearAdministrador(usuario.getText(), new String(clave.getPassword()), nombre.getText(), apellido.getText()));
            clave.setText("");
            if (!creado) { return false; }
        }
        JTextField usuario = new JTextField(22);
        JPasswordField clave = new JPasswordField(22);
        boolean ingreso = Ui.formulario(null, "Ingresar a la turnera", Ui.campos("Usuario", usuario, "Contraseña", clave), () ->
                clinica.iniciarSesion(usuario.getText(), new String(clave.getPassword())));
        clave.setText("");
        return ingreso;
    }
}
