package config;

public class TestConexion {
    public static void main(String[] args) {
        if (ConexionDB.getConnection() != null) {
            System.out.println("Todo listo, podemos seguir");
        } else {
            System.out.println("Falló la conexión");
        }
    }
}
