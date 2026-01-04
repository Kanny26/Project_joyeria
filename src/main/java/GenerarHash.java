import org.mindrot.jbcrypt.BCrypt;

public class GenerarHash {
    public static void main(String[] args) {
        String password = "contrasena";
        String hash = BCrypt.hashpw(password, BCrypt.gensalt(12));
        System.out.println("Hash generado: " + hash);
    }
}
