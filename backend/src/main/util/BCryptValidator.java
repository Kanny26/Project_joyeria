package main.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Clase utilitaria para validar contraseñas encriptadas con BCrypt
 * BCrypt es un algoritmo de hash de una sola vía, por lo que no se puede desencriptar
 * En su lugar, se hashea la contraseña ingresada y se compara con el hash almacenado
 */
public class BCryptValidator {
    
    /**
     * Valida si una contraseña en texto plano coincide con un hash BCrypt
     * 
     * @param passwordTextoPlano La contraseña que ingresó el usuario (sin encriptar)
     * @param hashAlmacenado El hash BCrypt almacenado en la base de datos
     * @return true si la contraseña coincide, false en caso contrario
     */
    public static boolean validarPassword(String passwordTextoPlano, String hashAlmacenado) {
        
        // Validar que los parámetros no sean nulos o vacíos
        if (passwordTextoPlano == null || passwordTextoPlano.trim().isEmpty()) {
            System.err.println("Error: La password en texto plano esta vacia");
            return false;
        }
        
        if (hashAlmacenado == null || hashAlmacenado.trim().isEmpty()) {
            System.err.println("Error: El hash almacenado esta vacio");
            return false;
        }
        
        // Validar que el hash tenga el formato correcto de BCrypt
        if (!hashAlmacenado.startsWith("$2a$") && 
            !hashAlmacenado.startsWith("$2b$") && 
            !hashAlmacenado.startsWith("$2y$")) {
            System.err.println("Error: El hash no tiene un formato BCrypt valido");
            return false;
        }
        
        try {
            // Comparar la contraseña en texto plano con el hash
            boolean passwordsCoinciden = BCrypt.checkpw(passwordTextoPlano, hashAlmacenado);
            
            if (passwordsCoinciden) {
                System.out.println("Password valida");
            } else {
                System.out.println("Password incorrecta");
            }
            
            return passwordsCoinciden;
            
        } catch (IllegalArgumentException excepcionArgumento) {
            System.err.println("Error al validar la password: Hash invalido");
            System.err.println("Detalle: " + excepcionArgumento.getMessage());
            return false;
            
        } catch (Exception excepcionGeneral) {
            System.err.println("Error inesperado al validar la password");
            System.err.println("Detalle: " + excepcionGeneral.getMessage());
            excepcionGeneral.printStackTrace();
            return false;
        }
    }
    
    /**
     * Genera un hash BCrypt a partir de una contraseña en texto plano
     * Útil para crear nuevos usuarios o cambiar contraseñas
     * 
     * @param passwordTextoPlano La contraseña a encriptar
     * @return El hash BCrypt generado
     */
    public static String generarHashBCrypt(String passwordTextoPlano) {
        
        if (passwordTextoPlano == null || passwordTextoPlano.trim().isEmpty()) {
            throw new IllegalArgumentException("La password no puede estar vacia");
        }
        
        try {
            // Generar sal (salt) y hash con nivel de complejidad 12
            String hashGenerado = BCrypt.hashpw(passwordTextoPlano, BCrypt.gensalt(12));
            System.out.println("Hash generado exitosamente");
            return hashGenerado;
            
        } catch (Exception excepcion) {
            System.err.println("Error al generar hash BCrypt");
            System.err.println("Detalle: " + excepcion.getMessage());
            throw new RuntimeException("Error al encriptar la password", excepcion);
        }
    }
    
    /**
     * Método de prueba para validar el funcionamiento de BCrypt
     * Útil para testing durante el desarrollo
     */
    public static void ejecutarPrueba() {
        System.out.println("=== PRUEBA DE BCRYPT ===");
        
        String passwordPrueba = "adminksmb";
        String hashPrueba = "$2a$12$X5/dP8Dv4BZ8GH8UH0iO9uvyaEjLEqHB/Bs42s6bgSFap9HJXwtq.";
        
        System.out.println("Password a probar: " + passwordPrueba);
        System.out.println("Hash almacenado: " + hashPrueba);
        
        boolean resultado = validarPassword(passwordPrueba, hashPrueba);
        
        if (resultado) {
            System.out.println("PRUEBA EXITOSA - Las passwords coinciden");
        } else {
            System.out.println("PRUEBA FALLIDA - Las passwords NO coinciden");
        }
        
        System.out.println("========================");
    }
    
    /**
     * Método main para ejecutar pruebas independientes
     */
    public static void main(String[] args) {
        ejecutarPrueba();
    }
}