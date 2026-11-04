package turnomedico.logica;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class Claves {
    private Claves() { }
    public static String crear(String clave) {
        byte[] sal = new byte[16];
        new SecureRandom().nextBytes(sal);
        return Base64.getEncoder().encodeToString(sal) + ":" + calcular(clave, sal);
    }
    public static boolean verificar(String clave, String guardada) {
        String[] partes = guardada.split(":");
        String calculada = calcular(clave, Base64.getDecoder().decode(partes[0]));
        return MessageDigest.isEqual(calculada.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                partes[1].getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
    private static String calcular(String clave, byte[] sal) {
        PBEKeySpec datos = new PBEKeySpec(clave.toCharArray(), sal, 120000, 256);
        try {
            return Base64.getEncoder().encodeToString(
                    SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(datos).getEncoded());
        } catch (java.security.GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo proteger la contraseña", e);
        } finally {
            datos.clearPassword();
        }
    }
}
